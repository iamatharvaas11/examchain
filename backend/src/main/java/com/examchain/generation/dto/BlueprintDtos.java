package com.examchain.generation.dto;

import com.examchain.generation.model.PolicyMode;
import com.examchain.question.model.DifficultyLevel;
import com.examchain.question.model.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class BlueprintDtos {

    public record BlueprintRuleDto(
            @Positive int unit,
            @Positive int marksPerQuestion,
            @NotNull DifficultyLevel difficulty,
            @NotNull QuestionType questionType,
            @Positive int count
    ) {}

    public record BlueprintRequest(
            @NotBlank String blueprintCode,
            @NotBlank String name,
            @Positive int totalMarks,
            @Positive int durationMinutes,
            @NotNull PolicyMode policyMode,
            @NotEmpty List<BlueprintRuleDto> rules
    ) {}

    public record BlueprintResponse(
            UUID id,
            UUID subjectId,
            String blueprintCode,
            String name,
            int totalMarks,
            int durationMinutes,
            PolicyMode policyMode,
            List<BlueprintRuleDto> rules,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record GeneratePaperRequest(
            @NotBlank String setCode
    ) {}

    public record PaperQuestionSummary(
            int sequenceNumber,
            String questionId,
            int unit,
            int marks,
            DifficultyLevel difficulty,
            QuestionType questionType,
            String questionText,
            String questionHash
    ) {}

    public record GeneratedPaperResponse(
            UUID id,
            String paperId,
            UUID blueprintId,
            UUID subjectId,
            String setCode,
            int totalMarks,
            int questionCount,
            String paperHash,
            String status,
            List<PaperQuestionSummary> questions,
            Instant createdAt
    ) {}
}

