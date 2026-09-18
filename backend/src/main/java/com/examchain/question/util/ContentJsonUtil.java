package com.examchain.question.util;

import com.examchain.question.dto.QuestionDtos.QuestionContent;

import java.util.ArrayList;
import java.util.List;

public class ContentJsonUtil {

    public static String toJson(QuestionContent content) {
        if (content == null) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"questionText\":").append(quote(content.questionText())).append(",");
        sb.append("\"options\":[");
        if (content.options() != null && !content.options().isEmpty()) {
            for (int i = 0; i < content.options().size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(quote(content.options().get(i)));
            }
        }
        sb.append("],");
        sb.append("\"correctOptionIndex\":").append(quote(content.correctOptionIndex())).append(",");
        sb.append("\"rubricOrExplanation\":").append(quote(content.rubricOrExplanation()));
        sb.append("}");
        return sb.toString();
    }

    public static QuestionContent fromJson(String json) {
        if (json == null || json.isBlank() || json.equals("{}")) {
            return new QuestionContent("", List.of(), "", "");
        }
        String text = extractField(json, "questionText");
        String correct = extractField(json, "correctOptionIndex");
        String rubric = extractField(json, "rubricOrExplanation");
        List<String> options = extractArray(json, "options");
        return new QuestionContent(text, options, correct, rubric);
    }

    private static String quote(String str) {
        if (str == null) return "null";
        return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    private static String extractField(String json, String key) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return "";
        idx += pattern.length();
        while (idx < json.length() && Character.isWhitespace(json.charAt(idx))) idx++;
        if (idx >= json.length() || json.charAt(idx) != '"') return "";
        idx++; // skip opening quote
        StringBuilder val = new StringBuilder();
        boolean escape = false;
        while (idx < json.length()) {
            char c = json.charAt(idx);
            if (escape) {
                if (c == 'n') val.append('\n');
                else if (c == 'r') val.append('\r');
                else val.append(c);
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                val.append(c);
            }
            idx++;
        }
        return val.toString();
    }

    private static List<String> extractArray(String json, String key) {
        List<String> result = new ArrayList<>();
        String pattern = "\"" + key + "\":[";
        int idx = json.indexOf(pattern);
        if (idx == -1) return result;
        idx += pattern.length();
        while (idx < json.length() && json.charAt(idx) != ']') {
            while (idx < json.length() && (Character.isWhitespace(json.charAt(idx)) || json.charAt(idx) == ',')) idx++;
            if (idx < json.length() && json.charAt(idx) == '"') {
                idx++;
                StringBuilder item = new StringBuilder();
                boolean escape = false;
                while (idx < json.length()) {
                    char c = json.charAt(idx);
                    if (escape) {
                        item.append(c);
                        escape = false;
                    } else if (c == '\\') {
                        escape = true;
                    } else if (c == '"') {
                        idx++;
                        break;
                    } else {
                        item.append(c);
                    }
                    idx++;
                }
                result.add(item.toString());
            } else {
                idx++;
            }
        }
        return result;
    }
}

