package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.exception.AnnouncementAlreadySentException;
import com.acm.acmwebsite.feature.exception.AnnouncementNotAllowedException;
import com.acm.acmwebsite.feature.exception.EmailsLockedException;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import com.acm.acmwebsite.feature.dto.EventCardDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.entity.EventFormQuestion;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.mapper.EventMapper;
import com.acm.acmwebsite.feature.repository.EventRepository;
import com.acm.acmwebsite.feature.repository.EventRegistrationRepository;
import com.acm.acmwebsite.feature.repository.EventFormQuestionRepository;
import com.acm.acmwebsite.feature.service.EventService;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void getAllCards_shouldReturnEventCards() {
        Event event = sampleEvent();
        EventCardDto dto = sampleEventCardDto();

        when(eventRepository.findAll(org.springframework.data.domain.Sort.by("eventTime").descending()))
                .thenReturn(List.of(event));
        when(eventMapper.toEventCardDto(event)).thenReturn(dto);

        List<EventCardDto> result = eventService.getAllCards();

        assertEquals(1, result.size());
        assertEquals("Hackathon", result.get(0).getName());
        verify(eventRepository).findAll(org.springframework.data.domain.Sort.by("eventTime").descending());
    }

    @Test
    void getById_found() {
        Event e = sampleEvent();
        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(e));

        Optional<Event> result = eventService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals("Hackathon", result.get().getName());
    }

    @Test
    void getById_notFound() {
        when(eventRepository.findById(1L))
                .thenReturn(Optional.empty());

        Optional<Event> result = eventService.getById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void createEvent_shouldSave() {
        Event e = sampleEvent();
        e.setEventTime(LocalDateTime.now().plusDays(7));
        when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
        when(eventRepository.save(e)).thenReturn(e);

        Event saved = eventService.createEvent(e);

        assertEquals("Hackathon", saved.getName());
        // Saved once on create, then again to record the announcement time
        verify(eventRepository, times(2)).save(e);
        verify(subscriptionService).sendNewEventNotificationToNewsSubscribers(e);
    }

    @Test
    void createQuestion_shouldSaveEventQuestion() {
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
    void updateEvent_success() {
        Event existing = sampleEvent();

        Event updated = new Event();
        updated.setName("New Name");
        updated.setDescription("New Desc");
        updated.setImageUrl("newImg");
        updated.setLocation("New Hall");
        updated.setEventTime(LocalDateTime.now().plusDays(1));

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(eventRepository.save(any(Event.class)))
                .thenReturn(existing);

        Event result = eventService.updateEvent(1L, updated);

        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
        assertEquals("New Hall", result.getLocation());

        verify(eventRepository).save(existing);
    }

    @Test
    void updateEvent_notFound_shouldThrow() {
        when(eventRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> eventService.updateEvent(1L, new Event()));

        assertEquals("EVENT not found", ex.getMessage());
    }

    @Test
    void deleteEvent_shouldCallRepository() {
        eventService.deleteEvent(1L);
        verify(eventRegistrationRepository).deleteByEventId(1L);
        verify(eventFormQuestionRepository).deleteByEventId(1L);
        verify(eventRepository).deleteById(1L);
    }

    @Test
    void getEventsByPage_shouldReturnPagedEvents() {
        Event e = sampleEvent();
        EventCardDto dto = sampleEventCardDto();

        Page<Event> page = new PageImpl<>(List.of(e));

        when(eventRepository.findAll(any(PageRequest.class)))
                .thenReturn(page);
        when(eventMapper.toEventCardDto(e)).thenReturn(dto);

        Page<EventCardDto> result = eventService.getEventsByPage(0);

        assertEquals(1, result.getContent().size());
        assertEquals("Hackathon", result.getContent().get(0).getName());
        verify(eventRepository).findAll(any(PageRequest.class));
    }

    @Test
    void getEventsByPage_emptyPage() {
        Page<Event> page = new PageImpl<>(List.of());

        when(eventRepository.findAll(any(PageRequest.class)))
                .thenReturn(page);

        Page<EventCardDto> result = eventService.getEventsByPage(0);

        assertTrue(result.getContent().isEmpty());
    }


    @Test
    void createEvent_withSendAnnouncementFalse_shouldSkipAnnouncement() {
        Event e = sampleEvent();
        e.setSendAnnouncement(false);
        when(eventRepository.save(e)).thenReturn(e);

        eventService.createEvent(e);

        verify(eventRepository).save(e);
        verify(subscriptionService, never()).sendNewEventNotificationToNewsSubscribers(any());
    }

    @Test
    void announceEvent_shouldNotifySubscribers() {
        Event e = sampleEvent();
        e.setEventTime(LocalDateTime.now().plusDays(7));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(e));
        when(eventRepository.claimAnnouncement(eq(1L), any())).thenReturn(1);

        eventService.announceEvent(1L, false);

        verify(subscriptionService).sendNewEventNotificationToNewsSubscribers(e);
    }

    @Test
    void announceEvent_emailsLocked_shouldThrowWithoutNotifying() {
        doThrow(new EmailsLockedException()).when(systemSettingsService).assertEmailsEnabled();

        assertThrows(EmailsLockedException.class, () -> eventService.announceEvent(1L, false));

        verify(subscriptionService, never()).sendNewEventNotificationToNewsSubscribers(any());
    }

    @Test
    void announceEvent_alreadySent_shouldRefuseWithoutNotifying() {
        Event e = sampleEvent();
        e.setEventTime(LocalDateTime.now().plusDays(7));
        e.setAnnouncementSentAt(LocalDateTime.of(2026, 5, 1, 12, 0));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(e));
        when(eventRepository.claimAnnouncement(eq(1L), any())).thenReturn(0);

        AnnouncementAlreadySentException ex = assertThrows(AnnouncementAlreadySentException.class,
                () -> eventService.announceEvent(1L, false));

        assertEquals(LocalDateTime.of(2026, 5, 1, 12, 0), ex.getSentAt());
        verify(subscriptionService, never()).sendNewEventNotificationToNewsSubscribers(any());
    }

    @Test
    void announceEvent_force_shouldResendAndUpdateSentAt() {
        Event e = sampleEvent();
        e.setEventTime(LocalDateTime.now().plusDays(7));
        e.setAnnouncementSentAt(LocalDateTime.of(2026, 5, 1, 12, 0));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(e));

        eventService.announceEvent(1L, true);

        assertTrue(e.getAnnouncementSentAt().isAfter(LocalDateTime.of(2026, 5, 1, 12, 0)));
        verify(eventRepository).save(e);
        verify(eventRepository, never()).claimAnnouncement(any(), any());
        verify(subscriptionService).sendNewEventNotificationToNewsSubscribers(e);
    }

    @Test
    void announceEvent_pastEvent_shouldRefuse() {
        Event e = sampleEvent();
        e.setEventTime(LocalDateTime.now().minusDays(1));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(AnnouncementNotAllowedException.class, () -> eventService.announceEvent(1L, false));

        verify(eventRepository, never()).claimAnnouncement(any(), any());
        verify(subscriptionService, never()).sendNewEventNotificationToNewsSubscribers(any());
    }

    @Test
    void createEvent_withAnnouncement_shouldRecordSentAt() {
        Event e = sampleEvent();
        e.setEventTime(LocalDateTime.now().plusDays(7));
        when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
        when(eventRepository.save(e)).thenReturn(e);

        eventService.createEvent(e);

        assertNotNull(e.getAnnouncementSentAt());
    }

    @Test
    void createEvent_whileEmailsLocked_shouldNotRecordSentAt() {
        Event e = sampleEvent();
        e.setEventTime(LocalDateTime.now().plusDays(7));
        when(systemSettingsService.isEmailsEnabled()).thenReturn(false);
        when(eventRepository.save(e)).thenReturn(e);

        eventService.createEvent(e);

        // The announcement was dropped, so it must still be sendable later
        assertNull(e.getAnnouncementSentAt());
        verify(subscriptionService, never()).sendNewEventNotificationToNewsSubscribers(any());
    }

    @Test
    void createEvent_announcementFails_shouldStillCreateWithoutSentAt() {
        Event e = sampleEvent();
        e.setEventTime(LocalDateTime.now().plusDays(7));
        when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
        when(eventRepository.save(e)).thenReturn(e);
        doThrow(new RuntimeException("db down")).when(subscriptionService).sendNewEventNotificationToNewsSubscribers(e);

        Event saved = eventService.createEvent(e);

        // The event exists, but stays sendable because the announcement never went out
        assertSame(e, saved);
        assertNull(e.getAnnouncementSentAt());
        verify(eventRepository, times(1)).save(e);
    }

    @Test
    void createEvent_pastEvent_shouldNotAnnounce() {
        Event e = sampleEvent();
        e.setEventTime(LocalDateTime.now().minusDays(1));
        when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
        when(eventRepository.save(e)).thenReturn(e);

        eventService.createEvent(e);

        assertNull(e.getAnnouncementSentAt());
        verify(subscriptionService, never()).sendNewEventNotificationToNewsSubscribers(any());
    }
}
