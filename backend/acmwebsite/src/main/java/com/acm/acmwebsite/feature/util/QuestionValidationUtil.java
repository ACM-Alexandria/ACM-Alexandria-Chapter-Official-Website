package com.acm.acmwebsite.feature.util;

import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.enums.QuestionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class QuestionValidationUtil {

    private QuestionValidationUtil() {
    }

    public static String validateQuestionText(FormQuestionRequestDto request) {
        if (request == null || request.getQuestionText() == null || request.getQuestionText().isBlank()) {
            throw new IllegalArgumentException("Question text is required");
        }
        return request.getQuestionText().trim();
    }

    public static QuestionType parseQuestionType(FormQuestionRequestDto request) {
        if (request == null || request.getQuestionType() == null || request.getQuestionType().isBlank()) {
            throw new IllegalArgumentException("Question type is required");
        }
        String typeStr = request.getQuestionType().trim().toUpperCase(Locale.ROOT);
        try {
            return QuestionType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid question type: " + request.getQuestionType(), e);
        }
    }

    public static List<String> normalizeOptions(List<String> options) {
        if (options == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(options.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(option -> !option.isBlank())
                .toList());
    }
}
