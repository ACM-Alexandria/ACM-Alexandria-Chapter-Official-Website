package com.acm.acmwebsite.feature.service.past;

import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.repository.past.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PastSeasonService {
    private final PastHighBoardRepository highBoardRepository;
    private final PastCommitteeRepository committeeRepository;
    private final PastCommitteeBoardRepository committeeBoardRepository;
    private final PastClubRepository clubRepository;
    private final PastClubBoardRepository clubBoardRepository;

    @Cacheable(CacheNames.BOARD_SEASONS)
    @Transactional(readOnly = true)
    public List<Integer> getSeasons() {
        return Stream.of(highBoardRepository.findDistinctSeasons(), committeeRepository.findDistinctSeasons(),
                        committeeBoardRepository.findDistinctSeasons(), clubRepository.findDistinctSeasons(),
                        clubBoardRepository.findDistinctSeasons())
                .flatMap(List::stream).distinct().sorted(Comparator.reverseOrder()).toList();
    }
}
