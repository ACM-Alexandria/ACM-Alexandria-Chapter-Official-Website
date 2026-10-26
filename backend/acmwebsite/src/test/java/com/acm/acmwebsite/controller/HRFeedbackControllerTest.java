package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.EmailServer.exception.GlobalExceptionHandler;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.HRFeedbackController;
import com.acm.acmwebsite.feature.dto.HRFeedbackRequest;
import com.acm.acmwebsite.feature.dto.HRFeedbackResponse;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.enums.HRFeedbackType;
import com.acm.acmwebsite.feature.service.HRFeedbackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = HRFeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("HRFeedbackController WebMvc Tests")
class HRFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HRFeedbackService hrFeedbackService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("POST /api/v1/hr-feedback")
    class CreateFeedbackTests {

        @Test
        @DisplayName("should create feedback when user is authenticated")
        void shouldCreateFeedbackWithAuthenticatedUser() throws Exception {
            HRFeedbackRequest req = new HRFeedbackRequest();
            req.setType(HRFeedbackType.SUGGESTION);
            req.setContent("Great workshop");
            req.setIsAnonymous(false);

            HRFeedbackResponse resp = new HRFeedbackResponse();
            resp.setId(1L);
            resp.setType(HRFeedbackType.SUGGESTION);
            resp.setContent("Great workshop");
            resp.setIsAnonymous(false);
            resp.setReporterEmail("member@test.com");
            resp.setCreatedAt(LocalDateTime.now());

            when(hrFeedbackService.saveFeedback(any(HRFeedbackRequest.class), eq("member@test.com"))).thenReturn(resp);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("member@test.com", null, Collections.emptyList());

            mockMvc.perform(post("/api/v1/hr-feedback")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.content", equalTo("Great workshop")))
                    .andExpect(jsonPath("$.reporterEmail", equalTo("member@test.com")));

            verify(hrFeedbackService).saveFeedback(any(HRFeedbackRequest.class), eq("member@test.com"));
        }

        @Test
        @DisplayName("should create feedback when unauthenticated/anonymous")
        void shouldCreateFeedbackWithoutAuthentication() throws Exception {
            HRFeedbackRequest req = new HRFeedbackRequest();
            req.setType(HRFeedbackType.COMPLAINT);
            req.setContent("Anonymous complaint");
            req.setIsAnonymous(true);

            HRFeedbackResponse resp = new HRFeedbackResponse();
            resp.setId(2L);
            resp.setType(HRFeedbackType.COMPLAINT);
            resp.setContent("Anonymous complaint");
            resp.setIsAnonymous(true);
            resp.setCreatedAt(LocalDateTime.now());

            when(hrFeedbackService.saveFeedback(any(HRFeedbackRequest.class), isNull())).thenReturn(resp);

            mockMvc.perform(post("/api/v1/hr-feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(2)))
                    .andExpect(jsonPath("$.content", equalTo("Anonymous complaint")));

            verify(hrFeedbackService).saveFeedback(any(HRFeedbackRequest.class), isNull());
        }

        @Test
        @DisplayName("should create feedback when principal is present but not authenticated")
        void shouldCreateFeedbackWithUnauthenticatedPrincipal() throws Exception {
            HRFeedbackRequest req = new HRFeedbackRequest();
            req.setType(HRFeedbackType.SUGGESTION);
            req.setContent("Unauthenticated feedback");
            req.setIsAnonymous(true);

            HRFeedbackResponse resp = new HRFeedbackResponse();
            resp.setId(3L);
            resp.setType(HRFeedbackType.SUGGESTION);
            resp.setContent("Unauthenticated feedback");
            resp.setIsAnonymous(true);
            resp.setCreatedAt(LocalDateTime.now());

            when(hrFeedbackService.saveFeedback(any(HRFeedbackRequest.class), isNull())).thenReturn(resp);

            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);

            mockMvc.perform(post("/api/v1/hr-feedback")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(3)))
                    .andExpect(jsonPath("$.content", equalTo("Unauthenticated feedback")));

            verify(hrFeedbackService).saveFeedback(any(HRFeedbackRequest.class), isNull());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/hr-feedback")
    class GetAllFeedbackTests {

        @Test
        @DisplayName("should return 403 Forbidden when user is not found in database")
        void shouldReturn403WhenUserNotFound() throws Exception {
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("unknown@test.com", null, Collections.emptyList());

            mockMvc.perform(get("/api/v1/hr-feedback").principal(auth))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string("User not found."));

            verify(userRepository).findByEmail("unknown@test.com");
        }

        @Test
        @DisplayName("should return 200 OK when user is SUPER_ADMIN")
        void shouldReturn200WhenSuperAdmin() throws Exception {
            User admin = new User();
            admin.setEmail("admin@test.com");
            admin.setRole(Role.SUPER_ADMIN);

            HRFeedbackResponse resp = new HRFeedbackResponse();
            resp.setId(10L);
            resp.setContent("Feedback 10");

            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
            when(hrFeedbackService.getAllFeedback()).thenReturn(List.of(resp));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("admin@test.com", null, Collections.emptyList());

            mockMvc.perform(get("/api/v1/hr-feedback").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(10)))
                    .andExpect(jsonPath("$[0].content", equalTo("Feedback 10")));

            verify(hrFeedbackService).getAllFeedback();
        }

        @Test
        @DisplayName("should return 200 OK when user is ACM_COMMITTEE_BOARD of HR Committee")
        void shouldReturn200WhenHRBoard() throws Exception {
            Committee hrCommittee = new Committee();
            hrCommittee.setName("HR Committee");

            User hrMember = new User();
            hrMember.setEmail("hr@test.com");
            hrMember.setRole(Role.ACM_COMMITTEE_BOARD);
            hrMember.setCommittee(hrCommittee);

            HRFeedbackResponse resp = new HRFeedbackResponse();
            resp.setId(11L);
            resp.setContent("Feedback 11");

            when(userRepository.findByEmail("hr@test.com")).thenReturn(Optional.of(hrMember));
            when(hrFeedbackService.getAllFeedback()).thenReturn(List.of(resp));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("hr@test.com", null, Collections.emptyList());

            mockMvc.perform(get("/api/v1/hr-feedback").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(11)));

            verify(hrFeedbackService).getAllFeedback();
        }

        @Test
        @DisplayName("should return 403 Forbidden when ACM_COMMITTEE_BOARD has null committee")
        void shouldReturn403WhenCommitteeIsNull() throws Exception {
            User boardMember = new User();
            boardMember.setEmail("board@test.com");
            boardMember.setRole(Role.ACM_COMMITTEE_BOARD);
            boardMember.setCommittee(null);

            when(userRepository.findByEmail("board@test.com")).thenReturn(Optional.of(boardMember));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("board@test.com", null, Collections.emptyList());

            mockMvc.perform(get("/api/v1/hr-feedback").principal(auth))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string("Access Denied: Only HR Board members can view this data."));
        }

        @Test
        @DisplayName("should return 403 Forbidden when ACM_COMMITTEE_BOARD belongs to non-HR committee")
        void shouldReturn403WhenNonHRCommittee() throws Exception {
            Committee prCommittee = new Committee();
            prCommittee.setName("PR Committee");

            User prMember = new User();
            prMember.setEmail("pr@test.com");
            prMember.setRole(Role.ACM_COMMITTEE_BOARD);
            prMember.setCommittee(prCommittee);

            when(userRepository.findByEmail("pr@test.com")).thenReturn(Optional.of(prMember));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("pr@test.com", null, Collections.emptyList());

            mockMvc.perform(get("/api/v1/hr-feedback").principal(auth))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string("Access Denied: Only HR Board members can view this data."));
        }

        @Test
        @DisplayName("should return 403 Forbidden when user has general USER role")
        void shouldReturn403WhenGeneralUser() throws Exception {
            User regularUser = new User();
            regularUser.setEmail("student@test.com");
            regularUser.setRole(Role.USER);

            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(regularUser));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("student@test.com", null, Collections.emptyList());

            mockMvc.perform(get("/api/v1/hr-feedback").principal(auth))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string("Access Denied: Only HR Board members can view this data."));
        }
    }
}
