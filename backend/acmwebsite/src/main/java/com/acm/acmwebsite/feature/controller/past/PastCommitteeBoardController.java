package com.acm.acmwebsite.feature.controller.past;

import com.acm.acmwebsite.feature.dto.past.PastCommitteeBoardRequest;
import com.acm.acmwebsite.feature.dto.past.PastCommitteeBoardResponse;
import com.acm.acmwebsite.feature.service.past.PastCommitteeBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/boards/past/committee-board")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class PastCommitteeBoardController {
    private final PastCommitteeBoardService service;

    @GetMapping
    public List<PastCommitteeBoardResponse> list(@RequestParam Integer season) {
        return service.list(season);
    }

    @PostMapping
    public PastCommitteeBoardResponse create(@Valid @RequestBody PastCommitteeBoardRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public PastCommitteeBoardResponse update(@PathVariable Long id, @Valid @RequestBody PastCommitteeBoardRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
