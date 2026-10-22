package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.SystemSettings;
import com.acm.acmwebsite.feature.exception.EmailsLockedException;
import com.acm.acmwebsite.feature.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class SystemSettingsService {
    private final SystemSettingsRepository systemSettingsRepository;
    private final Clock clock;

    // Cached so the email guard doesn't hit the database for every email; null until first read
    private volatile Boolean emailsEnabled;

    @Autowired
    public SystemSettingsService(SystemSettingsRepository systemSettingsRepository) {
        this(systemSettingsRepository, Clock.systemDefaultZone());
    }

    // A clock can be supplied by tests to verify both sides of the September boundary.
    public SystemSettingsService(SystemSettingsRepository systemSettingsRepository, Clock clock) {
        this.systemSettingsRepository = systemSettingsRepository;
        this.clock = clock;
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
                .orElseGet(() -> new SystemSettings(SystemSettings.SINGLETON_ID, true, null));
        settings.setEmailsEnabled(enabled);
        systemSettingsRepository.save(settings);
        emailsEnabled = enabled;
        return enabled;
    }

    public Integer getCurrentSeason() {
        SystemSettings settings = systemSettingsRepository.findById(SystemSettings.SINGLETON_ID)
                .orElseGet(() -> new SystemSettings(SystemSettings.SINGLETON_ID, true, null));
        if (settings.getCurrentSeason() == null) {
            LocalDate today = LocalDate.now(clock);
            int season = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
            settings.setCurrentSeason(season);
            systemSettingsRepository.save(settings);
        }
        return settings.getCurrentSeason();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public SystemSettings lockForArchive() {
        systemSettingsRepository.ensureSingletonExists();
        SystemSettings settings = systemSettingsRepository.findSingletonForUpdate()
                .orElseThrow(() -> new IllegalStateException("System settings could not be initialized"));
        if (settings.getCurrentSeason() == null) {
            getCurrentSeason();
        }
        return settings;
    }

    // Archive supplies the settings row locked by its transaction.
    public Integer incrementCurrentSeason(SystemSettings settings) {
        int nextSeason = settings.getCurrentSeason() + 1;
        settings.setCurrentSeason(nextSeason);
        systemSettingsRepository.save(settings);
        return nextSeason;
    }

    // For admin actions that exist only to send email, so they fail loudly instead of silently doing nothing
    public void assertEmailsEnabled() {
        if (!isEmailsEnabled()) {
            throw new EmailsLockedException();
        }
    }
}
