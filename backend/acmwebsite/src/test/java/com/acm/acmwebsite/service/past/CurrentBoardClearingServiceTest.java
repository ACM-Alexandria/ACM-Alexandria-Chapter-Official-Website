package com.acm.acmwebsite.service.past;

import com.acm.acmwebsite.feature.repository.*;
import com.acm.acmwebsite.feature.service.past.CurrentBoardClearingService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class CurrentBoardClearingServiceTest {
    @Test
    void flushesBeforeBulkDeletionAndClearsManagedEntitiesAfterwards() {
        var high = mock(HighBoardRepository.class);
        var committees = mock(CommitteeBoardRepository.class);
        var clubs = mock(ClubBoardRepository.class);
        var entities = mock(EntityManager.class);
        new CurrentBoardClearingService(high, committees, clubs, entities).clearBoards();
        var order = inOrder(entities, high, committees, clubs);
        order.verify(entities).flush();
        order.verify(high).deleteAllInBatch();
        order.verify(committees).deleteAllInBatch();
        order.verify(clubs).deleteAllInBatch();
        order.verify(entities).clear();
        verifyNoMoreInteractions(entities, high, committees, clubs);
    }
}
