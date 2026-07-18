package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.ProgramDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.service.ProgramService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/program")
public class ProgramController {
    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    // ── CRUD ──

    @GetMapping
    public ResponseEntity<Page<ProgramDto>> getAllPrograms(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(programService.getProgramsByPage(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramDto> findProgramById(@PathVariable("id") long id) {
        return programService.getProgramById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramDto> createProgram(@RequestBody ProgramDto programDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(programService.createProgram(programDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramDto> updateProgram(@PathVariable("id") long id,
                                                    @RequestBody ProgramDto programDto) {
        return ResponseEntity.ok(programService.updateProgram(id, programDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProgramById(@PathVariable long id) {
        programService.deleteProgram(id);
        return ResponseEntity.ok().build();
    }

    // ── Registration Toggle ──

    @PostMapping("/{id}/toggle-registration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramDto> toggleRegistration(
            @PathVariable("id") Long programId,
            @RequestParam boolean open) {
        return ResponseEntity.ok(programService.toggleRegistration(programId, open));
    }

    // ── Form Questions ──

    @PostMapping("/{id}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormQuestionResponseDto> createQuestion(
            @PathVariable("id") Long programId,
            @RequestBody FormQuestionRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(programService.createQuestion(programId, request));
    }

    @PutMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormQuestionResponseDto> updateQuestion(
            @PathVariable("id") Long programId,
            @PathVariable Long questionId,
            @RequestBody FormQuestionRequestDto request) {
        return ResponseEntity.ok(programService.updateQuestion(programId, questionId, request));
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable("id") Long programId,
            @PathVariable Long questionId) {
        programService.deleteQuestion(programId, questionId);
        return ResponseEntity.noContent().build();
    }

    // ── Registration Analytics ──

    @GetMapping("/{id}/registrations/analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegistrationAnalysisDto> getRegistrationAnalysis(@PathVariable Long id) {
        return ResponseEntity.ok(programService.getRegistrationAnalysis(id));
    }

    @PostMapping("/{id}/registrations/sheet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegistrationAnalysisDto> syncRegistrationsSheet(@PathVariable Long id) {
        return ResponseEntity.ok(programService.syncRegistrationsSheet(id));
    }
}
