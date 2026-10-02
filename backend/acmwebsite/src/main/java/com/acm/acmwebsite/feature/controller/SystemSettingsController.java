package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.EmailLockDto;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// /api/admin/** is limited to admin roles in SecurityConfig
@RestController
@RequestMapping("/api/admin/settings")
public class SystemSettingsController {
    private final SystemSettingsService systemSettingsService;

    public SystemSettingsController(SystemSettingsService systemSettingsService) {
        this.systemSettingsService = systemSettingsService;
    }

    // Any admin can read it, so the admin page can warn when emails are locked
    @GetMapping("/email-lock")
    public ResponseEntity<EmailLockDto> getEmailLock() {
        return ResponseEntity.ok(new EmailLockDto(systemSettingsService.isEmailsEnabled()));
    }

    @PutMapping("/email-lock")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<EmailLockDto> updateEmailLock(@RequestBody EmailLockDto request) {
        return ResponseEntity.ok(new EmailLockDto(systemSettingsService.setEmailsEnabled(request.isEmailsEnabled())));
    }
}
