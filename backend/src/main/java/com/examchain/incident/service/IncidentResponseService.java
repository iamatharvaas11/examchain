package com.examchain.incident.service;

import com.examchain.blockchain.service.FabricLedgerService;
import com.examchain.incident.dto.IncidentDtos.*;
import com.examchain.incident.entity.SecurityIncidentEntity;
import com.examchain.incident.model.FreezeLevel;
import com.examchain.incident.model.IncidentEventType;
import com.examchain.incident.model.IncidentSeverity;
import com.examchain.incident.repository.SecurityIncidentRepository;
import com.examchain.qr.model.ProvenanceStatus;
import com.examchain.qr.repository.PaperProvenanceRepository;
import com.examchain.release.model.ReleaseStatus;
import com.examchain.release.repository.ReleaseScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
public class IncidentResponseService {

    private static final Logger log = LoggerFactory.getLogger(IncidentResponseService.class);

    private final SecurityIncidentRepository incidentRepository;
    private final FreezeModeService freezeModeService;
    private final ReleaseScheduleRepository releaseScheduleRepository;
    private final PaperProvenanceRepository provenanceRepository;
    private final FabricLedgerService ledgerService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public IncidentResponseService(
            SecurityIncidentRepository incidentRepository,
            FreezeModeService freezeModeService,
            ReleaseScheduleRepository releaseScheduleRepository,
            PaperProvenanceRepository provenanceRepository,
            FabricLedgerService ledgerService
    ) {
        this.incidentRepository = incidentRepository;
        this.freezeModeService = freezeModeService;
        this.releaseScheduleRepository = releaseScheduleRepository;
        this.provenanceRepository = provenanceRepository;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public AnomalyReport reportAnomaly(
            IncidentEventType eventType,
            IncidentSeverity severity,
            String paperId,
            String centreCode,
            String operatorId,
            String details
    ) {
        String incidentId = generateIncidentId();
        boolean quarantineTriggered = false;

        // Auto-escalation policy
        if (severity == IncidentSeverity.CRITICAL || eventType == IncidentEventType.DUPLICATE_PRINT) {
            freezeModeService.updateFreezeLevel(FreezeLevel.SUSPICIOUS, "Anomaly detected: " + eventType, "INCIDENT_DETECTOR");
            if (paperId != null) {
                executeQuarantine(paperId, incidentId, "Automatic quarantine on anomaly: " + details, "AUTOMATED_INCIDENT_MONITOR");
                quarantineTriggered = true;
            }
        }

        SecurityIncidentEntity entity = new SecurityIncidentEntity(
                incidentId,
                eventType,
                severity,
                paperId,
                centreCode,
                operatorId,
                details,
                quarantineTriggered
        );
        incidentRepository.save(entity);

        String action = quarantineTriggered ? "QUARANTINE_VARIANT_AND_FLAG_SUSPICIOUS" : "LOGGED_WARNING";
        return new AnomalyReport(
                incidentId,
                eventType,
                severity,
                action,
                freezeModeService.getCurrentLevel(),
                Instant.now()
        );
    }

    @Transactional
    public SecurityIncidentDto quarantineVariant(QuarantineVariantRequest request) {
        executeQuarantine(request.paperId(), request.incidentId(), request.reason(), request.quarantinedBy());

        SecurityIncidentEntity entity = new SecurityIncidentEntity(
                request.incidentId(),
                IncidentEventType.MANUAL_OVERRIDE,
                IncidentSeverity.HIGH,
                request.paperId(),
                "AUTHORITY_HQ",
                request.quarantinedBy(),
                request.reason(),
                true
        );
        SecurityIncidentEntity saved = incidentRepository.save(entity);

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SecurityIncidentDto> getAllIncidents() {
        return incidentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .toList();
    }

    private void executeQuarantine(String paperId, String incidentId, String reason, String actor) {
        // 1. Lock release schedule
        releaseScheduleRepository.findByPaperId(paperId).ifPresent(schedule -> {
            schedule.setStatus(ReleaseStatus.QUARANTINED);
            releaseScheduleRepository.save(schedule);
        });

        // 2. Lock paper provenance scan status
        provenanceRepository.findByPaperId(paperId).ifPresent(prov -> {
            prov.setStatus(ProvenanceStatus.QUARANTINED);
            provenanceRepository.save(prov);
        });

        // 3. Commit QuarantineVariant transaction to Fabric ledger
        ledgerService.quarantineVariant(paperId, incidentId, reason, actor);
        log.warn("PAPER VARIANT [{}] HAS BEEN QUARANTINED by [{}] (Incident: {})", paperId, actor, incidentId);
    }

    private String generateIncidentId() {
        byte[] bytes = new byte[4];
        secureRandom.nextBytes(bytes);
        return "INC-" + HexFormat.of().formatHex(bytes).toUpperCase();
    }

    private SecurityIncidentDto mapToDto(SecurityIncidentEntity e) {
        return new SecurityIncidentDto(
                e.getIncidentId(),
                e.getEventType(),
                e.getSeverity(),
                e.getPaperId(),
                e.getCentreCode(),
                e.getOperatorId(),
                e.getDetails(),
                e.isQuarantined(),
                e.getCreatedAt()
        );
    }
}

