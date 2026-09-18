package com.examchain.generation.repository;

import com.examchain.generation.entity.GeneratedPaperQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GeneratedPaperQuestionRepository extends JpaRepository<GeneratedPaperQuestionEntity, UUID> {
    List<GeneratedPaperQuestionEntity> findByPaperIdOrderBySequenceNumberAsc(UUID paperId);
}

