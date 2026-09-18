package com.examchain.incident.dto;

import com.examchain.incident.model.FreezeLevel;
import com.examchain.incident.model.IncidentEventType;
import com.examchain.incident.model.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public class IncidentDtos {

    public record FreezeStateResponse(
            FreezeLevel freezeLevel,
            String reason,
            String triggeredBy,
            Instant updatedAt
    ) {}

    public record UpdateFreezeRequest(
            @NotNull FreezeLevel freezeLevel,
            @NotBlank String reason,
            @NotBlank String triggeredBy
    ) {}

    public record QuarantineVariantRequest(
            @NotBlank String paperId,
            @NotBlank String incidentId,
            @NotBlank String reason,
            @NotBlank String quarantinedBy
    ) {}

    public record SecurityIncidentDto(
            String incidentId,
            IncidentEventType eventType,
            IncidentSeverity severity,
            String paperId,
            String centreCode,
            String operatorId,
            String details,
            boolean isQuarantined,
            Instant createdAt
    ) {}

    public record AnomalyReport(
            String incidentId,
            IncidentEventType eventType,
            IncidentSeverity severity,
            String actionTaken,
            FreezeLevel currentFreezeLevel,
            Instant timestamp
    ) {}
}

