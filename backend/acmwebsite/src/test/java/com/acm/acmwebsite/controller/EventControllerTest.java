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
import com.acm.acmwebsite.feature.controller.EventController;
import com.acm.acmwebsite.feature.dto.EmailSettingsDto;
import com.acm.acmwebsite.feature.dto.EventCardDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.exception.AnnouncementAlreadySentException;
import com.acm.acmwebsite.feature.exception.AnnouncementNotAllowedException;
import com.acm.acmwebsite.feature.exception.EmailsLockedException;
import com.acm.acmwebsite.feature.exception.RegistrationExceptionHandler;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.service.EventService;
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

@WebMvcTest(controllers = EventController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({RegistrationExceptionHandler.class, GlobalExceptionHandler.class})
@DisplayName("EventController WebMvc Tests")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    private Event createSampleEvent(Long id, String name) {
        Event event = new Event();
        if (id != null) {
            event.setId(id);
        }
        event.setName(name);
        event.setDescription("Event Description");
        event.setEventTime(LocalDateTime.of(2026, 11, 20, 18, 0));
        event.setLocation("Auditorium Hall");
        event.setImageUrl("https://example.com/event.jpg");
        event.setRegistrationOpen(true);
        return event;
    }

    @Nested
    @DisplayName("GET /api/events")
    class GetAllEventsTests {

        @Test
        @DisplayName("should return 200 OK with default page=0 when no param provided")
        void shouldReturnEventsWithDefaultPage() throws Exception {
            EventCardDto card = new EventCardDto(1L, "Hackathon", "https://example.com/h.jpg", "Hackathon Desc",
                    LocalDateTime.of(2026, 11, 20, 18, 0), "Lab 1", true);
            Page<EventCardDto> page = new PageImpl<>(List.of(card), PageRequest.of(0, 10), 1);

            when(eventService.getEventsByPage(0)).thenReturn(page);

            mockMvc.perform(get("/api/events"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id", equalTo(1)))
                    .andExpect(jsonPath("$.content[0].name", equalTo("Hackathon")));

            verify(eventService).getEventsByPage(0);
        }

        @Test
        @DisplayName("should return 200 OK with specified page")
        void shouldReturnEventsWithSpecifiedPage() throws Exception {
            EventCardDto card = new EventCardDto(2L, "Workshop", "https://example.com/w.jpg", "Workshop Desc",
                    LocalDateTime.of(2026, 12, 1, 10, 0), "Hall B", false);
            Page<EventCardDto> page = new PageImpl<>(List.of(card), PageRequest.of(2, 10), 1);

            when(eventService.getEventsByPage(2)).thenReturn(page);

            mockMvc.perform(get("/api/events").param("page", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id", equalTo(2)))
                    .andExpect(jsonPath("$.content[0].name", equalTo("Workshop")));

            verify(eventService).getEventsByPage(2);
        }
    }

    @Nested
    @DisplayName("GET /api/events/{id}")
    class GetEventByIdTests {

        @Test
        @DisplayName("should return 200 OK when event is found")
        void shouldReturnEventWhenFound() throws Exception {
            Event event = createSampleEvent(1L, "Annual Conference");
            when(eventService.getById(1L)).thenReturn(Optional.of(event));

            mockMvc.perform(get("/api/events/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(1)))
                    .andExpect(jsonPath("$.name", equalTo("Annual Conference")));

            verify(eventService).getById(1L);
        }

        @Test
        @DisplayName("should return 404 Not Found when event does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            when(eventService.getById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/events/{id}", 999L))
                    .andExpect(status().isNotFound());

            verify(eventService).getById(999L);
        }
    }

    @Nested
    @DisplayName("POST /api/events")
    class CreateEventTests {

        @Test
        @DisplayName("should return 200 OK with created event")
        void shouldCreateEventSuccessfully() throws Exception {
            Event input = createSampleEvent(null, "Tech Talk");
            Event saved = createSampleEvent(5L, "Tech Talk");

            when(eventService.createEvent(any(Event.class))).thenReturn(saved);

            mockMvc.perform(post("/api/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(5)))
                    .andExpect(jsonPath("$.name", equalTo("Tech Talk")));

            verify(eventService).createEvent(any(Event.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/events/{id}")
    class UpdateEventTests {

        @Test
        @DisplayName("should return 200 OK with updated event")
        void shouldUpdateEventSuccessfully() throws Exception {
            Event input = createSampleEvent(null, "Updated Tech Talk");
            Event updated = createSampleEvent(5L, "Updated Tech Talk");

            when(eventService.updateEvent(eq(5L), any(Event.class))).thenReturn(updated);

            mockMvc.perform(put("/api/events/{id}", 5L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(5)))
                    .andExpect(jsonPath("$.name", equalTo("Updated Tech Talk")));

            verify(eventService).updateEvent(eq(5L), any(Event.class));
        }
    }

    @Nested
    @DisplayName("POST /api/events/{id}/announce")
    class AnnounceEventTests {

        @Test
        @DisplayName("should return 200 OK with default force=false")
        void shouldAnnounceEventWithDefaultForce() throws Exception {
            doNothing().when(eventService).announceEvent(1L, false);

            mockMvc.perform(post("/api/events/{id}/announce", 1L))
                    .andExpect(status().isOk());

            verify(eventService).announceEvent(1L, false);
        }

        @Test
        @DisplayName("should return 200 OK with force=true")
        void shouldAnnounceEventWithForceTrue() throws Exception {
            doNothing().when(eventService).announceEvent(1L, true);

            mockMvc.perform(post("/api/events/{id}/announce", 1L).param("force", "true"))
                    .andExpect(status().isOk());

            verify(eventService).announceEvent(1L, true);
        }

        @Test
        @DisplayName("should return 409 Conflict when already announced")
        void shouldReturn409WhenAlreadyAnnounced() throws Exception {
            LocalDateTime sentAt = LocalDateTime.of(2026, 10, 1, 14, 0);
            doThrow(new AnnouncementAlreadySentException(sentAt))
                    .when(eventService).announceEvent(1L, false);

            mockMvc.perform(post("/api/events/{id}/announce", 1L))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code", equalTo("ANNOUNCEMENT_ALREADY_SENT")));
        }

        @Test
        @DisplayName("should return 400 Bad Request when announcement not allowed")
        void shouldReturn400WhenAnnouncementNotAllowed() throws Exception {
            doThrow(new AnnouncementNotAllowedException("Cannot announce past event"))
                    .when(eventService).announceEvent(1L, false);

            mockMvc.perform(post("/api/events/{id}/announce", 1L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Cannot announce past event")));
        }

        @Test
        @DisplayName("should return 409 Conflict when emails locked")
        void shouldReturn409WhenEmailsLocked() throws Exception {
            doThrow(new EmailsLockedException())
                    .when(eventService).announceEvent(1L, false);

            mockMvc.perform(post("/api/events/{id}/announce", 1L))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", equalTo("Emails are locked by the super admin. Unlock them to send emails.")));
        }

        @Test
        @DisplayName("should return 404 Not Found when event not found")
        void shouldReturn404WhenEventNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Event not found"))
                    .when(eventService).announceEvent(999L, false);

            mockMvc.perform(post("/api/events/{id}/announce", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", equalTo("Event not found")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/events/{id}")
    class DeleteEventTests {

        @Test
        @DisplayName("should return 204 No Content when event deleted")
        void shouldDeleteEventSuccessfully() throws Exception {
            doNothing().when(eventService).deleteEvent(1L);

            mockMvc.perform(delete("/api/events/{id}", 1L))
                    .andExpect(status().isNoContent());

            verify(eventService).deleteEvent(1L);
        }
    }

    @Nested
    @DisplayName("Registration Call Toggles")
    class CallToggleTests {

        @Test
        @DisplayName("POST /api/events/{id}/open-call should return 200 OK")
        void shouldOpenCallSuccessfully() throws Exception {
            Event event = createSampleEvent(1L, "Event 1");
            event.setRegistrationOpen(true);
            when(eventService.openRegistration(1L)).thenReturn(event);

            mockMvc.perform(post("/api/events/{id}/open-call", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registrationOpen", equalTo(true)));

            verify(eventService).openRegistration(1L);
        }

        @Test
        @DisplayName("POST /api/events/{id}/close-call should return 200 OK")
        void shouldCloseCallSuccessfully() throws Exception {
            Event event = createSampleEvent(1L, "Event 1");
            event.setRegistrationOpen(false);
            when(eventService.closeRegistration(1L)).thenReturn(event);

            mockMvc.perform(post("/api/events/{id}/close-call", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registrationOpen", equalTo(false)));

            verify(eventService).closeRegistration(1L);
        }
    }

    @Nested
    @DisplayName("GET /api/events/{id}/email-settings")
    class EmailSettingsTests {

        @Test
        @DisplayName("should return 200 OK with email settings")
        void shouldReturnEmailSettings() throws Exception {
            LocalDateTime sentAt = LocalDateTime.of(2026, 10, 16, 9, 15, 0);
            EmailSettingsDto settings = new EmailSettingsDto(sentAt);

            when(eventService.getEmailSettings(1L)).thenReturn(settings);

            mockMvc.perform(get("/api/events/{id}/email-settings", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.announcementSentAt", equalTo("2026-10-16T09:15:00")));

            verify(eventService).getEmailSettings(1L);
        }
    }

    @Nested
    @DisplayName("Registration Analytics & Sheets Sync")
    class AnalyticsAndSheetTests {

        @Test
        @DisplayName("GET /api/events/{id}/registrations/analysis should return 200 OK with analysis")
        void shouldReturnRegistrationAnalysis() throws Exception {
            RegistrationAnalysisDto analysis = RegistrationAnalysisDto.builder()
                    .totalRegistrations(150)
                    .alexUniStudentCount(120)
                    .nonAlexUniStudentCount(30)
                    .build();

            when(eventService.getRegistrationAnalysis(1L)).thenReturn(analysis);

            mockMvc.perform(get("/api/events/{id}/registrations/analysis", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations", equalTo(150)))
                    .andExpect(jsonPath("$.alexUniStudentCount", equalTo(120)))
                    .andExpect(jsonPath("$.nonAlexUniStudentCount", equalTo(30)));

            verify(eventService).getRegistrationAnalysis(1L);
        }

        @Test
        @DisplayName("POST /api/events/{id}/registrations/sheet should return 200 OK with updated analysis")
        void shouldSyncRegistrationsSheet() throws Exception {
            RegistrationAnalysisDto analysis = RegistrationAnalysisDto.builder()
                    .totalRegistrations(150)
                    .googleSheetUrl("https://docs.google.com/spreadsheets/d/event-sheet")
                    .build();

            when(eventService.syncRegistrationsSheet(1L)).thenReturn(analysis);

            mockMvc.perform(post("/api/events/{id}/registrations/sheet", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations", equalTo(150)))
                    .andExpect(jsonPath("$.googleSheetUrl", equalTo("https://docs.google.com/spreadsheets/d/event-sheet")));

            verify(eventService).syncRegistrationsSheet(1L);
        }
    }

    @Nested
    @DisplayName("Question Management Endpoints")
    class QuestionTests {

        @Test
        @DisplayName("POST /api/events/{id}/questions should return 201 Created")
        void shouldCreateQuestionSuccessfully() throws Exception {
            FormQuestionRequestDto request = FormQuestionRequestDto.builder()
                    .questionText("Do you need transportation?")
                    .questionType("TEXT")
                    .isRequired(true)
                    .build();

            FormQuestionResponseDto response = FormQuestionResponseDto.builder()
                    .id(201L)
                    .questionText("Do you need transportation?")
                    .questionType("TEXT")
                    .isRequired(true)
                    .build();

            when(eventService.createQuestion(eq(1L), any(FormQuestionRequestDto.class))).thenReturn(response);

            mockMvc.perform(post("/api/events/{id}/questions", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(201)))
                    .andExpect(jsonPath("$.question_text", equalTo("Do you need transportation?")));

            verify(eventService).createQuestion(eq(1L), any(FormQuestionRequestDto.class));
        }

        @Test
        @DisplayName("PUT /api/events/{id}/questions/{questionId} should return 200 OK")
        void shouldUpdateQuestionSuccessfully() throws Exception {
            FormQuestionRequestDto request = FormQuestionRequestDto.builder()
                    .questionText("Updated Question?")
                    .questionType("RADIO")
                    .options(List.of("Yes", "No"))
                    .isRequired(false)
                    .build();

            FormQuestionResponseDto response = FormQuestionResponseDto.builder()
                    .id(201L)
                    .questionText("Updated Question?")
                    .questionType("RADIO")
                    .options(List.of("Yes", "No"))
                    .isRequired(false)
                    .build();

            when(eventService.updateQuestion(eq(1L), eq(201L), any(FormQuestionRequestDto.class))).thenReturn(response);

            mockMvc.perform(put("/api/events/{id}/questions/{questionId}", 1L, 201L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(201)))
                    .andExpect(jsonPath("$.question_text", equalTo("Updated Question?")));

            verify(eventService).updateQuestion(eq(1L), eq(201L), any(FormQuestionRequestDto.class));
        }

        @Test
        @DisplayName("DELETE /api/events/{id}/questions/{questionId} should return 204 No Content")
        void shouldDeleteQuestionSuccessfully() throws Exception {
            doNothing().when(eventService).deleteQuestion(1L, 201L);

            mockMvc.perform(delete("/api/events/{id}/questions/{questionId}", 1L, 201L))
                    .andExpect(status().isNoContent());

            verify(eventService).deleteQuestion(1L, 201L);
        }
    }
}
