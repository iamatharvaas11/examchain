package com.examchain.offline.repository;

import com.examchain.offline.entity.OfflineAuditQueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfflineAuditQueueRepository extends JpaRepository<OfflineAuditQueueEntity, UUID> {
    List<OfflineAuditQueueEntity> findByCentreCodeAndSyncedFalseOrderByCreatedAtAsc(String centreCode);
    List<OfflineAuditQueueEntity> findByCentreCodeOrderByCreatedAtDesc(String centreCode);
}

