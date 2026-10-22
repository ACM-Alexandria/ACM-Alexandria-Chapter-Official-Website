package com.acm.acmwebsite.feature.service.past;

import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import com.acm.acmwebsite.feature.repository.CommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.ClubBoardRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class CurrentBoardClearingService {
    private final HighBoardRepository highBoardRepository;
    private final CommitteeBoardRepository committeeBoardRepository;
    private final ClubBoardRepository clubBoardRepository;
    private final EntityManager entityManager;

    public void clearBoards() {
        // Persist snapshots and role changes before bulk deletes bypass the persistence context.
        entityManager.flush();
        highBoardRepository.deleteAllInBatch();
        committeeBoardRepository.deleteAllInBatch();
        clubBoardRepository.deleteAllInBatch();
        entityManager.clear();
    }
}
