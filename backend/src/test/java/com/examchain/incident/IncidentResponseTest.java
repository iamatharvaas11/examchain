package com.examchain.incident;

import com.examchain.blockchain.service.FabricLedgerService;
import com.examchain.incident.dto.IncidentDtos.AnomalyReport;
import com.examchain.incident.dto.IncidentDtos.FreezeStateResponse;
import com.examchain.incident.dto.IncidentDtos.QuarantineVariantRequest;
import com.examchain.incident.dto.IncidentDtos.SecurityIncidentDto;
import com.examchain.incident.entity.SecurityIncidentEntity;
import com.examchain.incident.entity.SystemFreezeStateEntity;
import com.examchain.incident.model.FreezeLevel;
import com.examchain.incident.model.IncidentEventType;
import com.examchain.incident.model.IncidentSeverity;
import com.examchain.incident.repository.SecurityIncidentRepository;
import com.examchain.incident.repository.SystemFreezeStateRepository;
import com.examchain.incident.service.FreezeModeService;
import com.examchain.incident.service.IncidentResponseService;
import com.examchain.qr.entity.PaperProvenanceEntity;
import com.examchain.qr.model.ProvenanceStatus;
import com.examchain.qr.repository.PaperProvenanceRepository;
import com.examchain.release.entity.ReleaseScheduleEntity;
import com.examchain.release.model.ReleaseStatus;
import com.examchain.release.repository.ReleaseScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IncidentResponseTest {

    private SystemFreezeStateRepository freezeRepository;
    private SecurityIncidentRepository incidentRepository;
    private ReleaseScheduleRepository releaseScheduleRepository;
    private PaperProvenanceRepository provenanceRepository;
    private FabricLedgerService ledgerService;

    private FreezeModeService freezeModeService;
    private IncidentResponseService incidentService;

    private SystemFreezeStateEntity currentFreezeEntity;

    @BeforeEach
    void setUp() {
        freezeRepository = Mockito.mock(SystemFreezeStateRepository.class);
        incidentRepository = Mockito.mock(SecurityIncidentRepository.class);
        releaseScheduleRepository = Mockito.mock(ReleaseScheduleRepository.class);
        provenanceRepository = Mockito.mock(PaperProvenanceRepository.class);
        ledgerService = Mockito.mock(FabricLedgerService.class);

        currentFreezeEntity = new SystemFreezeStateEntity(FreezeLevel.NORMAL, "Init", "SYSTEM");
        when(freezeRepository.findById(1)).thenReturn(Optional.of(currentFreezeEntity));
        when(freezeRepository.save(any(SystemFreezeStateEntity.class))).thenAnswer(i -> {
            currentFreezeEntity = i.getArgument(0);
            return currentFreezeEntity;
        });

        when(incidentRepository.save(any(SecurityIncidentEntity.class))).thenAnswer(i -> i.getArgument(0));

        freezeModeService = new FreezeModeService(freezeRepository);
        incidentService = new IncidentResponseService(
                incidentRepository,
                freezeModeService,
                releaseScheduleRepository,
                provenanceRepository,
                ledgerService
        );
    }

    @Test
    @DisplayName("Freeze mode transitions through NORMAL -> SUSPICIOUS -> FROZEN")
    void testFreezeStateTransitions() {
        assertThat(freezeModeService.getCurrentLevel()).isEqualTo(FreezeLevel.NORMAL);

        FreezeStateResponse r1 = freezeModeService.updateFreezeLevel(FreezeLevel.SUSPICIOUS, "Unusual traffic", "AUDIT_MONITOR");
        assertThat(r1.freezeLevel()).isEqualTo(FreezeLevel.SUSPICIOUS);

        FreezeStateResponse r2 = freezeModeService.updateFreezeLevel(FreezeLevel.FROZEN, "Critical integrity alert", "CONTROLLER");
        assertThat(r2.freezeLevel()).isEqualTo(FreezeLevel.FROZEN);

        assertThatThrownBy(() -> freezeModeService.assertNotFrozen())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("examination system is in FROZEN mode");
    }

    @Test
    @DisplayName("Duplicate print anomaly automatically escalates freeze level to SUSPICIOUS and quarantines variant")
    void testDuplicatePrintAnomaly_TriggersQuarantineAndSuspicious() {
        String paperId = "PAP-LEAK-CANDIDATE";
        ReleaseScheduleEntity schedule = new ReleaseScheduleEntity(paperId, null, 2, Instant.now(), Instant.now());
        schedule.setStatus(ReleaseStatus.RELEASED);
        when(releaseScheduleRepository.findByPaperId(paperId)).thenReturn(Optional.of(schedule));

        PaperProvenanceEntity prov = new PaperProvenanceEntity("TRC-001", paperId, "hash", "EX1", "Title", "C1", Instant.now(), Instant.now());
        when(provenanceRepository.findByPaperId(paperId)).thenReturn(Optional.of(prov));

        AnomalyReport report = incidentService.reportAnomaly(
                IncidentEventType.DUPLICATE_PRINT,
                IncidentSeverity.CRITICAL,
                paperId,
                "CENTRE-01",
                "OP-BAD",
                "Duplicate print request detected on unregistered terminal"
        );

        assertThat(report.eventType()).isEqualTo(IncidentEventType.DUPLICATE_PRINT);
        assertThat(report.actionTaken()).contains("QUARANTINE_VARIANT");
        assertThat(report.currentFreezeLevel()).isEqualTo(FreezeLevel.SUSPICIOUS);

        // Verify schedule and provenance status set to QUARANTINED
        assertThat(schedule.getStatus()).isEqualTo(ReleaseStatus.QUARANTINED);
        assertThat(prov.getStatus()).isEqualTo(ProvenanceStatus.QUARANTINED);

        // Verify on-chain quarantine transaction
        verify(ledgerService, times(1)).quarantineVariant(eq(paperId), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Manual quarantine request successfully quarantines variant and commits to ledger")
    void testManualQuarantine() {
        String paperId = "PAP-SUSPECT-02";
        ReleaseScheduleEntity schedule = new ReleaseScheduleEntity(paperId, null, 2, Instant.now(), Instant.now());
        when(releaseScheduleRepository.findByPaperId(paperId)).thenReturn(Optional.of(schedule));

        QuarantineVariantRequest req = new QuarantineVariantRequest(
                paperId, "INC-777", "Paper leak suspected on social media", "CONTROLLER_SECURITY"
        );

        SecurityIncidentDto res = incidentService.quarantineVariant(req);

        assertThat(res.paperId()).isEqualTo(paperId);
        assertThat(res.incidentId()).isEqualTo("INC-777");
        assertThat(res.isQuarantined()).isTrue();
        assertThat(schedule.getStatus()).isEqualTo(ReleaseStatus.QUARANTINED);

        verify(ledgerService, times(1)).quarantineVariant(paperId, "INC-777", "Paper leak suspected on social media", "CONTROLLER_SECURITY");
    }
}

