package com.examchain.generation.fairness;

import com.examchain.question.model.DifficultyLevel;
import com.examchain.question.model.QuestionType;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class FairnessDtos {

    public record FairnessToleranceConfig(
            int maxUnitMarksDiff,
            double maxDifficultyDiffPercentage,
            double maxQuestionOverlapPercentage
    ) {
        public static FairnessToleranceConfig defaultTolerance() {
            return new FairnessToleranceConfig(2, 5.0, 30.0);
        }
    }

    public record VariantMetric(
            String paperId,
            int totalMarks,
            Map<Integer, Integer> marksPerUnit,
            Map<DifficultyLevel, Double> difficultyPercentage,
            Map<QuestionType, Integer> questionTypeCounts
    ) {}

    public record FairnessEvaluationRequest(
            List<String> paperIds,
            FairnessToleranceConfig customTolerance
    ) {}

    public record FairnessReport(
            String reportId,
            List<String> paperIds,
            FairnessDecision decision,
            double measuredOverlapPercentage,
            Map<String, String> evaluationNotes,
            List<String> violations,
            List<VariantMetric> metrics,
            Instant timestamp
    ) {}
}

