package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.User_Authentication.service.AuthorizationService;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.service.ProgramRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProgramRegistrationController {

    private final ProgramRegistrationService programRegistrationService;
    private final AuthorizationService authorizationService;

    @GetMapping("/program/{id}/questions")
    public ResponseEntity<List<FormQuestionResponseDto>> getProgramQuestions(
            @PathVariable("id") Long programId) {
        List<FormQuestionResponseDto> questions = programRegistrationService.getQuestions(programId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/program/{id}/register")
    public ResponseEntity<Void> registerUserForProgram(
            @PathVariable("id") Long programId,
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

        programRegistrationService.registerUser(request.getUserId(), programId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/program/{id}/is-registered/{userId}")
    public ResponseEntity<Map<String, Boolean>> isRegisteredForProgram(
            @PathVariable("id") Long programId,
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

        boolean registered = programRegistrationService.isUserRegistered(userUuid, programId);
        return ResponseEntity.ok(Map.of("registered", registered));
    }
}
