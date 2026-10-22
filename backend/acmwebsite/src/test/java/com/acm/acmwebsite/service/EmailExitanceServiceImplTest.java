package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.service.impl.EmailExitanceServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailExitanceServiceImplTest {

    private static final String API_URL = "https://rapid-email-verifier.fly.dev/api/validate?email=";

    @Mock
    private RestTemplate restTemplate;

    private EmailExitanceServiceImpl emailExitanceService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        emailExitanceService = new EmailExitanceServiceImpl();
        ReflectionTestUtils.setField(emailExitanceService, "restTemplate", restTemplate);
    }

    @Test
    @DisplayName("Should return true when all validations pass")
    void shouldReturnTrueWhenAllValidationsPass() throws Exception {
        String json = """
            {
                "validations": {
                    "syntax": true,
                    "domain_exists": true,
                    "mx_records": true,
                    "mailbox_exists": true
                }
            }
            """;
        JsonNode responseNode = objectMapper.readTree(json);
        when(restTemplate.getForObject(API_URL + "valid@example.com", JsonNode.class)).thenReturn(responseNode);

        boolean result = emailExitanceService.isEmailReal("valid@example.com");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when syntax is invalid")
    void shouldReturnFalseWhenSyntaxIsInvalid() throws Exception {
        String json = """
            {
                "validations": {
                    "syntax": false,
                    "domain_exists": true,
                    "mx_records": true,
                    "mailbox_exists": true
                }
            }
            """;
        JsonNode responseNode = objectMapper.readTree(json);
        when(restTemplate.getForObject(API_URL + "invalid@example.com", JsonNode.class)).thenReturn(responseNode);

        boolean result = emailExitanceService.isEmailReal("invalid@example.com");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when domain does not exist")
    void shouldReturnFalseWhenDomainDoesNotExist() throws Exception {
        String json = """
            {
                "validations": {
                    "syntax": true,
                    "domain_exists": false,
                    "mx_records": true,
                    "mailbox_exists": true
                }
            }
            """;
        JsonNode responseNode = objectMapper.readTree(json);
        when(restTemplate.getForObject(API_URL + "test@domain.com", JsonNode.class)).thenReturn(responseNode);

        boolean result = emailExitanceService.isEmailReal("test@domain.com");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when mx records do not exist")
    void shouldReturnFalseWhenMxRecordsDoNotExist() throws Exception {
        String json = """
            {
                "validations": {
                    "syntax": true,
                    "domain_exists": true,
                    "mx_records": false,
                    "mailbox_exists": true
                }
            }
            """;
        JsonNode responseNode = objectMapper.readTree(json);
        when(restTemplate.getForObject(API_URL + "test@domain.com", JsonNode.class)).thenReturn(responseNode);

        boolean result = emailExitanceService.isEmailReal("test@domain.com");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when mailbox does not exist")
    void shouldReturnFalseWhenMailboxDoesNotExist() throws Exception {
        String json = """
            {
                "validations": {
                    "syntax": true,
                    "domain_exists": true,
                    "mx_records": true,
                    "mailbox_exists": false
                }
            }
            """;
        JsonNode responseNode = objectMapper.readTree(json);
        when(restTemplate.getForObject(API_URL + "test@domain.com", JsonNode.class)).thenReturn(responseNode);

        boolean result = emailExitanceService.isEmailReal("test@domain.com");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when response is null")
    void shouldReturnFalseWhenResponseIsNull() {
        when(restTemplate.getForObject(API_URL + "test@domain.com", JsonNode.class)).thenReturn(null);

        boolean result = emailExitanceService.isEmailReal("test@domain.com");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when validations field is null")
    void shouldReturnFalseWhenValidationsFieldIsNull() throws Exception {
        String json = """
            {
                "other": "data"
            }
            """;
        JsonNode responseNode = objectMapper.readTree(json);
        when(restTemplate.getForObject(API_URL + "test@domain.com", JsonNode.class)).thenReturn(responseNode);

        boolean result = emailExitanceService.isEmailReal("test@domain.com");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when restTemplate throws an exception")
    void shouldReturnFalseWhenRestTemplateThrowsException() {
        when(restTemplate.getForObject(API_URL + "test@domain.com", JsonNode.class))
                .thenThrow(new RestClientException("Connection refused"));

        boolean result = emailExitanceService.isEmailReal("test@domain.com");

        assertThat(result).isFalse();
    }
}
