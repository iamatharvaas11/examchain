package com.examchain.centre.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public class CentreDtos {

    public record RegisterTerminalRequest(
            @NotBlank String centreCode,
            @NotBlank String terminalId,
            @NotBlank String deviceFingerprint
    ) {}

    public record TerminalResponse(
            String centreCode,
            String terminalId,
            String status,
            Instant lastSeenAt
    ) {}

    public record SecurePrintRequest(
            @NotBlank String centreCode,
            @NotBlank String terminalId,
            @NotBlank String operatorId,
            @NotBlank String deviceFingerprint,
            @Min(1) int copyCount
    ) {}

    public record WatermarkedQuestionDto(
            int sequenceNumber,
            String questionText,
            int marks
    ) {}

    public record SecurePrintResponse(
            String receiptId,
            String paperId,
            String centreCode,
            String terminalId,
            String operatorId,
            int riskScore,
            String watermark,
            int copyCount,
            String status,
            Instant printedAt,
            List<WatermarkedQuestionDto> questions
    ) {}

    public record PrintReceiptDto(
            String receiptId,
            String paperId,
            String centreCode,
            String terminalId,
            String operatorId,
            int riskScore,
            String watermarkText,
            int copyCount,
            String status,
            Instant timestamp
    ) {}
}

