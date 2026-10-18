package com.acm.acmwebsite.service.past;

import com.acm.acmwebsite.feature.service.SystemSettingsService;
import com.acm.acmwebsite.feature.service.past.PastSeasonPolicy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PastSeasonPolicyTest {
    private final SystemSettingsService settingsService = mock(SystemSettingsService.class);
    private final PastSeasonPolicy policy = new PastSeasonPolicy(settingsService);

    @Test
    void acceptsOnlyYearsBetween2000AndCurrentSeasonExclusive() {
        when(settingsService.getCurrentSeason()).thenReturn(2026);
        assertDoesNotThrow(() -> policy.validate(2000));
        assertDoesNotThrow(() -> policy.validate(2025));
        for (Integer season : new Integer[]{null, 1999, 2026, 2027}) {
            assertThrows(IllegalArgumentException.class, () -> policy.validate(season));
        }
    }

    @Test
    void readsTheCurrentSeasonAgainAfterItChanges() {
        when(settingsService.getCurrentSeason()).thenReturn(2026, 2027);
        assertThrows(IllegalArgumentException.class, () -> policy.validate(2026));
        assertDoesNotThrow(() -> policy.validate(2026));
    }

    @Test
    void updateRequiresTheOriginalSeasonToRemainPast() {
        when(settingsService.getCurrentSeason()).thenReturn(2026);
        assertDoesNotThrow(() -> policy.validateUpdate(2025, 2025));
        assertThrows(IllegalArgumentException.class, () -> policy.validateUpdate(2024, 2025));
        assertThrows(IllegalArgumentException.class, () -> policy.validateUpdate(2026, 2026));
        assertThrows(IllegalArgumentException.class, () -> policy.validateUpdate(null, null));
    }

    @Test
    void memberAndParentMustBelongToTheSameSeason() {
        assertDoesNotThrow(() -> policy.validateSameSeason(2025, 2025));
        assertThrows(IllegalArgumentException.class, () -> policy.validateSameSeason(2024, 2025));
    }
}
