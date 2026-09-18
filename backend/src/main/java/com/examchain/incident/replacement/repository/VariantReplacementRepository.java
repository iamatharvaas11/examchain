package com.examchain.incident.replacement.repository;

import com.examchain.incident.replacement.entity.VariantReplacementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VariantReplacementRepository extends JpaRepository<VariantReplacementEntity, UUID> {
    Optional<VariantReplacementEntity> findByReplacementId(String replacementId);
    List<VariantReplacementEntity> findByQuarantinedPaperId(String quarantinedPaperId);
    List<VariantReplacementEntity> findAllByOrderByCreatedAtDesc();
}

