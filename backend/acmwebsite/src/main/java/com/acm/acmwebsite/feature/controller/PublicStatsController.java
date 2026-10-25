package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.PublicStatsDto;
import com.acm.acmwebsite.feature.service.PublicStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/** Public chapter totals for the home page. Open to everyone; browsers may reuse the answer for a minute. */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class PublicStatsController {

    private final PublicStatsService publicStatsService;

    @GetMapping
    public ResponseEntity<PublicStatsDto> getStats() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePublic())
                .body(publicStatsService.getStats());
    }
}
