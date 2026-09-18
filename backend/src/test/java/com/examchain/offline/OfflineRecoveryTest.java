package com.examchain.offline;

import com.examchain.blockchain.service.FabricLedgerService;
import com.examchain.offline.dto.OfflineDtos.*;
import com.examchain.offline.entity.OfflineAuditQueueEntity;
import com.examchain.offline.entity.OfflineTokenEntity;
import com.examchain.offline.repository.OfflineAuditQueueRepository;
import com.examchain.offline.repository.OfflineTokenRepository;
import com.examchain.offline.service.OfflineSyncService;
import com.examchain.offline.service.OfflineTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OfflineRecoveryTest {

    private OfflineTokenRepository tokenRepository;
    private OfflineAuditQueueRepository queueRepository;
    private FabricLedgerService ledgerService;

    private OfflineTokenService tokenService;
    private OfflineSyncService syncService;

    private OfflineTokenEntity mockTokenEntity;
    private final List<OfflineAuditQueueEntity> queueRecords = new ArrayList<>();

    @BeforeEach
    void setUp() {
        tokenRepository = Mockito.mock(OfflineTokenRepository.class);
        queueRepository = Mockito.mock(OfflineAuditQueueRepository.class);
        ledgerService = Mockito.mock(FabricLedgerService.class);
        queueRecords.clear();

        when(tokenRepository.save(any(OfflineTokenEntity.class))).thenAnswer(i -> {
            mockTokenEntity = i.getArgument(0);
            return mockTokenEntity;
        });

        when(queueRepository.save(any(OfflineAuditQueueEntity.class))).thenAnswer(i -> {
            OfflineAuditQueueEntity e = i.getArgument(0);
            queueRecords.add(e);
            return e;
        });

        tokenService = new OfflineTokenService(tokenRepository);
        syncService = new OfflineSyncService(queueRepository, ledgerService);
    }

    @Test
    @DisplayName("Generate and consume single-use emergency offline token succeeds once")
    void testGenerateAndConsumeToken_Success() {
        GenerateOfflineTokenRequest genReq = new GenerateOfflineTokenRequest(
                "CENTRE-DL-01", "PAP-PHY-SET_A", Instant.now().minus(5, ChronoUnit.MINUTES), Instant.now().plus(2, ChronoUnit.HOURS)
        );

        GeneratedTokenResponse genRes = tokenService.generateOfflineToken(genReq);
        assertThat(genRes.tokenId()).startsWith("OFL-TKN-");
        assertThat(genRes.rawTokenSecret()).startsWith("OFL-SEC-");

        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(mockTokenEntity));

        ConsumeTokenRequest consumeReq = new ConsumeTokenRequest(
                genRes.rawTokenSecret(), "CENTRE-DL-01", "PAP-PHY-SET_A", "TERM-EMERGENCY-01"
        );

        TokenValidationResponse valRes = tokenService.validateAndConsumeToken(consumeReq);
        assertThat(valRes.valid()).isTrue();
        assertThat(valRes.consumedAt()).isNotNull();
        assertThat(mockTokenEntity.isUsed()).isTrue();
    }

    @Test
    @DisplayName("Replaying an already consumed offline token is strictly blocked")
    void testReplayToken_ThrowsException() {
        GenerateOfflineTokenRequest genReq = new GenerateOfflineTokenRequest(
                "CENTRE-DL-01", "PAP-PHY-SET_A", Instant.now().minus(5, ChronoUnit.MINUTES), Instant.now().plus(2, ChronoUnit.HOURS)
        );
        GeneratedTokenResponse genRes = tokenService.generateOfflineToken(genReq);

        // Mark as already used
        mockTokenEntity.setUsed(true);
        mockTokenEntity.setUsedAt(Instant.now().minus(10, ChronoUnit.MINUTES));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(mockTokenEntity));

        ConsumeTokenRequest consumeReq = new ConsumeTokenRequest(
                genRes.rawTokenSecret(), "CENTRE-DL-01", "PAP-PHY-SET_A", "TERM-EMERGENCY-02"
        );

        assertThatThrownBy(() -> tokenService.validateAndConsumeToken(consumeReq))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Offline token replay detected");
    }

    @Test
    @DisplayName("Expired offline token is rejected")
    void testExpiredToken_ThrowsException() {
        GenerateOfflineTokenRequest genReq = new GenerateOfflineTokenRequest(
                "CENTRE-DL-01", "PAP-PHY-SET_A", Instant.now().minus(2, ChronoUnit.HOURS), Instant.now().minus(30, ChronoUnit.MINUTES)
        );
        GeneratedTokenResponse genRes = tokenService.generateOfflineToken(genReq);

        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(mockTokenEntity));

        ConsumeTokenRequest consumeReq = new ConsumeTokenRequest(
                genRes.rawTokenSecret(), "CENTRE-DL-01", "PAP-PHY-SET_A", "TERM-EMERGENCY-01"
        );

        assertThatThrownBy(() -> tokenService.validateAndConsumeToken(consumeReq))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Reconnection sync reconciles local offline queue events with central ledger")
    void testReconnectionSync_ReconcilesWithLedger() {
        OfflineAuditQueueEntity event1 = new OfflineAuditQueueEntity("CENTRE-DL-01", "TERM-01", "PRINT_COPY_1", "{\"copy\":1}");
        OfflineAuditQueueEntity event2 = new OfflineAuditQueueEntity("CENTRE-DL-01", "TERM-01", "PRINT_COPY_2", "{\"copy\":2}");

        when(queueRepository.findByCentreCodeAndSyncedFalseOrderByCreatedAtAsc("CENTRE-DL-01"))
                .thenReturn(List.of(event1, event2));

        SyncReconciliationResponse syncRes = syncService.synchronizeWithLedger("CENTRE-DL-01");

        assertThat(syncRes.reconciledCount()).isEqualTo(2);
        assertThat(syncRes.status()).isEqualTo("RECONCILED_WITH_LEDGER");
        assertThat(event1.isSynced()).isTrue();
        assertThat(event2.isSynced()).isTrue();

        verify(ledgerService, times(2)).recordAccess(eq("OFFLINE_EVENT"), eq("CENTRE-DL-01"), eq("TERM-01"), anyString(), eq("SYNCED_RECONCILED"));
    }
}

