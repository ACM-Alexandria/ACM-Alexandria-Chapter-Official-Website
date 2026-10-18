package com.acm.acmwebsite.feature.controller.past;

import com.acm.acmwebsite.feature.dto.past.PastHighBoardRequest;
import com.acm.acmwebsite.feature.dto.past.PastHighBoardResponse;
import com.acm.acmwebsite.feature.service.past.PastHighBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/boards/past/high-board")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class PastHighBoardController {
    private final PastHighBoardService service;

    @GetMapping
    public List<PastHighBoardResponse> list(@RequestParam Integer season) {
        return service.list(season);
    }

    @PostMapping
    public PastHighBoardResponse create(@Valid @RequestBody PastHighBoardRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public PastHighBoardResponse update(@PathVariable Long id, @Valid @RequestBody PastHighBoardRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
