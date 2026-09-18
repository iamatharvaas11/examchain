package com.examchain.qr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public class ProvenanceDtos {

    public record CreateTraceRequest(
            @NotBlank String paperId,
            @NotBlank String examCode,
            @NotBlank String examTitle,
            String centreCode,
            @NotNull Instant examStartTime,
            @NotNull Instant examEndTime
    ) {}

    public record CreateTraceResponse(
            String traceId,
            String paperId,
            String verificationUrl,
            String status,
            Instant examEndTime
    ) {}

    public record ProvenanceEvent(
            String stage,
            String actorRole,
            Instant timestamp,
            String status,
            String details
    ) {}

    public record VerificationResponse(
            String traceId,
            String status,
            boolean verified,
            String message,
            String examCode,
            String examTitle,
            Instant examEndTime,
            String paperHash,
            List<ProvenanceEvent> provenanceTrail,
            Instant verifiedAt
    ) {}
}

