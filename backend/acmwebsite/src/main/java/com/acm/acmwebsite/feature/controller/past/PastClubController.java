package com.acm.acmwebsite.feature.controller.past;

import com.acm.acmwebsite.feature.dto.past.PastClubRequest;
import com.acm.acmwebsite.feature.dto.past.PastClubResponse;
import com.acm.acmwebsite.feature.service.past.PastClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/boards/past/clubs")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class PastClubController {
    private final PastClubService service;

    @GetMapping
    public List<PastClubResponse> list(@RequestParam Integer season) {
        return service.list(season);
    }

    @PostMapping
    public PastClubResponse create(@Valid @RequestBody PastClubRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public PastClubResponse update(@PathVariable Long id, @Valid @RequestBody PastClubRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
