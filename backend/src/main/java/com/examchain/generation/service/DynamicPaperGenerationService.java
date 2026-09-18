package com.examchain.generation.service;

import com.examchain.crypto.service.CryptoService;
import com.examchain.crypto.service.EncryptedStorageService;
import com.examchain.exam.repository.SubjectRepository;
import com.examchain.generation.dto.BlueprintDtos.*;
import com.examchain.generation.entity.BlueprintEntity;
import com.examchain.generation.entity.GeneratedPaperEntity;
import com.examchain.generation.entity.GeneratedPaperQuestionEntity;
import com.examchain.generation.repository.BlueprintRepository;
import com.examchain.generation.repository.GeneratedPaperQuestionRepository;
import com.examchain.generation.repository.GeneratedPaperRepository;
import com.examchain.generation.util.BlueprintRulesUtil;
import com.examchain.question.dto.QuestionDtos.QuestionContent;
import com.examchain.question.entity.QuestionEntity;
import com.examchain.question.entity.QuestionPoolEntity;
import com.examchain.question.model.PoolStatus;
import com.examchain.question.repository.QuestionPoolRepository;
import com.examchain.question.repository.QuestionRepository;
import com.examchain.question.util.ContentJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DynamicPaperGenerationService {

    private static final Logger log = LoggerFactory.getLogger(DynamicPaperGenerationService.class);

    private final BlueprintRepository blueprintRepository;
    private final GeneratedPaperRepository paperRepository;
    private final GeneratedPaperQuestionRepository paperQuestionRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionPoolRepository poolRepository;
    private final QuestionRepository questionRepository;
    private final CryptoService cryptoService;
    private final EncryptedStorageService encryptedStorageService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public DynamicPaperGenerationService(
            BlueprintRepository blueprintRepository,
            GeneratedPaperRepository paperRepository,
            GeneratedPaperQuestionRepository paperQuestionRepository,
            SubjectRepository subjectRepository,
            QuestionPoolRepository poolRepository,
            QuestionRepository questionRepository,
            CryptoService cryptoService,
            EncryptedStorageService encryptedStorageService
    ) {
        this.blueprintRepository = blueprintRepository;
        this.paperRepository = paperRepository;
        this.paperQuestionRepository = paperQuestionRepository;
        this.subjectRepository = subjectRepository;
        this.poolRepository = poolRepository;
        this.questionRepository = questionRepository;
        this.cryptoService = cryptoService;
        this.encryptedStorageService = encryptedStorageService;
    }

    public BlueprintResponse createBlueprint(UUID subjectId, BlueprintRequest request) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new IllegalArgumentException("Subject not found: " + subjectId);
        }
        if (blueprintRepository.existsByBlueprintCode(request.blueprintCode())) {
            throw new IllegalArgumentException("Blueprint code '" + request.blueprintCode() + "' already exists");
        }

        // Validate that rule marks sum up exactly to requested totalMarks
        int calculatedMarks = request.rules().stream()
                .mapToInt(r -> r.marksPerQuestion() * r.count())
                .sum();
        if (calculatedMarks != request.totalMarks()) {
            throw new IllegalArgumentException("Sum of rule marks (" + calculatedMarks + ") does not match blueprint total marks (" + request.totalMarks() + ")");
        }

        String rulesJson = BlueprintRulesUtil.toJson(request.rules());

        BlueprintEntity entity = BlueprintEntity.builder()
                .subjectId(subjectId)
                .blueprintCode(request.blueprintCode().trim().toUpperCase())
                .name(request.name().trim())
                .totalMarks(request.totalMarks())
                .durationMinutes(request.durationMinutes())
                .policyMode(request.policyMode())
                .rulesJson(rulesJson)
                .status("ACTIVE")
                .build();

        BlueprintEntity saved = blueprintRepository.save(entity);
        return mapToBlueprintResponse(saved);
    }

    @Transactional(readOnly = true)
    public BlueprintResponse getBlueprintById(UUID blueprintId) {
        BlueprintEntity entity = blueprintRepository.findById(blueprintId)
                .orElseThrow(() -> new IllegalArgumentException("Blueprint not found: " + blueprintId));
        return mapToBlueprintResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<BlueprintResponse> getBlueprintsForSubject(UUID subjectId) {
        return blueprintRepository.findBySubjectId(subjectId).stream()
                .map(this::mapToBlueprintResponse)
                .toList();
    }

    public GeneratedPaperResponse generatePaper(UUID blueprintId, String setCode, SecureRandom rng) {
        BlueprintEntity blueprint = blueprintRepository.findById(blueprintId)
                .orElseThrow(() -> new IllegalArgumentException("Blueprint not found: " + blueprintId));

        String sanitizedSetCode = setCode != null && !setCode.isBlank() ? setCode.trim().toUpperCase() : "SET_A";
        String paperBusinessId = "PAP-" + blueprint.getBlueprintCode() + "-" + sanitizedSetCode;

        if (paperRepository.existsByPaperId(paperBusinessId)) {
            throw new IllegalArgumentException("Paper with ID '" + paperBusinessId + "' already generated");
        }

        List<BlueprintRuleDto> rules = BlueprintRulesUtil.fromJson(blueprint.getRulesJson());

        // Fetch candidate questions from approved pools for this subject
        List<QuestionPoolEntity> approvedPools = poolRepository.findBySubjectId(blueprint.getSubjectId()).stream()
                .filter(p -> p.getStatus() == PoolStatus.APPROVED)
                .toList();

        Set<UUID> approvedPoolIds = approvedPools.stream().map(QuestionPoolEntity::getId).collect(Collectors.toSet());

        // For MVP flexibility: if no pools are strictly marked APPROVED, fall back to all questions for subject
        List<QuestionEntity> allSubjectQuestions = questionRepository.findBySubjectId(blueprint.getSubjectId());
        List<QuestionEntity> candidatePool = allSubjectQuestions.stream()
                .filter(q -> approvedPoolIds.isEmpty() || approvedPoolIds.contains(q.getPoolId()))
                .toList();

        SecureRandom random = rng != null ? rng : this.secureRandom;
        Set<UUID> selectedIds = new HashSet<>();
        List<QuestionEntity> selectedQuestions = new ArrayList<>();

        for (BlueprintRuleDto rule : rules) {
            List<QuestionEntity> matching = candidatePool.stream()
                    .filter(q -> !selectedIds.contains(q.getId()))
                    .filter(q -> q.getUnit() == rule.unit())
                    .filter(q -> q.getMarks() == rule.marksPerQuestion())
                    .filter(q -> q.getDifficulty() == rule.difficulty())
                    .filter(q -> q.getQuestionType() == rule.questionType())
                    .collect(Collectors.toCollection(ArrayList::new));

            if (matching.size() < rule.count()) {
                throw new IllegalStateException("Insufficient pool for blueprint rule [Unit " + rule.unit() +
                        ", Marks " + rule.marksPerQuestion() + ", " + rule.difficulty() + ", " + rule.questionType() +
                        "]: Required " + rule.count() + ", Available " + matching.size());
            }

            // Cryptographically secure shuffle and selection
            Collections.shuffle(matching, random);
            for (int i = 0; i < rule.count(); i++) {
                QuestionEntity chosen = matching.get(i);
                selectedIds.add(chosen.getId());
                selectedQuestions.add(chosen);
            }
        }

        // Strict duplicate check
        if (selectedQuestions.size() != selectedIds.size()) {
            throw new IllegalStateException("Duplicate question selection detected during paper generation");
        }

        // Calculate cryptographic paper hash
        StringBuilder hashBuilder = new StringBuilder();
        hashBuilder.append(paperBusinessId).append("::")
                .append(blueprint.getBlueprintCode()).append("::")
                .append(blueprint.getTotalMarks()).append("::");
        for (QuestionEntity q : selectedQuestions) {
            hashBuilder.append(q.getHash()).append("::");
        }
        String paperHash = cryptoService.computeSha256(hashBuilder.toString().getBytes(StandardCharsets.UTF_8));

        // Save Generated Paper record
        GeneratedPaperEntity paperEntity = GeneratedPaperEntity.builder()
                .paperId(paperBusinessId)
                .blueprintId(blueprintId)
                .subjectId(blueprint.getSubjectId())
                .setCode(sanitizedSetCode)
                .totalMarks(blueprint.getTotalMarks())
                .questionCount(selectedQuestions.size())
                .paperHash(paperHash)
                .status("GENERATED")
                .build();

        GeneratedPaperEntity savedPaper = paperRepository.save(paperEntity);

        // Save Paper Question sequences
        List<PaperQuestionSummary> summaries = new ArrayList<>();
        for (int i = 0; i < selectedQuestions.size(); i++) {
            QuestionEntity q = selectedQuestions.get(i);
            int seq = i + 1;
            GeneratedPaperQuestionEntity pq = GeneratedPaperQuestionEntity.builder()
                    .paperId(savedPaper.getId())
                    .questionId(q.getId())
                    .sequenceNumber(seq)
                    .allocatedMarks(q.getMarks())
                    .build();
            paperQuestionRepository.save(pq);

            QuestionContent content = ContentJsonUtil.fromJson(q.getContentJson());
            summaries.add(new PaperQuestionSummary(
                    seq,
                    q.getQuestionId(),
                    q.getUnit(),
                    q.getMarks(),
                    q.getDifficulty(),
                    q.getQuestionType(),
                    content.questionText(),
                    q.getHash()
            ));
        }

        // Store encrypted artifact in MinIO
        String paperPayloadText = buildPaperPayloadJson(savedPaper, summaries);
        encryptedStorageService.storeEncryptedPaper(
                paperBusinessId,
                paperPayloadText.getBytes(StandardCharsets.UTF_8),
                blueprint.getBlueprintCode()
        );

        log.info("Successfully generated and encrypted paper [{}] with hash [{}]", paperBusinessId, paperHash);

        return new GeneratedPaperResponse(
                savedPaper.getId(),
                savedPaper.getPaperId(),
                savedPaper.getBlueprintId(),
                savedPaper.getSubjectId(),
                savedPaper.getSetCode(),
                savedPaper.getTotalMarks(),
                savedPaper.getQuestionCount(),
                savedPaper.getPaperHash(),
                savedPaper.getStatus(),
                summaries,
                savedPaper.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public GeneratedPaperResponse getPaperByPaperId(String paperId) {
        GeneratedPaperEntity paper = paperRepository.findByPaperId(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found: " + paperId));

        List<GeneratedPaperQuestionEntity> pqs = paperQuestionRepository.findByPaperIdOrderBySequenceNumberAsc(paper.getId());
        List<PaperQuestionSummary> summaries = new ArrayList<>();

        for (GeneratedPaperQuestionEntity pq : pqs) {
            QuestionEntity q = questionRepository.findById(pq.getQuestionId()).orElse(null);
            if (q != null) {
                QuestionContent content = ContentJsonUtil.fromJson(q.getContentJson());
                summaries.add(new PaperQuestionSummary(
                        pq.getSequenceNumber(),
                        q.getQuestionId(),
                        q.getUnit(),
                        pq.getAllocatedMarks(),
                        q.getDifficulty(),
                        q.getQuestionType(),
                        content.questionText(),
                        q.getHash()
                ));
            }
        }

        return new GeneratedPaperResponse(
                paper.getId(),
                paper.getPaperId(),
                paper.getBlueprintId(),
                paper.getSubjectId(),
                paper.getSetCode(),
                paper.getTotalMarks(),
                paper.getQuestionCount(),
                paper.getPaperHash(),
                paper.getStatus(),
                summaries,
                paper.getCreatedAt()
        );
    }

    private BlueprintResponse mapToBlueprintResponse(BlueprintEntity b) {
        List<BlueprintRuleDto> rules = BlueprintRulesUtil.fromJson(b.getRulesJson());
        return new BlueprintResponse(
                b.getId(),
                b.getSubjectId(),
                b.getBlueprintCode(),
                b.getName(),
                b.getTotalMarks(),
                b.getDurationMinutes(),
                b.getPolicyMode(),
                rules,
                b.getStatus(),
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }

    private String buildPaperPayloadJson(GeneratedPaperEntity paper, List<PaperQuestionSummary> questions) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"paperId\":\"").append(paper.getPaperId()).append("\",");
        sb.append("\"setCode\":\"").append(paper.getSetCode()).append("\",");
        sb.append("\"totalMarks\":").append(paper.getTotalMarks()).append(",");
        sb.append("\"questionCount\":").append(paper.getQuestionCount()).append(",");
        sb.append("\"paperHash\":\"").append(paper.getPaperHash()).append("\",");
        sb.append("\"questions\":[");
        for (int i = 0; i < questions.size(); i++) {
            if (i > 0) sb.append(",");
            PaperQuestionSummary q = questions.get(i);
            sb.append("{\"seq\":").append(q.sequenceNumber())
                    .append(",\"id\":\"").append(q.questionId()).append("\"")
                    .append(",\"marks\":").append(q.marks())
                    .append(",\"hash\":\"").append(q.questionHash()).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }
}
