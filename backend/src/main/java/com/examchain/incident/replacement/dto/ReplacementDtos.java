package com.examchain.incident.replacement.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public class ReplacementDtos {

    public record ExecuteReplacementRequest(
            @NotBlank String quarantinedPaperId,
            @NotBlank String reservePaperId,
            @NotBlank String examCode,
            @NotBlank String reason,
            @NotBlank String authorizedBy
    ) {}

    public record ReplacementResponse(
            String replacementId,
            String quarantinedPaperId,
            String reservePaperId,
            String examCode,
            String reason,
            String authorizedBy,
            String status,
            String newReserveStatus,
            Instant timestamp
    ) {}
}

