package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.EventCardDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {
  private final EventService eventService;

  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  @GetMapping
  public ResponseEntity<Page<EventCardDto>> getAllEvents(@RequestParam(defaultValue = "0") int page) {
    return ResponseEntity.ok(eventService.getEventsByPage(page));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Event> getEventById(@PathVariable Long id) {
    return eventService.getById(id).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public Event createEvent(@RequestBody Event event) {
    return eventService.createEvent(event);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Event updateEvent(@PathVariable Long id, @RequestBody Event event) {
    return eventService.updateEvent(id, event);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
    eventService.deleteEvent(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/registrations/analysis")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<RegistrationAnalysisDto> getEventRegistrationAnalysis(@PathVariable Long id) {
    return ResponseEntity.ok(eventService.getRegistrationAnalysis(id));
  }

  @PostMapping("/{id}/registrations/sheet")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<RegistrationAnalysisDto> syncEventRegistrationsSheet(@PathVariable Long id) {
    return ResponseEntity.ok(eventService.syncRegistrationsSheet(id));
  }

  @PostMapping("/{id}/questions")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<FormQuestionResponseDto> createEventQuestion(
      @PathVariable("id") Long eventId,
      @RequestBody FormQuestionRequestDto request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createQuestion(eventId, request));
  }

  @PutMapping("/{id}/questions/{questionId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<FormQuestionResponseDto> updateEventQuestion(
      @PathVariable("id") Long eventId,
      @PathVariable Long questionId,
      @RequestBody FormQuestionRequestDto request) {
    return ResponseEntity.ok(eventService.updateQuestion(eventId, questionId, request));
  }

  @DeleteMapping("/{id}/questions/{questionId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteEventQuestion(
      @PathVariable("id") Long eventId,
      @PathVariable Long questionId) {
    eventService.deleteQuestion(eventId, questionId);
    return ResponseEntity.noContent().build();
  }
}
