package com.examchain.generation.util;

import com.examchain.generation.dto.BlueprintDtos.BlueprintRuleDto;
import com.examchain.question.model.DifficultyLevel;
import com.examchain.question.model.QuestionType;

import java.util.ArrayList;
import java.util.List;

public class BlueprintRulesUtil {

    public static String toJson(List<BlueprintRuleDto> rules) {
        if (rules == null || rules.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < rules.size(); i++) {
            if (i > 0) sb.append(",");
            BlueprintRuleDto r = rules.get(i);
            sb.append("{")
                    .append("\"unit\":").append(r.unit()).append(",")
                    .append("\"marksPerQuestion\":").append(r.marksPerQuestion()).append(",")
                    .append("\"difficulty\":\"").append(r.difficulty().name()).append("\",")
                    .append("\"questionType\":\"").append(r.questionType().name()).append("\",")
                    .append("\"count\":").append(r.count())
                    .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    public static List<BlueprintRuleDto> fromJson(String json) {
        List<BlueprintRuleDto> result = new ArrayList<>();
        if (json == null || json.isBlank() || json.equals("[]")) return result;

        int start = json.indexOf('{');
        while (start != -1) {
            int end = json.indexOf('}', start);
            if (end == -1) break;
            String objStr = json.substring(start + 1, end);

            int unit = parseInt(objStr, "unit");
            int marks = parseInt(objStr, "marksPerQuestion");
            int count = parseInt(objStr, "count");
            String diffStr = parseString(objStr, "difficulty");
            String typeStr = parseString(objStr, "questionType");

            DifficultyLevel diff = DifficultyLevel.valueOf(diffStr);
            QuestionType type = QuestionType.valueOf(typeStr);

            result.add(new BlueprintRuleDto(unit, marks, diff, type, count));
            start = json.indexOf('{', end);
        }
        return result;
    }

    private static int parseInt(String str, String key) {
        String pattern = "\"" + key + "\":";
        int idx = str.indexOf(pattern);
        if (idx == -1) return 0;
        idx += pattern.length();
        while (idx < str.length() && Character.isWhitespace(str.charAt(idx))) idx++;
        int end = idx;
        while (end < str.length() && (Character.isDigit(str.charAt(end)) || str.charAt(end) == '-')) end++;
        try {
            return Integer.parseInt(str.substring(idx, end));
        } catch (Exception e) {
            return 0;
        }
    }

    private static String parseString(String str, String key) {
        String pattern = "\"" + key + "\":\"";
        int idx = str.indexOf(pattern);
        if (idx == -1) return "";
        idx += pattern.length();
        int end = str.indexOf('"', idx);
        return end != -1 ? str.substring(idx, end) : "";
    }
}
