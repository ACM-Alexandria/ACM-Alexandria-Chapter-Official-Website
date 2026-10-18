package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
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
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.ClubController;
import com.acm.acmwebsite.feature.dto.ClubCardDto;
import com.acm.acmwebsite.feature.dto.EmailSettingsDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.exception.AnnouncementAlreadySentException;
import com.acm.acmwebsite.feature.exception.EmailsLockedException;
import com.acm.acmwebsite.feature.exception.RegistrationExceptionHandler;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.service.ClubService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ClubController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({RegistrationExceptionHandler.class, GlobalExceptionHandler.class})
@DisplayName("ClubController WebMvc Tests")
class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClubService clubService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    private Club createSampleClub(Long id, String name) {
        Club club = new Club();
        club.setId(id);
        club.setName(name);
        club.setDescription("Test Description");
        club.setImageUrl("https://example.com/club.jpg");
        club.setRegistrationOpen(true);
        club.setIsExternal(false);
        club.setSocialMediaLinks(List.of("https://facebook.com/acm"));
        return club;
    }

    @Nested
    @DisplayName("GET /api/clubs")
    class GetAllClubsTests {

        @Test
        @DisplayName("should return 200 OK with list of all clubs when no page param is provided")
        void shouldReturnAllClubsWithoutPagination() throws Exception {
            ClubCardDto card = new ClubCardDto(1L, "AI Club", "https://example.com/ai.jpg", "AI & ML", false);

            when(clubService.getAllClubs()).thenReturn(List.of(card));

            mockMvc.perform(get("/api/clubs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", equalTo(1)))
                    .andExpect(jsonPath("$[0].name", equalTo("AI Club")));

            verify(clubService).getAllClubs();
        }

        @Test
        @DisplayName("should return 200 OK with paginated clubs when page param is provided")
        void shouldReturnPaginatedClubs() throws Exception {
            ClubCardDto card = new ClubCardDto(2L, "Web Dev Club", "https://example.com/web.jpg", "Fullstack Web", true);

            Page<ClubCardDto> page = new PageImpl<>(List.of(card), PageRequest.of(1, 10), 1);
            when(clubService.getClubsByPage(1)).thenReturn(page);

            mockMvc.perform(get("/api/clubs").param("page", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id", equalTo(2)))
                    .andExpect(jsonPath("$.content[0].name", equalTo("Web Dev Club")));

            verify(clubService).getClubsByPage(1);
        }
    }

    @Nested
    @DisplayName("GET /api/clubs/{id}")
    class GetClubByIdTests {

        @Test
        @DisplayName("should return 200 OK with club when found")
        void shouldReturnClubWhenFound() throws Exception {
            Club club = createSampleClub(1L, "Competitive Programming Club");
            when(clubService.getClubById(1L)).thenReturn(Optional.of(club));

            mockMvc.perform(get("/api/clubs/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.name", equalTo("Competitive Programming Club")));

            verify(clubService).getClubById(1L);
        }

        @Test
        @DisplayName("should return 404 Not Found when club does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            when(clubService.getClubById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/clubs/{id}", 999L))
                    .andExpect(status().isNotFound());

            verify(clubService).getClubById(999L);
        }
    }

    @Nested
    @DisplayName("POST /api/clubs")
    class CreateClubTests {

        @Test
        @DisplayName("should return 200 OK with created club")
        void shouldCreateClubSuccessfully() throws Exception {
            Club input = createSampleClub(null, "Cybersecurity Club");
            Club saved = createSampleClub(10L, "Cybersecurity Club");

            when(clubService.createClub(any(Club.class))).thenReturn(saved);

            mockMvc.perform(post("/api/clubs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(10)))
                    .andExpect(jsonPath("$.name", equalTo("Cybersecurity Club")));

            verify(clubService).createClub(any(Club.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/clubs/{id}")
    class UpdateClubTests {

        @Test
        @DisplayName("should return 200 OK with updated club")
        void shouldUpdateClubSuccessfully() throws Exception {
            Club input = createSampleClub(null, "Updated Club Name");
            Club updated = createSampleClub(5L, "Updated Club Name");

            when(clubService.updateClub(eq(5L), any(Club.class))).thenReturn(updated);

            mockMvc.perform(put("/api/clubs/{id}", 5L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(5)))
                    .andExpect(jsonPath("$.name", equalTo("Updated Club Name")));

            verify(clubService).updateClub(eq(5L), any(Club.class));
        }
    }

    @Nested
    @DisplayName("POST /api/clubs/{id}/announce")
    class AnnounceClubTests {

        @Test
        @DisplayName("should return 200 OK with default force=false")
        void shouldAnnounceClubWithDefaultForce() throws Exception {
            doNothing().when(clubService).announceClub(1L, false);

            mockMvc.perform(post("/api/clubs/{id}/announce", 1L))
                    .andExpect(status().isOk());

            verify(clubService).announceClub(1L, false);
        }

        @Test
        @DisplayName("should return 200 OK with force=true parameter")
        void shouldAnnounceClubWithForceTrue() throws Exception {
            doNothing().when(clubService).announceClub(1L, true);

            mockMvc.perform(post("/api/clubs/{id}/announce", 1L).param("force", "true"))
                    .andExpect(status().isOk());

            verify(clubService).announceClub(1L, true);
        }

        @Test
        @DisplayName("should return 409 Conflict when announcement was already sent")
        void shouldReturn409WhenAlreadyAnnounced() throws Exception {
            LocalDateTime sentAt = LocalDateTime.of(2026, 10, 1, 12, 0);
            doThrow(new AnnouncementAlreadySentException(sentAt))
                    .when(clubService).announceClub(1L, false);

            mockMvc.perform(post("/api/clubs/{id}/announce", 1L))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code", equalTo("ANNOUNCEMENT_ALREADY_SENT")))
                    .andExpect(jsonPath("$.message", equalTo("This announcement was already sent. Confirm to send it again.")));
        }

        @Test
        @DisplayName("should return 409 Conflict when emails are locked")
        void shouldReturn409WhenEmailsLocked() throws Exception {
            doThrow(new EmailsLockedException())
                    .when(clubService).announceClub(1L, false);

            mockMvc.perform(post("/api/clubs/{id}/announce", 1L))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", equalTo("Emails are locked by the super admin. Unlock them to send emails.")));
        }

        @Test
        @DisplayName("should return 404 Not Found when club not found")
        void shouldReturn404WhenClubNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Club not found"))
                    .when(clubService).announceClub(999L, false);

            mockMvc.perform(post("/api/clubs/{id}/announce", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", equalTo("Club not found")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/clubs/{id}")
    class DeleteClubTests {

        @Test
        @DisplayName("should return 204 No Content when deleted successfully")
        void shouldDeleteClubSuccessfully() throws Exception {
            doNothing().when(clubService).deleteClubById(1L);

            mockMvc.perform(delete("/api/clubs/{id}", 1L))
                    .andExpect(status().isNoContent());

            verify(clubService).deleteClubById(1L);
        }
    }

    @Nested
    @DisplayName("Registration Call Toggles")
    class CallToggleTests {

        @Test
        @DisplayName("POST /api/clubs/{id}/open-call should return 200 OK with updated club")
        void shouldOpenCallSuccessfully() throws Exception {
            Club club = createSampleClub(1L, "Test Club");
            club.setRegistrationOpen(true);
            when(clubService.openRegistration(1L)).thenReturn(club);

            mockMvc.perform(post("/api/clubs/{id}/open-call", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registrationOpen", equalTo(true)));

            verify(clubService).openRegistration(1L);
        }

        @Test
        @DisplayName("POST /api/clubs/{id}/close-call should return 200 OK with updated club")
        void shouldCloseCallSuccessfully() throws Exception {
            Club club = createSampleClub(1L, "Test Club");
            club.setRegistrationOpen(false);
            when(clubService.closeRegistration(1L)).thenReturn(club);

            mockMvc.perform(post("/api/clubs/{id}/close-call", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registrationOpen", equalTo(false)));

            verify(clubService).closeRegistration(1L);
        }
    }

    @Nested
    @DisplayName("GET /api/clubs/{id}/email-settings")
    class EmailSettingsTests {

        @Test
        @DisplayName("should return 200 OK with email settings")
        void shouldReturnEmailSettings() throws Exception {
            LocalDateTime sentAt = LocalDateTime.of(2026, 10, 15, 10, 30);
            EmailSettingsDto settings = new EmailSettingsDto(sentAt);

            when(clubService.getEmailSettings(1L)).thenReturn(settings);

            mockMvc.perform(get("/api/clubs/{id}/email-settings", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.announcementSentAt", equalTo("2026-10-15T10:30:00")));

            verify(clubService).getEmailSettings(1L);
        }
    }

    @Nested
    @DisplayName("Registration Analytics & Sheets Sync")
    class AnalyticsAndSheetTests {

        @Test
        @DisplayName("GET /api/clubs/{id}/registrations/analysis should return 200 OK with analysis")
        void shouldReturnRegistrationAnalysis() throws Exception {
            RegistrationAnalysisDto analysis = RegistrationAnalysisDto.builder()
                    .totalRegistrations(42)
                    .alexUniStudentCount(30)
                    .nonAlexUniStudentCount(12)
                    .build();

            when(clubService.getRegistrationAnalysis(1L)).thenReturn(analysis);

            mockMvc.perform(get("/api/clubs/{id}/registrations/analysis", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations", equalTo(42)))
                    .andExpect(jsonPath("$.alexUniStudentCount", equalTo(30)))
                    .andExpect(jsonPath("$.nonAlexUniStudentCount", equalTo(12)));

            verify(clubService).getRegistrationAnalysis(1L);
        }

        @Test
        @DisplayName("POST /api/clubs/{id}/registrations/sheet should return 200 OK with updated analysis")
        void shouldSyncRegistrationsSheet() throws Exception {
            RegistrationAnalysisDto analysis = RegistrationAnalysisDto.builder()
                    .totalRegistrations(42)
                    .googleSheetUrl("https://docs.google.com/spreadsheets/d/abc")
                    .build();

            when(clubService.syncRegistrationsSheet(1L)).thenReturn(analysis);

            mockMvc.perform(post("/api/clubs/{id}/registrations/sheet", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations", equalTo(42)))
                    .andExpect(jsonPath("$.googleSheetUrl", equalTo("https://docs.google.com/spreadsheets/d/abc")));

            verify(clubService).syncRegistrationsSheet(1L);
        }
    }

    @Nested
    @DisplayName("Question Management Endpoints")
    class QuestionTests {

        @Test
        @DisplayName("POST /api/clubs/{id}/questions should return 201 Created with question")
        void shouldCreateQuestionSuccessfully() throws Exception {
            FormQuestionRequestDto request = FormQuestionRequestDto.builder()
                    .questionText("Why do you want to join?")
                    .questionType("TEXT")
                    .isRequired(true)
                    .build();

            FormQuestionResponseDto response = FormQuestionResponseDto.builder()
                    .id(101L)
                    .questionText("Why do you want to join?")
                    .questionType("TEXT")
                    .isRequired(true)
                    .build();

            when(clubService.createQuestion(eq(1L), any(FormQuestionRequestDto.class))).thenReturn(response);

            mockMvc.perform(post("/api/clubs/{id}/questions", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(101)))
                    .andExpect(jsonPath("$.question_text", equalTo("Why do you want to join?")));

            verify(clubService).createQuestion(eq(1L), any(FormQuestionRequestDto.class));
        }

        @Test
        @DisplayName("PUT /api/clubs/{id}/questions/{questionId} should return 200 OK with updated question")
        void shouldUpdateQuestionSuccessfully() throws Exception {
            FormQuestionRequestDto request = FormQuestionRequestDto.builder()
                    .questionText("Updated Question?")
                    .questionType("SELECT")
                    .options(List.of("A", "B"))
                    .isRequired(false)
                    .build();

            FormQuestionResponseDto response = FormQuestionResponseDto.builder()
                    .id(101L)
                    .questionText("Updated Question?")
                    .questionType("SELECT")
                    .options(List.of("A", "B"))
                    .isRequired(false)
                    .build();

            when(clubService.updateQuestion(eq(1L), eq(101L), any(FormQuestionRequestDto.class))).thenReturn(response);

            mockMvc.perform(put("/api/clubs/{id}/questions/{questionId}", 1L, 101L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(101)))
                    .andExpect(jsonPath("$.question_text", equalTo("Updated Question?")));

            verify(clubService).updateQuestion(eq(1L), eq(101L), any(FormQuestionRequestDto.class));
        }

        @Test
        @DisplayName("DELETE /api/clubs/{id}/questions/{questionId} should return 204 No Content")
        void shouldDeleteQuestionSuccessfully() throws Exception {
            doNothing().when(clubService).deleteQuestion(1L, 101L);

            mockMvc.perform(delete("/api/clubs/{id}/questions/{questionId}", 1L, 101L))
                    .andExpect(status().isNoContent());

            verify(clubService).deleteQuestion(1L, 101L);
        }
    }

    @Nested
    @DisplayName("Social Links Endpoints")
    class SocialLinksTests {

        @Test
        @DisplayName("GET /api/clubs/{id}/social-links should return 200 OK with list of links")
        void shouldGetSocialLinks() throws Exception {
            List<String> links = List.of("https://facebook.com/acm", "https://linkedin.com/acm");
            when(clubService.getClubSocialLinks(1L)).thenReturn(links);

            mockMvc.perform(get("/api/clubs/{id}/social-links", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0]", equalTo("https://facebook.com/acm")));

            verify(clubService).getClubSocialLinks(1L);
        }

        @Test
        @DisplayName("PUT /api/clubs/{id}/social-links should return 200 OK with updated links")
        void shouldUpdateSocialLinks() throws Exception {
            List<String> links = List.of("https://instagram.com/acm");
            when(clubService.updateClubSocialLinks(eq(1L), any())).thenReturn(links);

            mockMvc.perform(put("/api/clubs/{id}/social-links", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(links)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0]", equalTo("https://instagram.com/acm")));

            verify(clubService).updateClubSocialLinks(eq(1L), any());
        }
    }
}
