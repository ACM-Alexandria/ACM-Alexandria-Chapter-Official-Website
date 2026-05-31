package com.acm.acmwebsite.feature.util;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.entity.FormQuestion;
import com.acm.acmwebsite.feature.exception.MissingRequiredAnswerException;
import com.acm.acmwebsite.feature.exception.ProfileIncompleteException;

import java.util.List;
import java.util.Map;

public class RegistrationValidationUtil {

    private RegistrationValidationUtil() {
    }

    // Helper to ensure basic user details are complete
    public static void validateUserProfile(User user) {
        if (user.getName() == null || user.getName().isBlank() ||
            user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()||
            user.getIsAlexEngStudent() == null) {
            throw new ProfileIncompleteException("Please complete your profile details (Name & Phone Number & Student Status) before registering.");
        }
    }

    // Event & Club question forms validator
    public static void validateAnswers(List<? extends FormQuestion> questions, Map<Long, String> answers) {
        for (FormQuestion question : questions) {
            if (Boolean.TRUE.equals(question.getIsRequired())) {
                String answer = answers.get(question.getId());
                if (answer == null || answer.isBlank()) {
                    throw new MissingRequiredAnswerException("Answer is required for: " + question.getQuestionText());
                }
            }
        }
    }
}
