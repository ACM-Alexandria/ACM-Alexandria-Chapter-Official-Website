package com.acm.acmwebsite.service.past;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.feature.entity.*;
import com.acm.acmwebsite.feature.entity.past.*;
import com.acm.acmwebsite.feature.exception.BoardArchiveConflictException;
import com.acm.acmwebsite.feature.mapper.PastBoardSnapshotMapper;
import com.acm.acmwebsite.feature.repository.*;
import com.acm.acmwebsite.feature.repository.past.*;
import com.acm.acmwebsite.feature.service.past.PastBoardSnapshotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PastBoardSnapshotServiceTest {
    private final CommitteeRepository committees = mock(CommitteeRepository.class);
    private final ClubRepository clubs = mock(ClubRepository.class);
    private final HighBoardRepository high = mock(HighBoardRepository.class);
    private final CommitteeBoardRepository committeeBoard = mock(CommitteeBoardRepository.class);
    private final ClubBoardRepository clubBoard = mock(ClubBoardRepository.class);
    private final PastCommitteeRepository pastCommittees = mock(PastCommitteeRepository.class);
    private final PastClubRepository pastClubs = mock(PastClubRepository.class);
    private final PastHighBoardRepository pastHigh = mock(PastHighBoardRepository.class);
    private final PastCommitteeBoardRepository pastCommitteeBoard = mock(PastCommitteeBoardRepository.class);
    private final PastClubBoardRepository pastClubBoard = mock(PastClubBoardRepository.class);
    private final PastBoardSnapshotService service = new PastBoardSnapshotService(committees, clubs, high,
            committeeBoard, clubBoard, pastCommittees, pastClubs, pastHigh, pastCommitteeBoard, pastClubBoard,
            Mappers.getMapper(PastBoardSnapshotMapper.class));

    @ParameterizedTest
    @ValueSource(strings = {"high", "committees", "committeeBoard", "clubs", "clubBoard"})
    void anyExistingPastRowsBlockTheEntireSnapshot(String table) {
        switch (table) {
            case "high" -> when(pastHigh.existsBySeason(2026)).thenReturn(true);
            case "committees" -> when(pastCommittees.existsBySeason(2026)).thenReturn(true);
            case "committeeBoard" -> when(pastCommitteeBoard.existsBySeason(2026)).thenReturn(true);
            case "clubs" -> when(pastClubs.existsBySeason(2026)).thenReturn(true);
            case "clubBoard" -> when(pastClubBoard.existsBySeason(2026)).thenReturn(true);
        }
        assertThrows(BoardArchiveConflictException.class, () -> service.snapshot(2026));
        verifyNoInteractions(committees, clubs, high, committeeBoard, clubBoard);
        verify(pastCommittees, never()).save(any());
        verify(pastClubs, never()).save(any());
        verify(pastHigh, never()).saveAll(any());
        verify(pastCommitteeBoard, never()).saveAll(any());
        verify(pastClubBoard, never()).saveAll(any());
    }

    @Test
    void copiesTextAndDescriptionsLinksParentsAndPreservesBoardOrder() {
        var committee = new Committee();
        committee.setId(1L);
        committee.setName("Committee");
        committee.setDescription("Committee description");
        committee.setLogoUrl("https://example.org/committee");
        var club = new Club();
        club.setId(2L);
        club.setName("Club");
        club.setDescription("Club description");
        club.setImageUrl("https://example.org/club");
        var user = new User();
        user.setName("Copied name");
        user.setRole(Role.ACM_HIGH_BOARD);
        user.setProfileImageUrl("https://example.org/photo");
        user.setLinkedinUrl("https://linkedin.com/in/member");
        when(committees.findAll()).thenReturn(List.of(committee));
        when(clubs.findAll()).thenReturn(List.of(club));
        when(pastCommittees.save(any())).thenAnswer(call -> {
            PastCommittee entity = call.getArgument(0);
            entity.setId(101L);
            return entity;
        });
        when(pastClubs.save(any())).thenAnswer(call -> {
            PastClub entity = call.getArgument(0);
            entity.setId(102L);
            return entity;
        });
        when(high.findAllWithUser()).thenReturn(List.of(new HighBoard(3L, "No order", null, user),
                new HighBoard(2L, "Second", 1, user), new HighBoard(1L, "First", 1, user)));
        when(committeeBoard.findAllWithUserAndCommittee()).thenReturn(List.of(new CommitteeBoard(1L, "Head", 2, committee, user)));
        when(clubBoard.findAllWithUserAndClub()).thenReturn(List.of(new ClubBoard(1L, "Coordinator", 3, club, user)));
        List<PastHighBoard> savedHigh = new ArrayList<>();
        doAnswer(call -> { ((Iterable<PastHighBoard>) call.getArgument(0)).forEach(savedHigh::add); return savedHigh; })
                .when(pastHigh).saveAll(any());
        List<PastCommitteeBoard> savedCommittee = new ArrayList<>();
        doAnswer(call -> { ((Iterable<PastCommitteeBoard>) call.getArgument(0)).forEach(savedCommittee::add); return savedCommittee; })
                .when(pastCommitteeBoard).saveAll(any());
        List<PastClubBoard> savedClub = new ArrayList<>();
        doAnswer(call -> { ((Iterable<PastClubBoard>) call.getArgument(0)).forEach(savedClub::add); return savedClub; })
                .when(pastClubBoard).saveAll(any());

        var summary = service.snapshot(2026);

        assertEquals(3, summary.getHighBoardMembers());
        assertEquals(1, summary.getCommittees());
        assertEquals(1, summary.getClubs());
        assertEquals(1, summary.getCommitteeBoardMembers());
        assertEquals(1, summary.getClubBoardMembers());
        assertEquals(List.of("First", "Second", "No order"), savedHigh.stream().map(PastHighBoard::getRole).toList());
        assertEquals("Copied name", savedHigh.getFirst().getName());
        assertEquals(2026, savedHigh.getFirst().getSeason());
        assertEquals(user.getProfileImageUrl(), savedHigh.getFirst().getImageUrl());
        assertEquals(user.getLinkedinUrl(), savedHigh.getFirst().getLinkedinUrl());
        assertNull(savedHigh.getLast().getDisplayOrder());
        var savedParent = savedCommittee.getFirst().getPastCommittee();
        assertEquals(101L, savedParent.getId());
        assertEquals("Committee description", savedParent.getDescription());
        assertEquals(committee.getLogoUrl(), savedParent.getLogoUrl());
        assertEquals(2026, savedParent.getSeason());
        assertEquals("Head", savedCommittee.getFirst().getRole());
        assertEquals(2, savedCommittee.getFirst().getDisplayOrder());
        assertEquals(102L, savedClub.getFirst().getPastClub().getId());
        assertEquals("Club description", savedClub.getFirst().getPastClub().getDescription());
        assertEquals(club.getImageUrl(), savedClub.getFirst().getPastClub().getLogoUrl());
        assertEquals("Coordinator", savedClub.getFirst().getRole());
        assertEquals(3, savedClub.getFirst().getDisplayOrder());
        user.setName("Changed afterwards");
        assertEquals("Copied name", savedHigh.getFirst().getName());
    }
}
