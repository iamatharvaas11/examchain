package com.examchain.generation.repository;

import com.examchain.generation.entity.BlueprintEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlueprintRepository extends JpaRepository<BlueprintEntity, UUID> {
    List<BlueprintEntity> findBySubjectId(UUID subjectId);
    Optional<BlueprintEntity> findByBlueprintCode(String blueprintCode);
    boolean existsByBlueprintCode(String blueprintCode);
}
