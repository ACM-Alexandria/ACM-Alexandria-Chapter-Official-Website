package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.EventCardDto;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {
  private final EventService eventService;

  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  @GetMapping
  public ResponseEntity<Page<EventCardDto>> getAllEvents(@RequestParam int page) {
    return ResponseEntity.ok(eventService.getEventsByPage(page));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Event> getEventById(@PathVariable Long id) {
    return eventService.getById(id).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public Event createEvent(@RequestBody Event event) {
    return eventService.createEvent(event);
  }

  @PutMapping("/{id}")
  public Event updateEvent(@PathVariable Long id, @RequestBody Event event) {
    return eventService.updateEvent(id, event);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
    eventService.deleteEvent(id);
    return ResponseEntity.noContent().build();
  }

}
