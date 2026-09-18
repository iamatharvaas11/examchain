package com.examchain.offline.service;

import com.examchain.blockchain.service.FabricLedgerService;
import com.examchain.offline.dto.OfflineDtos.*;
import com.examchain.offline.entity.OfflineAuditQueueEntity;
import com.examchain.offline.repository.OfflineAuditQueueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OfflineSyncService {

    private static final Logger log = LoggerFactory.getLogger(OfflineSyncService.class);

    private final OfflineAuditQueueRepository queueRepository;
    private final FabricLedgerService ledgerService;

    @Autowired
    public OfflineSyncService(
            OfflineAuditQueueRepository queueRepository,
            FabricLedgerService ledgerService
    ) {
        this.queueRepository = queueRepository;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public OfflineAuditItemDto queueAuditEvent(QueueAuditEventRequest request) {
        OfflineAuditQueueEntity entity = new OfflineAuditQueueEntity(
                request.centreCode(),
                request.terminalId(),
                request.eventType(),
                request.payloadJson()
        );
        OfflineAuditQueueEntity saved = queueRepository.save(entity);
        log.info("Queued offline audit event [{}] for centre [{}]", saved.getEventType(), saved.getCentreCode());

        return new OfflineAuditItemDto(
                saved.getCentreCode(),
                saved.getTerminalId(),
                saved.getEventType(),
                saved.getPayloadJson(),
                saved.isSynced(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public SyncReconciliationResponse synchronizeWithLedger(String centreCode) {
        List<OfflineAuditQueueEntity> pending = queueRepository.findByCentreCodeAndSyncedFalseOrderByCreatedAtAsc(centreCode);
        Instant now = Instant.now();
        int count = 0;

        for (OfflineAuditQueueEntity item : pending) {
            // Commit to ledger
            ledgerService.recordAccess(
                    "OFFLINE_EVENT",
                    item.getCentreCode(),
                    item.getTerminalId(),
                    item.getEventType(),
                    "SYNCED_RECONCILED"
            );

            item.setSynced(true);
            item.setSyncedAt(now);
            queueRepository.save(item);
            count++;
        }

        log.info("Completed reconnection sync for centre [{}]: reconciled [{}] offline audit records with ledger.",
                centreCode, count);

        return new SyncReconciliationResponse(
                centreCode,
                count,
                "RECONCILED_WITH_LEDGER",
                now
        );
    }

    @Transactional(readOnly = true)
    public List<OfflineAuditItemDto> getQueueStatus(String centreCode) {
        return queueRepository.findByCentreCodeOrderByCreatedAtDesc(centreCode).stream()
                .map(e -> new OfflineAuditItemDto(
                        e.getCentreCode(),
                        e.getTerminalId(),
                        e.getEventType(),
                        e.getPayloadJson(),
                        e.isSynced(),
                        e.getCreatedAt()
                ))
                .toList();
    }
}

