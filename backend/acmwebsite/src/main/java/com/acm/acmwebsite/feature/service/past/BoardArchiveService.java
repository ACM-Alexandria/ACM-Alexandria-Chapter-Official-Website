package com.acm.acmwebsite.feature.service.past;

import com.acm.acmwebsite.feature.dto.past.BoardArchiveSummary;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.feature.event.PastBoardsChangedEvent;
import com.acm.acmwebsite.feature.event.CurrentBoardsArchivedEvent;
import com.acm.acmwebsite.feature.exception.BoardArchiveConflictException;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

// Facade: one transaction owns the ordered archive operations and their shared settings lock.
@Service
@RequiredArgsConstructor
public class BoardArchiveService {
    private final SystemSettingsService settingsService;
    private final PastBoardSnapshotService snapshotService;
    private final UserService userService;
    private final CurrentBoardClearingService clearingService;
    private final ApplicationEventPublisher events;

    @Transactional
    public BoardArchiveSummary archive(Integer expectedSeason) {
        var settings = settingsService.lockForArchive();
        Integer season = settings.getCurrentSeason();
        if (!Objects.equals(expectedSeason, season)) {
            throw new BoardArchiveConflictException("The current season is " + season
                    + ", not " + expectedSeason + ". Refresh and confirm the current season again. Nothing was archived.");
        }
        var summary = snapshotService.snapshot(season);
        summary.setUsersDowngraded(userService.demoteBoardMembers());
        clearingService.clearBoards();
        summary.setNewCurrentSeason(settingsService.incrementCurrentSeason(settings));
        events.publishEvent(new PastBoardsChangedEvent());
        events.publishEvent(new CurrentBoardsArchivedEvent());
        return summary;
    }
}
