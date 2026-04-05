package com.acm.acmwebsite.EmailServer.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EmailRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequest_shouldHaveNoViolations() {
        EmailRequest request = new EmailRequest(
                List.of("user@example.com"),
                "Welcome!",
                "welcome-template",
                Map.of("name", "Alice")
        );
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void blankSubject_shouldTriggerViolation() {
        EmailRequest request = new EmailRequest(
                List.of("user@example.com"),
                "",
                "welcome-template",
                null
        );
        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().equals("subject"));
    }

    @Test
    void invalidEmailInList_shouldTriggerViolation() {
        EmailRequest request = new EmailRequest(
                List.of("not-an-email"),
                "Subject",
                "welcome-template",
                null
        );
        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().contains("to"));
    }

    @Test
    void emptyToList_shouldTriggerViolation() {
        EmailRequest request = new EmailRequest(
                List.of(),
                "Subject",
                "welcome-template",
                null
        );
        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().equals("to"));
    }

    @Test
    void nullTemplateName_shouldTriggerViolation() {
        EmailRequest request = new EmailRequest(
                List.of("user@example.com"),
                "Subject",
                null,
                null
        );
        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().equals("templateName"));
    }

    @Test
    void nullVariables_shouldBeValid() {
        EmailRequest request = new EmailRequest(
                List.of("user@example.com"),
                "Subject",
                "welcome-template",
                null
        );
        assertThat(validator.validate(request)).isEmpty();
    }
}