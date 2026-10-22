package com.acm.acmwebsite.service.past;

import com.acm.acmwebsite.feature.dto.past.BoardArchiveSummary;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.feature.entity.SystemSettings;
import com.acm.acmwebsite.feature.event.CurrentBoardsArchivedEvent;
import com.acm.acmwebsite.feature.event.PastBoardsChangedEvent;
import com.acm.acmwebsite.feature.exception.BoardArchiveConflictException;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import com.acm.acmwebsite.feature.service.past.*;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BoardArchiveServiceTest {
    private final SystemSettingsService settings = mock(SystemSettingsService.class);
    private final PastBoardSnapshotService snapshot = mock(PastBoardSnapshotService.class);
    private final UserService demotion = mock(UserService.class);
    private final CurrentBoardClearingService clearing = mock(CurrentBoardClearingService.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final BoardArchiveService service = new BoardArchiveService(settings, snapshot, demotion, clearing, events);

    @Test
    void locksAndRunsEveryStepInOrderBeforePublishingEvents() {
        var row = new SystemSettings(1L, false, 2026);
        var summary = BoardArchiveSummary.builder().archivedSeason(2026).highBoardMembers(2).build();
        when(settings.lockForArchive()).thenReturn(row);
        when(snapshot.snapshot(2026)).thenReturn(summary);
        when(demotion.demoteBoardMembers()).thenReturn(3);
        when(settings.incrementCurrentSeason(row)).thenReturn(2027);

        assertSame(summary, service.archive(2026));

        assertEquals(3, summary.getUsersDowngraded());
        assertEquals(2027, summary.getNewCurrentSeason());
        var order = inOrder(settings, snapshot, demotion, clearing, events);
        order.verify(settings).lockForArchive();
        order.verify(snapshot).snapshot(2026);
        order.verify(demotion).demoteBoardMembers();
        order.verify(clearing).clearBoards();
        order.verify(settings).incrementCurrentSeason(row);
        order.verify(events).publishEvent(new PastBoardsChangedEvent());
        order.verify(events).publishEvent(new CurrentBoardsArchivedEvent());
    }

    @Test
    void staleConfirmationStopsBeforeAnyBoardWork() {
        when(settings.lockForArchive()).thenReturn(new SystemSettings(1L, true, 2027));
        var error = assertThrows(BoardArchiveConflictException.class, () -> service.archive(2026));
        assertTrue(error.getMessage().contains("2027"));
        verifyNoInteractions(snapshot, demotion, clearing, events);
    }

    @Test
    void existingArchiveStopsBeforeDemotionAndClearing() {
        when(settings.lockForArchive()).thenReturn(new SystemSettings(1L, true, 2026));
        when(snapshot.snapshot(2026)).thenThrow(new BoardArchiveConflictException("Already archived"));
        assertThrows(BoardArchiveConflictException.class, () -> service.archive(2026));
        verifyNoInteractions(demotion, clearing, events);
        verify(settings, never()).incrementCurrentSeason(any());
    }

    @Test
    void failureAfterClearingNeverPublishesEvents() {
        var row = new SystemSettings(1L, true, 2026);
        when(settings.lockForArchive()).thenReturn(row);
        when(snapshot.snapshot(2026)).thenReturn(BoardArchiveSummary.builder().build());
        when(settings.incrementCurrentSeason(row)).thenThrow(new IllegalStateException("Save failed"));
        assertThrows(IllegalStateException.class, () -> service.archive(2026));
        verify(clearing).clearBoards();
        verifyNoInteractions(events);
    }
}
