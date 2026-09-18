package com.examchain.centre;

import com.examchain.blockchain.service.FabricLedgerService;
import com.examchain.centre.dto.CentreDtos.SecurePrintRequest;
import com.examchain.centre.dto.CentreDtos.SecurePrintResponse;
import com.examchain.centre.entity.CentreTerminalEntity;
import com.examchain.centre.entity.PrintAuditLogEntity;
import com.examchain.centre.repository.CentreTerminalRepository;
import com.examchain.centre.repository.PrintAuditLogRepository;
import com.examchain.centre.service.DeviceFingerprintService;
import com.examchain.centre.service.SecurePrintingService;
import com.examchain.generation.dto.BlueprintDtos.GeneratedPaperResponse;
import com.examchain.generation.dto.BlueprintDtos.PaperQuestionSummary;
import com.examchain.generation.service.DynamicPaperGenerationService;
import com.examchain.question.model.DifficultyLevel;
import com.examchain.question.model.QuestionType;
import com.examchain.release.entity.ReleaseScheduleEntity;
import com.examchain.release.model.ReleaseStatus;
import com.examchain.release.repository.ReleaseScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurePrintingTest {

    private CentreTerminalRepository terminalRepository;
    private PrintAuditLogRepository printAuditLogRepository;
    private ReleaseScheduleRepository releaseScheduleRepository;
    private DeviceFingerprintService fingerprintService;
    private DynamicPaperGenerationService generationService;
    private FabricLedgerService ledgerService;
    private SecurePrintingService printingService;

    @BeforeEach
    void setUp() {
        terminalRepository = Mockito.mock(CentreTerminalRepository.class);
        printAuditLogRepository = Mockito.mock(PrintAuditLogRepository.class);
        releaseScheduleRepository = Mockito.mock(ReleaseScheduleRepository.class);
        generationService = Mockito.mock(DynamicPaperGenerationService.class);
        ledgerService = Mockito.mock(FabricLedgerService.class);

        fingerprintService = new DeviceFingerprintService(terminalRepository);
        printingService = new SecurePrintingService(
                terminalRepository,
                printAuditLogRepository,
                releaseScheduleRepository,
                fingerprintService,
                generationService,
                ledgerService
        );

        when(printAuditLogRepository.save(any(PrintAuditLogEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("Print access is blocked if paper is not in RELEASED status")
    void testPrintBlocked_BeforeRelease() {
        ReleaseScheduleEntity schedule = new ReleaseScheduleEntity(
                "PAP-SE401", null, 2, Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(3, ChronoUnit.HOURS)
        );
        schedule.setStatus(ReleaseStatus.PENDING_APPROVAL);

        when(releaseScheduleRepository.findByPaperId("PAP-SE401")).thenReturn(Optional.of(schedule));

        SecurePrintRequest request = new SecurePrintRequest("CENTRE-01", "TERM-01", "OP-01", "fp-123", 50);

        assertThatThrownBy(() -> printingService.executeSecurePrint("PAP-SE401", request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Centre cannot access paper before official time-lock release");
    }

    @Test
    @DisplayName("Known trusted terminal produces risk score 0")
    void testTrustedTerminal_RiskScoreZero() {
        CentreTerminalEntity terminal = new CentreTerminalEntity("CENTRE-01", "TERM-01", "fp-trusted-hash");
        when(terminalRepository.findByCentreCodeAndTerminalId("CENTRE-01", "TERM-01")).thenReturn(Optional.of(terminal));

        int score = fingerprintService.evaluateRiskScore("CENTRE-01", "TERM-01", "fp-trusted-hash");
        assertThat(score).isEqualTo(0);
    }

    @Test
    @DisplayName("Unregistered terminal produces elevated risk score 85")
    void testUnregisteredTerminal_ElevatedRiskScore() {
        when(terminalRepository.findByCentreCodeAndTerminalId("CENTRE-01", "TERM-UNKNOWN")).thenReturn(Optional.empty());

        int score = fingerprintService.evaluateRiskScore("CENTRE-01", "TERM-UNKNOWN", "fp-any");
        assertThat(score).isEqualTo(85);
    }

    @Test
    @DisplayName("Explicitly blocked terminal produces risk score 100 and blocks printing")
    void testBlockedTerminal_BlocksPrinting() {
        ReleaseScheduleEntity schedule = new ReleaseScheduleEntity(
                "PAP-SE401", null, 2, Instant.now().minus(1, ChronoUnit.HOURS), Instant.now().plus(1, ChronoUnit.HOURS)
        );
        schedule.setStatus(ReleaseStatus.RELEASED);
        when(releaseScheduleRepository.findByPaperId("PAP-SE401")).thenReturn(Optional.of(schedule));

        CentreTerminalEntity blockedTerminal = new CentreTerminalEntity("CENTRE-01", "TERM-COMPROMISED", "fp-bad");
        blockedTerminal.setStatus("BLOCKED");
        when(terminalRepository.findByCentreCodeAndTerminalId("CENTRE-01", "TERM-COMPROMISED")).thenReturn(Optional.of(blockedTerminal));

        SecurePrintRequest request = new SecurePrintRequest("CENTRE-01", "TERM-COMPROMISED", "OP-01", "fp-bad", 50);

        assertThatThrownBy(() -> printingService.executeSecurePrint("PAP-SE401", request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exceeds security threshold");
    }

    @Test
    @DisplayName("Authorized secure print generates forensic watermark, receipt ID, and commits ledger access")
    void testSuccessfulSecurePrint_GeneratesWatermarkAndReceipt() {
        ReleaseScheduleEntity schedule = new ReleaseScheduleEntity(
                "PAP-SE401", null, 2, Instant.now().minus(1, ChronoUnit.HOURS), Instant.now().plus(1, ChronoUnit.HOURS)
        );
        schedule.setStatus(ReleaseStatus.RELEASED);
        when(releaseScheduleRepository.findByPaperId("PAP-SE401")).thenReturn(Optional.of(schedule));

        CentreTerminalEntity terminal = new CentreTerminalEntity("CENTRE-01", "TERM-01", "fp-valid");
        when(terminalRepository.findByCentreCodeAndTerminalId("CENTRE-01", "TERM-01")).thenReturn(Optional.of(terminal));

        List<PaperQuestionSummary> questions = List.of(
                new PaperQuestionSummary(1, "Q1", 1, 10, DifficultyLevel.MEDIUM, QuestionType.LONG_ANSWER, "Explain Byzantine Fault Tolerance", "hash-q1")
        );
        GeneratedPaperResponse paper = new GeneratedPaperResponse(
                UUID.randomUUID(), "PAP-SE401", UUID.randomUUID(), UUID.randomUUID(), "SET_A", 100, 1, "paper-hash", "RELEASED", questions, Instant.now()
        );
        when(generationService.getPaperByPaperId("PAP-SE401")).thenReturn(paper);

        SecurePrintRequest request = new SecurePrintRequest("CENTRE-01", "TERM-01", "OP-ALICE", "fp-valid", 100);

        SecurePrintResponse response = printingService.executeSecurePrint("PAP-SE401", request);

        assertThat(response.receiptId()).startsWith("PRN-REC-");
        assertThat(response.riskScore()).isEqualTo(0);
        assertThat(response.watermark()).contains("CENTRE: CENTRE-01");
        assertThat(response.watermark()).contains("TERMINAL: TERM-01");
        assertThat(response.watermark()).contains("OP: OP-ALICE");
        assertThat(response.watermark()).contains("COPY: 100");
        assertThat(response.questions()).hasSize(1);
        assertThat(response.questions().get(0).questionText()).contains("Byzantine Fault Tolerance");

        // Verify access log recorded on Fabric ledger
        verify(ledgerService, times(1)).recordAccess("PAP-SE401", "CENTRE-01", "OP-ALICE", "SECURE_PRINT", "SUCCESS");
    }
}
