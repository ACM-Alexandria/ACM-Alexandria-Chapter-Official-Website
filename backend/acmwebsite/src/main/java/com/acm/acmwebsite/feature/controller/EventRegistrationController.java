package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.User_Authentication.service.AuthorizationService;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.service.EventRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;
    private final AuthorizationService authorizationService;

    @GetMapping("/events/{id}/questions")
    public ResponseEntity<List<FormQuestionResponseDto>> getEventQuestions(@PathVariable("id") Long eventId) {
        List<FormQuestionResponseDto> questions = eventRegistrationService.getQuestions(eventId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/events/{id}/register")
    public ResponseEntity<Void> registerUserForEvent(
            @PathVariable("id") Long eventId,
            @RequestBody RegistrationRequestDto request,
            Authentication authentication) {
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }

        if (!authorizationService.isAuthenticated(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!authorizationService.isSelf(request.getUserId(), authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        eventRegistrationService.registerUser(request.getUserId(), eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/events/{id}/is-registered/{userId}")
    public ResponseEntity<java.util.Map<String, Boolean>> isRegisteredForEvent(
            @PathVariable("id") Long eventId,
            @PathVariable("userId") String userId) {
        UUID requestedUserId;
        try {
            requestedUserId = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        boolean registered = eventRegistrationService.isUserRegistered(requestedUserId, eventId);
        return ResponseEntity.ok(java.util.Map.of("registered", registered));
    }
}
