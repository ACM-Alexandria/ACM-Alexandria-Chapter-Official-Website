package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.EmailServer.exception.GlobalExceptionHandler;
import com.acm.acmwebsite.User_Authentication.service.AuthorizationService;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.CommitteeController;
import com.acm.acmwebsite.feature.dto.CommitteeCallResponseDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.OpenCallRequestDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeBoardMemberDto;
import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeDto;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.enums.SubscripeTo;
import com.acm.acmwebsite.feature.exception.AnnouncementAlreadySentException;
import com.acm.acmwebsite.feature.exception.EmailsLockedException;
import com.acm.acmwebsite.feature.exception.RegistrationExceptionHandler;
import com.acm.acmwebsite.feature.mapper.CommitteeMapper;
import com.acm.acmwebsite.feature.service.CommitteeRegistrationService;
import com.acm.acmwebsite.feature.service.CommitteeService;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CommitteeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({RegistrationExceptionHandler.class, GlobalExceptionHandler.class})
@DisplayName("CommitteeController WebMvc Tests")
class CommitteeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommitteeService committeeService;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoBean
    private CommitteeMapper committeeMapper;

    @MockitoBean
    private CommitteeRegistrationService committeeRegistrationService;

    @MockitoBean
    private AuthorizationService authorizationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    private UsernamePasswordAuthenticationToken authPrincipal;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        authPrincipal = new UsernamePasswordAuthenticationToken("user@acm.org", null, List.of());
    }

    private Committee createSampleCommittee(Long id, String name) {
        Committee c = new Committee();
        c.setId(id != null ? id : 1L);
        c.setName(name);
        c.setDescription("Committee Description");
        c.setLogoUrl("https://example.com/logo.png");
        c.setOpen(true);
        return c;
    }

    private CommitteeDto createSampleDto(Long id, String name) {
        CommitteeDto dto = new CommitteeDto();
        dto.setId(id != null ? id : 1L);
        dto.setName(name);
        dto.setDescription("Committee Description");
        dto.setLogoUrl("https://example.com/logo.png");
        dto.setOpen(true);
        return dto;
    }

    @Nested
    @DisplayName("GET /api/committee")
    class GetAllCommitteesTests {

        @Test
        @DisplayName("should return 200 OK with list of committee DTOs")
        void shouldReturnAllCommittees() throws Exception {
            Committee c = createSampleCommittee(1L, "Web Committee");
            CommitteeDto dto = createSampleDto(1L, "Web Committee");

            when(committeeService.getAllCommittees()).thenReturn(List.of(c));
            when(committeeMapper.toDto(c)).thenReturn(dto);

            mockMvc.perform(get("/api/committee"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(1)))
                    .andExpect(jsonPath("$[0].name", equalTo("Web Committee")));

            verify(committeeService).getAllCommittees();
        }
    }

    @Nested
    @DisplayName("GET /api/committee/{id}")
    class FindCommitteeByIdTests {

        @Test
        @DisplayName("should return 200 OK when committee found")
        void shouldReturnCommitteeWhenFound() throws Exception {
            Committee c = createSampleCommittee(1L, "Web Committee");
            CommitteeDto dto = createSampleDto(1L, "Web Committee");

            when(committeeService.getCommitteeById(1L)).thenReturn(c);
            when(committeeMapper.toDto(c)).thenReturn(dto);

            mockMvc.perform(get("/api/committee/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.name", equalTo("Web Committee")));

            verify(committeeService).getCommitteeById(1L);
        }

        @Test
        @DisplayName("should return 404 Not Found when committee does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            when(committeeService.getCommitteeById(999L)).thenReturn(null);

            mockMvc.perform(get("/api/committee/{id}", 999L))
                    .andExpect(status().isNotFound());

            verify(committeeService).getCommitteeById(999L);
        }
    }

    @Nested
    @DisplayName("PUT /api/committee/{id}")
    class UpdateCommitteeTests {

        @Test
        @DisplayName("should return 200 OK with updated committee DTO")
        void shouldUpdateSuccessfully() throws Exception {
            CommitteeDto input = createSampleDto(null, "Updated Committee");
            CommitteeDto updated = createSampleDto(1L, "Updated Committee");

            when(committeeService.updateCommittee(eq(1L), any(CommitteeDto.class))).thenReturn(updated);

            mockMvc.perform(put("/api/committee/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.name", equalTo("Updated Committee")));

            verify(committeeService).updateCommittee(eq(1L), any(CommitteeDto.class));
        }

        @Test
        @DisplayName("should return 404 Not Found when EntityNotFoundException is thrown")
        void shouldReturn404WhenEntityNotFound() throws Exception {
            CommitteeDto input = createSampleDto(null, "Nonexistent");
            when(committeeService.updateCommittee(eq(999L), any(CommitteeDto.class)))
                    .thenThrow(new EntityNotFoundException("Committee not found"));

            mockMvc.perform(put("/api/committee/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 Bad Request on generic exception")
        void shouldReturn400OnGenericException() throws Exception {
            CommitteeDto input = createSampleDto(null, "Invalid");
            when(committeeService.updateCommittee(eq(1L), any(CommitteeDto.class)))
                    .thenThrow(new RuntimeException("Update failed"));

            mockMvc.perform(put("/api/committee/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Update failed")));
        }
    }

    @Nested
    @DisplayName("POST /api/committee")
    class CreateCommitteeTests {

        @Test
        @DisplayName("should return 200 OK with created committee DTO")
        void shouldCreateSuccessfully() throws Exception {
            CommitteeDto input = createSampleDto(null, "New Committee");
            Committee entity = createSampleCommittee(null, "New Committee");
            CommitteeDto result = createSampleDto(10L, "New Committee");

            when(committeeMapper.toEntity(any(CommitteeDto.class))).thenReturn(entity);
            when(committeeService.saveCommittee(entity)).thenReturn(entity);
            when(committeeMapper.toDto(entity)).thenReturn(result);

            mockMvc.perform(post("/api/committee")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.name", equalTo("New Committee")));

            verify(committeeService).saveCommittee(entity);
        }

        @Test
        @DisplayName("should return 400 Bad Request on IllegalArgumentException")
        void shouldReturn400OnIllegalArgument() throws Exception {
            CommitteeDto input = createSampleDto(null, "");
            Committee entity = createSampleCommittee(null, "");

            when(committeeMapper.toEntity(any(CommitteeDto.class))).thenReturn(entity);
            doThrow(new IllegalArgumentException("Name cannot be empty"))
                    .when(committeeService).saveCommittee(entity);

            mockMvc.perform(post("/api/committee")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Name cannot be empty")));
        }

        @Test
        @DisplayName("should return 400 Bad Request on duplicate committee generic exception")
        void shouldReturn400OnGenericDuplicate() throws Exception {
            CommitteeDto input = createSampleDto(null, "Existing");
            Committee entity = createSampleCommittee(null, "Existing");

            when(committeeMapper.toEntity(any(CommitteeDto.class))).thenReturn(entity);
            doThrow(new RuntimeException("Duplicate"))
                    .when(committeeService).saveCommittee(entity);

            mockMvc.perform(post("/api/committee")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("this committee already exists")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/committee/{id}")
    class DeleteCommitteeTests {

        @Test
        @DisplayName("should return 200 OK when deleted successfully")
        void shouldDeleteSuccessfully() throws Exception {
            Committee c = createSampleCommittee(1L, "PR Committee");
            when(committeeService.getCommitteeById(1L)).thenReturn(c);
            doNothing().when(committeeService).deleteCommittee(1L);
            doNothing().when(subscriptionService).deleteByTopic(SubscripeTo.COMMITTEE, 1L);

            mockMvc.perform(delete("/api/committee/{id}", 1L))
                    .andExpect(status().isOk());

            verify(committeeService).deleteCommittee(1L);
            verify(subscriptionService).deleteByTopic(SubscripeTo.COMMITTEE, 1L);
        }

        @Test
        @DisplayName("should return 404 Not Found when committee does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            when(committeeService.getCommitteeById(999L)).thenReturn(null);

            mockMvc.perform(delete("/api/committee/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/committee/{id}/open-call")
    class OpenCallTests {

        @Test
        @DisplayName("should return 200 OK with default sendAnnouncement=true when body is omitted")
        void shouldOpenCallWithDefaultAnnouncement() throws Exception {
            doNothing().when(committeeService).openCommitteeCall(1L, true);

            mockMvc.perform(post("/api/committee/{id}/open-call", 1L))
                    .andExpect(status().isOk());

            verify(committeeService).openCommitteeCall(1L, true);
        }

        @Test
        @DisplayName("should return 200 OK with sendAnnouncement=false from body")
        void shouldOpenCallWithSendAnnouncementFalse() throws Exception {
            OpenCallRequestDto req = new OpenCallRequestDto(false);
            doNothing().when(committeeService).openCommitteeCall(1L, false);

            mockMvc.perform(post("/api/committee/{id}/open-call", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            verify(committeeService).openCommitteeCall(1L, false);
        }

        @Test
        @DisplayName("should return 200 OK with sendAnnouncement=true from body")
        void shouldOpenCallWithSendAnnouncementTrue() throws Exception {
            OpenCallRequestDto req = new OpenCallRequestDto(true);
            doNothing().when(committeeService).openCommitteeCall(1L, true);

            mockMvc.perform(post("/api/committee/{id}/open-call", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            verify(committeeService).openCommitteeCall(1L, true);
        }

        @Test
        @DisplayName("should return 404 Not Found on EntityNotFoundException")
        void shouldReturn404WhenEntityNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Not found"))
                    .when(committeeService).openCommitteeCall(999L, true);

            mockMvc.perform(post("/api/committee/{id}/open-call", 999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 Bad Request on generic exception")
        void shouldReturn400OnGenericException() throws Exception {
            doThrow(new RuntimeException("Call error"))
                    .when(committeeService).openCommitteeCall(1L, true);

            mockMvc.perform(post("/api/committee/{id}/open-call", 1L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", equalTo("Call error")));
        }
    }

    @Nested
    @DisplayName("POST /api/committee/{id}/announce-call")
    class AnnounceCallTests {

        @Test
        @DisplayName("should return 200 OK with default force=false")
        void shouldAnnounceWithDefaultForce() throws Exception {
            doNothing().when(committeeService).announceCommitteeCall(1L, false);

            mockMvc.perform(post("/api/committee/{id}/announce-call", 1L))
                    .andExpect(status().isOk());

            verify(committeeService).announceCommitteeCall(1L, false);
        }

        @Test
        @DisplayName("should return 200 OK with force=true")
        void shouldAnnounceWithForceTrue() throws Exception {
            doNothing().when(committeeService).announceCommitteeCall(1L, true);

            mockMvc.perform(post("/api/committee/{id}/announce-call", 1L).param("force", "true"))
                    .andExpect(status().isOk());

            verify(committeeService).announceCommitteeCall(1L, true);
        }

        @Test
        @DisplayName("should return 409 Conflict when AnnouncementAlreadySentException is thrown")
        void shouldReturn409WhenAlreadySent() throws Exception {
            doThrow(new AnnouncementAlreadySentException(LocalDateTime.of(2026, 10, 1, 10, 0)))
                    .when(committeeService).announceCommitteeCall(1L, false);

            mockMvc.perform(post("/api/committee/{id}/announce-call", 1L))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code", equalTo("ANNOUNCEMENT_ALREADY_SENT")));
        }

        @Test
        @DisplayName("should return 409 Conflict when EmailsLockedException is thrown")
        void shouldReturn409WhenEmailsLocked() throws Exception {
            doThrow(new EmailsLockedException())
                    .when(committeeService).announceCommitteeCall(1L, false);

            mockMvc.perform(post("/api/committee/{id}/announce-call", 1L))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("should return 404 Not Found on EntityNotFoundException")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Not found"))
                    .when(committeeService).announceCommitteeCall(999L, false);

            mockMvc.perform(post("/api/committee/{id}/announce-call", 999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 Bad Request on generic exception")
        void shouldReturn400OnGenericException() throws Exception {
            doThrow(new RuntimeException("Announce failed"))
                    .when(committeeService).announceCommitteeCall(1L, false);

            mockMvc.perform(post("/api/committee/{id}/announce-call", 1L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", equalTo("Announce failed")));
        }
    }

    @Nested
    @DisplayName("POST /api/committee/{id}/close-call")
    class CloseCallTests {

        @Test
        @DisplayName("should return 200 OK when closed successfully")
        void shouldCloseCallSuccessfully() throws Exception {
            doNothing().when(committeeService).closeCommitteeCall(1L);

            mockMvc.perform(post("/api/committee/{id}/close-call", 1L))
                    .andExpect(status().isOk());

            verify(committeeService).closeCommitteeCall(1L);
        }

        @Test
        @DisplayName("should return 404 Not Found on EntityNotFoundException")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Not found"))
                    .when(committeeService).closeCommitteeCall(999L);

            mockMvc.perform(post("/api/committee/{id}/close-call", 999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 Bad Request on generic exception")
        void shouldReturn400OnGenericException() throws Exception {
            doThrow(new RuntimeException("Close error"))
                    .when(committeeService).closeCommitteeCall(1L);

            mockMvc.perform(post("/api/committee/{id}/close-call", 1L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", equalTo("Close error")));
        }
    }

    @Nested
    @DisplayName("Registration / Application Endpoints")
    class MemberRegistrationTests {

        @Test
        @DisplayName("GET /api/committee/{id}/questions should return 200 OK with questions")
        void shouldGetQuestions() throws Exception {
            FormQuestionResponseDto q = FormQuestionResponseDto.builder()
                    .id(50L)
                    .questionText("Past experience?")
                    .questionType("TEXT")
                    .isRequired(true)
                    .build();

            when(committeeRegistrationService.getQuestions(1L)).thenReturn(List.of(q));

            mockMvc.perform(get("/api/committee/{id}/questions", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(50)))
                    .andExpect(jsonPath("$[0].question_text", equalTo("Past experience?")));

            verify(committeeRegistrationService).getQuestions(1L);
        }

        @Test
        @DisplayName("POST /api/committee/{id}/register should return 400 Bad Request when userId is null")
        void shouldReturn400WhenUserIdNull() throws Exception {
            RegistrationRequestDto req = new RegistrationRequestDto();
            req.setUserId(null);

            mockMvc.perform(post("/api/committee/{id}/register", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/committee/{id}/register should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            UUID userId = UUID.randomUUID();
            RegistrationRequestDto req = new RegistrationRequestDto();
            req.setUserId(userId);

            when(authorizationService.isAuthenticated(any())).thenReturn(false);

            mockMvc.perform(post("/api/committee/{id}/register", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/committee/{id}/register should return 403 Forbidden when not self")
        void shouldReturn403WhenNotSelf() throws Exception {
            UUID userId = UUID.randomUUID();
            RegistrationRequestDto req = new RegistrationRequestDto();
            req.setUserId(userId);

            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(false);

            mockMvc.perform(post("/api/committee/{id}/register", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/committee/{id}/register should return 201 Created on success")
        void shouldRegisterSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            RegistrationRequestDto req = new RegistrationRequestDto();
            req.setUserId(userId);

            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(true);
            doNothing().when(committeeRegistrationService).registerUser(eq(userId), eq(1L), any(RegistrationRequestDto.class));

            mockMvc.perform(post("/api/committee/{id}/register", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());

            verify(committeeRegistrationService).registerUser(eq(userId), eq(1L), any(RegistrationRequestDto.class));
        }

        @Test
        @DisplayName("DELETE /api/committee/{id}/register/{userId} should return 401 when unauthenticated")
        void shouldReturn401WhenCancelUnauthenticated() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(false);

            mockMvc.perform(delete("/api/committee/{id}/register/{userId}", 1L, userId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/committee/{id}/register/{userId} should return 403 when not self")
        void shouldReturn403WhenCancelNotSelf() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(false);

            mockMvc.perform(delete("/api/committee/{id}/register/{userId}", 1L, userId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/committee/{id}/register/{userId} should return 204 No Content on success")
        void shouldCancelRegistrationSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(true);
            doNothing().when(committeeRegistrationService).cancelRegistration(userId, 1L);

            mockMvc.perform(delete("/api/committee/{id}/register/{userId}", 1L, userId))
                    .andExpect(status().isNoContent());

            verify(committeeRegistrationService).cancelRegistration(userId, 1L);
        }

        @Test
        @DisplayName("GET /api/committee/{id}/is-registered/{userId} should return 400 on invalid UUID")
        void shouldReturn400OnInvalidUuid() throws Exception {
            mockMvc.perform(get("/api/committee/{id}/is-registered/{userId}", 1L, "not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/committee/{id}/is-registered/{userId} should return 401 when unauthenticated")
        void shouldReturn401WhenIsRegisteredUnauthenticated() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(false);

            mockMvc.perform(get("/api/committee/{id}/is-registered/{userId}", 1L, userId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/committee/{id}/is-registered/{userId} should return 403 when not self")
        void shouldReturn403WhenIsRegisteredNotSelf() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(false);

            mockMvc.perform(get("/api/committee/{id}/is-registered/{userId}", 1L, userId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/committee/{id}/is-registered/{userId} should return 200 OK with registered flag")
        void shouldReturnRegisteredStatus() throws Exception {
            UUID userId = UUID.randomUUID();
            when(authorizationService.isAuthenticated(any())).thenReturn(true);
            when(authorizationService.isSelf(eq(userId), any())).thenReturn(true);
            when(committeeRegistrationService.isUserRegistered(userId, 1L)).thenReturn(true);

            mockMvc.perform(get("/api/committee/{id}/is-registered/{userId}", 1L, userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered", equalTo(true)));

            verify(committeeRegistrationService).isUserRegistered(userId, 1L);
        }
    }

    @Nested
    @DisplayName("Admin Question Management Endpoints")
    class QuestionTests {

        @Test
        @DisplayName("POST /api/committee/{id}/questions should return 201 Created")
        void shouldCreateQuestionSuccessfully() throws Exception {
            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Question 1")
                    .questionType("TEXT")
                    .isRequired(true)
                    .build();

            FormQuestionResponseDto res = FormQuestionResponseDto.builder()
                    .id(1L)
                    .questionText("Question 1")
                    .questionType("TEXT")
                    .isRequired(true)
                    .build();

            when(committeeRegistrationService.createQuestion(eq(1L), any(FormQuestionRequestDto.class))).thenReturn(res);

            mockMvc.perform(post("/api/committee/{id}/questions", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.question_text", equalTo("Question 1")));

            verify(committeeRegistrationService).createQuestion(eq(1L), any(FormQuestionRequestDto.class));
        }

        @Test
        @DisplayName("PUT /api/committee/{id}/questions/{questionId} should return 200 OK")
        void shouldUpdateQuestionSuccessfully() throws Exception {
            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Updated Question")
                    .questionType("TEXT")
                    .isRequired(false)
                    .build();

            FormQuestionResponseDto res = FormQuestionResponseDto.builder()
                    .id(1L)
                    .questionText("Updated Question")
                    .questionType("TEXT")
                    .isRequired(false)
                    .build();

            when(committeeRegistrationService.updateQuestion(eq(1L), eq(1L), any(FormQuestionRequestDto.class))).thenReturn(res);

            mockMvc.perform(put("/api/committee/{id}/questions/{questionId}", 1L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.question_text", equalTo("Updated Question")));

            verify(committeeRegistrationService).updateQuestion(eq(1L), eq(1L), any(FormQuestionRequestDto.class));
        }

        @Test
        @DisplayName("DELETE /api/committee/{id}/questions/{questionId} should return 204 No Content")
        void shouldDeleteQuestionSuccessfully() throws Exception {
            doNothing().when(committeeRegistrationService).deleteQuestion(1L, 1L);

            mockMvc.perform(delete("/api/committee/{id}/questions/{questionId}", 1L, 1L))
                    .andExpect(status().isNoContent());

            verify(committeeRegistrationService).deleteQuestion(1L, 1L);
        }
    }

    @Nested
    @DisplayName("Admin Call History & Analytics & Sheets")
    class CallHistoryAndAnalyticsTests {

        @Test
        @DisplayName("GET /api/committee/{id}/calls should return 200 OK with call list")
        void shouldGetCommitteeCalls() throws Exception {
            CommitteeCallResponseDto callDto = CommitteeCallResponseDto.builder()
                    .id(10L)
                    .openedAt(LocalDateTime.of(2026, 9, 1, 10, 0))
                    .registrationsCount(25)
                    .build();

            when(committeeRegistrationService.getCallsForCommittee(1L)).thenReturn(List.of(callDto));

            mockMvc.perform(get("/api/committee/{id}/calls", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(10)))
                    .andExpect(jsonPath("$[0].registrationsCount", equalTo(25)));

            verify(committeeRegistrationService).getCallsForCommittee(1L);
        }

        @Test
        @DisplayName("GET /api/committee/calls/{callId}/registrations/analysis should return 200 OK")
        void shouldGetCallAnalysis() throws Exception {
            RegistrationAnalysisDto analysis = RegistrationAnalysisDto.builder()
                    .totalRegistrations(50)
                    .alexUniStudentCount(40)
                    .nonAlexUniStudentCount(10)
                    .build();

            when(committeeRegistrationService.getRegistrationAnalysis(10L)).thenReturn(analysis);

            mockMvc.perform(get("/api/committee/calls/{callId}/registrations/analysis", 10L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations", equalTo(50)))
                    .andExpect(jsonPath("$.alexUniStudentCount", equalTo(40)));

            verify(committeeRegistrationService).getRegistrationAnalysis(10L);
        }

        @Test
        @DisplayName("POST /api/committee/calls/{callId}/registrations/sheet should return 200 OK")
        void shouldSyncCallRegistrationsSheet() throws Exception {
            RegistrationAnalysisDto analysis = RegistrationAnalysisDto.builder()
                    .totalRegistrations(50)
                    .googleSheetUrl("https://docs.google.com/spreadsheets/d/call-sheet")
                    .build();

            when(committeeRegistrationService.syncRegistrationsSheet(10L)).thenReturn(analysis);

            mockMvc.perform(post("/api/committee/calls/{callId}/registrations/sheet", 10L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations", equalTo(50)))
                    .andExpect(jsonPath("$.googleSheetUrl", equalTo("https://docs.google.com/spreadsheets/d/call-sheet")));

            verify(committeeRegistrationService).syncRegistrationsSheet(10L);
        }

        @Test
        @DisplayName("POST /api/committee/{id}/change-message should return 200 OK on success")
        void shouldChangeCallMessageSuccessfully() throws Exception {
            Message message = new Message("Welcome", "Call message body");
            doNothing().when(committeeService).changeCallMessage(eq(1L), any(Message.class));

            mockMvc.perform(post("/api/committee/{id}/change-message", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(message)))
                    .andExpect(status().isOk());

            verify(committeeService).changeCallMessage(eq(1L), any(Message.class));
        }

        @Test
        @DisplayName("POST /api/committee/{id}/change-message should return 404 on EntityNotFoundException")
        void shouldReturn404OnChangeMessageNotFound() throws Exception {
            Message message = new Message("Welcome", "Body");
            doThrow(new EntityNotFoundException("Not found"))
                    .when(committeeService).changeCallMessage(eq(999L), any(Message.class));

            mockMvc.perform(post("/api/committee/{id}/change-message", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(message)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/committee/{id}/change-message should return 400 on generic exception")
        void shouldReturn400OnChangeMessageError() throws Exception {
            Message message = new Message("Welcome", "Body");
            doThrow(new RuntimeException("Message update failed"))
                    .when(committeeService).changeCallMessage(eq(1L), any(Message.class));

            mockMvc.perform(post("/api/committee/{id}/change-message", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(message)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Message update failed")));
        }
    }

    @Nested
    @DisplayName("Subscription Endpoints")
    class SubscriptionTests {

        @Test
        @DisplayName("POST /api/committee/{id}/subscribe should return 401 when authentication is null or unauthenticated")
        void shouldReturn401WhenSubscribeUnauthenticated() throws Exception {
            mockMvc.perform(post("/api/committee/{id}/subscribe", 1L))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", equalTo("Unauthorized")));
        }

        @Test
        @DisplayName("POST /api/committee/{id}/subscribe should return 200 OK when subscribed")
        void shouldSubscribeSuccessfully() throws Exception {
            doNothing().when(subscriptionService).subscribeToCommittee(1L, "user@acm.org");

            mockMvc.perform(post("/api/committee/{id}/subscribe", 1L).principal(authPrincipal))
                    .andExpect(status().isOk());

            verify(subscriptionService).subscribeToCommittee(1L, "user@acm.org");
        }

        @Test
        @DisplayName("POST /api/committee/{id}/subscribe should return 404 on EntityNotFoundException")
        void shouldReturn404WhenSubscribeNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Committee not found"))
                    .when(subscriptionService).subscribeToCommittee(999L, "user@acm.org");

            mockMvc.perform(post("/api/committee/{id}/subscribe", 999L).principal(authPrincipal))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/committee/{id}/subscribe should return 400 on generic exception")
        void shouldReturn400WhenSubscribeError() throws Exception {
            doThrow(new RuntimeException("Subscribe failed"))
                    .when(subscriptionService).subscribeToCommittee(1L, "user@acm.org");

            mockMvc.perform(post("/api/committee/{id}/subscribe", 1L).principal(authPrincipal))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Subscribe failed")));
        }

        @Test
        @DisplayName("POST /api/committee/{id}/unsubscribe should return 401 when unauthenticated")
        void shouldReturn401WhenUnsubscribeUnauthenticated() throws Exception {
            mockMvc.perform(post("/api/committee/{id}/unsubscribe", 1L))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", equalTo("Unauthorized")));
        }

        @Test
        @DisplayName("POST /api/committee/{id}/unsubscribe should return 200 OK when unsubscribed")
        void shouldUnsubscribeSuccessfully() throws Exception {
            doNothing().when(subscriptionService).unsubscribeFromCommittee(1L, "user@acm.org");

            mockMvc.perform(post("/api/committee/{id}/unsubscribe", 1L).principal(authPrincipal))
                    .andExpect(status().isOk());

            verify(subscriptionService).unsubscribeFromCommittee(1L, "user@acm.org");
        }

        @Test
        @DisplayName("POST /api/committee/{id}/unsubscribe should return 404 on EntityNotFoundException")
        void shouldReturn404WhenUnsubscribeNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Committee not found"))
                    .when(subscriptionService).unsubscribeFromCommittee(999L, "user@acm.org");

            mockMvc.perform(post("/api/committee/{id}/unsubscribe", 999L).principal(authPrincipal))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/committee/{id}/unsubscribe should return 400 on generic exception")
        void shouldReturn400WhenUnsubscribeError() throws Exception {
            doThrow(new RuntimeException("Unsubscribe failed"))
                    .when(subscriptionService).unsubscribeFromCommittee(1L, "user@acm.org");

            mockMvc.perform(post("/api/committee/{id}/unsubscribe", 1L).principal(authPrincipal))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Unsubscribe failed")));
        }

        @Test
        @DisplayName("GET /api/committee/{id}/subscription-status should return false when unauthenticated")
        void shouldReturnFalseWhenStatusUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/committee/{id}/subscription-status", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subscribed", equalTo(false)));
        }

        @Test
        @DisplayName("GET /api/committee/{id}/subscription-status should return status when authenticated")
        void shouldReturnSubscriptionStatusWhenAuthenticated() throws Exception {
            when(subscriptionService.isSubscribedToCommittee(1L, "user@acm.org")).thenReturn(true);

            mockMvc.perform(get("/api/committee/{id}/subscription-status", 1L).principal(authPrincipal))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subscribed", equalTo(true)));

            verify(subscriptionService).isSubscribedToCommittee(1L, "user@acm.org");
        }

        @Test
        @DisplayName("POST /api/committee/{id}/subscribe should return 401 when principal is not authenticated")
        void shouldReturn401WhenSubscribePrincipalUnauthenticated() throws Exception {
            TestingAuthenticationToken unauthenticated = new TestingAuthenticationToken("user", null);
            unauthenticated.setAuthenticated(false);

            mockMvc.perform(post("/api/committee/{id}/subscribe", 1L).principal(unauthenticated))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", equalTo("Unauthorized")));
        }

        @Test
        @DisplayName("POST /api/committee/{id}/unsubscribe should return 401 when principal is not authenticated")
        void shouldReturn401WhenUnsubscribePrincipalUnauthenticated() throws Exception {
            TestingAuthenticationToken unauthenticated = new TestingAuthenticationToken("user", null);
            unauthenticated.setAuthenticated(false);

            mockMvc.perform(post("/api/committee/{id}/unsubscribe", 1L).principal(unauthenticated))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", equalTo("Unauthorized")));
        }

        @Test
        @DisplayName("GET /api/committee/{id}/subscription-status should return false when principal is not authenticated")
        void shouldReturnFalseWhenPrincipalUnauthenticated() throws Exception {
            TestingAuthenticationToken unauthenticated = new TestingAuthenticationToken("user", null);
            unauthenticated.setAuthenticated(false);

            mockMvc.perform(get("/api/committee/{id}/subscription-status", 1L).principal(unauthenticated))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subscribed", equalTo(false)));
        }

        @Test
        @DisplayName("GET /api/committee/{id}/subscription-status should return false when principal name is null")
        void shouldReturnFalseWhenPrincipalNameIsNull() throws Exception {
            TestingAuthenticationToken nullNameToken = new TestingAuthenticationToken(null, null);
            nullNameToken.setAuthenticated(true);

            mockMvc.perform(get("/api/committee/{id}/subscription-status", 1L).principal(nullNameToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subscribed", equalTo(false)));
        }

        @Test
        @DisplayName("GET /api/committee/{id}/subscription-status should return false when service throws exception")
        void shouldReturnFalseWhenServiceThrowsException() throws Exception {
            when(subscriptionService.isSubscribedToCommittee(1L, "user@acm.org"))
                    .thenThrow(new RuntimeException("Status check failed"));

            mockMvc.perform(get("/api/committee/{id}/subscription-status", 1L).principal(authPrincipal))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subscribed", equalTo(false)));
        }
    }

    @Nested
    @DisplayName("Board Member Management Endpoints")
    class BoardMemberTests {

        @Test
        @DisplayName("POST /api/committee/{committeeId}/board-members should return 200 OK on success")
        void shouldAddBoardMemberSuccessfully() throws Exception {
            UUID memberUserId = UUID.randomUUID();
            CommitteeBoardMemberDto input = new CommitteeBoardMemberDto(null, "Lead", 1, memberUserId);
            CommitteeBoardMemberDto output = new CommitteeBoardMemberDto(10L, "Lead", 1, memberUserId);

            when(committeeService.addCommitteeBoardMember(eq(1L), any(CommitteeBoardMemberDto.class))).thenReturn(output);

            mockMvc.perform(post("/api/committee/{committeeId}/board-members", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.role", equalTo("Lead")));

            verify(committeeService).addCommitteeBoardMember(eq(1L), any(CommitteeBoardMemberDto.class));
        }

        @Test
        @DisplayName("POST /api/committee/{committeeId}/board-members should return 404 on EntityNotFoundException")
        void shouldReturn404WhenAddBoardMemberNotFound() throws Exception {
            CommitteeBoardMemberDto input = new CommitteeBoardMemberDto(null, "Lead", 1, UUID.randomUUID());
            when(committeeService.addCommitteeBoardMember(eq(999L), any(CommitteeBoardMemberDto.class)))
                    .thenThrow(new EntityNotFoundException("Committee not found"));

            mockMvc.perform(post("/api/committee/{committeeId}/board-members", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/committee/{committeeId}/board-members should return 400 on generic exception")
        void shouldReturn400WhenAddBoardMemberError() throws Exception {
            CommitteeBoardMemberDto input = new CommitteeBoardMemberDto(null, "Lead", 1, UUID.randomUUID());
            when(committeeService.addCommitteeBoardMember(eq(1L), any(CommitteeBoardMemberDto.class)))
                    .thenThrow(new RuntimeException("Member addition failed"));

            mockMvc.perform(post("/api/committee/{committeeId}/board-members", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Member addition failed")));
        }

        @Test
        @DisplayName("PUT /api/committee/board-members/{id} should return 200 OK on success")
        void shouldUpdateBoardMemberSuccessfully() throws Exception {
            UUID memberUserId = UUID.randomUUID();
            CommitteeBoardMemberDto input = new CommitteeBoardMemberDto(10L, "Co-Lead", 2, memberUserId);
            CommitteeBoardMemberDto output = new CommitteeBoardMemberDto(10L, "Co-Lead", 2, memberUserId);

            when(committeeService.updateCommitteeBoardMember(eq(10L), any(CommitteeBoardMemberDto.class))).thenReturn(output);

            mockMvc.perform(put("/api/committee/board-members/{id}", 10L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.role", equalTo("Co-Lead")));

            verify(committeeService).updateCommitteeBoardMember(eq(10L), any(CommitteeBoardMemberDto.class));
        }

        @Test
        @DisplayName("PUT /api/committee/board-members/{id} should return 404 on EntityNotFoundException")
        void shouldReturn404WhenUpdateBoardMemberNotFound() throws Exception {
            CommitteeBoardMemberDto input = new CommitteeBoardMemberDto(10L, "Co-Lead", 2, UUID.randomUUID());
            when(committeeService.updateCommitteeBoardMember(eq(10L), any(CommitteeBoardMemberDto.class)))
                    .thenThrow(new EntityNotFoundException("Board member not found"));

            mockMvc.perform(put("/api/committee/board-members/{id}", 10L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/committee/board-members/{id} should return 400 on generic exception")
        void shouldReturn400WhenUpdateBoardMemberError() throws Exception {
            CommitteeBoardMemberDto input = new CommitteeBoardMemberDto(10L, "Co-Lead", 2, UUID.randomUUID());
            when(committeeService.updateCommitteeBoardMember(eq(10L), any(CommitteeBoardMemberDto.class)))
                    .thenThrow(new RuntimeException("Update error"));

            mockMvc.perform(put("/api/committee/board-members/{id}", 10L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Update error")));
        }

        @Test
        @DisplayName("DELETE /api/committee/board-members/{id} should return 200 OK on success")
        void shouldDeleteBoardMemberSuccessfully() throws Exception {
            doNothing().when(committeeService).deleteCommitteeBoardMember(10L);

            mockMvc.perform(delete("/api/committee/board-members/{id}", 10L))
                    .andExpect(status().isOk());

            verify(committeeService).deleteCommitteeBoardMember(10L);
        }

        @Test
        @DisplayName("DELETE /api/committee/board-members/{id} should return 404 on EntityNotFoundException")
        void shouldReturn404WhenDeleteBoardMemberNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Member not found"))
                    .when(committeeService).deleteCommitteeBoardMember(999L);

            mockMvc.perform(delete("/api/committee/board-members/{id}", 999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/committee/board-members/{id} should return 400 on generic exception")
        void shouldReturn400WhenDeleteBoardMemberError() throws Exception {
            doThrow(new RuntimeException("Delete error"))
                    .when(committeeService).deleteCommitteeBoardMember(10L);

            mockMvc.perform(delete("/api/committee/board-members/{id}", 10L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Delete error")));
        }
    }
}
