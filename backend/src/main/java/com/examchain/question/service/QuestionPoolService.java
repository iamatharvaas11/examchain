package com.examchain.question.service;

import com.examchain.exam.repository.SubjectRepository;
import com.examchain.question.dto.QuestionDtos.QuestionPoolRequest;
import com.examchain.question.dto.QuestionDtos.QuestionPoolResponse;
import com.examchain.question.entity.QuestionPoolEntity;
import com.examchain.question.model.PoolStatus;
import com.examchain.question.repository.QuestionPoolRepository;
import com.examchain.question.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class QuestionPoolService {

    private final QuestionPoolRepository poolRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;

    @Autowired
    public QuestionPoolService(
            QuestionPoolRepository poolRepository,
            QuestionRepository questionRepository,
            SubjectRepository subjectRepository
    ) {
        this.poolRepository = poolRepository;
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
    }

    public QuestionPoolResponse createPool(QuestionPoolRequest request, String createdBy) {
        if (!subjectRepository.existsById(request.subjectId())) {
            throw new IllegalArgumentException("Subject not found: " + request.subjectId());
        }
        if (poolRepository.existsBySubjectIdAndPoolCode(request.subjectId(), request.poolCode())) {
            throw new IllegalArgumentException("Pool code '" + request.poolCode() + "' already exists for subject");
        }

        QuestionPoolEntity entity = QuestionPoolEntity.builder()
                .subjectId(request.subjectId())
                .poolCode(request.poolCode().trim().toUpperCase())
                .name(request.name().trim())
                .description(request.description())
                .status(PoolStatus.DRAFT)
                .createdBy(createdBy)
                .build();

        QuestionPoolEntity saved = poolRepository.save(entity);
        return mapToPoolResponse(saved);
    }

    @Transactional(readOnly = true)
    public QuestionPoolResponse getPoolById(UUID poolId) {
        QuestionPoolEntity entity = poolRepository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Question pool not found: " + poolId));
        return mapToPoolResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<QuestionPoolResponse> getPoolsForSubject(UUID subjectId) {
        return poolRepository.findBySubjectId(subjectId).stream()
                .map(this::mapToPoolResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionPoolResponse> getPoolsByCreator(String creator) {
        return poolRepository.findByCreatedBy(creator).stream()
                .map(this::mapToPoolResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionPoolResponse> getAllPools() {
        return poolRepository.findAll().stream()
                .map(this::mapToPoolResponse)
                .toList();
    }

    public QuestionPoolResponse submitPool(UUID poolId, String setterId) {
        QuestionPoolEntity pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Question pool not found: " + poolId));

        if (pool.getStatus() != PoolStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT pools can be submitted. Current status: " + pool.getStatus());
        }

        long count = questionRepository.countByPoolId(poolId);
        if (count == 0) {
            throw new IllegalStateException("Cannot submit an empty pool. Add at least one question.");
        }

        pool.setStatus(PoolStatus.SUBMITTED);
        QuestionPoolEntity updated = poolRepository.save(pool);
        return mapToPoolResponse(updated);
    }

    public QuestionPoolResponse reviewPool(UUID poolId, PoolStatus decision, String reviewedBy) {
        if (decision != PoolStatus.APPROVED && decision != PoolStatus.REJECTED) {
            throw new IllegalArgumentException("Review decision must be APPROVED or REJECTED");
        }

        QuestionPoolEntity pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Question pool not found: " + poolId));

        if (pool.getStatus() != PoolStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED pools can be reviewed. Current status: " + pool.getStatus());
        }

        pool.setStatus(decision);
        pool.setApprovedBy(reviewedBy);
        QuestionPoolEntity updated = poolRepository.save(pool);
        return mapToPoolResponse(updated);
    }

    private QuestionPoolResponse mapToPoolResponse(QuestionPoolEntity p) {
        long count = questionRepository.countByPoolId(p.getId());
        return new QuestionPoolResponse(
                p.getId(),
                p.getSubjectId(),
                p.getPoolCode(),
                p.getName(),
                p.getDescription(),
                p.getStatus(),
                p.getCreatedBy(),
                p.getApprovedBy(),
                count,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

