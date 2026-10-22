package com.acm.acmwebsite.service.past;

import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.event.PastBoardsChangedEvent;
import com.acm.acmwebsite.feature.event.CurrentBoardsArchivedEvent;
import com.acm.acmwebsite.feature.service.past.BoardCacheInvalidationListener;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import static org.junit.jupiter.api.Assertions.*;

class BoardCacheInvalidationListenerTest {
    @Test
    void clearsAllPastEntriesAndLeavesLiveCachesAlone() {
        var manager = new ConcurrentMapCacheManager(CacheNames.BOARD_SEASONS, CacheNames.PAST_BOARDS, CacheNames.HIGH_BOARD);
        for (String cache : manager.getCacheNames()) manager.getCache(cache).put(2025, "cached");
        new BoardCacheInvalidationListener(manager).onPastBoardsChanged(new PastBoardsChangedEvent());
        assertNull(manager.getCache(CacheNames.BOARD_SEASONS).get(2025));
        assertNull(manager.getCache(CacheNames.PAST_BOARDS).get(2025));
        assertNotNull(manager.getCache(CacheNames.HIGH_BOARD).get(2025));
    }

    @Test
    void archiveEventClearsTheThreeLiveCachesAndLeavesUnrelatedCachesAlone() {
        var manager = new ConcurrentMapCacheManager(CacheNames.HIGH_BOARD, CacheNames.COMMITTEES,
                CacheNames.CLUBS, CacheNames.EVENTS);
        for (String name : manager.getCacheNames()) manager.getCache(name).put("probe", "cached");
        new BoardCacheInvalidationListener(manager).onCurrentBoardsArchived(new CurrentBoardsArchivedEvent());
        assertNull(manager.getCache(CacheNames.HIGH_BOARD).get("probe"));
        assertNull(manager.getCache(CacheNames.COMMITTEES).get("probe"));
        assertNull(manager.getCache(CacheNames.CLUBS).get("probe"));
        assertNotNull(manager.getCache(CacheNames.EVENTS).get("probe"));
    }
}
