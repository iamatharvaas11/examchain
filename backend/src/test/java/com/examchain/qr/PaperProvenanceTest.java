package com.examchain.qr;

import com.examchain.core.exception.ResourceNotFoundException;
import com.examchain.generation.dto.BlueprintDtos.GeneratedPaperResponse;
import com.examchain.generation.service.DynamicPaperGenerationService;
import com.examchain.qr.dto.ProvenanceDtos.CreateTraceRequest;
import com.examchain.qr.dto.ProvenanceDtos.CreateTraceResponse;
import com.examchain.qr.dto.ProvenanceDtos.VerificationResponse;
import com.examchain.qr.entity.PaperProvenanceEntity;
import com.examchain.qr.model.ProvenanceStatus;
import com.examchain.qr.repository.PaperProvenanceRepository;
import com.examchain.qr.service.PaperProvenanceService;
import com.examchain.qr.service.RateLimiterService;
import com.examchain.qr.service.TraceIdGenerator;
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
import static org.mockito.Mockito.when;

class PaperProvenanceTest {

    private PaperProvenanceRepository repository;
    private DynamicPaperGenerationService generationService;
    private TraceIdGenerator traceIdGenerator;
    private PaperProvenanceService provenanceService;
    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(PaperProvenanceRepository.class);
        generationService = Mockito.mock(DynamicPaperGenerationService.class);
        traceIdGenerator = new TraceIdGenerator();
        provenanceService = new PaperProvenanceService(repository, traceIdGenerator, generationService);
        rateLimiterService = new RateLimiterService();
    }

    @Test
    @DisplayName("Pre-exam verification scan must return locked metadata with zero plaintext/hash leakage")
    void testPreExamScan_ReturnsLockedSanitizedMetadata() {
        String traceId = "TRC-ABC123XYZ";
        Instant futureEndTime = Instant.now().plus(2, ChronoUnit.HOURS);

        PaperProvenanceEntity entity = new PaperProvenanceEntity(
                traceId,
                "PAP-CS101-SET_A",
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                "EXAM-CS101",
                "Computer Networks Final",
                "CENTRE-NORTH-01",
                Instant.now().minus(1, ChronoUnit.HOURS),
                futureEndTime
        );

        when(repository.findByTraceId(traceId)).thenReturn(Optional.of(entity));

        VerificationResponse response = provenanceService.verifyPaper(traceId);

        assertThat(response.verified()).isTrue();
        assertThat(response.status()).isEqualTo("PRE_EXAM_LOCKED");
        assertThat(response.paperHash()).isNull();
        assertThat(response.provenanceTrail()).isNull();
        assertThat(response.message()).contains("cryptographically sealed");
    }

    @Test
    @DisplayName("Post-exam verification scan returns full provenance trail and SHA-256 hash")
    void testPostExamScan_ReturnsFullReceipt() {
        String traceId = "TRC-COMPLETED-EXAM";
        Instant pastEndTime = Instant.now().minus(1, ChronoUnit.HOURS);
        String expectedHash = "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e";

        PaperProvenanceEntity entity = new PaperProvenanceEntity(
                traceId,
                "PAP-CS101-SET_A",
                expectedHash,
                "EXAM-CS101",
                "Computer Networks Final",
                "CENTRE-NORTH-01",
                Instant.now().minus(3, ChronoUnit.HOURS),
                pastEndTime
        );

        when(repository.findByTraceId(traceId)).thenReturn(Optional.of(entity));

        VerificationResponse response = provenanceService.verifyPaper(traceId);

        assertThat(response.verified()).isTrue();
        assertThat(response.status()).isEqualTo("VERIFIED_POST_EXAM");
        assertThat(response.paperHash()).isEqualTo(expectedHash);
        assertThat(response.provenanceTrail()).isNotNull();
        assertThat(response.provenanceTrail()).hasSize(3);
    }

    @Test
    @DisplayName("Quarantined paper returns quarantine status and blocks verification")
    void testQuarantinedPaper_ReturnsQuarantineStatus() {
        String traceId = "TRC-QUARANTINED-PAPER";
        PaperProvenanceEntity entity = new PaperProvenanceEntity(
                traceId,
                "PAP-LEAKED-SET_B",
                "some-hash",
                "EXAM-CS101",
                "Computer Networks Final",
                "CENTRE-SOUTH-02",
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(1, ChronoUnit.HOURS)
        );
        entity.setStatus(ProvenanceStatus.QUARANTINED);

        when(repository.findByTraceId(traceId)).thenReturn(Optional.of(entity));

        VerificationResponse response = provenanceService.verifyPaper(traceId);

        assertThat(response.verified()).isFalse();
        assertThat(response.status()).isEqualTo("QUARANTINED");
        assertThat(response.message()).contains("quarantined");
    }

    @Test
    @DisplayName("Unknown trace ID throws ResourceNotFoundException")
    void testUnknownTraceId_ThrowsNotFound() {
        when(repository.findByTraceId("TRC-UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> provenanceService.verifyPaper("TRC-UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No examination record found");
    }

    @Test
    @DisplayName("Register paper trace creates new trace with opaque TRC- prefix")
    void testRegisterPaperTrace_GeneratesValidTrace() {
        String paperId = "PAP-2026-ENG";
        String paperHash = "5891b5b522d5df086d0ff0b110fbd9d21bb4fc7163af34d08286a2e846f6be03";

        GeneratedPaperResponse paper = new GeneratedPaperResponse(
                UUID.randomUUID(),
                paperId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SET_A",
                100,
                20,
                paperHash,
                "GENERATED",
                Collections.emptyList(),
                Instant.now()
        );

        when(generationService.getPaperByPaperId(paperId)).thenReturn(paper);
        when(repository.findByPaperId(paperId)).thenReturn(Optional.empty());
        when(repository.save(any(PaperProvenanceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateTraceRequest request = new CreateTraceRequest(
                paperId,
                "EXAM-ENG",
                "Advanced Engineering Math",
                "CENTRE-01",
                Instant.now(),
                Instant.now().plus(3, ChronoUnit.HOURS)
        );

        CreateTraceResponse response = provenanceService.registerPaperTrace(request);

        assertThat(response.paperId()).isEqualTo(paperId);
        assertThat(response.traceId()).startsWith("TRC-");
        assertThat(response.verificationUrl()).contains(response.traceId());
    }

    @Test
    @DisplayName("Rate limiter enforces request ceiling per client IP")
    void testRateLimiter_EnforcesLimit() {
        String ip = "192.168.1.100";
        for (int i = 0; i < 60; i++) {
            assertThat(rateLimiterService.tryAcquire(ip)).isTrue();
        }
        // 61st request in the same window must be rejected
        assertThat(rateLimiterService.tryAcquire(ip)).isFalse();

        // Different IP is still allowed
        assertThat(rateLimiterService.tryAcquire("10.0.0.1")).isTrue();
    }
}

