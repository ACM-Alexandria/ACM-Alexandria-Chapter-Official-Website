package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {
    private final EventRepository eventRepository;
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }
    public List<Event> getAll() {
        return eventRepository.findAll();
    }
    public Optional<Event> getById(Long id) {
        return eventRepository.findById(id);
    }
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }
    public Event updateEvent(Long id,Event updatedEvent) {
        return eventRepository.findById(id).map(event->{
            event.setName(updatedEvent.getName());
            event.setDescription(updatedEvent.getDescription());
            event.setImageUrl(updatedEvent.getImageUrl());
            event.setGoogleFormUrl(updatedEvent.getGoogleFormUrl());
            event.setEventTime(updatedEvent.getEventTime());
            event.setLocation(updatedEvent.getLocation());
                    return eventRepository.save(event);
                }
                ).orElseThrow(()->new RuntimeException("EVENT not found"));

    }

    public void deleteEvent(long id) {
        eventRepository.deleteById(id);
    }

    public Page<Event> getEventsByPage(int pageNumber){
        pageNumber=Math.max(0,pageNumber);
        Pageable page= PageRequest.of(pageNumber,5, Sort.by("eventTime").descending());
        System.out.println(page.getPageNumber());
        System.out.println(page.getPageSize());
        return eventRepository.findAll(page);
    }

}
