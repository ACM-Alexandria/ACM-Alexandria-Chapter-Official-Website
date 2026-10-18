package com.acm.acmwebsite.service.past;

import com.acm.acmwebsite.feature.repository.past.*;
import com.acm.acmwebsite.feature.service.past.PastSeasonService;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PastSeasonServiceTest {
    @Test
    void unionsEveryTableAndSortsDistinctYearsDescending() {
        var high = mock(PastHighBoardRepository.class);
        var committees = mock(PastCommitteeRepository.class);
        var committeeBoard = mock(PastCommitteeBoardRepository.class);
        var clubs = mock(PastClubRepository.class);
        var clubBoard = mock(PastClubBoardRepository.class);
        when(high.findDistinctSeasons()).thenReturn(List.of(2022, 2024));
        when(committees.findDistinctSeasons()).thenReturn(List.of(2023, 2024));
        when(committeeBoard.findDistinctSeasons()).thenReturn(List.of(2021));
        when(clubs.findDistinctSeasons()).thenReturn(List.of(2020));
        when(clubBoard.findDistinctSeasons()).thenReturn(List.of(2019));
        var service = new PastSeasonService(high, committees, committeeBoard, clubs, clubBoard);
        assertEquals(List.of(2024, 2023, 2022, 2021, 2020, 2019), service.getSeasons());
    }
}
