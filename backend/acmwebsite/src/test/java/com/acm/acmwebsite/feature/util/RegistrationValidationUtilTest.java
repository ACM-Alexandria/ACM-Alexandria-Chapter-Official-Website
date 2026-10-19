package com.acm.acmwebsite.feature.util;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.entity.FormQuestion;
import com.acm.acmwebsite.feature.exception.MissingRequiredAnswerException;
import com.acm.acmwebsite.feature.exception.ProfileIncompleteException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrationValidationUtilTest {

    private record TestQuestion(Long id, String questionText, Boolean isRequired) implements FormQuestion {
        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getQuestionText() {
            return questionText;
        }

        @Override
        public Boolean getIsRequired() {
            return isRequired;
        }
    }

    @Nested
    @DisplayName("validateUserProfile")
    class ValidateUserProfileTests {

        @Test
        @DisplayName("Should throw ProfileIncompleteException when name is null")
        void shouldThrowWhenNameIsNull() {
            User user = new User();
            user.setName(null);
            user.setPhoneNumber("+201234567890");
            user.setIsAlexEngStudent(true);

            assertThatThrownBy(() -> RegistrationValidationUtil.validateUserProfile(user))
                    .isInstanceOf(ProfileIncompleteException.class)
                    .hasMessageContaining("Please complete your profile details");
        }

        @Test
        @DisplayName("Should throw ProfileIncompleteException when name is blank")
        void shouldThrowWhenNameIsBlank() {
            User user = new User();
            user.setName("   ");
            user.setPhoneNumber("+201234567890");
            user.setIsAlexEngStudent(true);

            assertThatThrownBy(() -> RegistrationValidationUtil.validateUserProfile(user))
                    .isInstanceOf(ProfileIncompleteException.class)
                    .hasMessageContaining("Please complete your profile details");
        }

        @Test
        @DisplayName("Should throw ProfileIncompleteException when phone number is null")
        void shouldThrowWhenPhoneNumberIsNull() {
            User user = new User();
            user.setName("John Doe");
            user.setPhoneNumber(null);
            user.setIsAlexEngStudent(true);

            assertThatThrownBy(() -> RegistrationValidationUtil.validateUserProfile(user))
                    .isInstanceOf(ProfileIncompleteException.class);
        }

        @Test
        @DisplayName("Should throw ProfileIncompleteException when phone number is blank")
        void shouldThrowWhenPhoneNumberIsBlank() {
            User user = new User();
            user.setName("John Doe");
            user.setPhoneNumber("  ");
            user.setIsAlexEngStudent(true);

            assertThatThrownBy(() -> RegistrationValidationUtil.validateUserProfile(user))
                    .isInstanceOf(ProfileIncompleteException.class);
        }

        @Test
        @DisplayName("Should throw ProfileIncompleteException when isAlexEngStudent is null")
        void shouldThrowWhenIsAlexEngStudentIsNull() {
            User user = new User();
            user.setName("John Doe");
            user.setPhoneNumber("+201234567890");
            user.setIsAlexEngStudent(null);

            assertThatThrownBy(() -> RegistrationValidationUtil.validateUserProfile(user))
                    .isInstanceOf(ProfileIncompleteException.class);
        }

        @Test
        @DisplayName("Should not throw when user profile is completely filled")
        void shouldPassWhenProfileIsComplete() {
            User user = new User();
            user.setName("John Doe");
            user.setPhoneNumber("+201234567890");
            user.setIsAlexEngStudent(false);

            assertThatCode(() -> RegistrationValidationUtil.validateUserProfile(user))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("validateAnswers")
    class ValidateAnswersTests {

        @Test
        @DisplayName("Should throw MissingRequiredAnswerException when required question answer is missing")
        void shouldThrowWhenRequiredAnswerIsMissing() {
            List<FormQuestion> questions = List.of(
                    new TestQuestion(1L, "Why join?", true)
            );
            Map<Long, String> answers = Collections.emptyMap();

            assertThatThrownBy(() -> RegistrationValidationUtil.validateAnswers(questions, answers))
                    .isInstanceOf(MissingRequiredAnswerException.class)
                    .hasMessageContaining("Answer is required for: Why join?");
        }

        @Test
        @DisplayName("Should throw MissingRequiredAnswerException when required question answer is blank")
        void shouldThrowWhenRequiredAnswerIsBlank() {
            List<FormQuestion> questions = List.of(
                    new TestQuestion(1L, "Why join?", true)
            );
            Map<Long, String> answers = Map.of(1L, "   ");

            assertThatThrownBy(() -> RegistrationValidationUtil.validateAnswers(questions, answers))
                    .isInstanceOf(MissingRequiredAnswerException.class)
                    .hasMessageContaining("Answer is required for: Why join?");
        }

        @Test
        @DisplayName("Should pass when required question has valid answer")
        void shouldPassWhenRequiredQuestionHasValidAnswer() {
            List<FormQuestion> questions = List.of(
                    new TestQuestion(1L, "Why join?", true)
            );
            Map<Long, String> answers = Map.of(1L, "To learn and network.");

            assertThatCode(() -> RegistrationValidationUtil.validateAnswers(questions, answers))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass when optional question has no answer")
        void shouldPassWhenOptionalQuestionHasNoAnswer() {
            List<FormQuestion> questions = List.of(
                    new TestQuestion(1L, "Any dietary restrictions?", false)
            );
            Map<Long, String> answers = Collections.emptyMap();

            assertThatCode(() -> RegistrationValidationUtil.validateAnswers(questions, answers))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass when question isRequired is null")
        void shouldPassWhenQuestionIsRequiredIsNull() {
            List<FormQuestion> questions = List.of(
                    new TestQuestion(1L, "Comments?", null)
            );
            Map<Long, String> answers = Collections.emptyMap();

            assertThatCode(() -> RegistrationValidationUtil.validateAnswers(questions, answers))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("Private constructor reflection coverage")
    void testPrivateConstructor() throws Exception {
        Constructor<RegistrationValidationUtil> constructor = RegistrationValidationUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        RegistrationValidationUtil instance = constructor.newInstance();
        assertThat(instance).isNotNull();
    }
}
