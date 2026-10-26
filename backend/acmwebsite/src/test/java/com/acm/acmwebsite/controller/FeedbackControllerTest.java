package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.EmailServer.exception.GlobalExceptionHandler;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.FeedbackController;
import com.acm.acmwebsite.feature.dto.BugReportResponse;
import com.acm.acmwebsite.feature.dto.FeatureSuggestionResponse;
import com.acm.acmwebsite.feature.entity.BugReport;
import com.acm.acmwebsite.feature.entity.FeatureSuggestion;
import com.acm.acmwebsite.feature.service.FeedbackService;
import com.acm.acmwebsite.feature.service.ImageUploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = FeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("FeedbackController WebMvc Tests")
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FeedbackService feedbackService;

    @MockitoBean
    private ImageUploadService imageUploadService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("POST /api/feedback/features")
    class CreateFeatureSuggestionTests {

        @Test
        @DisplayName("should create feature suggestion with authenticated user")
        void shouldCreateFeatureWithAuthenticatedUser() throws Exception {
            FeatureSuggestion input = new FeatureSuggestion(null, "Dark Mode", "Add dark mode to the site");
            FeatureSuggestion saved = new FeatureSuggestion(UUID.randomUUID(), "Dark Mode", "Add dark mode to the site");
            saved.setId(1L);

            when(feedbackService.saveFeatureSuggestion(any(FeatureSuggestion.class), eq("user@test.com")))
                    .thenReturn(saved);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("user@test.com", null, Collections.emptyList());

            mockMvc.perform(post("/api/feedback/features")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.name", equalTo("Dark Mode")));

            verify(feedbackService).saveFeatureSuggestion(any(FeatureSuggestion.class), eq("user@test.com"));
        }

        @Test
        @DisplayName("should create feature suggestion without authentication")
        void shouldCreateFeatureWithoutAuthentication() throws Exception {
            FeatureSuggestion input = new FeatureSuggestion(null, "Export Data", "Add export data option");
            FeatureSuggestion saved = new FeatureSuggestion(null, "Export Data", "Add export data option");
            saved.setId(2L);

            when(feedbackService.saveFeatureSuggestion(any(FeatureSuggestion.class), isNull()))
                    .thenReturn(saved);

            mockMvc.perform(post("/api/feedback/features")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(2)))
                    .andExpect(jsonPath("$.name", equalTo("Export Data")));

            verify(feedbackService).saveFeatureSuggestion(any(FeatureSuggestion.class), isNull());
        }

        @Test
        @DisplayName("should create feature suggestion when principal is present but not authenticated")
        void shouldCreateFeatureWithUnauthenticatedPrincipal() throws Exception {
            FeatureSuggestion input = new FeatureSuggestion(null, "Theme Support", "Desc");
            FeatureSuggestion saved = new FeatureSuggestion(null, "Theme Support", "Desc");
            saved.setId(3L);

            when(feedbackService.saveFeatureSuggestion(any(FeatureSuggestion.class), isNull()))
                    .thenReturn(saved);

            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);

            mockMvc.perform(post("/api/feedback/features")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(3)));

            verify(feedbackService).saveFeatureSuggestion(any(FeatureSuggestion.class), isNull());
        }
    }

    @Nested
    @DisplayName("POST /api/feedback/bugs")
    class CreateBugReportTests {

        @Test
        @DisplayName("should create bug report with authenticated user")
        void shouldCreateBugWithAuthenticatedUser() throws Exception {
            FeedbackController.BugReportRequest req = new FeedbackController.BugReportRequest();
            req.setName("Login Error");
            req.setDescription("Button does not click");
            req.setImageUrls(List.of("https://img1.png"));

            BugReport saved = new BugReport();
            saved.setId(10L);
            saved.setName("Login Error");

            when(feedbackService.saveBugReport(any(BugReport.class), eq(List.of("https://img1.png")), eq("tester@test.com")))
                    .thenReturn(saved);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("tester@test.com", null, Collections.emptyList());

            mockMvc.perform(post("/api/feedback/bugs")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.name", equalTo("Login Error")));

            verify(feedbackService).saveBugReport(any(BugReport.class), eq(List.of("https://img1.png")), eq("tester@test.com"));
        }

        @Test
        @DisplayName("should create bug report without authentication")
        void shouldCreateBugWithoutAuthentication() throws Exception {
            FeedbackController.BugReportRequest req = new FeedbackController.BugReportRequest();
            req.setName("Footer Link Broken");
            req.setDescription("Footer link is 404");
            req.setImageUrls(null);

            BugReport saved = new BugReport();
            saved.setId(20L);
            saved.setName("Footer Link Broken");

            when(feedbackService.saveBugReport(any(BugReport.class), isNull(), isNull()))
                    .thenReturn(saved);

            mockMvc.perform(post("/api/feedback/bugs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(20)))
                    .andExpect(jsonPath("$.name", equalTo("Footer Link Broken")));

            verify(feedbackService).saveBugReport(any(BugReport.class), isNull(), isNull());
        }

        @Test
        @DisplayName("should create bug report when principal is present but not authenticated")
        void shouldCreateBugWithUnauthenticatedPrincipal() throws Exception {
            FeedbackController.BugReportRequest req = new FeedbackController.BugReportRequest();
            req.setName("Unauth Bug");
            req.setDescription("Desc");

            BugReport saved = new BugReport();
            saved.setId(30L);
            saved.setName("Unauth Bug");

            when(feedbackService.saveBugReport(any(BugReport.class), isNull(), isNull()))
                    .thenReturn(saved);

            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);

            mockMvc.perform(post("/api/feedback/bugs")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(30)));

            verify(feedbackService).saveBugReport(any(BugReport.class), isNull(), isNull());
        }
    }

    @Nested
    @DisplayName("POST /api/feedback/upload-screenshot")
    class UploadScreenshotTests {

        @Test
        @DisplayName("should return 200 OK with uploaded image URL on success")
        void shouldUploadScreenshotSuccessfully() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "screenshot.png", MediaType.IMAGE_PNG_VALUE, "fake-image-bytes".getBytes());

            when(imageUploadService.uploadImage(any())).thenReturn("https://cloudinary.com/shot.png");

            mockMvc.perform(multipart("/api/feedback/upload-screenshot").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.url", equalTo("https://cloudinary.com/shot.png")));

            verify(imageUploadService).uploadImage(any());
        }

        @Test
        @DisplayName("should return 400 Bad Request when upload throws IllegalArgumentException")
        void shouldReturn400OnIllegalArgument() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "empty.png", MediaType.IMAGE_PNG_VALUE, new byte[0]);

            when(imageUploadService.uploadImage(any())).thenThrow(new IllegalArgumentException("File cannot be empty"));

            mockMvc.perform(multipart("/api/feedback/upload-screenshot").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", equalTo("File cannot be empty")));

            verify(imageUploadService).uploadImage(any());
        }

        @Test
        @DisplayName("should return 500 Internal Server Error when upload throws general Exception")
        void shouldReturn500OnGeneralException() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "shot.png", MediaType.IMAGE_PNG_VALUE, "data".getBytes());

            when(imageUploadService.uploadImage(any())).thenThrow(new RuntimeException("Cloudinary network timeout"));

            mockMvc.perform(multipart("/api/feedback/upload-screenshot").file(file))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error", containsString("Cloudinary network timeout")));

            verify(imageUploadService).uploadImage(any());
        }
    }

    @Nested
    @DisplayName("GET /api/feedback/features")
    class GetAllFeaturesTests {

        @Test
        @DisplayName("should return 200 OK with feature suggestions list")
        void shouldReturnAllFeatures() throws Exception {
            FeatureSuggestionResponse r = new FeatureSuggestionResponse(
                    1L, UUID.randomUUID(), "Alice", "alice@test.com", "Theme", "Support themes", "NEW", LocalDateTime.now());

            when(feedbackService.getAllFeatureSuggestions()).thenReturn(List.of(r));

            mockMvc.perform(get("/api/feedback/features"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(1)))
                    .andExpect(jsonPath("$[0].name", equalTo("Theme")));

            verify(feedbackService).getAllFeatureSuggestions();
        }
    }

    @Nested
    @DisplayName("GET /api/feedback/bugs")
    class GetAllBugsTests {

        @Test
        @DisplayName("should return 200 OK with bug reports list")
        void shouldReturnAllBugs() throws Exception {
            BugReportResponse r = new BugReportResponse(
                    10L, UUID.randomUUID(), "Bob", "bob@test.com", "Crash", "App crashes on submit",
                    List.of("https://pic.png"), "NEW", LocalDateTime.now());

            when(feedbackService.getAllBugReports()).thenReturn(List.of(r));

            mockMvc.perform(get("/api/feedback/bugs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(10)))
                    .andExpect(jsonPath("$[0].name", equalTo("Crash")));

            verify(feedbackService).getAllBugReports();
        }
    }

    @Nested
    @DisplayName("PUT /api/feedback/features/{id}/toggle-status")
    class ToggleFeatureStatusTests {

        @Test
        @DisplayName("should return 200 OK on successful feature status toggle")
        void shouldToggleFeatureStatusSuccessfully() throws Exception {
            doNothing().when(feedbackService).toggleFeatureSuggestionStatus(1L);

            mockMvc.perform(put("/api/feedback/features/{id}/toggle-status", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", equalTo("Feature suggestion status toggled successfully")));

            verify(feedbackService).toggleFeatureSuggestionStatus(1L);
        }

        @Test
        @DisplayName("should return 404 Not Found when feature does not exist")
        void shouldReturn404WhenFeatureNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Feature suggestion not found")).when(feedbackService).toggleFeatureSuggestionStatus(99L);

            mockMvc.perform(put("/api/feedback/features/{id}/toggle-status", 99L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", equalTo("Feature suggestion not found")));

            verify(feedbackService).toggleFeatureSuggestionStatus(99L);
        }
    }

    @Nested
    @DisplayName("PUT /api/feedback/bugs/{id}/toggle-status")
    class ToggleBugStatusTests {

        @Test
        @DisplayName("should return 200 OK on successful bug status toggle")
        void shouldToggleBugStatusSuccessfully() throws Exception {
            doNothing().when(feedbackService).toggleBugReportStatus(1L);

            mockMvc.perform(put("/api/feedback/bugs/{id}/toggle-status", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", equalTo("Bug report status toggled successfully")));

            verify(feedbackService).toggleBugReportStatus(1L);
        }

        @Test
        @DisplayName("should return 404 Not Found when bug report does not exist")
        void shouldReturn404WhenBugNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Bug report not found")).when(feedbackService).toggleBugReportStatus(99L);

            mockMvc.perform(put("/api/feedback/bugs/{id}/toggle-status", 99L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", equalTo("Bug report not found")));

            verify(feedbackService).toggleBugReportStatus(99L);
        }
    }

    @Nested
    @DisplayName("BugReportRequest DTO Tests")
    class BugReportRequestDtoTests {

        @Test
        @DisplayName("should cover all getters and setters for BugReportRequest")
        void testBugReportRequestGettersAndSetters() {
            FeedbackController.BugReportRequest req = new FeedbackController.BugReportRequest();
            req.setName("Title");
            req.setDescription("Desc");
            req.setImageUrls(List.of("url1", "url2"));

            assertEquals("Title", req.getName());
            assertEquals("Desc", req.getDescription());
            assertEquals(List.of("url1", "url2"), req.getImageUrls());
        }
    }
}
