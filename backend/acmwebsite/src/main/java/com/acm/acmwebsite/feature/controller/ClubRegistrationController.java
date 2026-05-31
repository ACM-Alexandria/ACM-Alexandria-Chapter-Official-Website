package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.service.ClubRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClubRegistrationController {

    private final ClubRegistrationService clubRegistrationService;

    @GetMapping("/clubs/{id}/questions")
    public ResponseEntity<List<FormQuestionResponseDto>> getClubQuestions(@PathVariable("id") Long clubId) {
        List<FormQuestionResponseDto> questions = clubRegistrationService.getQuestions(clubId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/clubs/{id}/register")
    public ResponseEntity<Void> registerUserForClub(
            @PathVariable("id") Long clubId,
            @RequestBody RegistrationRequestDto request) {

        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }

        clubRegistrationService.registerUser(request.getUserId(), clubId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/clubs/{id}/is-registered")
    public ResponseEntity<java.util.Map<String, Boolean>> isRegisteredForClub(
            @PathVariable("id") Long clubId,
            @RequestParam("userId") java.util.UUID userId) {
        boolean registered = clubRegistrationService.isUserRegistered(userId, clubId);
        return ResponseEntity.ok(java.util.Map.of("registered", registered));
    }
}
