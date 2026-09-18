package com.examchain.qr.repository;

import com.examchain.qr.entity.PaperProvenanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaperProvenanceRepository extends JpaRepository<PaperProvenanceEntity, UUID> {
    Optional<PaperProvenanceEntity> findByTraceId(String traceId);
    Optional<PaperProvenanceEntity> findByPaperId(String paperId);
}
