package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.event.SheetSyncEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Listens for {@link SheetSyncEvent}s and triggers a Google Sheets sync.
 *
 * <p>Debounce strategy: each unique (entityType, entityId) pair gets its own pending sync task.
 * If a new event arrives for the same pair before the delay elapses, the previous task is
 * canceled and a fresh one is scheduled. This collapses a burst of rapid registrations into a
 * single Sheets API call.
 *
 * <p>The listener is wired to {@link TransactionPhase#AFTER_COMMIT} so it only fires after the
 * registration is persisted, so that an uncompleted transaction never triggers a sync.
 */
@Component
public class SheetSyncListenerService {

    private static final Logger log = LoggerFactory.getLogger(SheetSyncListenerService.class);

    private static final long DEBOUNCE_DELAY_SECONDS = 5;

    private final CommitteeRegistrationService committeeRegistrationService;
    private final ProgramService programService;
    private final EventService eventService;
    private final ClubService clubService;
    private final ExclusiveFormRegistrationService exclusiveFormRegistrationService;


     /* using a single threaded executor, so that all sync tasks run sequentially so concurrent calls to
     different entities don't overwhelm the Sheets API. */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // tracks pending sync tasks keyed by entityId.
    private final ConcurrentHashMap<String, ScheduledFuture<?>> pendingTasks = new ConcurrentHashMap<>();

    public SheetSyncListenerService(
            CommitteeRegistrationService committeeRegistrationService,
            ProgramService programService,
            EventService eventService,
            ClubService clubService,
            ExclusiveFormRegistrationService exclusiveFormRegistrationService)
    {
        this.committeeRegistrationService = committeeRegistrationService;
        this.programService = programService;
        this.eventService = eventService;
        this.clubService = clubService;
        this.exclusiveFormRegistrationService = exclusiveFormRegistrationService;
    }

    /**
     * Receives a sync event after the registering transaction commits, then schedules
     * (or re-schedules) a debounced sync for the affected entity.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRegistrationChanged(SheetSyncEvent event) {
        String key = event.getEntityType().name() + ":" + event.getEntityId();
        log.debug("Sheet sync event received for key={}. Debouncing for {}s.", key, DEBOUNCE_DELAY_SECONDS);

        // cancels any existing pending task for this entity before scheduling a new one
        ScheduledFuture<?> existing = pendingTasks.remove(key);
        if (existing != null && !existing.isDone()) {
            existing.cancel(false);
        }

        // schedules the sync task to run after DEBOUNCE_DELAY_SECONDS seconds
        ScheduledFuture<?> task = scheduler.schedule(
                () -> runSync(event, key),
                DEBOUNCE_DELAY_SECONDS,
                TimeUnit.SECONDS
        );
        pendingTasks.put(key, task);
    }

    /**
     * Executes the sheet sync. Any exception is caught and logged so a
     * sheets API failure never propagates or kills the scheduler thread.
     */
    private void runSync(SheetSyncEvent event, String key) {
        pendingTasks.remove(key);
        log.info("Auto-syncing sheet for key={}.", key);
        try {
            switch (event.getEntityType()) {
                case COMMITTEE_CALL ->
                        committeeRegistrationService.syncRegistrationsSheet(event.getEntityId());
                case PROGRAM ->
                        programService.syncRegistrationsSheet(event.getEntityId());
                case EVENT ->
                        eventService.syncRegistrationsSheet(event.getEntityId());
                case CLUB ->
                        clubService.syncRegistrationsSheet(event.getEntityId());
                case EXCLUSIVE_FORM ->
                        exclusiveFormRegistrationService.syncRegistrationsSheet(event.getEntityId());
            }
            log.info("Auto-sync completed successfully for key={}.", key);
        } catch (Exception e) {
            log.error("Auto-sync failed for key={}. The sheet can be re-synced manually. Cause: {}",
                    key, e.getMessage(), e);
        }
    }
}