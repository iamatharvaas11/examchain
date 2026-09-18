package com.examchain.question.dto;

import com.examchain.question.model.CognitiveLevel;
import com.examchain.question.model.DifficultyLevel;
import com.examchain.question.model.PoolStatus;
import com.examchain.question.model.QuestionStatus;
import com.examchain.question.model.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class QuestionDtos {

    public record QuestionPoolRequest(
            @NotNull(message = "Subject ID is required") UUID subjectId,
            @NotBlank(message = "Pool code is required") String poolCode,
            @NotBlank(message = "Pool name is required") String name,
            String description
    ) {}

    public record QuestionPoolResponse(
            UUID id,
            UUID subjectId,
            String poolCode,
            String name,
            String description,
            PoolStatus status,
            String createdBy,
            String approvedBy,
            long questionCount,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record PoolApprovalRequest(
            @NotNull(message = "Decision status is required") PoolStatus status
    ) {}

    public record QuestionContent(
            @NotBlank(message = "Question text cannot be blank") String questionText,
            List<String> options,
            String correctOptionIndex,
            String rubricOrExplanation
    ) {}

    public record QuestionRequest(
            @NotBlank(message = "Question ID is required") String questionId,
            @Positive(message = "Unit must be positive") int unit,
            @Positive(message = "Marks must be positive") int marks,
            @NotNull(message = "Difficulty is required") DifficultyLevel difficulty,
            @NotNull(message = "Question type is required") QuestionType questionType,
            @NotNull(message = "Cognitive level is required") CognitiveLevel cognitiveLevel,
            @NotNull(message = "Content is required") @Valid QuestionContent content
    ) {}

    public record QuestionResponse(
            UUID id,
            String questionId,
            UUID poolId,
            UUID subjectId,
            int unit,
            int marks,
            DifficultyLevel difficulty,
            QuestionType questionType,
            CognitiveLevel cognitiveLevel,
            String setterId,
            QuestionStatus status,
            int version,
            QuestionContent content,
            String hash,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
