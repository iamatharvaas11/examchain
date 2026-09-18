package com.examchain.centre.repository;

import com.examchain.centre.entity.PrintAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrintAuditLogRepository extends JpaRepository<PrintAuditLogEntity, UUID> {
    Optional<PrintAuditLogEntity> findByReceiptId(String receiptId);
    List<PrintAuditLogEntity> findByPaperId(String paperId);
    List<PrintAuditLogEntity> findByCentreCode(String centreCode);
}

