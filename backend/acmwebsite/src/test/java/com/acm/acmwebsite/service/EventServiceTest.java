package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.feature.dto.EmailSettingsDto;
import com.acm.acmwebsite.feature.dto.EventCardDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.entity.EventFormQuestion;
import com.acm.acmwebsite.feature.entity.EventRegistration;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.exception.AnnouncementAlreadySentException;
import com.acm.acmwebsite.feature.exception.AnnouncementNotAllowedException;
import com.acm.acmwebsite.feature.exception.EmailsLockedException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.EventMapper;
import com.acm.acmwebsite.feature.repository.EventFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.EventRegistrationRepository;
import com.acm.acmwebsite.feature.repository.EventRepository;
import com.acm.acmwebsite.feature.service.EventService;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private EventRegistrationRepository eventRegistrationRepository;

    @Mock
    private EventFormQuestionRepository eventFormQuestionRepository;

    @Mock
    private GoogleSheetsService googleSheetsService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private SystemSettingsService systemSettingsService;

    @InjectMocks
    private EventService eventService;

    // ---------- helper ----------
    private static final LocalDateTime EVENT_TIME = LocalDateTime.of(2026, 5, 25, 10, 0);

    private Event sampleEvent() {
        Event e = new Event();
        e.setId(1L);
        e.setName("Hackathon");
        e.setDescription("Coding event");
        e.setImageUrl("img");
        e.setLocation("Hall A");
        e.setEventTime(EVENT_TIME);
        return e;
    }

    private EventCardDto sampleEventCardDto() {
        return new EventCardDto(1L, "Hackathon", "img", "Coding event", EVENT_TIME, "Hall A");
    }

    @Nested
    @DisplayName("getAllCards Tests")
    class GetAllCardsTests {

        @Test
        @DisplayName("getAllCards should return event cards ordered by eventTime descending")
        void getAllCards_shouldReturnEventCards() {
            Event event = sampleEvent();
            EventCardDto dto = sampleEventCardDto();

            when(eventRepository.findAll(Sort.by("eventTime").descending()))
                    .thenReturn(List.of(event));
            when(eventMapper.toEventCardDto(event)).thenReturn(dto);

            List<EventCardDto> result = eventService.getAllCards();

            assertEquals(1, result.size());
            assertEquals("Hackathon", result.getFirst().getName());
            verify(eventRepository).findAll(Sort.by("eventTime").descending());
        }
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {

        @Test
        @DisplayName("getById found should return Event")
        void getById_found() {
            Event e = sampleEvent();
            when(eventRepository.findById(1L)).thenReturn(Optional.of(e));

            Optional<Event> result = eventService.getById(1L);

            assertTrue(result.isPresent());
            assertEquals("Hackathon", result.get().getName());
        }

        @Test
        @DisplayName("getById not found should return empty Optional")
        void getById_notFound() {
            when(eventRepository.findById(1L)).thenReturn(Optional.empty());

            Optional<Event> result = eventService.getById(1L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("createEvent Tests")
    class CreateEventTests {

        @Test
        @DisplayName("createEvent with null name should throw IllegalArgumentException")
        void createEvent_nullName_shouldThrow() {
            Event e = sampleEvent();
            e.setName(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> eventService.createEvent(e));

            assertEquals("Event name is required", ex.getMessage());
            verify(eventRepository, never()).save(any());
        }

        @Test
        @DisplayName("createEvent with blank name should throw IllegalArgumentException")
        void createEvent_blankName_shouldThrow() {
            Event e = sampleEvent();
            e.setName("   ");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> eventService.createEvent(e));

            assertEquals("Event name is required", ex.getMessage());
            verify(eventRepository, never()).save(any());
        }

        @Test
        @DisplayName("createEvent should save and announce when emails are enabled and event is upcoming")
        void createEvent_shouldSave() {
            Event e = sampleEvent();
            e.setEventTime(LocalDateTime.now().plusDays(7));
            when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
            when(eventRepository.save(e)).thenReturn(e);

            Event saved = eventService.createEvent(e);

            assertEquals("Hackathon", saved.getName());
            assertNotNull(e.getAnnouncementSentAt());
            verify(eventRepository, times(2)).save(e);
            verify(subscriptionService).sendNewEventNotificationToNewsSubscribers(e);
        }

        @Test
        @DisplayName("createEvent with announcement should record sentAt")
        void createEvent_withAnnouncement_shouldRecordSentAt() {
            Event e = sampleEvent();
            e.setEventTime(LocalDateTime.now().plusDays(7));
            when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
            when(eventRepository.save(e)).thenReturn(e);

            eventService.createEvent(e);

            assertNotNull(e.getAnnouncementSentAt());
        }

        @Test
        @DisplayName("createEvent with sendAnnouncement false should skip announcement")
        void createEvent_withSendAnnouncementFalse_shouldSkipAnnouncement() {
            Event e = sampleEvent();
            e.setSendAnnouncement(false);
            when(eventRepository.save(e)).thenReturn(e);

            eventService.createEvent(e);

            verify(eventRepository).save(e);
            assertNull(e.getAnnouncementSentAt());
            verify(subscriptionService, never()).sendNewEventNotificationToNewsSubscribers(any());
        }

        @Test
        @DisplayName("createEvent while emails locked should not record announcementSentAt")
        void createEvent_whileEmailsLocked_shouldNotRecordSentAt() {
            Event e = sampleEvent();
            e.setEventTime(LocalDateTime.now().plusDays(7));
            when(systemSettingsService.isEmailsEnabled()).thenReturn(false);
            when(eventRepository.save(e)).thenReturn(e);

            eventService.createEvent(e);

            assertNull(e.getAnnouncementSentAt());
            verify(subscriptionService, never()).sendNewEventNotificationToNewsSubscribers(any());
        }

        @Test
        @DisplayName("createEvent announcement fails should still save without sentAt")
        void createEvent_announcementFails_shouldStillCreateWithoutSentAt() {
            Event e = sampleEvent();
            e.setEventTime(LocalDateTime.now().plusDays(7));
            when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
            when(eventRepository.save(e)).thenReturn(e);
            doThrow(new RuntimeException("db down")).when(subscriptionService).sendNewEventNotificationToNewsSubscribers(e);

            Event saved = eventService.createEvent(e);

            assertSame(e, saved);
            assertNull(e.getAnnouncementSentAt());
            verify(eventRepository, times(1)).save(e);
        }

        @Test
        @DisplayName("createEvent past event should not announce")
        void createEvent_pastEvent_shouldNotAnnounce() {
            Event e = sampleEvent();
            e.setEventTime(LocalDateTime.now().minusDays(1));
            when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
            when(eventRepository.save(e)).thenReturn(e);

            eventService.createEvent(e);

            assertNull(e.getAnnouncementSentAt());
            verify(subscriptionService, never()).sendNewEventNotificationToNewsSubscribers(any());
        }

        @Test
        @DisplayName("createEvent with null eventTime should not be treated as past event and announce")
        void createEvent_withNullEventTime_shouldAnnounce() {
            Event e = sampleEvent();
            e.setEventTime(null);
            when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
            when(eventRepository.save(e)).thenReturn(e);

            Event saved = eventService.createEvent(e);

            assertNotNull(e.getAnnouncementSentAt());
            verify(subscriptionService).sendNewEventNotificationToNewsSubscribers(e);
        }
    }

    @Nested
    @DisplayName("getEmailSettings Tests")
    class GetEmailSettingsTests {

        @Test
        @DisplayName("getEmailSettings found should return EmailSettingsDto")
        void getEmailSettings_found_shouldReturnDto() {
            Event event = sampleEvent();
            LocalDateTime sentAt = LocalDateTime.of(2026, 4, 10, 11, 0);
            event.setAnnouncementSentAt(sentAt);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            EmailSettingsDto dto = eventService.getEmailSettings(1L);

            assertNotNull(dto);
            assertEquals(sentAt, dto.getAnnouncementSentAt());
        }

        @Test
        @DisplayName("getEmailSettings not found should throw ResourceNotFoundException")
        void getEmailSettings_notFound_shouldThrow() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> eventService.getEmailSettings(99L));
        }
    }

    @Nested
    @DisplayName("announceEvent Tests")
    class AnnounceEventTests {

        @Test
        @DisplayName("announceEvent when emails locked should throw EmailsLockedException")
        void announceEvent_emailsLocked_shouldThrowWithoutNotifying() {
            doThrow(new EmailsLockedException()).when(systemSettingsService).assertEmailsEnabled();

            assertThrows(EmailsLockedException.class, () -> eventService.announceEvent(1L, false));
            verify(subscriptionService, never()).sendNewEventNotificationToNewsSubscribers(any());
        }

        @Test
        @DisplayName("announceEvent not found should throw ResourceNotFoundException")
        void announceEvent_notFound_shouldThrow() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> eventService.announceEvent(99L, false));
        }

        @Test
        @DisplayName("announceEvent past event should refuse with AnnouncementNotAllowedException")
        void announceEvent_pastEvent_shouldRefuse() {
            Event e = sampleEvent();
            e.setEventTime(LocalDateTime.now().minusDays(1));
            when(eventRepository.findById(1L)).thenReturn(Optional.of(e));

            assertThrows(AnnouncementNotAllowedException.class, () -> eventService.announceEvent(1L, false));

            verify(eventRepository, never()).claimAnnouncement(any(), any());
            verify(subscriptionService, never()).sendNewEventNotificationToNewsSubscribers(any());
        }

        @Test
        @DisplayName("announceEvent should notify subscribers on successful claim")
        void announceEvent_shouldNotifySubscribers() {
            Event e = sampleEvent();
            e.setEventTime(LocalDateTime.now().plusDays(7));
            when(eventRepository.findById(1L)).thenReturn(Optional.of(e));
            when(eventRepository.claimAnnouncement(eq(1L), any())).thenReturn(1);

            eventService.announceEvent(1L, false);

            verify(subscriptionService).sendNewEventNotificationToNewsSubscribers(e);
        }

        @Test
        @DisplayName("announceEvent with null eventTime should not be treated as past event")
        void announceEvent_nullEventTime_shouldNotBeTreatedAsPastEvent() {
            Event e = sampleEvent();
            e.setEventTime(null);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(e));
            when(eventRepository.claimAnnouncement(eq(1L), any())).thenReturn(1);

            eventService.announceEvent(1L, false);

            verify(subscriptionService).sendNewEventNotificationToNewsSubscribers(e);
        }

        @Test
        @DisplayName("announceEvent already sent should throw AnnouncementAlreadySentException")
        void announceEvent_alreadySent_shouldRefuseWithoutNotifying() {
            Event e = sampleEvent();
            e.setEventTime(LocalDateTime.now().plusDays(7));
            LocalDateTime sentAt = LocalDateTime.of(2026, 5, 1, 12, 0);
            e.setAnnouncementSentAt(sentAt);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(e));
            when(eventRepository.claimAnnouncement(eq(1L), any())).thenReturn(0);

            AnnouncementAlreadySentException ex = assertThrows(AnnouncementAlreadySentException.class,
                    () -> eventService.announceEvent(1L, false));

            assertEquals(sentAt, ex.getSentAt());
            verify(subscriptionService, never()).sendNewEventNotificationToNewsSubscribers(any());
        }

        @Test
        @DisplayName("announceEvent force resends and updates sentAt")
        void announceEvent_force_shouldResendAndUpdateSentAt() {
            Event e = sampleEvent();
            e.setEventTime(LocalDateTime.now().plusDays(7));
            LocalDateTime oldSent = LocalDateTime.of(2026, 5, 1, 12, 0);
            e.setAnnouncementSentAt(oldSent);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(e));

            eventService.announceEvent(1L, true);

            assertTrue(e.getAnnouncementSentAt().isAfter(oldSent));
            verify(eventRepository).save(e);
            verify(eventRepository, never()).claimAnnouncement(any(), any());
            verify(subscriptionService).sendNewEventNotificationToNewsSubscribers(e);
        }
    }

    @Nested
    @DisplayName("Question Management Tests")
    class QuestionManagementTests {

        @Test
        @DisplayName("createQuestion event not found should throw ResourceNotFoundException")
        void createQuestion_eventNotFound_shouldThrow() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Question?")
                    .questionType("TEXT")
                    .build();

            assertThrows(ResourceNotFoundException.class, () -> eventService.createQuestion(99L, req));
        }

        @Test
        @DisplayName("createQuestion success should save event question and return DTO")
        void createQuestion_success() {
            Event event = sampleEvent();
            FormQuestionRequestDto request = FormQuestionRequestDto.builder()
                    .questionText("Why do you want to attend?")
                    .questionType("text")
                    .isRequired(true)
                    .options(List.of("  ", "ignored"))
                    .build();

            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventFormQuestionRepository.save(any(EventFormQuestion.class))).thenAnswer(invocation -> {
                EventFormQuestion question = invocation.getArgument(0);
                question.setId(20L);
                return question;
            });

            FormQuestionResponseDto result = eventService.createQuestion(1L, request);

            assertEquals(20L, result.getId());
            assertEquals("Why do you want to attend?", result.getQuestionText());
            assertEquals(QuestionType.TEXT.name(), result.getQuestionType());
            assertTrue(result.getIsRequired());
            verify(eventFormQuestionRepository).save(argThat(question ->
                    question.getEvent() == event
                            && question.getQuestionType() == QuestionType.TEXT
                            && question.getOptions().equals(List.of("ignored"))
            ));
        }

        @Test
        @DisplayName("updateQuestion question not found should throw ResourceNotFoundException")
        void updateQuestion_questionNotFound_shouldThrow() {
            when(eventFormQuestionRepository.findById(100L)).thenReturn(Optional.empty());

            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Updated?")
                    .questionType("TEXT")
                    .build();

            assertThrows(ResourceNotFoundException.class, () -> eventService.updateQuestion(1L, 100L, req));
        }

        @Test
        @DisplayName("updateQuestion event mismatch or null event should throw ResourceNotFoundException")
        void updateQuestion_eventMismatch_shouldThrow() {
            Event otherEvent = sampleEvent();
            otherEvent.setId(999L);
            EventFormQuestion existingWithOther = EventFormQuestion.builder()
                    .id(100L)
                    .event(otherEvent)
                    .questionText("Old text")
                    .build();

            when(eventFormQuestionRepository.findById(100L)).thenReturn(Optional.of(existingWithOther));

            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Updated?")
                    .questionType("TEXT")
                    .build();

            assertThrows(ResourceNotFoundException.class, () -> eventService.updateQuestion(1L, 100L, req));

            // Also test null event
            EventFormQuestion existingWithNull = EventFormQuestion.builder()
                    .id(101L)
                    .event(null)
                    .questionText("Old text")
                    .build();
            when(eventFormQuestionRepository.findById(101L)).thenReturn(Optional.of(existingWithNull));

            assertThrows(ResourceNotFoundException.class, () -> eventService.updateQuestion(1L, 101L, req));
        }

        @Test
        @DisplayName("updateQuestion success should update fields and save")
        void updateQuestion_success_shouldUpdateAndSave() {
            Event event = sampleEvent();
            EventFormQuestion existing = EventFormQuestion.builder()
                    .id(100L)
                    .event(event)
                    .questionText("Old text")
                    .questionType(QuestionType.TEXT)
                    .isRequired(false)
                    .build();

            when(eventFormQuestionRepository.findById(100L)).thenReturn(Optional.of(existing));
            when(eventFormQuestionRepository.save(any(EventFormQuestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Updated text?")
                    .questionType("CHECKBOX")
                    .isRequired(true)
                    .options(List.of("Option A", "Option B"))
                    .build();

            FormQuestionResponseDto result = eventService.updateQuestion(1L, 100L, req);

            assertEquals(100L, result.getId());
            assertEquals("Updated text?", result.getQuestionText());
            assertEquals(QuestionType.CHECKBOX.name(), result.getQuestionType());
            assertTrue(result.getIsRequired());
            assertEquals(List.of("Option A", "Option B"), result.getOptions());
            verify(eventFormQuestionRepository).save(existing);
        }

        @Test
        @DisplayName("deleteQuestion question not found should throw ResourceNotFoundException")
        void deleteQuestion_questionNotFound_shouldThrow() {
            when(eventFormQuestionRepository.findById(100L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> eventService.deleteQuestion(1L, 100L));
        }

        @Test
        @DisplayName("deleteQuestion event mismatch or null event should throw ResourceNotFoundException")
        void deleteQuestion_eventMismatch_shouldThrow() {
            Event otherEvent = sampleEvent();
            otherEvent.setId(999L);
            EventFormQuestion existingWithOther = EventFormQuestion.builder()
                    .id(100L)
                    .event(otherEvent)
                    .build();

            when(eventFormQuestionRepository.findById(100L)).thenReturn(Optional.of(existingWithOther));

            assertThrows(ResourceNotFoundException.class, () -> eventService.deleteQuestion(1L, 100L));

            // Also test null event
            EventFormQuestion existingWithNull = EventFormQuestion.builder()
                    .id(101L)
                    .event(null)
                    .build();
            when(eventFormQuestionRepository.findById(101L)).thenReturn(Optional.of(existingWithNull));

            assertThrows(ResourceNotFoundException.class, () -> eventService.deleteQuestion(1L, 101L));
            verify(eventFormQuestionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deleteQuestion success should delete entity")
        void deleteQuestion_success_shouldDelete() {
            Event event = sampleEvent();
            EventFormQuestion existing = EventFormQuestion.builder()
                    .id(100L)
                    .event(event)
                    .build();

            when(eventFormQuestionRepository.findById(100L)).thenReturn(Optional.of(existing));

            eventService.deleteQuestion(1L, 100L);

            verify(eventFormQuestionRepository).delete(existing);
        }
    }

    @Nested
    @DisplayName("updateEvent Tests")
    class UpdateEventTests {

        @Test
        @DisplayName("updateEvent not found should throw RuntimeException")
        void updateEvent_notFound_shouldThrow() {
            when(eventRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> eventService.updateEvent(1L, new Event()));

            assertEquals("EVENT not found", ex.getMessage());
        }

        @Test
        @DisplayName("updateEvent null name should throw IllegalArgumentException")
        void updateEvent_nullName_shouldThrow() {
            Event existing = sampleEvent();
            when(eventRepository.findById(1L)).thenReturn(Optional.of(existing));

            Event updateReq = new Event();
            updateReq.setName(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> eventService.updateEvent(1L, updateReq));

            assertEquals("Event name is required", ex.getMessage());
            verify(eventRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateEvent blank name should throw IllegalArgumentException")
        void updateEvent_blankName_shouldThrow() {
            Event existing = sampleEvent();
            when(eventRepository.findById(1L)).thenReturn(Optional.of(existing));

            Event updateReq = new Event();
            updateReq.setName("   ");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> eventService.updateEvent(1L, updateReq));

            assertEquals("Event name is required", ex.getMessage());
            verify(eventRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateEvent success with existing attached images and non-null updated attached images")
        void updateEvent_success_withAttachedImagesReplacement() {
            Event existing = sampleEvent();
            existing.setAttachedImages(new ArrayList<>(List.of("old1.jpg", "old2.jpg")));

            Event updated = new Event();
            updated.setName("New Name");
            updated.setDescription("New Desc");
            updated.setImageUrl("newImg");
            updated.setLocation("New Hall");
            LocalDateTime newTime = LocalDateTime.now().plusDays(2);
            LocalDateTime newEnd = LocalDateTime.now().plusDays(2).plusHours(3);
            updated.setEventTime(newTime);
            updated.setEndTime(newEnd);
            updated.setRegistrationOpen(true);
            updated.setAttachedImages(List.of("new1.jpg", "new2.jpg"));

            when(eventRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Event result = eventService.updateEvent(1L, updated);

            assertEquals("New Name", result.getName());
            assertEquals("New Desc", result.getDescription());
            assertEquals("New Hall", result.getLocation());
            assertEquals(newTime, result.getEventTime());
            assertEquals(newEnd, result.getEndTime());
            assertTrue(result.getRegistrationOpen());
            assertEquals(List.of("new1.jpg", "new2.jpg"), result.getAttachedImages());
            verify(eventRepository).save(existing);
        }

        @Test
        @DisplayName("updateEvent success when existing attachedImages is non-null but updated is null")
        void updateEvent_success_existingAttachedImages_updatedNull() {
            Event existing = sampleEvent();
            existing.setAttachedImages(new ArrayList<>(List.of("old1.jpg")));

            Event updated = new Event();
            updated.setName("Updated");
            updated.setAttachedImages(null);

            when(eventRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Event result = eventService.updateEvent(1L, updated);

            assertNotNull(result.getAttachedImages());
            assertTrue(result.getAttachedImages().isEmpty());
        }

        @Test
        @DisplayName("updateEvent success when existing attachedImages is null")
        void updateEvent_success_existingAttachedImagesNull() {
            Event existing = sampleEvent();
            existing.setAttachedImages(null);

            Event updated = new Event();
            updated.setName("Updated");
            updated.setAttachedImages(List.of("imgA.jpg"));

            when(eventRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Event result = eventService.updateEvent(1L, updated);

            assertEquals(List.of("imgA.jpg"), result.getAttachedImages());

            // Also test null updated attached images when existing is null
            existing.setAttachedImages(null);
            updated.setAttachedImages(null);
            Event result2 = eventService.updateEvent(1L, updated);
            assertNotNull(result2.getAttachedImages());
            assertTrue(result2.getAttachedImages().isEmpty());
        }
    }

    @Nested
    @DisplayName("openRegistration & closeRegistration Tests")
    class RegistrationStatusTests {

        @Test
        @DisplayName("openRegistration success should set registrationOpen to true")
        void openRegistration_success() {
            Event event = sampleEvent();
            event.setRegistrationOpen(false);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Event result = eventService.openRegistration(1L);

            assertTrue(result.getRegistrationOpen());
            verify(eventRepository).save(event);
        }

        @Test
        @DisplayName("openRegistration not found should throw ResourceNotFoundException")
        void openRegistration_notFound_shouldThrow() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> eventService.openRegistration(99L));
        }

        @Test
        @DisplayName("closeRegistration success should set registrationOpen to false")
        void closeRegistration_success() {
            Event event = sampleEvent();
            event.setRegistrationOpen(true);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Event result = eventService.closeRegistration(1L);

            assertFalse(result.getRegistrationOpen());
            verify(eventRepository).save(event);
        }

        @Test
        @DisplayName("closeRegistration not found should throw ResourceNotFoundException")
        void closeRegistration_notFound_shouldThrow() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> eventService.closeRegistration(99L));
        }
    }

    @Nested
    @DisplayName("deleteEvent Tests")
    class DeleteEventTests {

        @Test
        @DisplayName("deleteEvent should delete cascade references and event")
        void deleteEvent_shouldCallRepository() {
            eventService.deleteEvent(1L);
            verify(eventRegistrationRepository).deleteByEventId(1L);
            verify(eventFormQuestionRepository).deleteByEventId(1L);
            verify(eventRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("getEventsByPage Tests")
    class GetEventsByPageTests {

        @Test
        @DisplayName("getEventsByPage should return paged events")
        void getEventsByPage_shouldReturnPagedEvents() {
            Event e = sampleEvent();
            EventCardDto dto = sampleEventCardDto();
            Page<Event> page = new PageImpl<>(List.of(e));

            when(eventRepository.findAll(any(PageRequest.class))).thenReturn(page);
            when(eventMapper.toEventCardDto(e)).thenReturn(dto);

            Page<EventCardDto> result = eventService.getEventsByPage(0);

            assertEquals(1, result.getContent().size());
            assertEquals("Hackathon", result.getContent().getFirst().getName());
            verify(eventRepository).findAll(any(PageRequest.class));
        }

        @Test
        @DisplayName("getEventsByPage with negative page should clamp to zero")
        void getEventsByPage_negativePage_shouldClampToZero() {
            Page<Event> page = new PageImpl<>(List.of());
            when(eventRepository.findAll(argThat((PageRequest pr) -> pr.getPageNumber() == 0))).thenReturn(page);

            Page<EventCardDto> result = eventService.getEventsByPage(-3);

            assertTrue(result.getContent().isEmpty());
            verify(eventRepository).findAll(argThat((PageRequest pr) -> pr.getPageNumber() == 0));
        }

        @Test
        @DisplayName("getEventsByPage empty page")
        void getEventsByPage_emptyPage() {
            Page<Event> page = new PageImpl<>(List.of());

            when(eventRepository.findAll(any(PageRequest.class))).thenReturn(page);

            Page<EventCardDto> result = eventService.getEventsByPage(0);

            assertTrue(result.getContent().isEmpty());
        }
    }

    @Nested
    @DisplayName("Registration Analysis & Google Sheets Sync Tests")
    class RegistrationAnalysisTests {

        @Test
        @DisplayName("getRegistrationAnalysis not found should throw ResourceNotFoundException")
        void getRegistrationAnalysis_notFound_shouldThrow() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> eventService.getRegistrationAnalysis(99L));
        }

        @Test
        @DisplayName("getRegistrationAnalysis should compute statistics correctly across diverse registrations")
        void getRegistrationAnalysis_success_shouldComputeStats() {
            Event event = sampleEvent();
            event.setGoogleSheetUrl("https://docs.google.com/event-sheet");
            LocalDateTime sheetTime = LocalDateTime.of(2026, 9, 21, 14, 0);
            event.setSheetLastUpdatedAt(sheetTime);

            User userAlexCsed = new User();
            userAlexCsed.setIsAlexEngStudent(true);
            userAlexCsed.setDepartment(Department.CSED);
            userAlexCsed.setBatch("2026");

            User userNonAlexCce = new User();
            userNonAlexCce.setIsAlexEngStudent(false);
            userNonAlexCce.setDepartment(Department.CCE);
            userNonAlexCce.setBatch("2027");

            User userNullDeptAndBatch = new User();
            userNullDeptAndBatch.setIsAlexEngStudent(null);
            userNullDeptAndBatch.setDepartment(null);
            userNullDeptAndBatch.setBatch(null);

            User userBlankBatch = new User();
            userBlankBatch.setIsAlexEngStudent(false);
            userBlankBatch.setDepartment(Department.OTHER);
            userBlankBatch.setBatch("   ");

            EventRegistration reg1 = EventRegistration.builder().id(1L).user(userAlexCsed).event(event).build();
            EventRegistration reg2 = EventRegistration.builder().id(2L).user(userNonAlexCce).event(event).build();
            EventRegistration reg3 = EventRegistration.builder().id(3L).user(userNullDeptAndBatch).event(event).build();
            EventRegistration reg4 = EventRegistration.builder().id(4L).user(userBlankBatch).event(event).build();
            EventRegistration reg5 = EventRegistration.builder().id(5L).user(null).event(event).build();

            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRegistrationRepository.findByEventId(1L))
                    .thenReturn(List.of(reg1, reg2, reg3, reg4, reg5));

            RegistrationAnalysisDto analysis = eventService.getRegistrationAnalysis(1L);

            assertEquals(5, analysis.getTotalRegistrations());
            assertEquals(1L, analysis.getAlexUniStudentCount());
            assertEquals(4L, analysis.getNonAlexUniStudentCount());

            assertEquals(1L, analysis.getDepartmentCounts().get("CSED"));
            assertEquals(1L, analysis.getDepartmentCounts().get("CCE"));
            assertEquals(1L, analysis.getDepartmentCounts().get("OTHER"));
            assertEquals(1L, analysis.getDepartmentCounts().get("N/A"));

            assertEquals(1L, analysis.getBatchCounts().get("2026"));
            assertEquals(1L, analysis.getBatchCounts().get("2027"));
            assertEquals(2L, analysis.getBatchCounts().get("N/A"));

            assertEquals("https://docs.google.com/event-sheet", analysis.getGoogleSheetUrl());
            assertEquals(sheetTime, analysis.getSheetLastUpdatedAt());
        }

        @Test
        @DisplayName("syncRegistrationsSheet not found should throw ResourceNotFoundException")
        void syncRegistrationsSheet_notFound_shouldThrow() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> eventService.syncRegistrationsSheet(99L));
        }

        @Test
        @DisplayName("syncRegistrationsSheet should invoke GoogleSheetsService and update sheet metadata")
        void syncRegistrationsSheet_success_shouldSync() {
            Event event = sampleEvent();
            event.setGoogleSheetUrl("https://docs.google.com/old-event-url");

            EventRegistration reg = EventRegistration.builder().id(1L).event(event).build();
            EventFormQuestion q = EventFormQuestion.builder().id(10L).event(event).questionText("Question?").build();

            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of(reg));
            when(eventFormQuestionRepository.findByEventId(1L)).thenReturn(List.of(q));
            when(googleSheetsService.syncRegistrationData(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn("https://docs.google.com/new-event-url");
            when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RegistrationAnalysisDto result = eventService.syncRegistrationsSheet(1L);

            assertNotNull(result);
            assertEquals("https://docs.google.com/new-event-url", event.getGoogleSheetUrl());
            assertNotNull(event.getSheetLastUpdatedAt());
            verify(eventRepository).save(event);
            verify(googleSheetsService).syncRegistrationData(
                    eq("ACM Alexandria - Event: Hackathon - Registrations"),
                    any(),
                    eq("https://docs.google.com/old-event-url"),
                    eq(Arrays.asList("Event Name:", "Hackathon", "Event Time:", EVENT_TIME.toString())),
                    eq(List.of(reg)),
                    eq(List.of(q)),
                    any(), any(), any(), any(), any(), any()
            );

            // Also test branch where eventTime is null (prefix header has "N/A")
            event.setEventTime(null);
            eventService.syncRegistrationsSheet(1L);
            verify(googleSheetsService).syncRegistrationData(
                    eq("ACM Alexandria - Event: Hackathon - Registrations"),
                    any(),
                    eq("https://docs.google.com/new-event-url"),
                    eq(Arrays.asList("Event Name:", "Hackathon", "Event Time:", "N/A")),
                    eq(List.of(reg)),
                    eq(List.of(q)),
                    any(), any(), any(), any(), any(), any()
            );
        }
    }
}
