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
}
