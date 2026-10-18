package com.acm.acmwebsite.feature.service.past;

import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.event.PastBoardsChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class BoardCacheInvalidationListener {
    private final CacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPastBoardsChanged(PastBoardsChangedEvent event) {
        Objects.requireNonNull(cacheManager.getCache(CacheNames.BOARD_SEASONS)).clear();
        Objects.requireNonNull(cacheManager.getCache(CacheNames.PAST_BOARDS)).clear();
    }
}
