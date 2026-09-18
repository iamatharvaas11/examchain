package com.examchain.release.dto;

import com.examchain.release.model.ReleaseStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ReleaseDtos {

    public record ScheduleReleaseRequest(
            @NotBlank String paperId,
            UUID examId,
            @Min(1) int requiredThreshold,
            @NotNull Instant scheduledReleaseTime,
            @NotNull Instant releaseWindowEndTime
    ) {}

    public record SubmitApprovalRequest(
            @NotBlank String authorityId,
            @NotBlank String authorityRole,
            String authorityName,
            String comments
    ) {}

    public record AuthorizeReleaseRequest(
            @NotBlank String authorizedBy
    ) {}

    public record ApprovalSummaryDto(
            String authorityId,
            String authorityRole,
            String authorityName,
            String signatureHash,
            Instant timestamp
    ) {}

    public record ReleaseStatusResponse(
            String paperId,
            int requiredThreshold,
            int currentApprovalCount,
            Instant scheduledReleaseTime,
            Instant releaseWindowEndTime,
            ReleaseStatus status,
            boolean canReleaseNow,
            Instant releasedAt,
            String releasedBy,
            List<ApprovalSummaryDto> approvals
    ) {}
}

