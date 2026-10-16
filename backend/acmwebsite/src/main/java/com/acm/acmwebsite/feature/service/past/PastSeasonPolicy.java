package com.acm.acmwebsite.feature.service.past;

import com.acm.acmwebsite.feature.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PastSeasonPolicy {
    private final SystemSettingsService systemSettingsService;

    public void validate(Integer season) {
        if (season == null || season < 2000) {
            throw new IllegalArgumentException("Season must be a starting year of at least 2000");
        }
        if (season >= systemSettingsService.getCurrentSeason()) {
            throw new IllegalArgumentException("Past season must be earlier than the current season");
        }
    }
}
