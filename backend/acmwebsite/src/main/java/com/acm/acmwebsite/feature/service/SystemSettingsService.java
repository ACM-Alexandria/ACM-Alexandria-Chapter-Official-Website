package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.SystemSettings;
import com.acm.acmwebsite.feature.exception.EmailsLockedException;
import com.acm.acmwebsite.feature.repository.SystemSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingsService {
    private final SystemSettingsRepository systemSettingsRepository;

    // Cached so the email guard doesn't hit the database for every email; null until first read
    private volatile Boolean emailsEnabled;

    public SystemSettingsService(SystemSettingsRepository systemSettingsRepository) {
        this.systemSettingsRepository = systemSettingsRepository;
    }

    public boolean isEmailsEnabled() {
        Boolean cached = emailsEnabled;
        if (cached == null) {
            cached = systemSettingsRepository.findById(SystemSettings.SINGLETON_ID)
                    .map(SystemSettings::isEmailsEnabled)
                    .orElse(true);
            emailsEnabled = cached;
        }
        return cached;
    }

    public boolean setEmailsEnabled(boolean enabled) {
        SystemSettings settings = systemSettingsRepository.findById(SystemSettings.SINGLETON_ID)
                .orElseGet(() -> new SystemSettings(SystemSettings.SINGLETON_ID, true));
        settings.setEmailsEnabled(enabled);
        systemSettingsRepository.save(settings);
        emailsEnabled = enabled;
        return enabled;
    }

    // For admin actions that exist only to send email, so they fail loudly instead of silently doing nothing
    public void assertEmailsEnabled() {
        if (!isEmailsEnabled()) {
            throw new EmailsLockedException();
        }
    }
}
