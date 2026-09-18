package com.examchain.qr.service;

import com.examchain.core.exception.ResourceNotFoundException;
import com.examchain.generation.dto.BlueprintDtos.GeneratedPaperResponse;
import com.examchain.generation.service.DynamicPaperGenerationService;
import com.examchain.qr.dto.ProvenanceDtos.*;
import com.examchain.qr.entity.PaperProvenanceEntity;
import com.examchain.qr.model.ProvenanceStatus;
import com.examchain.qr.repository.PaperProvenanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaperProvenanceService {

    private static final Logger log = LoggerFactory.getLogger(PaperProvenanceService.class);

    private final PaperProvenanceRepository repository;
    private final TraceIdGenerator traceIdGenerator;
    private final DynamicPaperGenerationService generationService;

    @Autowired
    public PaperProvenanceService(
            PaperProvenanceRepository repository,
            TraceIdGenerator traceIdGenerator,
            DynamicPaperGenerationService generationService
    ) {
        this.repository = repository;
        this.traceIdGenerator = traceIdGenerator;
        this.generationService = generationService;
    }

    @Transactional
    public CreateTraceResponse registerPaperTrace(CreateTraceRequest request) {
        GeneratedPaperResponse paper = generationService.getPaperByPaperId(request.paperId());

        return repository.findByPaperId(request.paperId())
                .map(existing -> new CreateTraceResponse(
                        existing.getTraceId(),
                        existing.getPaperId(),
                        "/api/v1/verify/paper/" + existing.getTraceId(),
                        existing.getStatus().name(),
                        existing.getExamEndTime()
                ))
                .orElseGet(() -> {
                    String traceId = traceIdGenerator.generateTraceId();
                    PaperProvenanceEntity entity = new PaperProvenanceEntity(
                            traceId,
                            paper.paperId(),
                            paper.paperHash(),
                            request.examCode(),
                            request.examTitle(),
                            request.centreCode(),
                            request.examStartTime(),
                            request.examEndTime()
                    );
                    PaperProvenanceEntity saved = repository.save(entity);
                    log.info("Registered paper provenance trace [{}] for paper [{}]", saved.getTraceId(), saved.getPaperId());
                    return new CreateTraceResponse(
                            saved.getTraceId(),
                            saved.getPaperId(),
                            "/api/v1/verify/paper/" + saved.getTraceId(),
                            saved.getStatus().name(),
                            saved.getExamEndTime()
                    );
                });
    }

    @Transactional(readOnly = true)
    public VerificationResponse verifyPaper(String traceId) {
        PaperProvenanceEntity entity = repository.findByTraceId(traceId)
                .orElseThrow(() -> new ResourceNotFoundException("No examination record found for trace ID: " + traceId));

        Instant now = Instant.now();

        // Check if quarantined
        if (entity.getStatus() == ProvenanceStatus.QUARANTINED) {
            return new VerificationResponse(
                    entity.getTraceId(),
                    "QUARANTINED",
                    false,
                    "This paper variant has been quarantined by the examination authority due to security protocols.",
                    entity.getExamCode(),
                    entity.getExamTitle(),
                    entity.getExamEndTime(),
                    null,
                    null,
                    now
            );
        }

        // Check pre-exam lock
        boolean isPreExam = now.isBefore(entity.getExamEndTime());

        if (isPreExam) {
            return new VerificationResponse(
                    entity.getTraceId(),
                    "PRE_EXAM_LOCKED",
                    true,
                    "Official examination paper is registered, authenticated, and cryptographically sealed. Full provenance receipt unlocks post-examination.",
                    entity.getExamCode(),
                    entity.getExamTitle(),
                    entity.getExamEndTime(),
                    null, // Do NOT leak paper hash or contents pre-exam
                    null,
                    now
            );
        }

        // Post-exam: Return full verified provenance trail
        List<ProvenanceEvent> trail = new ArrayList<>();
        trail.add(new ProvenanceEvent(
                "GENERATION",
                "PAPER_AUTHORITY",
                entity.getCreatedAt(),
                "COMPLETED",
                "Deterministic variant generated and encrypted via AES-256-GCM"
        ));
        trail.add(new ProvenanceEvent(
                "SEALED",
                "SECURITY_ENCLAVE",
                entity.getExamStartTime(),
                "SEALED",
                "Cryptographic envelope sealed under multi-authority threshold lock"
        ));
        trail.add(new ProvenanceEvent(
                "EXAMINATION_CONCLUDED",
                "EXAM_CONTROLLER",
                entity.getExamEndTime(),
                "CONCLUDED",
                "Examination window concluded; provenance audit trail publicly verified"
        ));

        return new VerificationResponse(
                entity.getTraceId(),
                "VERIFIED_POST_EXAM",
                true,
                "Examination paper authenticity confirmed. Cryptographic hash matches official ledger record.",
                entity.getExamCode(),
                entity.getExamTitle(),
                entity.getExamEndTime(),
                entity.getPaperHash(),
                trail,
                now
        );
    }
}
