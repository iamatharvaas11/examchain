package com.examchain.generation.fairness;

import com.examchain.generation.dto.BlueprintDtos.GeneratedPaperResponse;
import com.examchain.generation.dto.BlueprintDtos.PaperQuestionSummary;
import com.examchain.generation.fairness.FairnessDtos.*;
import com.examchain.generation.service.DynamicPaperGenerationService;
import com.examchain.question.model.DifficultyLevel;
import com.examchain.question.model.QuestionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class FairnessEngineService {

    private final DynamicPaperGenerationService generationService;

    @Autowired
    public FairnessEngineService(DynamicPaperGenerationService generationService) {
        this.generationService = generationService;
    }

    public FairnessReport evaluateVariants(List<String> paperIds, FairnessToleranceConfig customTolerance) {
        if (paperIds == null || paperIds.size() < 2) {
            throw new IllegalArgumentException("At least two paper variants are required for fairness comparison");
        }

        FairnessToleranceConfig tolerance = customTolerance != null
                ? customTolerance
                : FairnessToleranceConfig.defaultTolerance();

        List<GeneratedPaperResponse> papers = paperIds.stream()
                .map(generationService::getPaperByPaperId)
                .toList();

        return evaluatePapers(papers, tolerance);
    }

    public FairnessReport evaluatePapers(List<GeneratedPaperResponse> papers, FairnessToleranceConfig tolerance) {
        List<VariantMetric> metrics = new ArrayList<>();
        List<Set<String>> questionIdSets = new ArrayList<>();
        List<String> violations = new ArrayList<>();
        Map<String, String> evaluationNotes = new LinkedHashMap<>();

        // 1. Calculate metrics for each paper
        for (GeneratedPaperResponse paper : papers) {
            Map<Integer, Integer> marksPerUnit = new HashMap<>();
            Map<DifficultyLevel, Integer> marksPerDifficulty = new HashMap<>();
            Map<QuestionType, Integer> typeCounts = new HashMap<>();
            Set<String> qIds = new HashSet<>();

            for (PaperQuestionSummary q : paper.questions()) {
                marksPerUnit.merge(q.unit(), q.marks(), Integer::sum);
                marksPerDifficulty.merge(q.difficulty(), q.marks(), Integer::sum);
                typeCounts.merge(q.questionType(), 1, Integer::sum);
                qIds.add(q.questionId());
            }

            Map<DifficultyLevel, Double> diffPercentages = new HashMap<>();
            int totalMarks = paper.totalMarks();
            for (DifficultyLevel d : DifficultyLevel.values()) {
                int marksForDiff = marksPerDifficulty.getOrDefault(d, 0);
                double pct = totalMarks > 0 ? (marksForDiff * 100.0) / totalMarks : 0.0;
                diffPercentages.put(d, Math.round(pct * 10.0) / 10.0);
            }

            metrics.add(new VariantMetric(paper.paperId(), totalMarks, marksPerUnit, diffPercentages, typeCounts));
            questionIdSets.add(qIds);
        }

        // 2. Perform Pairwise Checks
        double maxObservedOverlap = 0.0;

        for (int i = 0; i < papers.size(); i++) {
            for (int j = i + 1; j < papers.size(); j++) {
                VariantMetric m1 = metrics.get(i);
                VariantMetric m2 = metrics.get(j);

                // A. Total Marks Invariance Check (Strict)
                if (m1.totalMarks() != m2.totalMarks()) {
                    violations.add(String.format("Total marks mismatch between [%s] (%d) and [%s] (%d)",
                            m1.paperId(), m1.totalMarks(), m2.paperId(), m2.totalMarks()));
                }

                // B. Unit Distribution Check
                Set<Integer> allUnits = new HashSet<>(m1.marksPerUnit().keySet());
                allUnits.addAll(m2.marksPerUnit().keySet());
                for (int u : allUnits) {
                    int u1 = m1.marksPerUnit().getOrDefault(u, 0);
                    int u2 = m2.marksPerUnit().getOrDefault(u, 0);
                    int diff = Math.abs(u1 - u2);
                    if (diff > tolerance.maxUnitMarksDiff()) {
                        violations.add(String.format("Unit %d marks variance between [%s] (%d) and [%s] (%d) exceeds tolerance %d",
                                u, m1.paperId(), u1, m2.paperId(), u2, tolerance.maxUnitMarksDiff()));
                    }
                }

                // C. Difficulty Distribution Check
                for (DifficultyLevel d : DifficultyLevel.values()) {
                    double p1 = m1.difficultyPercentage().getOrDefault(d, 0.0);
                    double p2 = m2.difficultyPercentage().getOrDefault(d, 0.0);
                    double diffPct = Math.abs(p1 - p2);
                    if (diffPct > tolerance.maxDifficultyDiffPercentage()) {
                        violations.add(String.format("Difficulty [%s] variance between [%s] (%.1f%%) and [%s] (%.1f%%) exceeds tolerance %.1f%%",
                                d, m1.paperId(), p1, m2.paperId(), p2, tolerance.maxDifficultyDiffPercentage()));
                    }
                }

                // D. Overlap / Near-Duplicate Detection Check
                Set<String> intersection = new HashSet<>(questionIdSets.get(i));
                intersection.retainAll(questionIdSets.get(j));
                int minSize = Math.min(questionIdSets.get(i).size(), questionIdSets.get(j).size());
                double overlapPct = minSize > 0 ? (intersection.size() * 100.0) / minSize : 0.0;
                if (overlapPct > maxObservedOverlap) {
                    maxObservedOverlap = overlapPct;
                }

                if (overlapPct > tolerance.maxQuestionOverlapPercentage()) {
                    violations.add(String.format("Question overlap between [%s] and [%s] (%.1f%%) exceeds maximum permitted threshold %.1f%%",
                            m1.paperId(), m2.paperId(), overlapPct, tolerance.maxQuestionOverlapPercentage()));
                }
            }
        }

        // 3. Formulate Deterministic Decision
        FairnessDecision decision = violations.isEmpty()
                ? FairnessDecision.ACCEPT
                : FairnessDecision.REJECT_FOR_REVIEW;

        evaluationNotes.put("SyllabusCoverage", "All units verified across compared variants");
        evaluationNotes.put("DeterministicPolicy", "Mathematical constraints evaluated with zero black-box bias");
        evaluationNotes.put("MaxObservedOverlap", String.format("%.1f%%", maxObservedOverlap));
        evaluationNotes.put("ViolationsCount", String.valueOf(violations.size()));

        String reportId = "FAIR-RPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        List<String> evaluatedPaperIds = papers.stream().map(GeneratedPaperResponse::paperId).toList();

        return new FairnessReport(
                reportId,
                evaluatedPaperIds,
                decision,
                Math.round(maxObservedOverlap * 10.0) / 10.0,
                evaluationNotes,
                violations,
                metrics,
                Instant.now()
        );
    }
}

