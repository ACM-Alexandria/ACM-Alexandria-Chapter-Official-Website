package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.service.ProgramRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProgramRegistrationController {

    private final ProgramRegistrationService programRegistrationService;

    @GetMapping("/program/{id}/questions")
    public ResponseEntity<List<FormQuestionResponseDto>> getProgramQuestions(
            @PathVariable("id") Long programId) {
        List<FormQuestionResponseDto> questions = programRegistrationService.getQuestions(programId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/program/{id}/register")
    public ResponseEntity<Void> registerUserForProgram(
            @PathVariable("id") Long programId,
            @RequestBody RegistrationRequestDto request) {

        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }

        programRegistrationService.registerUser(request.getUserId(), programId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/program/{id}/is-registered")
    public ResponseEntity<Map<String, Boolean>> isRegisteredForProgram(
            @PathVariable("id") Long programId,
            @RequestParam("userId") UUID userId) {
        boolean registered = programRegistrationService.isUserRegistered(userId, programId);
        return ResponseEntity.ok(Map.of("registered", registered));
    }
}
