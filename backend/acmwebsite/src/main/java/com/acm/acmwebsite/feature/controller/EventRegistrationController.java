package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.service.EventRegistrationService;
import com.acm.acmwebsite.feature.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;
    private final EventService eventService;

    @GetMapping("/events/{id}/questions")
    public ResponseEntity<List<FormQuestionResponseDto>> getEventQuestions(@PathVariable("id") Long eventId) {
        List<FormQuestionResponseDto> questions = eventRegistrationService.getQuestions(eventId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/events/{id}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormQuestionResponseDto> createEventQuestion(
            @PathVariable("id") Long eventId,
            @RequestBody FormQuestionRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createQuestion(eventId, request));
    }

    @PutMapping("/events/{id}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormQuestionResponseDto> updateEventQuestion(
            @PathVariable("id") Long eventId,
            @PathVariable Long questionId,
            @RequestBody FormQuestionRequestDto request) {
        return ResponseEntity.ok(eventService.updateQuestion(eventId, questionId, request));
    }

    @DeleteMapping("/events/{id}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEventQuestion(
            @PathVariable("id") Long eventId,
            @PathVariable Long questionId) {
        eventService.deleteQuestion(eventId, questionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/events/{id}/register")
    public ResponseEntity<Void> registerUserForEvent(
            @PathVariable("id") Long eventId,
            @RequestBody RegistrationRequestDto request) {
        
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }

        eventRegistrationService.registerUser(request.getUserId(), eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/events/{id}/is-registered")
    public ResponseEntity<java.util.Map<String, Boolean>> isRegisteredForEvent(
            @PathVariable("id") Long eventId,
            @RequestParam("userId") java.util.UUID userId) {
        boolean registered = eventRegistrationService.isUserRegistered(userId, eventId);
        return ResponseEntity.ok(java.util.Map.of("registered", registered));
    }
}
