package com.examchain.generation.repository;

import com.examchain.generation.entity.GeneratedPaperEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GeneratedPaperRepository extends JpaRepository<GeneratedPaperEntity, UUID> {
    List<GeneratedPaperEntity> findByBlueprintId(UUID blueprintId);
    Optional<GeneratedPaperEntity> findByPaperId(String paperId);
    boolean existsByPaperId(String paperId);
}

