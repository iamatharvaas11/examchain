package com.examchain.offline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public class OfflineDtos {

    public record GenerateOfflineTokenRequest(
            @NotBlank String centreCode,
            @NotBlank String paperId,
            @NotNull Instant validFrom,
            @NotNull Instant validUntil
    ) {}

    public record GeneratedTokenResponse(
            String tokenId,
            String rawTokenSecret,
            String centreCode,
            String paperId,
            Instant validFrom,
            Instant validUntil
    ) {}

    public record ConsumeTokenRequest(
            @NotBlank String rawTokenSecret,
            @NotBlank String centreCode,
            @NotBlank String paperId,
            @NotBlank String terminalId
    ) {}

    public record TokenValidationResponse(
            boolean valid,
            String tokenId,
            String centreCode,
            String paperId,
            String message,
            Instant consumedAt
    ) {}

    public record QueueAuditEventRequest(
            @NotBlank String centreCode,
            @NotBlank String terminalId,
            @NotBlank String eventType,
            @NotBlank String payloadJson
    ) {}

    public record OfflineAuditItemDto(
            String centreCode,
            String terminalId,
            String eventType,
            String payloadJson,
            boolean synced,
            Instant createdAt
    ) {}

    public record SyncReconciliationResponse(
            String centreCode,
            int reconciledCount,
            String status,
            Instant syncedAt
    ) {}
}

