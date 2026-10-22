package com.acm.acmwebsite.feature.controller.past;

import com.acm.acmwebsite.feature.dto.past.BoardArchiveRequest;
import com.acm.acmwebsite.feature.dto.past.BoardArchiveSummary;
import com.acm.acmwebsite.feature.service.past.BoardArchiveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/boards")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class BoardArchiveController {
    private final BoardArchiveService service;

    @PostMapping("/archive")
    public BoardArchiveSummary archive(@Valid @RequestBody BoardArchiveRequest request) {
        return service.archive(request.getSeason());
    }
}
