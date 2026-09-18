package com.examchain.security;

import com.examchain.audit.service.AuditorDashboardService;
import com.examchain.blockchain.service.FabricLedgerService;
import com.examchain.centre.dto.CentreDtos.SecurePrintRequest;
import com.examchain.centre.entity.CentreTerminalEntity;
import com.examchain.centre.repository.CentreTerminalRepository;
import com.examchain.centre.repository.PrintAuditLogRepository;
import com.examchain.centre.service.DeviceFingerprintService;
import com.examchain.centre.service.SecurePrintingService;
import com.examchain.core.exception.ResourceNotFoundException;
import com.examchain.crypto.service.CryptoService;
import com.examchain.generation.dto.BlueprintDtos.GeneratedPaperResponse;
import com.examchain.generation.service.DynamicPaperGenerationService;
import com.examchain.incident.model.FreezeLevel;
import com.examchain.incident.service.FreezeModeService;
import com.examchain.offline.dto.OfflineDtos.ConsumeTokenRequest;
import com.examchain.offline.dto.OfflineDtos.GenerateOfflineTokenRequest;
import com.examchain.offline.dto.OfflineDtos.GeneratedTokenResponse;
import com.examchain.offline.entity.OfflineTokenEntity;
import com.examchain.offline.repository.OfflineTokenRepository;
import com.examchain.offline.service.OfflineTokenService;
import com.examchain.qr.dto.ProvenanceDtos.VerificationResponse;
import com.examchain.qr.entity.PaperProvenanceEntity;
import com.examchain.qr.model.ProvenanceStatus;
import com.examchain.qr.repository.PaperProvenanceRepository;
import com.examchain.qr.service.PaperProvenanceService;
import com.examchain.qr.service.TraceIdGenerator;
import com.examchain.release.dto.ReleaseDtos.SubmitApprovalRequest;
import com.examchain.release.entity.PaperApprovalEntity;
import com.examchain.release.entity.ReleaseScheduleEntity;
import com.examchain.release.model.ReleaseStatus;
import com.examchain.release.repository.PaperApprovalRepository;
import com.examchain.release.repository.ReleaseScheduleRepository;
import com.examchain.release.service.ThresholdApprovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.crypto.AEADBadTagException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Phase 16: Comprehensive Adversarial Attack Simulation Suite
 * Validates system resilience against real-world examination leak vectors:
 * 1. Unauthorized early access before time-lock
 * 2. Unregistered / compromised terminal print attempt
 * 3. Ciphertext bit tampering (AES-256-GCM AEAD authentication failure)
 * 4. Fake / spoofed QR trace ID verification
 * 5. Offline authorization token replay attack
 * 6. Sybil / duplicate approval by single authority attempting quorum spoofing
 * 7. Release of quarantined paper variant
 */
class SecurityAttackSimulationTest {

    private CryptoService cryptoService;
    private ReleaseScheduleRepository releaseScheduleRepository;
    private PaperApprovalRepository approvalRepository;
    private PaperProvenanceRepository provenanceRepository;
    private DynamicPaperGenerationService generationService;
    private FabricLedgerService ledgerService;
    private CentreTerminalRepository terminalRepository;
    private PrintAuditLogRepository printAuditLogRepository;
    private OfflineTokenRepository offlineTokenRepository;
    private FreezeModeService freezeModeService;

    private ThresholdApprovalService thresholdApprovalService;
    private SecurePrintingService securePrintingService;
    private PaperProvenanceService provenanceService;
    private OfflineTokenService offlineTokenService;

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService();
        releaseScheduleRepository = Mockito.mock(ReleaseScheduleRepository.class);
        approvalRepository = Mockito.mock(PaperApprovalRepository.class);
        provenanceRepository = Mockito.mock(PaperProvenanceRepository.class);
        generationService = Mockito.mock(DynamicPaperGenerationService.class);
        ledgerService = Mockito.mock(FabricLedgerService.class);
        terminalRepository = Mockito.mock(CentreTerminalRepository.class);
        printAuditLogRepository = Mockito.mock(PrintAuditLogRepository.class);
        offlineTokenRepository = Mockito.mock(OfflineTokenRepository.class);
        freezeModeService = Mockito.mock(FreezeModeService.class);

