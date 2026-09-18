package com.examchain.question.repository;

import com.examchain.question.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, UUID> {
    List<QuestionEntity> findByPoolId(UUID poolId);
    List<QuestionEntity> findBySubjectId(UUID subjectId);
    Optional<QuestionEntity> findByQuestionId(String questionId);
    boolean existsByQuestionId(String questionId);
    long countByPoolId(UUID poolId);
}

