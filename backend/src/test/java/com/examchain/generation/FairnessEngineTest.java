package com.examchain.generation;

import com.examchain.generation.dto.BlueprintDtos.GeneratedPaperResponse;
import com.examchain.generation.dto.BlueprintDtos.PaperQuestionSummary;
import com.examchain.generation.fairness.FairnessDecision;
import com.examchain.generation.fairness.FairnessDtos.FairnessReport;
import com.examchain.generation.fairness.FairnessDtos.FairnessToleranceConfig;
import com.examchain.generation.fairness.FairnessEngineService;
import com.examchain.generation.service.DynamicPaperGenerationService;
import com.examchain.question.model.DifficultyLevel;
import com.examchain.question.model.QuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FairnessEngineTest {

    private DynamicPaperGenerationService generationService;
    private FairnessEngineService fairnessEngineService;

    @BeforeEach
    void setUp() {
        generationService = Mockito.mock(DynamicPaperGenerationService.class);
        fairnessEngineService = new FairnessEngineService(generationService);
    }

    private PaperQuestionSummary createQuestion(int seq, String qId, int unit, int marks, DifficultyLevel diff) {
        return new PaperQuestionSummary(
                seq,
                qId,
                unit,
                marks,
                diff,
                QuestionType.MCQ,
                "Sample question " + qId,
                "hash-" + qId
        );
    }

    private GeneratedPaperResponse createPaper(String paperId, String setCode, int totalMarks, List<PaperQuestionSummary> questions) {
        return new GeneratedPaperResponse(
                UUID.randomUUID(),
                paperId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                setCode,
                totalMarks,
                questions.size(),
                "hash-" + paperId,
                "GENERATED",
                questions,
                Instant.now()
        );
    }

    @Test
    @DisplayName("Balanced variants should yield ACCEPT decision")
    void testBalancedVariants_Accepted() {
        // Set A: Unit 1 (Easy 5m), Unit 2 (Medium 5m), Unit 3 (Hard 10m) -> 20 marks
        List<PaperQuestionSummary> questionsA = List.of(
                createQuestion(1, "Q1", 1, 5, DifficultyLevel.EASY),
                createQuestion(2, "Q2", 2, 5, DifficultyLevel.MEDIUM),
                createQuestion(3, "Q3", 3, 10, DifficultyLevel.HARD)
        );
        // Set B: Unit 1 (Easy 5m), Unit 2 (Medium 5m), Unit 3 (Hard 10m) -> 20 marks, distinct questions
        List<PaperQuestionSummary> questionsB = List.of(
                createQuestion(1, "Q4", 1, 5, DifficultyLevel.EASY),
                createQuestion(2, "Q5", 2, 5, DifficultyLevel.MEDIUM),
                createQuestion(3, "Q6", 3, 10, DifficultyLevel.HARD)
        );

        GeneratedPaperResponse paperA = createPaper("PAPER-SET-A", "SET_A", 20, questionsA);
        GeneratedPaperResponse paperB = createPaper("PAPER-SET-B", "SET_B", 20, questionsB);

        FairnessReport report = fairnessEngineService.evaluatePapers(List.of(paperA, paperB), FairnessToleranceConfig.defaultTolerance());

        assertThat(report.decision()).isEqualTo(FairnessDecision.ACCEPT);
        assertThat(report.violations()).isEmpty();
        assertThat(report.measuredOverlapPercentage()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Total marks mismatch should yield REJECT_FOR_REVIEW")
    void testTotalMarksMismatch_Rejected() {
        List<PaperQuestionSummary> questionsA = List.of(
                createQuestion(1, "Q1", 1, 10, DifficultyLevel.EASY)
        );
        List<PaperQuestionSummary> questionsB = List.of(
                createQuestion(1, "Q2", 1, 12, DifficultyLevel.EASY)
        );

        GeneratedPaperResponse paperA = createPaper("PAPER-SET-A", "SET_A", 10, questionsA);
        GeneratedPaperResponse paperB = createPaper("PAPER-SET-B", "SET_B", 12, questionsB);

        FairnessReport report = fairnessEngineService.evaluatePapers(List.of(paperA, paperB), FairnessToleranceConfig.defaultTolerance());

        assertThat(report.decision()).isEqualTo(FairnessDecision.REJECT_FOR_REVIEW);
        assertThat(report.violations()).anyMatch(v -> v.contains("Total marks mismatch"));
    }

    @Test
    @DisplayName("Difficulty skew exceeding tolerance should yield REJECT_FOR_REVIEW")
    void testDifficultySkew_Rejected() {
        // Paper A: 100% EASY (20m)
        List<PaperQuestionSummary> questionsA = List.of(
                createQuestion(1, "Q1", 1, 20, DifficultyLevel.EASY)
        );
        // Paper B: 100% HARD (20m)
        List<PaperQuestionSummary> questionsB = List.of(
                createQuestion(1, "Q2", 1, 20, DifficultyLevel.HARD)
        );

        GeneratedPaperResponse paperA = createPaper("PAPER-SET-A", "SET_A", 20, questionsA);
        GeneratedPaperResponse paperB = createPaper("PAPER-SET-B", "SET_B", 20, questionsB);

        FairnessReport report = fairnessEngineService.evaluatePapers(List.of(paperA, paperB), FairnessToleranceConfig.defaultTolerance());

        assertThat(report.decision()).isEqualTo(FairnessDecision.REJECT_FOR_REVIEW);
        assertThat(report.violations()).anyMatch(v -> v.contains("Difficulty [EASY] variance"));
    }

    @Test
    @DisplayName("Question overlap exceeding 30% tolerance should yield REJECT_FOR_REVIEW")
    void testHighQuestionOverlap_Rejected() {
        // Paper A: Q1, Q2, Q3
        List<PaperQuestionSummary> questionsA = List.of(
                createQuestion(1, "Q1", 1, 10, DifficultyLevel.EASY),
                createQuestion(2, "Q2", 1, 10, DifficultyLevel.MEDIUM),
                createQuestion(3, "Q3", 1, 10, DifficultyLevel.HARD)
        );
        // Paper B: Q1, Q2, Q4 (2 of 3 overlap = 66.7% > 30%)
        List<PaperQuestionSummary> questionsB = List.of(
                createQuestion(1, "Q1", 1, 10, DifficultyLevel.EASY),
                createQuestion(2, "Q2", 1, 10, DifficultyLevel.MEDIUM),
                createQuestion(3, "Q4", 1, 10, DifficultyLevel.HARD)
        );

        GeneratedPaperResponse paperA = createPaper("PAPER-SET-A", "SET_A", 30, questionsA);
        GeneratedPaperResponse paperB = createPaper("PAPER-SET-B", "SET_B", 30, questionsB);

        FairnessReport report = fairnessEngineService.evaluatePapers(List.of(paperA, paperB), FairnessToleranceConfig.defaultTolerance());

        assertThat(report.decision()).isEqualTo(FairnessDecision.REJECT_FOR_REVIEW);
        assertThat(report.violations()).anyMatch(v -> v.contains("Question overlap between"));
        assertThat(report.measuredOverlapPercentage()).isGreaterThan(30.0);
    }

    @Test
    @DisplayName("Single variant evaluation should throw IllegalArgumentException")
    void testSingleVariant_ThrowsException() {
        assertThatThrownBy(() -> fairnessEngineService.evaluateVariants(List.of("PAPER-1"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least two paper variants are required");
    }
}

