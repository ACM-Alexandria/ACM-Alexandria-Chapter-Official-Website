package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @GetMapping("/events/{id}/questions")
    public ResponseEntity<List<FormQuestionResponseDto>> getEventQuestions(@PathVariable("id") Long eventId) {
        List<FormQuestionResponseDto> questions = registrationService.getEventQuestions(eventId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/events/{id}/register")
    public ResponseEntity<Void> registerUserForEvent(
            @PathVariable("id") Long eventId,
            @RequestBody RegistrationRequestDto request) {
        
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }

        registrationService.registerUserForEvent(request.getUserId(), eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping("/clubs/{id}/questions")
    public ResponseEntity<List<FormQuestionResponseDto>> getClubQuestions(@PathVariable("id") Long clubId) {
        List<FormQuestionResponseDto> questions = registrationService.getClubQuestions(clubId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/clubs/{id}/register")
    public ResponseEntity<Void> registerUserForClub(
            @PathVariable("id") Long clubId,
            @RequestBody RegistrationRequestDto request) {

        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }

        registrationService.registerUserForClub(request.getUserId(), clubId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/events/{id}/is-registered")
    public ResponseEntity<java.util.Map<String, Boolean>> isRegisteredForEvent(
            @PathVariable("id") Long eventId,
            @RequestParam("userId") java.util.UUID userId) {
        boolean registered = registrationService.isUserRegisteredForEvent(userId, eventId);
        return ResponseEntity.ok(java.util.Map.of("registered", registered));
    }

    @GetMapping("/clubs/{id}/is-registered")
    public ResponseEntity<java.util.Map<String, Boolean>> isRegisteredForClub(
            @PathVariable("id") Long clubId,
            @RequestParam("userId") java.util.UUID userId) {
        boolean registered = registrationService.isUserRegisteredForClub(userId, clubId);
        return ResponseEntity.ok(java.util.Map.of("registered", registered));
    }
}
