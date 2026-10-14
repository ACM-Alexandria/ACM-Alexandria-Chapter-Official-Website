package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.entity.SystemSettings;
import com.acm.acmwebsite.feature.repository.SystemSettingsRepository;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:board-settings-test;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SystemSettingsServiceIntegrationTest {
    @Autowired
    private SystemSettingsRepository repository;

    private SystemSettingsService service;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transaction;

    @BeforeEach
    void resetSettings() {
        transaction = new TransactionTemplate(transactionManager);
        repository.deleteAllInBatch();
        service = new SystemSettingsService(repository,
                Clock.fixed(Instant.parse("2026-09-01T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void missingSingletonIsCreatedAndDerivedSeasonIsPersisted() {
        assertEquals(2026, service.getCurrentSeason());

        SystemSettings stored = repository.findById(SystemSettings.SINGLETON_ID).orElseThrow();
        assertTrue(stored.isEmailsEnabled());
        assertEquals(2026, stored.getCurrentSeason());
        assertEquals(1, repository.count());
    }

    @Test
    void existingSettingsAreNotOverwrittenBySingletonInitialization() {
        repository.saveAndFlush(new SystemSettings(SystemSettings.SINGLETON_ID, false, 2024));

        assertEquals(2024, service.getCurrentSeason());

        SystemSettings stored = repository.findById(SystemSettings.SINGLETON_ID).orElseThrow();
        assertFalse(stored.isEmailsEnabled());
        assertEquals(2024, stored.getCurrentSeason());
    }

    @Test
    void simultaneousFirstReadsCreateOnlyOneSettingsRow() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                assertTrue(start.await(5, TimeUnit.SECONDS));
                return service.getCurrentSeason();
            });
            var second = executor.submit(() -> {
                assertTrue(start.await(5, TimeUnit.SECONDS));
                return service.getCurrentSeason();
            });
            start.countDown();

            assertEquals(2026, first.get(5, TimeUnit.SECONDS));
            assertEquals(2026, second.get(5, TimeUnit.SECONDS));
            assertEquals(1, repository.count());
        }
    }

    @Test
    void rolledBackInitializationLeavesDatabaseAndCacheUninitialized() {
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            assertEquals(2026, service.getCurrentSeason());
            throw new IllegalStateException("Simulated archive failure");
        }));
        assertFalse(repository.existsById(SystemSettings.SINGLETON_ID));
        repository.saveAndFlush(new SystemSettings(SystemSettings.SINGLETON_ID, true, 2024));

        assertEquals(2024, service.getCurrentSeason());
    }

    @Test
    void rolledBackSeasonChangePreservesDatabaseAndCachedSeason() {
        assertEquals(2026, service.getCurrentSeason());
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            SystemSettings settings = repository.findSingletonForUpdate().orElseThrow();
            settings.setCurrentSeason(2027);
            repository.saveAndFlush(settings);
            throw new IllegalStateException("Simulated archive failure");
        }));

        assertEquals(2026, service.getCurrentSeason());
        assertEquals(2026, repository.findById(SystemSettings.SINGLETON_ID).orElseThrow().getCurrentSeason());
    }

    @Test
    void pessimisticLockIsHeldUntilTransactionEnds() throws Exception {
        repository.saveAndFlush(new SystemSettings(SystemSettings.SINGLETON_ID, true, 2026));
        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondAttempting = new CountDownLatch(1);
        CountDownLatch secondLocked = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> transaction.executeWithoutResult(status -> {
                repository.findSingletonForUpdate().orElseThrow();
                firstLocked.countDown();
                try {
                    if (!releaseFirst.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timed out waiting to release first lock");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
            }));
            try {
                assertTrue(firstLocked.await(5, TimeUnit.SECONDS));
                var second = executor.submit(() -> transaction.executeWithoutResult(status -> {
                    secondAttempting.countDown();
                    repository.findSingletonForUpdate().orElseThrow();
                    secondLocked.countDown();
                }));
                assertTrue(secondAttempting.await(5, TimeUnit.SECONDS));
                assertFalse(secondLocked.await(200, TimeUnit.MILLISECONDS));
                releaseFirst.countDown();
                first.get(5, TimeUnit.SECONDS);
                second.get(5, TimeUnit.SECONDS);
                assertEquals(0, secondLocked.getCount());
            } finally {
                releaseFirst.countDown();
            }
        }
    }
}
