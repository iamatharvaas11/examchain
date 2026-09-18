package com.examchain.incident;

import com.examchain.blockchain.service.FabricLedgerService;
import com.examchain.generation.dto.BlueprintDtos.GeneratedPaperResponse;
import com.examchain.generation.service.DynamicPaperGenerationService;
import com.examchain.incident.replacement.dto.ReplacementDtos.ExecuteReplacementRequest;
import com.examchain.incident.replacement.dto.ReplacementDtos.ReplacementResponse;
import com.examchain.incident.replacement.entity.VariantReplacementEntity;
import com.examchain.incident.replacement.repository.VariantReplacementRepository;
import com.examchain.incident.replacement.service.VariantReplacementService;
import com.examchain.qr.model.ProvenanceStatus;
import com.examchain.qr.repository.PaperProvenanceRepository;
import com.examchain.qr.service.PaperProvenanceService;
import com.examchain.release.entity.ReleaseScheduleEntity;
import com.examchain.release.model.ReleaseStatus;
import com.examchain.release.repository.ReleaseScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VariantReplacementTest {

    private VariantReplacementRepository replacementRepository;
    private ReleaseScheduleRepository releaseScheduleRepository;
    private PaperProvenanceRepository provenanceRepository;
    private PaperProvenanceService provenanceService;
    private DynamicPaperGenerationService generationService;
    private FabricLedgerService ledgerService;
    private VariantReplacementService replacementService;

    @BeforeEach
    void setUp() {
        replacementRepository = Mockito.mock(VariantReplacementRepository.class);
        releaseScheduleRepository = Mockito.mock(ReleaseScheduleRepository.class);
        provenanceRepository = Mockito.mock(PaperProvenanceRepository.class);
        provenanceService = Mockito.mock(PaperProvenanceService.class);
        generationService = Mockito.mock(DynamicPaperGenerationService.class);
        ledgerService = Mockito.mock(FabricLedgerService.class);

        when(replacementRepository.save(any(VariantReplacementEntity.class))).thenAnswer(i -> i.getArgument(0));

        replacementService = new VariantReplacementService(
                replacementRepository,
                releaseScheduleRepository,
                provenanceRepository,
                provenanceService,
                generationService,
                ledgerService
        );
    }

    @Test
    @DisplayName("Attempting to replace an unquarantined variant throws IllegalStateException")
    void testReplaceUnquarantinedVariant_ThrowsException() {
        ReleaseScheduleEntity schedule = new ReleaseScheduleEntity("PAP-NORMAL", null, 2, Instant.now(), Instant.now());
        schedule.setStatus(ReleaseStatus.RELEASED);
        when(releaseScheduleRepository.findByPaperId("PAP-NORMAL")).thenReturn(Optional.of(schedule));

        ExecuteReplacementRequest req = new ExecuteReplacementRequest(
                "PAP-NORMAL", "PAP-RESERVE", "EXAM-1", "Suspected leak", "CONTROLLER"
        );

        assertThatThrownBy(() -> replacementService.executeReplacement(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only quarantined variants can be replaced");
    }

    @Test
    @DisplayName("Valid replacement revokes compromised variant, activates reserve, and logs on ledger")
    void testSuccessfulVariantReplacement() {
        String quarantinedId = "PAP-QUARANTINED-A";
        String reserveId = "PAP-RESERVE-B";

        ReleaseScheduleEntity quarantinedSchedule = new ReleaseScheduleEntity(quarantinedId, null, 2, Instant.now(), Instant.now().plus(2, ChronoUnit.HOURS));
        quarantinedSchedule.setStatus(ReleaseStatus.QUARANTINED);
        when(releaseScheduleRepository.findByPaperId(quarantinedId)).thenReturn(Optional.of(quarantinedSchedule));

        GeneratedPaperResponse reservePaper = new GeneratedPaperResponse(
                UUID.randomUUID(), reserveId, UUID.randomUUID(), UUID.randomUUID(), "SET_B", 100, 10, "hash-b", "GENERATED", Collections.emptyList(), Instant.now()
        );
        when(generationService.getPaperByPaperId(reserveId)).thenReturn(reservePaper);
        when(releaseScheduleRepository.findByPaperId(reserveId)).thenReturn(Optional.empty());

        ExecuteReplacementRequest req = new ExecuteReplacementRequest(
                quarantinedId, reserveId, "CS101", "Tamper anomaly at Centre North", "CHIEF_CONTROLLER"
        );

        ReplacementResponse res = replacementService.executeReplacement(req);

        assertThat(res.replacementId()).startsWith("RPL-");
        assertThat(res.quarantinedPaperId()).isEqualTo(quarantinedId);
        assertThat(res.reservePaperId()).isEqualTo(reserveId);
        assertThat(res.status()).isEqualTo("COMPLETED");

        // Quarantined paper status should be updated to REPLACED
        assertThat(quarantinedSchedule.getStatus()).isEqualTo(ReleaseStatus.REPLACED);

        // Verify on-chain ReplaceVariant transaction committed
        verify(ledgerService, times(1)).replaceVariant(quarantinedId, reserveId, "CHIEF_CONTROLLER");
    }
}