        thresholdApprovalService = new ThresholdApprovalService(releaseScheduleRepository, approvalRepository, ledgerService);
        DeviceFingerprintService fingerprintService = new DeviceFingerprintService(terminalRepository);
        securePrintingService = new SecurePrintingService(
                terminalRepository, printAuditLogRepository, releaseScheduleRepository,
                fingerprintService, generationService, ledgerService, freezeModeService
        );
        provenanceService = new PaperProvenanceService(provenanceRepository, new TraceIdGenerator(), generationService);
        offlineTokenService = new OfflineTokenService(offlineTokenRepository);
    }

    @Test
    @DisplayName("ATTACK 1: Centre attempts early print before time-lock release -> BLOCKED")
    void attack1_EarlyPrintAttempt_Blocked() {
        String paperId = "PAP-ATTACK-01";
        ReleaseScheduleEntity schedule = new ReleaseScheduleEntity(
                paperId, null, 2, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(4, ChronoUnit.HOURS)
        );
        schedule.setStatus(ReleaseStatus.PENDING_APPROVAL);
        when(releaseScheduleRepository.findByPaperId(paperId)).thenReturn(Optional.of(schedule));

        SecurePrintRequest request = new SecurePrintRequest("CENTRE-DELHI-01", "TERM-01", "OP-MALICIOUS", "fp-123", 50);

        assertThatThrownBy(() -> securePrintingService.executeSecurePrint(paperId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Centre cannot access paper before official time-lock release");
    }

    @Test
    @DisplayName("ATTACK 2: Compromised / blocked terminal attempts printing -> BLOCKED by device continuity")
    void attack2_BlockedTerminalPrint_Blocked() {
        String paperId = "PAP-ATTACK-02";
        ReleaseScheduleEntity schedule = new ReleaseScheduleEntity(paperId, null, 2, Instant.now().minus(1, ChronoUnit.HOURS), Instant.now().plus(1, ChronoUnit.HOURS));
        schedule.setStatus(ReleaseStatus.RELEASED);
        when(releaseScheduleRepository.findByPaperId(paperId)).thenReturn(Optional.of(schedule));

        CentreTerminalEntity blockedTerminal = new CentreTerminalEntity("CENTRE-01", "TERM-ROGUE", "fp-rogue");
        blockedTerminal.setStatus("BLOCKED");
        when(terminalRepository.findByCentreCodeAndTerminalId("CENTRE-01", "TERM-ROGUE")).thenReturn(Optional.of(blockedTerminal));

        SecurePrintRequest request = new SecurePrintRequest("CENTRE-01", "TERM-ROGUE", "OP-01", "fp-rogue", 100);

        assertThatThrownBy(() -> securePrintingService.executeSecurePrint(paperId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exceeds security threshold");
    }

    @Test
    @DisplayName("ATTACK 3: Malicious adversary tampers with encrypted ciphertext -> AEAD authentication failure")
    void attack3_CiphertextTampering_AEADTagFailure() {
        javax.crypto.SecretKey key = cryptoService.generateAes256Key();
        byte[] aad = "EXAM-CONFIDENTIAL".getBytes(StandardCharsets.UTF_8);
        byte[] plaintext = "CONFIDENTIAL EXAMINATION PAPER DATA".getBytes(StandardCharsets.UTF_8);

        com.examchain.crypto.model.EncryptedPayload payload = cryptoService.encrypt(plaintext, key, aad);

        // Adversary flips a byte in ciphertext
        byte[] tamperedCiphertext = payload.ciphertext().clone();
        tamperedCiphertext[tamperedCiphertext.length - 1] ^= 0xFF;

        com.examchain.crypto.model.EncryptedPayload tamperedPayload = new com.examchain.crypto.model.EncryptedPayload(
                tamperedCiphertext, payload.iv(), payload.sha256PlaintextHash(), payload.sha256CiphertextHash()
        );

        assertThatThrownBy(() -> cryptoService.decrypt(tamperedPayload, key, aad))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Authentication tag mismatch");
    }

    @Test
    @DisplayName("ATTACK 4: Adversary scans or submits fake QR trace ID -> 404 Not Found")
    void attack4_FakeQrVerification_Rejected() {
        when(provenanceRepository.findByTraceId("TRC-FAKE-SPOOFED-ID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> provenanceService.verifyPaper("TRC-FAKE-SPOOFED-ID"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No examination record found");
    }

    @Test
    @DisplayName("ATTACK 5: Adversary intercepts and replays offline emergency token -> BLOCKED (Anti-Replay)")
    void attack5_OfflineTokenReplay_Blocked() {
        OfflineTokenEntity token = new OfflineTokenEntity(
                "OFL-TKN-TEST", "CENTRE-01", "PAP-01", "hash", Instant.now().minus(1, ChronoUnit.HOURS), Instant.now().plus(1, ChronoUnit.HOURS)
        );
        token.setUsed(true); // Already consumed!
        token.setUsedAt(Instant.now().minus(15, ChronoUnit.MINUTES));

        when(offlineTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        ConsumeTokenRequest replayReq = new ConsumeTokenRequest("raw-secret", "CENTRE-01", "PAP-01", "TERM-02");

        assertThatThrownBy(() -> offlineTokenService.validateAndConsumeToken(replayReq))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Offline token replay detected");
    }

    @Test
    @DisplayName("ATTACK 6: Single corrupt authority attempts duplicate approval to meet quorum -> BLOCKED")
    void attack6_DuplicateApprovalQuorumSpoofing_Blocked() {
        String paperId = "PAP-CORRUPT-ATTEMPT";
        PaperApprovalEntity existingApproval = new PaperApprovalEntity(paperId, "AUTH-CORRUPT", "AUTHORITY", "Alice", "hash", null);
        when(approvalRepository.findByPaperIdAndAuthorityId(paperId, "AUTH-CORRUPT")).thenReturn(Optional.of(existingApproval));

        ReleaseScheduleEntity schedule = new ReleaseScheduleEntity(paperId, null, 2, Instant.now(), Instant.now());
        when(releaseScheduleRepository.findByPaperId(paperId)).thenReturn(Optional.of(schedule));

        SubmitApprovalRequest req = new SubmitApprovalRequest("AUTH-CORRUPT", "AUTHORITY", "Alice", "Attempt 2");

        assertThatThrownBy(() -> thresholdApprovalService.submitApproval(paperId, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already submitted approval");
    }

    @Test
    @DisplayName("ATTACK 7: Malicious operator attempts release of quarantined variant -> BLOCKED")
    void attack7_QuarantinedVariantRelease_Blocked() {
        String paperId = "PAP-LEAKED-QUARANTINED";
        ReleaseScheduleEntity schedule = new ReleaseScheduleEntity(paperId, null, 2, Instant.now().minus(1, ChronoUnit.HOURS), Instant.now().plus(1, ChronoUnit.HOURS));
        schedule.setCurrentApprovalCount(3);
        schedule.setStatus(ReleaseStatus.QUARANTINED);
        when(releaseScheduleRepository.findByPaperId(paperId)).thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> thresholdApprovalService.authorizeRelease(paperId, "CONTROLLER_COMPROMISED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("paper variant is quarantined");
    }
}
