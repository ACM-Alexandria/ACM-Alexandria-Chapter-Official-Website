package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.ExclusiveFormDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.ExclusiveForm;
import com.acm.acmwebsite.feature.service.ExclusiveFormService;
import com.acm.acmwebsite.feature.service.ExclusiveFormRegistrationService;
import com.acm.acmwebsite.feature.mapper.ExclusiveFormMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exclusive-forms")
public class ExclusiveFormController {

    private final ExclusiveFormService exclusiveFormService;
    private final ExclusiveFormRegistrationService registrationService;
    private final ExclusiveFormMapper mapper;

    public ExclusiveFormController(
            ExclusiveFormService exclusiveFormService,
            ExclusiveFormRegistrationService registrationService,
            ExclusiveFormMapper mapper) {
        this.exclusiveFormService = exclusiveFormService;
        this.registrationService = registrationService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ExclusiveFormDto> getAllForms() {
        return exclusiveFormService.getAllForms().stream().map(mapper::toDto).toList();
    }

    @GetMapping("/active")
    public List<ExclusiveFormDto> getActiveForms() {
        return exclusiveFormService.getActiveForms().stream().map(mapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExclusiveFormDto> getFormById(@PathVariable Long id) {
        try {
            ExclusiveFormDto dto = mapper.toDto(exclusiveFormService.getFormById(id));
            return ResponseEntity.ok(dto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createForm(@RequestBody ExclusiveFormDto formDto) {
        try {
            ExclusiveForm entity = mapper.toEntity(formDto);
            // Assuming saveForm is a method in ExclusiveFormService returning the saved entity
            ExclusiveForm saved = exclusiveFormService.saveForm(entity);
            return ResponseEntity.ok(mapper.toDto(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateForm(@PathVariable Long id, @RequestBody ExclusiveFormDto formDto) {
        try {
            ExclusiveFormDto updated = mapper.toDto(exclusiveFormService.updateForm(id, formDto));
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteForm(@PathVariable Long id) {
        try {
            exclusiveFormService.deleteForm(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Question Management ---

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<FormQuestionResponseDto>> getFormQuestions(@PathVariable("id") Long formId) {
        return ResponseEntity.ok(registrationService.getQuestions(formId));
    }

    @PostMapping("/{id}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormQuestionResponseDto> createFormQuestion(
            @PathVariable("id") Long formId,
            @RequestBody FormQuestionRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.createQuestion(formId, request));
    }

    @PutMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormQuestionResponseDto> updateFormQuestion(
            @PathVariable("id") Long formId,
            @PathVariable Long questionId,
            @RequestBody FormQuestionRequestDto request) {
        return ResponseEntity.ok(registrationService.updateQuestion(formId, questionId, request));
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFormQuestion(
            @PathVariable("id") Long formId,
            @PathVariable Long questionId) {
        registrationService.deleteQuestion(formId, questionId);
        return ResponseEntity.noContent().build();
    }

    // --- Registration Management ---

    @PostMapping("/{id}/register")
    public ResponseEntity<Void> registerUserForForm(
            @PathVariable("id") Long formId,
            @RequestBody RegistrationRequestDto request) {
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }
        registrationService.registerUser(request.getUserId(), formId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/is-registered/{userId}")
    public ResponseEntity<Map<String, Boolean>> isRegisteredForForm(
            @PathVariable("id") Long formId,
            @PathVariable("userId") String userId) {
        java.util.UUID userUuid;
        try {
            userUuid = java.util.UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        boolean registered = registrationService.isUserRegistered(userUuid, formId);
        return ResponseEntity.ok(Map.of("registered", registered));
    }

    // --- Analytics and Sync ---

    @GetMapping("/{id}/registrations/analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegistrationAnalysisDto> getFormRegistrationAnalysis(@PathVariable("id") Long formId) {
        return ResponseEntity.ok(registrationService.getRegistrationAnalysis(formId));
    }

    @PostMapping("/{id}/registrations/sheet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegistrationAnalysisDto> syncFormRegistrationsSheet(@PathVariable("id") Long formId) {
        return ResponseEntity.ok(registrationService.syncRegistrationsSheet(formId));
    }
}
