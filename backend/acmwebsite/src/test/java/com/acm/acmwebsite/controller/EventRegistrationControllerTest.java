package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.EmailServer.exception.GlobalExceptionHandler;
import com.acm.acmwebsite.User_Authentication.service.AuthorizationService;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.EventRegistrationController;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.exception.RegistrationExceptionHandler;
import com.acm.acmwebsite.feature.service.EventRegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
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

@WebMvcTest(controllers = EventRegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({RegistrationExceptionHandler.class, GlobalExceptionHandler.class})
@DisplayName("EventRegistrationController WebMvc Tests")
class EventRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventRegistrationService eventRegistrationService;

    @MockitoBean
    private AuthorizationService authorizationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("GET /api/events/{id}/questions")
    class GetQuestionsTests {

        @Test
        @DisplayName("should return 200 OK with list of questions")
        void shouldReturnQuestions() throws Exception {
            FormQuestionResponseDto q = FormQuestionResponseDto.builder()
                    .id(1L)
                    .questionText("Dietary requirements?")
                    .questionType("TEXT")
                    .isRequired(false)
                    .build();

            when(eventRegistrationService.getQuestions(1L)).thenReturn(List.of(q));

            mockMvc.perform(get("/api/events/{id}/questions", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(1)))
                    .andExpect(jsonPath("$[0].question_text", equalTo("Dietary requirements?")));

            verify(eventRegistrationService).getQuestions(1L);
        }
    }

    @Nested
    @DisplayName("POST /api/events/{id}/register")
    class RegisterTests {

        @Test
        @DisplayName("should return 400 Bad Request when userId is null")
        void shouldReturn400WhenUserIdNull() throws Exception {
            RegistrationRequestDto req = new RegistrationRequestDto();
            req.setUserId(null);

            mockMvc.perform(post("/api/events/{id}/register", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            UUID userId = UUID.randomUUID();
            RegistrationRequestDto req = new RegistrationRequestDto();
            req.setUserId(userId);

            when(authorizationService.isAuthenticated(any())).thenReturn(false);

            mockMvc.perform(post("/api/events/{id}/register", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 Forbidden when not self")
        void shouldReturn403WhenNotSelf() throws Exception {
            UUID userId = UUID.randomUUID();
            RegistrationRequestDto req = new RegistrationRequestDto();
            req.setUserId(userId);

            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(false);

            mockMvc.perform(post("/api/events/{id}/register", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 201 Created on valid registration")
        void shouldRegisterSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            RegistrationRequestDto req = new RegistrationRequestDto();
            req.setUserId(userId);

            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(true);
            doNothing().when(eventRegistrationService).registerUser(eq(userId), eq(1L), any(RegistrationRequestDto.class));

            mockMvc.perform(post("/api/events/{id}/register", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());

            verify(eventRegistrationService).registerUser(eq(userId), eq(1L), any(RegistrationRequestDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/events/{id}/register/{userId}")
    class CancelRegistrationTests {

        @Test
        @DisplayName("should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(false);

            mockMvc.perform(delete("/api/events/{id}/register/{userId}", 1L, userId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 Forbidden when not self")
        void shouldReturn403WhenNotSelf() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(false);

            mockMvc.perform(delete("/api/events/{id}/register/{userId}", 1L, userId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 204 No Content on successful cancellation")
        void shouldCancelSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(true);
            doNothing().when(eventRegistrationService).cancelRegistration(userId, 1L);

            mockMvc.perform(delete("/api/events/{id}/register/{userId}", 1L, userId))
                    .andExpect(status().isNoContent());

            verify(eventRegistrationService).cancelRegistration(userId, 1L);
        }
    }

    @Nested
    @DisplayName("GET /api/events/{id}/is-registered/{userId}")
    class IsRegisteredForEventTests {

        @Test
        @DisplayName("should return 400 Bad Request on invalid UUID string")
        void shouldReturn400OnInvalidUuid() throws Exception {
            mockMvc.perform(get("/api/events/{id}/is-registered/{userId}", 1L, "not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(false);

            mockMvc.perform(get("/api/events/{id}/is-registered/{userId}", 1L, userId.toString()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 Forbidden when not self")
        void shouldReturn403WhenNotSelf() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(false);

            mockMvc.perform(get("/api/events/{id}/is-registered/{userId}", 1L, userId.toString()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 200 OK with registered flag on success")
        void shouldReturnRegisteredStatus() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(true);
            when(eventRegistrationService.isUserRegistered(userId, 1L)).thenReturn(true);

            mockMvc.perform(get("/api/events/{id}/is-registered/{userId}", 1L, userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered", equalTo(true)));

            verify(eventRegistrationService).isUserRegistered(userId, 1L);
        }
    }
}
