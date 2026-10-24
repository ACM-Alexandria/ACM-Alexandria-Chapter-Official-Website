package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.entity.SystemSettings;
import com.acm.acmwebsite.feature.exception.EmailsLockedException;
import com.acm.acmwebsite.feature.repository.SystemSettingsRepository;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SystemSettingsServiceTest {

    @Mock
    private SystemSettingsRepository systemSettingsRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private SystemSettingsService systemSettingsService;

    @Test
    void isEmailsEnabled_noSettingsRow_shouldDefaultToTrue() {
        when(systemSettingsRepository.findById(SystemSettings.SINGLETON_ID)).thenReturn(Optional.empty());

        assertTrue(systemSettingsService.isEmailsEnabled());
    }

    @Test
    void isEmailsEnabled_shouldCacheAfterFirstRead() {
        when(systemSettingsRepository.findById(SystemSettings.SINGLETON_ID))
                .thenReturn(Optional.of(new SystemSettings(SystemSettings.SINGLETON_ID, false, null)));

        assertFalse(systemSettingsService.isEmailsEnabled());
        assertFalse(systemSettingsService.isEmailsEnabled());

        verify(systemSettingsRepository, times(1)).findById(SystemSettings.SINGLETON_ID);
    }

    @Test
    void setEmailsEnabled_shouldPersistAndUpdateCache() {
        when(systemSettingsRepository.findById(SystemSettings.SINGLETON_ID)).thenReturn(Optional.empty());

        assertFalse(systemSettingsService.setEmailsEnabled(false));

        verify(systemSettingsRepository).save(argThat(s -> s.getId() == SystemSettings.SINGLETON_ID && !s.isEmailsEnabled()));
        assertFalse(systemSettingsService.isEmailsEnabled());
    }

    @Test
    void setEmailsEnabled_whenRowAlreadyExists_shouldUpdateExisting() {
        SystemSettings existing = new SystemSettings(SystemSettings.SINGLETON_ID, false, null);
        when(systemSettingsRepository.findById(SystemSettings.SINGLETON_ID)).thenReturn(Optional.of(existing));

        assertTrue(systemSettingsService.setEmailsEnabled(true));

        verify(systemSettingsRepository).save(existing);
        assertTrue(systemSettingsService.isEmailsEnabled());
    }

    @Test
    void assertEmailsEnabled_whenLocked_shouldThrow() {
        settings(true, null);
        systemSettingsService.setEmailsEnabled(false);

        assertThrows(EmailsLockedException.class, () -> systemSettingsService.assertEmailsEnabled());
        verify(systemSettingsRepository).save(any(SystemSettings.class));
    }

    @Test
    void assertEmailsEnabled_whenUnlocked_shouldNotThrow() {
        when(systemSettingsRepository.findById(SystemSettings.SINGLETON_ID)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> systemSettingsService.assertEmailsEnabled());
    }

    @Test
    void incrementCurrentSeason_shouldPersistNextSeasonAndPreserveEmailFlag() {
        SystemSettings settings = new SystemSettings(SystemSettings.SINGLETON_ID, false, 2026);

        assertEquals(2027, systemSettingsService.incrementCurrentSeason(settings));

        verify(systemSettingsRepository).save(settings);
        assertEquals(2027, settings.getCurrentSeason());
        assertFalse(settings.isEmailsEnabled());
    }

    @Test
    void incrementCurrentSeason_shouldPropagateSaveFailure() {
        SystemSettings settings = new SystemSettings(SystemSettings.SINGLETON_ID, true, 2026);
        doThrow(new IllegalStateException("Save failed")).when(systemSettingsRepository).save(settings);

        assertThrows(IllegalStateException.class, () -> systemSettingsService.incrementCurrentSeason(settings));
    }

    private SystemSettings settings(boolean emailsEnabled, Integer season) {
        SystemSettings settings = new SystemSettings(SystemSettings.SINGLETON_ID, emailsEnabled, season);
        when(systemSettingsRepository.findById(SystemSettings.SINGLETON_ID)).thenReturn(Optional.of(settings));
        return settings;
    }

    private void serverDate(String instant, String zone) {
        when(clock.instant()).thenReturn(Instant.parse(instant));
        when(clock.getZone()).thenReturn(ZoneId.of(zone));
    }

    @Test
    void getCurrentSeason_inAugust_shouldPersistPreviousYear() {
        SystemSettings settings = settings(false, null);
        serverDate("2026-08-31T12:00:00Z", "UTC");

        assertEquals(2025, systemSettingsService.getCurrentSeason());
        assertEquals(2025, systemSettingsService.getCurrentSeason());
        assertFalse(settings.isEmailsEnabled());
        verify(systemSettingsRepository).save(settings);
        verify(systemSettingsRepository, times(2)).findById(SystemSettings.SINGLETON_ID);
    }

    @Test
    void getCurrentSeason_inSeptember_shouldPersistCurrentYear() {
        SystemSettings settings = settings(true, null);
        serverDate("2026-09-01T00:00:00Z", "UTC");

        assertEquals(2026, systemSettingsService.getCurrentSeason());
        verify(systemSettingsRepository).save(settings);
    }

    @Test
    void getCurrentSeason_inJanuary_shouldPersistPreviousYear() {
        settings(true, null);
        serverDate("2027-01-01T00:00:00Z", "UTC");

        assertEquals(2026, systemSettingsService.getCurrentSeason());
    }

    @Test
    void getCurrentSeason_shouldUseServerTimeZoneAtBoundary() {
        settings(true, null);
        serverDate("2026-08-31T22:00:00Z", "Africa/Cairo");

        assertEquals(2026, systemSettingsService.getCurrentSeason());
    }

    @Test
    void getCurrentSeason_shouldKeepStoredSeasonWithoutConsultingDate() {
        settings(true, 2024);

        assertEquals(2024, systemSettingsService.getCurrentSeason());
        assertEquals(2024, systemSettingsService.getCurrentSeason());
        verify(systemSettingsRepository, never()).save(any());
        verifyNoInteractions(clock);
        verify(systemSettingsRepository, times(2)).findById(SystemSettings.SINGLETON_ID);
    }

    @Test
    void setEmailsEnabled_shouldPreserveCurrentSeason() {
        SystemSettings settings = settings(true, 2025);

        systemSettingsService.setEmailsEnabled(false);

        assertEquals(2025, settings.getCurrentSeason());
        verify(systemSettingsRepository).save(settings);
    }

    @Test
    void ordinaryReadsAndEmailUpdates_shouldNotAcquireArchiveLock() {
        settings(true, 2025);

        assertEquals(2025, systemSettingsService.getCurrentSeason());
        systemSettingsService.setEmailsEnabled(false);

        verify(systemSettingsRepository, never()).findSingletonForUpdate();
    }

    @Test
    void getCurrentSeason_shouldCreateMissingSettingsWithDefaultEmailFlag() {
        when(systemSettingsRepository.findById(SystemSettings.SINGLETON_ID)).thenReturn(Optional.empty());
        serverDate("2026-09-01T00:00:00Z", "UTC");

        assertEquals(2026, systemSettingsService.getCurrentSeason());

        verify(systemSettingsRepository).save(argThat(settings ->
                settings.getId().equals(SystemSettings.SINGLETON_ID)
                        && settings.isEmailsEnabled() && settings.getCurrentSeason() == 2026));
    }

    @Test
    void getCurrentSeason_shouldReadUpdatedStoredSeason() {
        SystemSettings settings = settings(true, 2025);
        assertEquals(2025, systemSettingsService.getCurrentSeason());

        settings.setCurrentSeason(2026);

        assertEquals(2026, systemSettingsService.getCurrentSeason());
    }


    @Test
    void lockForArchiveBootstrapsAndLocksBeforeInitializingNullSeason() {
        var row = new SystemSettings(1L, false, null);
        when(systemSettingsRepository.findSingletonForUpdate()).thenReturn(Optional.of(row));
        when(systemSettingsRepository.findById(1L)).thenReturn(Optional.of(row));
        serverDate("2026-09-01T00:00:00Z", "UTC");

        assertSame(row, systemSettingsService.lockForArchive());

        var order = inOrder(systemSettingsRepository);
        order.verify(systemSettingsRepository).ensureSingletonExists();
        order.verify(systemSettingsRepository).findSingletonForUpdate();
        order.verify(systemSettingsRepository).findById(1L);
        order.verify(systemSettingsRepository).save(row);
        assertEquals(2026, row.getCurrentSeason());
        assertFalse(row.isEmailsEnabled());
    }

    @Test
    void lockForArchivePreservesAnExistingSeasonWithoutReinitializingIt() {
        var row = new SystemSettings(1L, false, 2024);
        when(systemSettingsRepository.findSingletonForUpdate()).thenReturn(Optional.of(row));
        assertSame(row, systemSettingsService.lockForArchive());
        verify(systemSettingsRepository, never()).findById(anyLong());
        verifyNoInteractions(clock);
    }
}
