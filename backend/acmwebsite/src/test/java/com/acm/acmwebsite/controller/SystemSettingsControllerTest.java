package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.EmailServer.exception.GlobalExceptionHandler;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.SystemSettingsController;
import com.acm.acmwebsite.feature.dto.EmailLockDto;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SystemSettingsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("SystemSettingsController WebMvc Tests")
class SystemSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SystemSettingsService systemSettingsService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("GET /api/admin/settings/email-lock")
    class GetEmailLockTests {

        @Test
        @DisplayName("should return 200 OK with email lock status true")
        void shouldReturnEmailLockStatusTrue() throws Exception {
            when(systemSettingsService.isEmailsEnabled()).thenReturn(true);

            mockMvc.perform(get("/api/admin/settings/email-lock"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.emailsEnabled", equalTo(true)));

            verify(systemSettingsService).isEmailsEnabled();
        }

        @Test
        @DisplayName("should return 200 OK with email lock status false")
        void shouldReturnEmailLockStatusFalse() throws Exception {
            when(systemSettingsService.isEmailsEnabled()).thenReturn(false);

            mockMvc.perform(get("/api/admin/settings/email-lock"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.emailsEnabled", equalTo(false)));

            verify(systemSettingsService).isEmailsEnabled();
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/settings/email-lock")
    class UpdateEmailLockTests {

        @Test
        @DisplayName("should return 200 OK with updated email lock status")
        void shouldUpdateEmailLockStatus() throws Exception {
            EmailLockDto req = new EmailLockDto(false);
            when(systemSettingsService.setEmailsEnabled(false)).thenReturn(false);

            mockMvc.perform(put("/api/admin/settings/email-lock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.emailsEnabled", equalTo(false)));

            verify(systemSettingsService).setEmailsEnabled(false);
        }
    }
}
