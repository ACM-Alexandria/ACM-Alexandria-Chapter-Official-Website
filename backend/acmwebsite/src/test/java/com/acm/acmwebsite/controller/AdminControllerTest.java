package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.EmailServer.exception.GlobalExceptionHandler;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.AdminController;
import com.acm.acmwebsite.feature.dto.AdminInsightsDto;
import com.acm.acmwebsite.feature.dto.CustomEmailPreviewDto;
import com.acm.acmwebsite.feature.dto.CustomEmailPreviewRequestDto;
import com.acm.acmwebsite.feature.dto.CustomEmailRequestDto;
import com.acm.acmwebsite.feature.dto.CustomEmailResultDto;
import com.acm.acmwebsite.feature.service.AdminEmailService;
import com.acm.acmwebsite.feature.service.AdminInsightsService;
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

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("AdminController WebMvc Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminInsightsService adminInsightsService;

    @MockitoBean
    private AdminEmailService adminEmailService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("GET /api/admin/insights")
    class GetInsightsTests {

        @Test
        @DisplayName("should return 200 OK with aggregated admin insights")
        void shouldReturnInsights() throws Exception {
            AdminInsightsDto insights = AdminInsightsDto.builder()
                    .totalUsers(100L)
                    .totalEvents(15L)
                    .totalClubs(5L)
                    .build();

            when(adminInsightsService.getInsights()).thenReturn(insights);

            mockMvc.perform(get("/api/admin/insights"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalUsers", equalTo(100)))
                    .andExpect(jsonPath("$.totalEvents", equalTo(15)))
                    .andExpect(jsonPath("$.totalClubs", equalTo(5)));

            verify(adminInsightsService).getInsights();
        }
    }

    @Nested
    @DisplayName("POST /api/admin/emails/preview")
    class PreviewCustomEmailTests {

        @Test
        @DisplayName("should return 200 OK with email preview on valid request")
        void shouldPreviewCustomEmailSuccessfully() throws Exception {
            CustomEmailPreviewRequestDto req = new CustomEmailPreviewRequestDto();
            req.setSubject("General Announcement");
            req.setBody("Hello all members");

            CustomEmailPreviewDto preview = CustomEmailPreviewDto.builder()
                    .totalRecipients(50L)
                    .emailHtml("<p>Hello all members</p>")
                    .build();

            when(adminEmailService.previewCustomEmail(any(CustomEmailPreviewRequestDto.class))).thenReturn(preview);

            mockMvc.perform(post("/api/admin/emails/preview")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRecipients", equalTo(50)))
                    .andExpect(jsonPath("$.emailHtml", equalTo("<p>Hello all members</p>")));

            verify(adminEmailService).previewCustomEmail(any(CustomEmailPreviewRequestDto.class));
        }

        @Test
        @DisplayName("should return 400 Bad Request when preview throws IllegalArgumentException")
        void shouldReturn400WhenPreviewThrowsIllegalArgumentException() throws Exception {
            CustomEmailPreviewRequestDto req = new CustomEmailPreviewRequestDto();
            req.setSubject("General Announcement");
            req.setBody("Hello all members");

            when(adminEmailService.previewCustomEmail(any(CustomEmailPreviewRequestDto.class)))
                    .thenThrow(new IllegalArgumentException("No recipients selected"));

            mockMvc.perform(post("/api/admin/emails/preview")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("No recipients selected"));

            verify(adminEmailService).previewCustomEmail(any(CustomEmailPreviewRequestDto.class));
        }
    }

    @Nested
    @DisplayName("POST /api/admin/emails/send-custom")
    class SendCustomEmailTests {

        @Test
        @DisplayName("should return 200 OK with result on valid send request")
        void shouldSendCustomEmailSuccessfully() throws Exception {
            CustomEmailRequestDto req = new CustomEmailRequestDto();
            req.setRecipientFilter("FILTERED_USERS");
            req.setSubject("Urgent Update");
            req.setBody("Meeting rescheduled");

            CustomEmailResultDto result = CustomEmailResultDto.builder()
                    .totalRecipients(30)
                    .message("Emails queued successfully")
                    .build();

            when(adminEmailService.sendCustomEmail(any(CustomEmailRequestDto.class))).thenReturn(result);

            mockMvc.perform(post("/api/admin/emails/send-custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRecipients", equalTo(30)))
                    .andExpect(jsonPath("$.message", equalTo("Emails queued successfully")));

            verify(adminEmailService).sendCustomEmail(any(CustomEmailRequestDto.class));
        }

        @Test
        @DisplayName("should return 400 Bad Request when send throws IllegalArgumentException")
        void shouldReturn400WhenSendThrowsIllegalArgumentException() throws Exception {
            CustomEmailRequestDto req = new CustomEmailRequestDto();
            req.setRecipientFilter("FILTERED_USERS");
            req.setSubject("Urgent Update");
            req.setBody("Meeting rescheduled");

            when(adminEmailService.sendCustomEmail(any(CustomEmailRequestDto.class)))
                    .thenThrow(new IllegalArgumentException("Email sending failed"));

            mockMvc.perform(post("/api/admin/emails/send-custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Email sending failed"));

            verify(adminEmailService).sendCustomEmail(any(CustomEmailRequestDto.class));
        }
    }
}
