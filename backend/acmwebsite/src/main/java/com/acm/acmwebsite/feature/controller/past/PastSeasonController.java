package com.acm.acmwebsite.feature.controller.past;

import com.acm.acmwebsite.feature.service.SystemSettingsService;
import com.acm.acmwebsite.feature.service.past.PastSeasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/boards")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class PastSeasonController {
    private final PastSeasonService service;
    private final SystemSettingsService systemSettingsService;

    @GetMapping("/past/seasons")
    public List<Integer> seasons() {
        return service.getSeasons();
    }

    @GetMapping("/current-season")
    public Map<String, Integer> currentSeason() {
        return Map.of("currentSeason", systemSettingsService.getCurrentSeason());
    }
}
