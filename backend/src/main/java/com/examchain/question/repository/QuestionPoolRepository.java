package com.examchain.question.repository;

import com.examchain.question.entity.QuestionPoolEntity;
import com.examchain.question.model.PoolStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionPoolRepository extends JpaRepository<QuestionPoolEntity, UUID> {
    List<QuestionPoolEntity> findBySubjectId(UUID subjectId);
    List<QuestionPoolEntity> findByCreatedBy(String createdBy);
    List<QuestionPoolEntity> findByStatus(PoolStatus status);
    Optional<QuestionPoolEntity> findBySubjectIdAndPoolCode(UUID subjectId, String poolCode);
    boolean existsBySubjectIdAndPoolCode(UUID subjectId, String poolCode);
}
