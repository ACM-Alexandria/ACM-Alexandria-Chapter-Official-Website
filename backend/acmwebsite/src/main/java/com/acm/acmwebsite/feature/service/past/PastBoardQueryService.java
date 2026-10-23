package com.acm.acmwebsite.feature.service.past;

import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.dto.past.PastBoardResponse;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PastBoardQueryService {
    private final PastSeasonService seasons;
    private final SystemSettingsService settings;
    private final PastHighBoardService highBoard;
    private final PastCommitteeService committees;
    private final PastCommitteeBoardService committeeBoard;
    private final PastClubService clubs;
    private final PastClubBoardService clubBoard;
    private final PastSeasonPolicy seasonPolicy;

    @Cacheable(value = CacheNames.PAST_BOARDS, key = "#season")
    @Transactional
    public PastBoardResponse getBySeason(Integer season) {
        seasonPolicy.validateStartingYear(season);
        if (!seasons.getSeasons().contains(season) || season >= settings.getCurrentSeason()) {
            throw new ResourceNotFoundException("Past boards not found for season " + season);
        }
        var committeeMembers = committeeBoard.list(season);
        var clubMembers = clubBoard.list(season);
        var pastCommittees = committees.list(season);
        for (var committee : pastCommittees) {
            committee.setBoardRoles(committeeMembers.stream()
                    .filter(member -> Objects.equals(member.getPastCommitteeId(), committee.getId()))
                    .toList());
        }
        var pastClubs = clubs.list(season);
        for (var club : pastClubs) {
            club.setBoardRoles(clubMembers.stream()
                    .filter(member -> Objects.equals(member.getPastClubId(), club.getId()))
                    .toList());
        }
        return PastBoardResponse.builder().season(season)
                .highBoard(highBoard.list(season))
                .committees(pastCommittees).clubs(pastClubs).build();
    }
}
