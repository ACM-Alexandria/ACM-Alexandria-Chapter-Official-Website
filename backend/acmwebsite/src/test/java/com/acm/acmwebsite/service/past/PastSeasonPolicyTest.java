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
}
