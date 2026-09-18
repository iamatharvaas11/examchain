package com.examchain.question.service;

import com.examchain.question.dto.QuestionDtos.QuestionContent;
import com.examchain.question.dto.QuestionDtos.QuestionRequest;
import com.examchain.question.dto.QuestionDtos.QuestionResponse;
import com.examchain.question.entity.QuestionEntity;
import com.examchain.question.entity.QuestionPoolEntity;
import com.examchain.question.model.PoolStatus;
import com.examchain.question.model.QuestionStatus;
import com.examchain.question.repository.QuestionPoolRepository;
import com.examchain.question.repository.QuestionRepository;
import com.examchain.question.util.ContentJsonUtil;
import com.examchain.question.util.QuestionHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionPoolRepository poolRepository;

    @Autowired
    public QuestionService(QuestionRepository questionRepository, QuestionPoolRepository poolRepository) {
        this.questionRepository = questionRepository;
        this.poolRepository = poolRepository;
    }

    public QuestionResponse createQuestion(UUID poolId, QuestionRequest request, String setterId) {
        QuestionPoolEntity pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Question pool not found: " + poolId));

        if (pool.getStatus() != PoolStatus.DRAFT) {
            throw new IllegalStateException("Cannot add questions to locked pool with status: " + pool.getStatus());
        }

        if (questionRepository.existsByQuestionId(request.questionId())) {
            throw new IllegalArgumentException("Question ID '" + request.questionId() + "' already exists");
        }

        if (request.unit() <= 0) {
            throw new IllegalArgumentException("Unit must be greater than zero");
        }
        if (request.marks() <= 0) {
            throw new IllegalArgumentException("Marks must be greater than zero");
        }

        String hash = QuestionHasher.computeHash(
                request.questionId(),
                request.unit(),
                request.marks(),
                request.difficulty(),
                request.questionType(),
                request.cognitiveLevel(),
                request.content()
        );

        String contentJson = ContentJsonUtil.toJson(request.content());

        QuestionEntity entity = QuestionEntity.builder()
                .questionId(request.questionId().trim())
                .poolId(poolId)
                .subjectId(pool.getSubjectId())
                .unit(request.unit())
                .marks(request.marks())
                .difficulty(request.difficulty())
                .questionType(request.questionType())
                .cognitiveLevel(request.cognitiveLevel())
                .setterId(setterId)
                .status(QuestionStatus.DRAFT)
                .version(1)
                .contentJson(contentJson)
                .hash(hash)
                .build();

        QuestionEntity saved = questionRepository.save(entity);
        return mapToQuestionResponse(saved);
    }

    public QuestionResponse updateQuestion(UUID questionDbId, QuestionRequest request, String setterId) {
        QuestionEntity existing = questionRepository.findById(questionDbId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionDbId));

        QuestionPoolEntity pool = poolRepository.findById(existing.getPoolId())
                .orElseThrow(() -> new IllegalArgumentException("Question pool not found: " + existing.getPoolId()));

        if (pool.getStatus() != PoolStatus.DRAFT) {
            throw new IllegalStateException("Cannot edit questions in locked pool with status: " + pool.getStatus());
        }

        // If questionId changed, check uniqueness
        if (!existing.getQuestionId().equalsIgnoreCase(request.questionId()) &&
                questionRepository.existsByQuestionId(request.questionId())) {
            throw new IllegalArgumentException("Question ID '" + request.questionId() + "' already exists");
        }

        if (request.unit() <= 0 || request.marks() <= 0) {
            throw new IllegalArgumentException("Unit and marks must be positive values");
        }

        String newHash = QuestionHasher.computeHash(
                request.questionId(),
                request.unit(),
                request.marks(),
                request.difficulty(),
                request.questionType(),
                request.cognitiveLevel(),
                request.content()
        );

        existing.setQuestionId(request.questionId().trim());
        existing.setUnit(request.unit());
        existing.setMarks(request.marks());
        existing.setDifficulty(request.difficulty());
        existing.setQuestionType(request.questionType());
        existing.setCognitiveLevel(request.cognitiveLevel());
        existing.setContentJson(ContentJsonUtil.toJson(request.content()));
        existing.setHash(newHash);
        existing.setVersion(existing.getVersion() + 1);

        QuestionEntity updated = questionRepository.save(existing);
        return mapToQuestionResponse(updated);
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(UUID questionDbId) {
        QuestionEntity entity = questionRepository.findById(questionDbId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionDbId));
        return mapToQuestionResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByPoolId(UUID poolId) {
        return questionRepository.findByPoolId(poolId).stream()
                .map(this::mapToQuestionResponse)
                .toList();
    }

    public void deleteQuestion(UUID questionDbId, String setterId) {
        QuestionEntity existing = questionRepository.findById(questionDbId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionDbId));

        QuestionPoolEntity pool = poolRepository.findById(existing.getPoolId())
                .orElseThrow(() -> new IllegalArgumentException("Question pool not found: " + existing.getPoolId()));

        if (pool.getStatus() != PoolStatus.DRAFT) {
            throw new IllegalStateException("Cannot delete question from locked pool with status: " + pool.getStatus());
        }

        questionRepository.delete(existing);
    }

    private QuestionResponse mapToQuestionResponse(QuestionEntity q) {
        QuestionContent content = ContentJsonUtil.fromJson(q.getContentJson());
        return new QuestionResponse(
                q.getId(),
                q.getQuestionId(),
                q.getPoolId(),
                q.getSubjectId(),
                q.getUnit(),
                q.getMarks(),
                q.getDifficulty(),
                q.getQuestionType(),
                q.getCognitiveLevel(),
                q.getSetterId(),
                q.getStatus(),
                q.getVersion(),
                content,
                q.getHash(),
                q.getCreatedAt(),
                q.getUpdatedAt()
        );
    }
}
