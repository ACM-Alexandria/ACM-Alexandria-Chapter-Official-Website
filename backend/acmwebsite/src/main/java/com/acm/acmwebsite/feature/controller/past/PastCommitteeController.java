package com.acm.acmwebsite.feature.controller.past;

import com.acm.acmwebsite.feature.dto.past.PastCommitteeRequest;
import com.acm.acmwebsite.feature.dto.past.PastCommitteeResponse;
import com.acm.acmwebsite.feature.service.past.PastCommitteeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/boards/past/committees")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class PastCommitteeController {
    private final PastCommitteeService service;

    @GetMapping
    public List<PastCommitteeResponse> list(@RequestParam Integer season) {
        return service.list(season);
    }

    @PostMapping
    public PastCommitteeResponse create(@Valid @RequestBody PastCommitteeRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public PastCommitteeResponse update(@PathVariable Long id, @Valid @RequestBody PastCommitteeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
