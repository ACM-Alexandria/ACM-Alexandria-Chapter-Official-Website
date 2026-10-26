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
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.ProgramController;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.ProgramDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.exception.AnnouncementAlreadySentException;
import com.acm.acmwebsite.feature.exception.EmailsLockedException;
import com.acm.acmwebsite.feature.exception.RegistrationExceptionHandler;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.service.ProgramService;
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

@WebMvcTest(controllers = ProgramController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({RegistrationExceptionHandler.class, GlobalExceptionHandler.class})
@DisplayName("ProgramController WebMvc Tests")
class ProgramControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProgramService programService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    private ProgramDto createSampleProgram(Long id, String name) {
        ProgramDto dto = new ProgramDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription("Program Description");
        dto.setStartDate(LocalDateTime.of(2026, 11, 1, 9, 0));
        dto.setEndDate(LocalDateTime.of(2026, 11, 30, 17, 0));
        dto.setTime("10:00 AM");
        dto.setImageUrl("https://example.com/program.jpg");
        dto.setRegistrationOpen(true);
        return dto;
    }

    @Nested
    @DisplayName("GET /api/program")
    class GetAllProgramsTests {

        @Test
        @DisplayName("should return 200 OK with default page=0 when no param provided")
        void shouldReturnProgramsWithDefaultPage() throws Exception {
            ProgramDto p = createSampleProgram(1L, "Mentorship Program");
            Page<ProgramDto> page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1);

            when(programService.getProgramsByPage(0)).thenReturn(page);

            mockMvc.perform(get("/api/program"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id", equalTo(1)))
                    .andExpect(jsonPath("$.content[0].name", equalTo("Mentorship Program")));

            verify(programService).getProgramsByPage(0);
        }

        @Test
        @DisplayName("should return 200 OK with specified page")
        void shouldReturnProgramsWithSpecifiedPage() throws Exception {
            ProgramDto p = createSampleProgram(2L, "Leadership Program");
            Page<ProgramDto> page = new PageImpl<>(List.of(p), PageRequest.of(2, 10), 1);

            when(programService.getProgramsByPage(2)).thenReturn(page);

            mockMvc.perform(get("/api/program").param("page", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id", equalTo(2)))
                    .andExpect(jsonPath("$.content[0].name", equalTo("Leadership Program")));

            verify(programService).getProgramsByPage(2);
        }
    }

    @Nested
    @DisplayName("GET /api/program/{id}")
    class FindProgramByIdTests {

        @Test
        @DisplayName("should return 200 OK when program found")
        void shouldReturnProgramWhenFound() throws Exception {
            ProgramDto p = createSampleProgram(1L, "Tech Apprenticeship");
            when(programService.getProgramById(1L)).thenReturn(Optional.of(p));

            mockMvc.perform(get("/api/program/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.name", equalTo("Tech Apprenticeship")));

            verify(programService).getProgramById(1L);
        }

        @Test
        @DisplayName("should return 404 Not Found when program does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            when(programService.getProgramById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/program/{id}", 999L))
                    .andExpect(status().isNotFound());

            verify(programService).getProgramById(999L);
        }
    }

    @Nested
    @DisplayName("POST /api/program")
    class CreateProgramTests {

        @Test
        @DisplayName("should return 201 Created with created program")
        void shouldCreateProgramSuccessfully() throws Exception {
            ProgramDto input = createSampleProgram(null, "New Program");
            ProgramDto created = createSampleProgram(5L, "New Program");

            when(programService.createProgram(any(ProgramDto.class))).thenReturn(created);

            mockMvc.perform(post("/api/program")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(5)))
                    .andExpect(jsonPath("$.name", equalTo("New Program")));

            verify(programService).createProgram(any(ProgramDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/program/{id}")
    class UpdateProgramTests {

        @Test
        @DisplayName("should return 200 OK with updated program")
        void shouldUpdateProgramSuccessfully() throws Exception {
            ProgramDto input = createSampleProgram(null, "Updated Program");
            ProgramDto updated = createSampleProgram(5L, "Updated Program");

            when(programService.updateProgram(eq(5L), any(ProgramDto.class))).thenReturn(updated);

            mockMvc.perform(put("/api/program/{id}", 5L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(5)))
                    .andExpect(jsonPath("$.name", equalTo("Updated Program")));

            verify(programService).updateProgram(eq(5L), any(ProgramDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/program/{id}")
    class DeleteProgramTests {

        @Test
        @DisplayName("should return 200 OK when program deleted")
        void shouldDeleteProgramSuccessfully() throws Exception {
            doNothing().when(programService).deleteProgram(1L);

            mockMvc.perform(delete("/api/program/{id}", 1L))
                    .andExpect(status().isOk());

            verify(programService).deleteProgram(1L);
        }
    }

    @Nested
    @DisplayName("POST /api/program/{id}/announce")
    class AnnounceProgramTests {

        @Test
        @DisplayName("should return 200 OK with default force=false")
        void shouldAnnounceWithDefaultForce() throws Exception {
            doNothing().when(programService).announceProgram(1L, false);

            mockMvc.perform(post("/api/program/{id}/announce", 1L))
                    .andExpect(status().isOk());

            verify(programService).announceProgram(1L, false);
        }

        @Test
        @DisplayName("should return 200 OK with force=true")
        void shouldAnnounceWithForceTrue() throws Exception {
            doNothing().when(programService).announceProgram(1L, true);

            mockMvc.perform(post("/api/program/{id}/announce", 1L).param("force", "true"))
                    .andExpect(status().isOk());

            verify(programService).announceProgram(1L, true);
        }

        @Test
        @DisplayName("should return 409 Conflict when already announced")
        void shouldReturn409WhenAlreadyAnnounced() throws Exception {
            LocalDateTime sentAt = LocalDateTime.of(2026, 10, 1, 10, 0);
            doThrow(new AnnouncementAlreadySentException(sentAt))
                    .when(programService).announceProgram(1L, false);

            mockMvc.perform(post("/api/program/{id}/announce", 1L))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code", equalTo("ANNOUNCEMENT_ALREADY_SENT")));
        }

        @Test
        @DisplayName("should return 409 Conflict when emails locked")
        void shouldReturn409WhenEmailsLocked() throws Exception {
            doThrow(new EmailsLockedException())
                    .when(programService).announceProgram(1L, false);

            mockMvc.perform(post("/api/program/{id}/announce", 1L))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", equalTo("Emails are locked by the super admin. Unlock them to send emails.")));
        }

        @Test
        @DisplayName("should return 404 Not Found when program not found")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Program not found"))
                    .when(programService).announceProgram(999L, false);

            mockMvc.perform(post("/api/program/{id}/announce", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", equalTo("Program not found")));
        }
    }

    @Nested
    @DisplayName("POST /api/program/{id}/toggle-registration")
    class ToggleRegistrationTests {

        @Test
        @DisplayName("should return 200 OK when opening registration (open=true)")
        void shouldOpenRegistration() throws Exception {
            ProgramDto updated = createSampleProgram(1L, "Program 1");
            updated.setRegistrationOpen(true);

            when(programService.toggleRegistration(1L, true)).thenReturn(updated);

            mockMvc.perform(post("/api/program/{id}/toggle-registration", 1L).param("open", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registrationOpen", equalTo(true)));

            verify(programService).toggleRegistration(1L, true);
        }

        @Test
        @DisplayName("should return 200 OK when closing registration (open=false)")
        void shouldCloseRegistration() throws Exception {
            ProgramDto updated = createSampleProgram(1L, "Program 1");
            updated.setRegistrationOpen(false);

            when(programService.toggleRegistration(1L, false)).thenReturn(updated);

            mockMvc.perform(post("/api/program/{id}/toggle-registration", 1L).param("open", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registrationOpen", equalTo(false)));

            verify(programService).toggleRegistration(1L, false);
        }
    }

    @Nested
    @DisplayName("Question Management Endpoints")
    class QuestionTests {

        @Test
        @DisplayName("POST /api/program/{id}/questions should return 201 Created")
        void shouldCreateQuestionSuccessfully() throws Exception {
            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("GitHub Profile URL?")
                    .questionType("TEXT")
                    .isRequired(true)
                    .build();

            FormQuestionResponseDto res = FormQuestionResponseDto.builder()
                    .id(301L)
                    .questionText("GitHub Profile URL?")
                    .questionType("TEXT")
                    .isRequired(true)
                    .build();

            when(programService.createQuestion(eq(1L), any(FormQuestionRequestDto.class))).thenReturn(res);

            mockMvc.perform(post("/api/program/{id}/questions", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(301)))
                    .andExpect(jsonPath("$.question_text", equalTo("GitHub Profile URL?")));

            verify(programService).createQuestion(eq(1L), any(FormQuestionRequestDto.class));
        }

        @Test
        @DisplayName("PUT /api/program/{id}/questions/{questionId} should return 200 OK")
        void shouldUpdateQuestionSuccessfully() throws Exception {
            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Updated Question?")
                    .questionType("TEXT")
                    .isRequired(false)
                    .build();

            FormQuestionResponseDto res = FormQuestionResponseDto.builder()
                    .id(301L)
                    .questionText("Updated Question?")
                    .questionType("TEXT")
                    .isRequired(false)
                    .build();

            when(programService.updateQuestion(eq(1L), eq(301L), any(FormQuestionRequestDto.class))).thenReturn(res);

            mockMvc.perform(put("/api/program/{id}/questions/{questionId}", 1L, 301L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(301)))
                    .andExpect(jsonPath("$.question_text", equalTo("Updated Question?")));

            verify(programService).updateQuestion(eq(1L), eq(301L), any(FormQuestionRequestDto.class));
        }

        @Test
        @DisplayName("DELETE /api/program/{id}/questions/{questionId} should return 204 No Content")
        void shouldDeleteQuestionSuccessfully() throws Exception {
            doNothing().when(programService).deleteQuestion(1L, 301L);

            mockMvc.perform(delete("/api/program/{id}/questions/{questionId}", 1L, 301L))
                    .andExpect(status().isNoContent());

            verify(programService).deleteQuestion(1L, 301L);
        }
    }

    @Nested
    @DisplayName("Registration Analytics & Sheets Sync")
    class AnalyticsAndSheetTests {

        @Test
        @DisplayName("GET /api/program/{id}/registrations/analysis should return 200 OK")
        void shouldGetRegistrationAnalysis() throws Exception {
            RegistrationAnalysisDto analysis = RegistrationAnalysisDto.builder()
                    .totalRegistrations(80)
                    .alexUniStudentCount(60)
                    .nonAlexUniStudentCount(20)
                    .build();

            when(programService.getRegistrationAnalysis(1L)).thenReturn(analysis);

            mockMvc.perform(get("/api/program/{id}/registrations/analysis", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations", equalTo(80)))
                    .andExpect(jsonPath("$.alexUniStudentCount", equalTo(60)))
                    .andExpect(jsonPath("$.nonAlexUniStudentCount", equalTo(20)));

            verify(programService).getRegistrationAnalysis(1L);
        }

        @Test
        @DisplayName("POST /api/program/{id}/registrations/sheet should return 200 OK")
        void shouldSyncRegistrationsSheet() throws Exception {
            RegistrationAnalysisDto analysis = RegistrationAnalysisDto.builder()
                    .totalRegistrations(80)
                    .googleSheetUrl("https://docs.google.com/spreadsheets/d/program-sheet")
                    .build();

            when(programService.syncRegistrationsSheet(1L)).thenReturn(analysis);

            mockMvc.perform(post("/api/program/{id}/registrations/sheet", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations", equalTo(80)))
                    .andExpect(jsonPath("$.googleSheetUrl", equalTo("https://docs.google.com/spreadsheets/d/program-sheet")));

            verify(programService).syncRegistrationsSheet(1L);
        }
    }
}
