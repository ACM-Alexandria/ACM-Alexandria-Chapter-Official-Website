package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.enums.RegistrationEntityType;
import com.acm.acmwebsite.feature.event.SheetSyncEvent;
import com.acm.acmwebsite.feature.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SheetSyncListenerServiceTest {

    @Mock
    private CommitteeRegistrationService committeeRegistrationService;

    @Mock
    private ProgramService programService;

    @Mock
    private EventService eventService;

    @Mock
    private ClubService clubService;

    @Mock
    private ExclusiveFormRegistrationService exclusiveFormRegistrationService;

    @Mock
    private ScheduledExecutorService mockScheduler;

    @Mock
    private ScheduledFuture<?> mockFuture;

    @InjectMocks
    private SheetSyncListenerService sheetSyncListenerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sheetSyncListenerService, "scheduler", mockScheduler);
    }

    @Test
    @DisplayName("onRegistrationChanged - triggers CommitteeRegistrationService sync for COMMITTEE_CALL")
    void onRegistrationChanged_committeeCall() {
        SheetSyncEvent event = new SheetSyncEvent(this, RegistrationEntityType.COMMITTEE_CALL, 10L);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doReturn(mockFuture).when(mockScheduler).schedule(runnableCaptor.capture(), eq(5L), eq(TimeUnit.SECONDS));

        sheetSyncListenerService.onRegistrationChanged(event);

        verify(mockScheduler, times(1)).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));

        runnableCaptor.getValue().run();
        verify(committeeRegistrationService, times(1)).syncRegistrationsSheet(10L);
    }

    @Test
    @DisplayName("onRegistrationChanged - triggers ProgramService sync for PROGRAM")
    void onRegistrationChanged_program() {
        SheetSyncEvent event = new SheetSyncEvent(this, RegistrationEntityType.PROGRAM, 20L);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doReturn(mockFuture).when(mockScheduler).schedule(runnableCaptor.capture(), eq(5L), eq(TimeUnit.SECONDS));

        sheetSyncListenerService.onRegistrationChanged(event);
        runnableCaptor.getValue().run();

        verify(programService, times(1)).syncRegistrationsSheet(20L);
    }

    @Test
    @DisplayName("onRegistrationChanged - triggers EventService sync for EVENT")
    void onRegistrationChanged_event() {
        SheetSyncEvent event = new SheetSyncEvent(this, RegistrationEntityType.EVENT, 30L);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doReturn(mockFuture).when(mockScheduler).schedule(runnableCaptor.capture(), eq(5L), eq(TimeUnit.SECONDS));

        sheetSyncListenerService.onRegistrationChanged(event);
        runnableCaptor.getValue().run();

        verify(eventService, times(1)).syncRegistrationsSheet(30L);
    }

    @Test
    @DisplayName("onRegistrationChanged - triggers ClubService sync for CLUB")
    void onRegistrationChanged_club() {
        SheetSyncEvent event = new SheetSyncEvent(this, RegistrationEntityType.CLUB, 40L);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doReturn(mockFuture).when(mockScheduler).schedule(runnableCaptor.capture(), eq(5L), eq(TimeUnit.SECONDS));

        sheetSyncListenerService.onRegistrationChanged(event);
        runnableCaptor.getValue().run();

        verify(clubService, times(1)).syncRegistrationsSheet(40L);
    }

    @Test
    @DisplayName("onRegistrationChanged - triggers ExclusiveFormRegistrationService sync for EXCLUSIVE_FORM")
    void onRegistrationChanged_exclusiveForm() {
        SheetSyncEvent event = new SheetSyncEvent(this, RegistrationEntityType.EXCLUSIVE_FORM, 50L);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doReturn(mockFuture).when(mockScheduler).schedule(runnableCaptor.capture(), eq(5L), eq(TimeUnit.SECONDS));

        sheetSyncListenerService.onRegistrationChanged(event);
        runnableCaptor.getValue().run();

        verify(exclusiveFormRegistrationService, times(1)).syncRegistrationsSheet(50L);
    }

    @Test
    @DisplayName("onRegistrationChanged - catches exceptions thrown during sync without propagating")
    void onRegistrationChanged_syncExceptionHandled() {
        SheetSyncEvent event = new SheetSyncEvent(this, RegistrationEntityType.EVENT, 99L);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doReturn(mockFuture).when(mockScheduler).schedule(runnableCaptor.capture(), eq(5L), eq(TimeUnit.SECONDS));

        sheetSyncListenerService.onRegistrationChanged(event);

        doThrow(new RuntimeException("Sheets API connection failure"))
                .when(eventService).syncRegistrationsSheet(99L);

        assertDoesNotThrow(() -> runnableCaptor.getValue().run());
        verify(eventService, times(1)).syncRegistrationsSheet(99L);
    }

    @Test
    @DisplayName("onRegistrationChanged - cancels pending task if another event arrives for same entity and not done")
    void onRegistrationChanged_debouncesAndCancelsPendingTask() {
        SheetSyncEvent event1 = new SheetSyncEvent(this, RegistrationEntityType.EVENT, 1L);
        SheetSyncEvent event2 = new SheetSyncEvent(this, RegistrationEntityType.EVENT, 1L);

        ScheduledFuture<?> firstTask = mock(ScheduledFuture.class);
        when(firstTask.isDone()).thenReturn(false);

        ScheduledFuture<?> secondTask = mock(ScheduledFuture.class);

        doReturn(firstTask)
                .doReturn(secondTask)
                .when(mockScheduler).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));

        sheetSyncListenerService.onRegistrationChanged(event1);
        sheetSyncListenerService.onRegistrationChanged(event2);

        verify(firstTask, times(1)).cancel(false);
        verify(mockScheduler, times(2)).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("onRegistrationChanged - does not cancel existing task if it is already done")
    void onRegistrationChanged_doesNotCancelIfTaskIsDone() {
        SheetSyncEvent event1 = new SheetSyncEvent(this, RegistrationEntityType.EVENT, 2L);
        SheetSyncEvent event2 = new SheetSyncEvent(this, RegistrationEntityType.EVENT, 2L);

        ScheduledFuture<?> firstTask = mock(ScheduledFuture.class);
        when(firstTask.isDone()).thenReturn(true);

        ScheduledFuture<?> secondTask = mock(ScheduledFuture.class);

        doReturn(firstTask)
                .doReturn(secondTask)
                .when(mockScheduler).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));

        sheetSyncListenerService.onRegistrationChanged(event1);
        sheetSyncListenerService.onRegistrationChanged(event2);

        verify(firstTask, never()).cancel(anyBoolean());
        verify(mockScheduler, times(2)).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));
    }
}
