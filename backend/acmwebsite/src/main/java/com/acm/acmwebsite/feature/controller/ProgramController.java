package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.ProgramDto;
import com.acm.acmwebsite.feature.service.ProgramService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<ProgramDto>> getAll() {
        return ResponseEntity.ok(programService.getAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity findById(@PathVariable("id") long id) {
        return ResponseEntity.ok(programService.getById(id));
    }
    @PostMapping
    public ResponseEntity create(@RequestBody ProgramDto programDto) {
        return ResponseEntity.ok(programService.create(programDto));
    }
    @PutMapping("/{id}")
    public ResponseEntity update(@PathVariable("id") long id, @RequestBody ProgramDto programDto) {
        return ResponseEntity.ok(programService.update(id, programDto));
    }
    @DeleteMapping ("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        programService.deleteProgram(id);
        return ResponseEntity.ok().build();
    }

}
