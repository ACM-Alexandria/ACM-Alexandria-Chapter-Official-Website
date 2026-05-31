package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.service.EventRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;

    @GetMapping("/events/{id}/questions")
    public ResponseEntity<List<FormQuestionResponseDto>> getEventQuestions(@PathVariable("id") Long eventId) {
        List<FormQuestionResponseDto> questions = eventRegistrationService.getQuestions(eventId);
        return ResponseEntity.ok(questions);
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
