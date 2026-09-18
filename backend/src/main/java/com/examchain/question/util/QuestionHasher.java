package com.examchain.question.util;

import com.examchain.question.dto.QuestionDtos.QuestionContent;
import com.examchain.question.model.CognitiveLevel;
import com.examchain.question.model.DifficultyLevel;
import com.examchain.question.model.QuestionType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class QuestionHasher {

    public static String computeHash(
            String questionId,
            int unit,
            int marks,
            DifficultyLevel difficulty,
            QuestionType questionType,
            CognitiveLevel cognitiveLevel,
            QuestionContent content
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(questionId != null ? questionId.trim() : "").append("::");
        sb.append(unit).append("::");
        sb.append(marks).append("::");
        sb.append(difficulty != null ? difficulty.name() : "").append("::");
        sb.append(questionType != null ? questionType.name() : "").append("::");
        sb.append(cognitiveLevel != null ? cognitiveLevel.name() : "").append("::");

        if (content != null) {
            sb.append(content.questionText() != null ? content.questionText().trim() : "").append("::");
            if (content.options() != null) {
                sb.append(String.join("|", content.options()));
            }
            sb.append("::");
            sb.append(content.correctOptionIndex() != null ? content.correctOptionIndex() : "").append("::");
            sb.append(content.rubricOrExplanation() != null ? content.rubricOrExplanation().trim() : "");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}

