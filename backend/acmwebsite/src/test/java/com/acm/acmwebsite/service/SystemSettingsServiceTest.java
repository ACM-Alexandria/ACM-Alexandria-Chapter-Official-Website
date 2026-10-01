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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SystemSettingsServiceTest {

    @Mock
    private SystemSettingsRepository systemSettingsRepository;

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
                .thenReturn(Optional.of(new SystemSettings(SystemSettings.SINGLETON_ID, false)));

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
    void assertEmailsEnabled_whenLocked_shouldThrow() {
        systemSettingsService.setEmailsEnabled(false);

        assertThrows(EmailsLockedException.class, () -> systemSettingsService.assertEmailsEnabled());
        verify(systemSettingsRepository).save(any(SystemSettings.class));
    }
}
