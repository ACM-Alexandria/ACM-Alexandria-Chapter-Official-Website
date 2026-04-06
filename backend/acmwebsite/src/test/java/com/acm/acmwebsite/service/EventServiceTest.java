package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.repository.EventRepository;
import com.acm.acmwebsite.feature.service.EventService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    // ---------- helper ----------
    private Event sampleEvent() {
        Event e = new Event();
        e.setId(1L);
        e.setName("Hackathon");
        e.setDescription("Coding event");
        e.setImageUrl("img");
        e.setGoogleFormUrl("form");
        e.setLocation("Hall A");
        e.setEventTime(LocalDateTime.now());
        return e;
    }



    @Test
    void getAll_shouldReturnEvents() {
        when(eventRepository.findAll())
                .thenReturn(List.of(sampleEvent()));

        List<Event> result = eventService.getAll();

        assertEquals(1, result.size());
        verify(eventRepository).findAll();
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
        when(eventRepository.save(e)).thenReturn(e);

        Event saved = eventService.createEvent(e);

        assertEquals("Hackathon", saved.getName());
        verify(eventRepository).save(e);
    }



    @Test
    void updateEvent_success() {
        Event existing = sampleEvent();

        Event updated = new Event();
        updated.setName("New Name");
        updated.setDescription("New Desc");
        updated.setImageUrl("newImg");
        updated.setGoogleFormUrl("newForm");
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
                () -> eventService.updateEvent(1L, new Event())
        );

        assertEquals("EVENT not found", ex.getMessage());
    }



    @Test
    void deleteEvent_shouldCallRepository() {
        eventService.deleteEvent(1L);
        verify(eventRepository).deleteById(1L);
    }

    @Test
    void getEventsByPage_shouldReturnPagedEvents() {
        Event e = sampleEvent();

        Page<Event> page = new PageImpl<>(List.of(e));

        when(eventRepository.findAll(any(PageRequest.class)))
                .thenReturn(page);

        Page<Event> result = eventService.getEventsByPage(0);

        assertEquals(1, result.getContent().size());
        verify(eventRepository).findAll(any(PageRequest.class));
    }

    @Test
    void getEventsByPage_emptyPage() {
        Page<Event> page = new PageImpl<>(List.of());

        when(eventRepository.findAll(any(PageRequest.class)))
                .thenReturn(page);

        Page<Event> result = eventService.getEventsByPage(0);

        assertTrue(result.getContent().isEmpty());
    }


}

