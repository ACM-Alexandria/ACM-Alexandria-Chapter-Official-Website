package com.acm.acmwebsite.feature.util;

import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.enums.QuestionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionValidationUtilTest {

    @Nested
    @DisplayName("validateQuestionText")
    class ValidateQuestionTextTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when request is null")
        void shouldThrowWhenRequestIsNull() {
            assertThatThrownBy(() -> QuestionValidationUtil.validateQuestionText(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Question text is required");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when questionText is null")
        void shouldThrowWhenQuestionTextIsNull() {
            FormQuestionRequestDto dto = FormQuestionRequestDto.builder()
                    .questionText(null)
                    .build();

            assertThatThrownBy(() -> QuestionValidationUtil.validateQuestionText(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Question text is required");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when questionText is blank")
        void shouldThrowWhenQuestionTextIsBlank() {
            FormQuestionRequestDto dto = FormQuestionRequestDto.builder()
                    .questionText("   ")
                    .build();

            assertThatThrownBy(() -> QuestionValidationUtil.validateQuestionText(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Question text is required");
        }

        @Test
        @DisplayName("Should return trimmed question text when valid")
        void shouldReturnTrimmedTextWhenValid() {
            FormQuestionRequestDto dto = FormQuestionRequestDto.builder()
                    .questionText("  What is your favorite language?  ")
                    .build();

            String result = QuestionValidationUtil.validateQuestionText(dto);

            assertThat(result).isEqualTo("What is your favorite language?");
        }
    }

    @Nested
    @DisplayName("parseQuestionType")
    class ParseQuestionTypeTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when request is null")
        void shouldThrowWhenRequestIsNull() {
            assertThatThrownBy(() -> QuestionValidationUtil.parseQuestionType(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Question type is required");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when questionType is null")
        void shouldThrowWhenQuestionTypeIsNull() {
            FormQuestionRequestDto dto = FormQuestionRequestDto.builder()
                    .questionType(null)
                    .build();

            assertThatThrownBy(() -> QuestionValidationUtil.parseQuestionType(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Question type is required");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when questionType is blank")
        void shouldThrowWhenQuestionTypeIsBlank() {
            FormQuestionRequestDto dto = FormQuestionRequestDto.builder()
                    .questionType(" \t ")
                    .build();

            assertThatThrownBy(() -> QuestionValidationUtil.parseQuestionType(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Question type is required");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when questionType is invalid")
        void shouldThrowWhenQuestionTypeIsInvalid() {
            FormQuestionRequestDto dto = FormQuestionRequestDto.builder()
                    .questionType("NON_EXISTENT_TYPE")
                    .build();

            assertThatThrownBy(() -> QuestionValidationUtil.parseQuestionType(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid question type: NON_EXISTENT_TYPE");
        }

        @Test
        @DisplayName("Should parse question type correctly regardless of casing and spaces")
        void shouldParseQuestionTypeCorrectly() {
            for (QuestionType type : QuestionType.values()) {
                FormQuestionRequestDto dto = FormQuestionRequestDto.builder()
                        .questionType("  " + type.name().toLowerCase() + "  ")
                        .build();

                QuestionType parsed = QuestionValidationUtil.parseQuestionType(dto);
                assertThat(parsed).isEqualTo(type);
            }
        }
    }

    @Nested
    @DisplayName("normalizeOptions")
    class NormalizeOptionsTests {

        @Test
        @DisplayName("Should return empty list when options is null")
        void shouldReturnEmptyListWhenNull() {
            List<String> result = QuestionValidationUtil.normalizeOptions(null);

            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should filter out nulls and blanks and trim valid options")
        void shouldFilterAndTrimOptions() {
            List<String> rawOptions = Arrays.asList(" Option A ", null, "  ", "Option B", "\tOption C\n");

            List<String> result = QuestionValidationUtil.normalizeOptions(rawOptions);

            assertThat(result).containsExactly("Option A", "Option B", "Option C");
        }
    }

    @Test
    @DisplayName("Private constructor reflection coverage")
    void testPrivateConstructor() throws Exception {
        Constructor<QuestionValidationUtil> constructor = QuestionValidationUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        QuestionValidationUtil instance = constructor.newInstance();
        assertThat(instance).isNotNull();
    }
}
