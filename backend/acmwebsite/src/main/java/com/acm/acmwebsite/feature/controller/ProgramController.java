package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.ProgramDto;
import com.acm.acmwebsite.feature.service.ProgramService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/program")
public class ProgramController {
    private final ProgramService programService;
    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }
    @GetMapping
    public ResponseEntity<List<ProgramDto>> getAllPrograms() {
        return ResponseEntity.ok(programService.getAllPrograms());
    }
    @GetMapping("/{id}")
    public ResponseEntity findProgramById(@PathVariable("id") long id) {
        return ResponseEntity.ok(programService.getProgramById(id));
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity createProgram(@RequestBody ProgramDto programDto) {
        return ResponseEntity.ok(programService.createProgram(programDto));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity updateProgram(@PathVariable("id") long id, @RequestBody ProgramDto programDto) {
        return ResponseEntity.ok(programService.updateProgram(id, programDto));
    }
    @DeleteMapping ("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProgramById(@PathVariable long id) {
        programService.deleteProgram(id);
        return ResponseEntity.ok().build();
    }

}
