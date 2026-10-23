package com.acm.acmwebsite.service.past;

import com.acm.acmwebsite.core.config.CacheConfig;
import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.dto.past.*;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import com.acm.acmwebsite.feature.service.past.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig({CacheConfig.class, PastBoardQueryService.class, PastSeasonPolicy.class})
class PastBoardQueryServiceTest {
    @Autowired private PastBoardQueryService service;
    @Autowired private CacheManager caches;
    @MockitoBean private PastSeasonService seasons;
    @MockitoBean private SystemSettingsService settings;
    @MockitoBean private PastHighBoardService high;
    @MockitoBean private PastCommitteeService committees;
    @MockitoBean private PastCommitteeBoardService committeeBoard;
    @MockitoBean private PastClubService clubs;
    @MockitoBean private PastClubBoardService clubBoard;

    @BeforeEach
    void clearCache() {
        caches.getCache(CacheNames.PAST_BOARDS).clear();
    }

    @Test
    void reusesCrudResponsesGroupsMembersAndCachesTheComposedBoard() {
        when(seasons.getSeasons()).thenReturn(List.of(2025));
        when(settings.getCurrentSeason()).thenReturn(2026);
        var committee = new PastCommitteeResponse();
        committee.setId(1L);
        committee.setName("Technical");
        committee.setLogoUrl("https://example.org/committee.png");
        var empty = new PastCommitteeResponse();
        empty.setId(2L);
        empty.setName("Boardless");
        var club = new PastClubResponse();
        club.setId(1L);
        club.setName("Robotics");
        club.setLogoUrl("https://example.org/club.png");
        var head = new PastCommitteeBoardResponse();
        head.setId(1L);
        head.setPastCommitteeId(1L);
        head.setUserName("Committee head");
        head.setRole("Head");
        head.setOrder(1);
        head.setProfileImageUrl("https://example.org/head.png");
        head.setLinkedinUrl("https://linkedin.com/in/head");
        var mentor = new PastCommitteeBoardResponse();
        mentor.setId(2L);
        mentor.setPastCommitteeId(1L);
        mentor.setRole("Mentor");
        var lead = new PastClubBoardResponse();
        lead.setId(3L);
        lead.setPastClubId(1L);
        lead.setUserName("Club lead");
        var chair = new PastHighBoardResponse();
        chair.setId(4L);
        chair.setUserName("Chair");
        when(committees.list(2025)).thenReturn(List.of(committee, empty));
        when(clubs.list(2025)).thenReturn(List.of(club));
        when(committeeBoard.list(2025)).thenReturn(List.of(head, mentor));
        when(clubBoard.list(2025)).thenReturn(List.of(lead));
        when(high.list(2025)).thenReturn(List.of(chair));

        var result = service.getBySeason(2025);

        assertEquals(2025, result.getSeason());
        assertEquals("Chair", result.getHighBoard().getFirst().getUserName());
        assertEquals(2, result.getCommittees().size());
        var group = result.getCommittees().getFirst();
        assertEquals(committee.getLogoUrl(), group.getLogoUrl());
        assertEquals(List.of("Head", "Mentor"), group.getBoardRoles().stream().map(member -> member.getRole()).toList());
        assertEquals(head.getProfileImageUrl(), group.getBoardRoles().getFirst().getProfileImageUrl());
        assertEquals(head.getLinkedinUrl(), group.getBoardRoles().getFirst().getLinkedinUrl());
        assertNull(group.getBoardRoles().getLast().getOrder());
        assertSame(chair, result.getHighBoard().getFirst());
        assertSame(committee, group);
        assertSame(head, group.getBoardRoles().getFirst());
        assertSame(club, result.getClubs().getFirst());
        assertSame(lead, result.getClubs().getFirst().getBoardRoles().getFirst());
        assertTrue(result.getCommittees().getLast().getBoardRoles().isEmpty());
        assertEquals(club.getLogoUrl(), result.getClubs().getFirst().getLogoUrl());
        assertEquals("Club lead", result.getClubs().getFirst().getBoardRoles().getFirst().getUserName());
        assertSame(result, service.getBySeason(2025));
        verify(high, times(1)).list(2025);
        verify(committees, times(1)).list(2025);
        verify(committeeBoard, times(1)).list(2025);
        verify(clubs, times(1)).list(2025);
        verify(clubBoard, times(1)).list(2025);
    }

    @ParameterizedTest
    @ValueSource(ints = {2000, 2024, 2026, 2027, 9999})
    void unknownCurrentAndFutureSeasonsReturnNotFoundAndNeverPopulateCache(int season) {
        // Even manually inserted current/future rows must not become public past boards.
        when(seasons.getSeasons()).thenReturn(List.of(2025, 2026, 2027));
        when(settings.getCurrentSeason()).thenReturn(2026);
        assertThrows(ResourceNotFoundException.class, () -> service.getBySeason(season));
        assertNull(caches.getCache(CacheNames.PAST_BOARDS).get(season));
        verifyNoInteractions(high, committees, committeeBoard, clubs, clubBoard);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1999})
    void invalidSeasonIsRejectedBeforePersistenceAccess(int season) {
        assertThrows(IllegalArgumentException.class, () -> service.getBySeason(season));
        assertNull(caches.getCache(CacheNames.PAST_BOARDS).get(season));
        verifyNoInteractions(seasons, settings, high, committees, committeeBoard, clubs, clubBoard);
    }

    @Test
    void failedCompositionIsNotCached() {
        when(seasons.getSeasons()).thenReturn(List.of(2025));
        when(settings.getCurrentSeason()).thenReturn(2026);
        when(committeeBoard.list(2025)).thenThrow(new IllegalStateException("Database unavailable"));
        assertThrows(IllegalStateException.class, () -> service.getBySeason(2025));
        assertNull(caches.getCache(CacheNames.PAST_BOARDS).get(2025));
    }
}
