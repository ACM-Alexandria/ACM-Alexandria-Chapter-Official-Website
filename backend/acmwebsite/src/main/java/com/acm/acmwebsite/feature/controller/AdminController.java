package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.CustomEmailPreviewDto;
import com.acm.acmwebsite.feature.dto.CustomEmailPreviewRequestDto;
import com.acm.acmwebsite.feature.dto.CustomEmailRequestDto;
import com.acm.acmwebsite.feature.dto.CustomEmailResultDto;
import com.acm.acmwebsite.feature.service.AdminEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
public class AdminController {

    private final AdminEmailService adminEmailService;

    @PostMapping("/emails/preview")
    public ResponseEntity<?> previewCustomEmail(@Valid @RequestBody CustomEmailPreviewRequestDto request) {
        try {
            CustomEmailPreviewDto preview = adminEmailService.previewCustomEmail(request);
            return ResponseEntity.ok(preview);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @PostMapping("/emails/send-custom")
    public ResponseEntity<?> sendCustomEmail(@Valid @RequestBody CustomEmailRequestDto request) {
        try {
            CustomEmailResultDto result = adminEmailService.sendCustomEmail(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }
}
