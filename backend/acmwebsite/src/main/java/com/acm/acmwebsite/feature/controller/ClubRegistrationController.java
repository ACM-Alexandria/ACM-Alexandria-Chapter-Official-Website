package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.User_Authentication.service.AuthorizationService;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.service.ClubRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClubRegistrationController {

    private final ClubRegistrationService clubRegistrationService;
    private final AuthorizationService authorizationService;

    @GetMapping("/clubs/{id}/questions")
    public ResponseEntity<List<FormQuestionResponseDto>> getClubQuestions(@PathVariable("id") Long clubId) {
        List<FormQuestionResponseDto> questions = clubRegistrationService.getQuestions(clubId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/clubs/{id}/register")
    public ResponseEntity<Void> registerUserForClub(
            @PathVariable("id") Long clubId,
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

        clubRegistrationService.registerUser(request.getUserId(), clubId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/clubs/{id}/is-registered/{userId}")
    public ResponseEntity<java.util.Map<String, Boolean>> isRegisteredForClub(
            @PathVariable("id") Long clubId,
            @PathVariable("userId") String userId,
            Authentication authentication) {
        java.util.UUID userUuid;
        try {
            userUuid = java.util.UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        if (!authorizationService.isAuthenticated(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!authorizationService.isSelf(userUuid, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean registered = clubRegistrationService.isUserRegistered(userUuid, clubId);
        return ResponseEntity.ok(java.util.Map.of("registered", registered));
    }
}
