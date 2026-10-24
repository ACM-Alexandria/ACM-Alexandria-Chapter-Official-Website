package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.BoardArchiveSummary;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastClubBoardRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastClubRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeBoardRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastHighBoardRequest;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.ClubBoard;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.entity.PastClub;
import com.acm.acmwebsite.feature.entity.PastClubBoard;
import com.acm.acmwebsite.feature.entity.PastCommittee;
import com.acm.acmwebsite.feature.entity.PastCommitteeBoard;
import com.acm.acmwebsite.feature.entity.PastHighBoard;
import com.acm.acmwebsite.feature.entity.SystemSettings;
import com.acm.acmwebsite.feature.event.CurrentBoardsArchivedEvent;
import com.acm.acmwebsite.feature.event.PastBoardsChangedEvent;
import com.acm.acmwebsite.feature.exception.BoardArchiveConflictException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.PastBoardMapper;
import com.acm.acmwebsite.feature.mapper.PastBoardSnapshotMapper;
import com.acm.acmwebsite.feature.repository.ClubBoardRepository;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import com.acm.acmwebsite.feature.repository.CommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import com.acm.acmwebsite.feature.repository.PastClubBoardRepository;
import com.acm.acmwebsite.feature.repository.PastClubRepository;
import com.acm.acmwebsite.feature.repository.PastCommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.PastCommitteeRepository;
import com.acm.acmwebsite.feature.repository.PastHighBoardRepository;
import com.acm.acmwebsite.feature.service.PastSeasonsService;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PastSeasonsServiceTest {
    @Mock private PastHighBoardRepository pastHighBoards;
    @Mock private PastCommitteeRepository pastCommittees;
    @Mock private PastCommitteeBoardRepository pastCommitteeBoards;
    @Mock private PastClubRepository pastClubs;
    @Mock private PastClubBoardRepository pastClubBoards;
    @Mock private CommitteeRepository committees;
    @Mock private ClubRepository clubs;
    @Mock private HighBoardRepository highBoards;
    @Mock private CommitteeBoardRepository committeeBoards;
    @Mock private ClubBoardRepository clubBoards;
    @Mock private SystemSettingsService settings;
    @Mock private UserService userService;
    @Mock private ApplicationEventPublisher events;
    @Mock private EntityManager entityManager;
    @Spy private PastBoardMapper mapper = Mappers.getMapper(PastBoardMapper.class);
    @Spy private PastBoardSnapshotMapper snapshotMapper = Mappers.getMapper(PastBoardSnapshotMapper.class);
    @Spy private CacheManager cacheManager = new ConcurrentMapCacheManager(CacheNames.BOARD_SEASONS,
            CacheNames.PAST_BOARDS, CacheNames.HIGH_BOARD, CacheNames.COMMITTEES, CacheNames.CLUBS, CacheNames.EVENTS);
    @InjectMocks private PastSeasonsService service;

    // ----- seasons and the public board view -----

    @Test
    void getSeasonsUnionsEveryTableAndSortsDistinctYearsDescending() {
        when(pastHighBoards.findDistinctSeasons()).thenReturn(List.of(2022, 2024));
        when(pastCommittees.findDistinctSeasons()).thenReturn(List.of(2023, 2024));
        when(pastCommitteeBoards.findDistinctSeasons()).thenReturn(List.of(2021));
        when(pastClubs.findDistinctSeasons()).thenReturn(List.of(2020));
        when(pastClubBoards.findDistinctSeasons()).thenReturn(List.of(2019));
        assertEquals(List.of(2024, 2023, 2022, 2021, 2020, 2019), service.getSeasons());
    }

    @Test
    void getBySeasonComposesCommitteesAndClubsWithTheirBoardMembers() {
        stubSeasons(2025);
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var committee = new PastCommittee();
        committee.setId(1L);
        committee.setName("Technical");
        committee.setLogoUrl("https://example.org/committee.png");
        var empty = new PastCommittee();
        empty.setId(2L);
        empty.setName("Boardless");
        var club = new PastClub();
        club.setId(1L);
        club.setName("Robotics");
        var head = new PastCommitteeBoard();
        head.setId(1L);
        head.setName("Committee head");
        head.setRole("Head");
        head.setSeason(2025);
        head.setPastCommittee(committee);
        var mentor = new PastCommitteeBoard();
        mentor.setId(2L);
        mentor.setName("Mentor");
        mentor.setRole("Mentor");
        mentor.setSeason(2025);
        mentor.setPastCommittee(committee);
        var lead = new PastClubBoard();
        lead.setId(3L);
        lead.setName("Club lead");
        lead.setRole("Lead");
        lead.setSeason(2025);
        lead.setPastClub(club);
        var chair = new PastHighBoard();
        chair.setId(4L);
        chair.setName("Chair");
        chair.setRole("Chair");
        chair.setSeason(2025);
        when(pastCommittees.findBySeasonOrderByNameAscIdAsc(2025)).thenReturn(List.of(committee, empty));
        when(pastClubs.findBySeasonOrderByNameAscIdAsc(2025)).thenReturn(List.of(club));
        when(pastCommitteeBoards.findBySeasonOrdered(2025)).thenReturn(List.of(head, mentor));
        when(pastClubBoards.findBySeasonOrdered(2025)).thenReturn(List.of(lead));
        when(pastHighBoards.findBySeasonOrdered(2025)).thenReturn(List.of(chair));

        var result = service.getBySeason(2025);

        assertEquals(2025, result.getSeason());
        assertEquals("Chair", result.getHighBoard().getFirst().getUserName());
        assertEquals(2, result.getCommittees().size());
        var group = result.getCommittees().getFirst();
        assertEquals(List.of("Committee head", "Mentor"),
                group.getBoardRoles().stream().map(member -> member.getUserName()).toList());
        assertTrue(result.getCommittees().getLast().getBoardRoles().isEmpty());
        assertEquals("Club lead", result.getClubs().getFirst().getBoardRoles().getFirst().getUserName());
    }

    @ParameterizedTest
    @ValueSource(ints = {2000, 2024, 2026, 2027})
    void getBySeasonRejectsSeasonsThatAreNotPastBoards(int season) {
        stubSeasons(2025, 2026, 2027);
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        assertThrows(ResourceNotFoundException.class, () -> service.getBySeason(season));
        verify(pastCommitteeBoards, never()).findBySeasonOrdered(anyInt());
        verify(pastClubBoards, never()).findBySeasonOrdered(anyInt());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1999})
    void getBySeasonRejectsInvalidYearsBeforeQueryingAnything(int season) {
        assertThrows(IllegalArgumentException.class, () -> service.getBySeason(season));
        verifyNoInteractions(pastHighBoards, pastCommittees, pastCommitteeBoards, pastClubs, pastClubBoards, settings);
    }

    @Test
    void getBySeasonPropagatesRepositoryFailures() {
        stubSeasons(2025);
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        when(pastCommitteeBoards.findBySeasonOrdered(2025)).thenThrow(new IllegalStateException("Database unavailable"));
        assertThrows(IllegalStateException.class, () -> service.getBySeason(2025));
    }

    // ----- archiving -----

    @Test
    void archiveLocksAndRunsEveryStepInOrderBeforePublishingEvents() {
        var row = new SystemSettings(1L, false, 2026);
        when(settings.lockForArchive()).thenReturn(row);
        when(userService.demoteBoardMembers()).thenReturn(3);
        when(settings.incrementCurrentSeason(row)).thenReturn(2027);

        var summary = service.archive(2026);

        assertEquals(2026, summary.getArchivedSeason());
        assertEquals(3, summary.getUsersDowngraded());
        assertEquals(2027, summary.getNewCurrentSeason());
        var order = inOrder(settings, userService, entityManager, highBoards, events);
        order.verify(settings).lockForArchive();
        order.verify(userService).demoteBoardMembers();
        order.verify(entityManager).flush();
        order.verify(highBoards).deleteAllInBatch();
        order.verify(entityManager).clear();
        order.verify(settings).incrementCurrentSeason(row);
        order.verify(events).publishEvent(new PastBoardsChangedEvent());
        order.verify(events).publishEvent(new CurrentBoardsArchivedEvent());
    }

    @Test
    void staleConfirmationStopsBeforeAnyBoardWork() {
        when(settings.lockForArchive()).thenReturn(new SystemSettings(1L, true, 2027));
        var error = assertThrows(BoardArchiveConflictException.class, () -> service.archive(2026));
        assertTrue(error.getMessage().contains("2027"));
        verifyNoInteractions(pastHighBoards, userService, entityManager, highBoards, events);
    }

    @Test
    void existingArchiveStopsBeforeDemotionAndClearing() {
        when(settings.lockForArchive()).thenReturn(new SystemSettings(1L, true, 2026));
        when(pastHighBoards.existsBySeason(2026)).thenReturn(true);
        assertThrows(BoardArchiveConflictException.class, () -> service.archive(2026));
        verifyNoInteractions(userService, entityManager, events);
        verify(settings, never()).incrementCurrentSeason(any());
    }

    @Test
    void failureAfterClearingNeverPublishesEvents() {
        var row = new SystemSettings(1L, true, 2026);
        when(settings.lockForArchive()).thenReturn(row);
        when(settings.incrementCurrentSeason(row)).thenThrow(new IllegalStateException("Save failed"));
        assertThrows(IllegalStateException.class, () -> service.archive(2026));
        verify(entityManager).flush();
        verifyNoInteractions(events);
    }

    @Test
    void snapshotCopiesTextDescriptionsLinksParentsAndPreservesBoardOrder() {
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
        when(highBoards.findAllWithUser()).thenReturn(List.of(new HighBoard(3L, "No order", null, user),
                new HighBoard(2L, "Second", 1, user), new HighBoard(1L, "First", 1, user)));
        when(committeeBoards.findAllWithUserAndCommittee()).thenReturn(List.of(new CommitteeBoard(1L, "Head", 2, committee, user)));
        when(clubBoards.findAllWithUserAndClub()).thenReturn(List.of(new ClubBoard(1L, "Coordinator", 3, club, user)));
        List<PastHighBoard> savedHigh = new ArrayList<>();
        doAnswer(call -> { ((Iterable<PastHighBoard>) call.getArgument(0)).forEach(savedHigh::add); return savedHigh; })
                .when(pastHighBoards).saveAll(any());
        List<PastCommitteeBoard> savedCommittee = new ArrayList<>();
        doAnswer(call -> { ((Iterable<PastCommitteeBoard>) call.getArgument(0)).forEach(savedCommittee::add); return savedCommittee; })
                .when(pastCommitteeBoards).saveAll(any());
        List<PastClubBoard> savedClub = new ArrayList<>();
        doAnswer(call -> { ((Iterable<PastClubBoard>) call.getArgument(0)).forEach(savedClub::add); return savedClub; })
                .when(pastClubBoards).saveAll(any());

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

    @ParameterizedTest
    @ValueSource(strings = {"high", "committees", "committeeBoard", "clubs", "clubBoard"})
    void anyExistingPastRowsBlockTheEntireSnapshot(String table) {
        switch (table) {
            case "high" -> when(pastHighBoards.existsBySeason(2026)).thenReturn(true);
            case "committees" -> when(pastCommittees.existsBySeason(2026)).thenReturn(true);
            case "committeeBoard" -> when(pastCommitteeBoards.existsBySeason(2026)).thenReturn(true);
            case "clubs" -> when(pastClubs.existsBySeason(2026)).thenReturn(true);
            case "clubBoard" -> when(pastClubBoards.existsBySeason(2026)).thenReturn(true);
        }
        assertThrows(BoardArchiveConflictException.class, () -> service.snapshot(2026));
        verifyNoInteractions(committees, clubs, highBoards, committeeBoards, clubBoards);
        verify(pastCommittees, never()).save(any());
        verify(pastClubs, never()).save(any());
        verify(pastHighBoards, never()).saveAll(any());
        verify(pastCommitteeBoards, never()).saveAll(any());
        verify(pastClubBoards, never()).saveAll(any());
    }

    @Test
    void clearBoardsFlushesBeforeBulkDeletionAndClearsManagedEntitiesAfterwards() {
        service.clearBoards();
        var order = inOrder(entityManager, highBoards, committeeBoards, clubBoards);
        order.verify(entityManager).flush();
        order.verify(highBoards).deleteAllInBatch();
        order.verify(committeeBoards).deleteAllInBatch();
        order.verify(clubBoards).deleteAllInBatch();
        order.verify(entityManager).clear();
        verifyNoMoreInteractions(entityManager, highBoards, committeeBoards, clubBoards);
    }

    // ----- cache invalidation -----

    @Test
    void pastBoardChangesClearPastCachesAndLeaveLiveCachesAlone() {
        for (String cache : cacheManager.getCacheNames()) cacheManager.getCache(cache).put(2025, "cached");
        service.onPastBoardsChanged(new PastBoardsChangedEvent());
        assertNull(cacheManager.getCache(CacheNames.BOARD_SEASONS).get(2025));
        assertNull(cacheManager.getCache(CacheNames.PAST_BOARDS).get(2025));
        assertNotNull(cacheManager.getCache(CacheNames.HIGH_BOARD).get(2025));
    }

    @Test
    void archiveEventClearsTheThreeLiveCachesAndLeavesUnrelatedCachesAlone() {
        for (String name : cacheManager.getCacheNames()) cacheManager.getCache(name).put("probe", "cached");
        service.onCurrentBoardsArchived(new CurrentBoardsArchivedEvent());
        assertNull(cacheManager.getCache(CacheNames.HIGH_BOARD).get("probe"));
        assertNull(cacheManager.getCache(CacheNames.COMMITTEES).get("probe"));
        assertNull(cacheManager.getCache(CacheNames.CLUBS).get("probe"));
        assertNotNull(cacheManager.getCache(CacheNames.EVENTS).get("probe"));
    }

    // ----- season validation -----

    @Test
    void acceptsOnlyYearsBetween2000AndCurrentSeasonExclusive() {
        when(settings.getCurrentSeason()).thenReturn(2026);
        assertDoesNotThrow(() -> service.validate(2000));
        assertDoesNotThrow(() -> service.validate(2025));
        for (Integer season : new Integer[]{null, 1999, 2026, 2027}) {
            assertThrows(IllegalArgumentException.class, () -> service.validate(season));
        }
    }

    @Test
    void readsTheCurrentSeasonAgainAfterItChanges() {
        when(settings.getCurrentSeason()).thenReturn(2026, 2027);
        assertThrows(IllegalArgumentException.class, () -> service.validate(2026));
        assertDoesNotThrow(() -> service.validate(2026));
    }

    @Test
    void updateRequiresTheOriginalSeasonToRemainPast() {
        when(settings.getCurrentSeason()).thenReturn(2026);
        assertDoesNotThrow(() -> service.validateUpdate(2025, 2025));
        assertThrows(IllegalArgumentException.class, () -> service.validateUpdate(2024, 2025));
        assertThrows(IllegalArgumentException.class, () -> service.validateUpdate(2026, 2026));
        assertThrows(IllegalArgumentException.class, () -> service.validateUpdate(null, null));
    }

    @Test
    void memberAndParentMustBelongToTheSameSeason() {
        assertDoesNotThrow(() -> service.validateSameSeason(2025, 2025));
        assertThrows(IllegalArgumentException.class, () -> service.validateSameSeason(2024, 2025));
    }

    // ----- past clubs -----

    @Test
    void listPastClubsUsesTheOrderedFinderAndMapsInsideTheService() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var entity = new PastClub();
        entity.setSeason(2025);
        entity.setName("Stored name");
        when(pastClubs.findBySeasonOrderByNameAscIdAsc(2025)).thenReturn(List.of(entity));
        var result = service.listPastClubs(2025);
        assertEquals("Stored name", result.getFirst().getName());
        verifyNoInteractions(events);
    }

    @Test
    void createPastClubSnapshotsAndPublishesChange() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var request = clubRequest(2025);
        when(pastClubs.save(any())).thenAnswer(call -> {
            PastClub entity = call.getArgument(0);
            entity.setId(9L);
            return entity;
        });
        var response = service.createPastClub(request);
        assertEquals(9L, response.getId());
        assertEquals(2025, response.getSeason());
        assertEquals("Snapshot name", response.getName());
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void updatePastClubUpdatesExistingRecordAndAllowsOptionalFieldsToBeCleared() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var entity = new PastClub();
        entity.setId(9L);
        entity.setSeason(2025);
        entity.setLogoUrl("https://example.org/old");
        var request = clubRequest(2025);
        request.setLogoUrl(null);
        when(pastClubs.findById(9L)).thenReturn(Optional.of(entity));
        when(pastClubs.save(entity)).thenReturn(entity);
        var result = service.updatePastClub(9L, request);
        assertEquals(2025, result.getSeason());
        assertNull(result.getLogoUrl());
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void updatePastClubRejectsChangedSeasonBeforeSaving() {
        var entity = new PastClub();
        entity.setSeason(2024);
        when(pastClubs.findById(9L)).thenReturn(Optional.of(entity));
        assertThrows(IllegalArgumentException.class, () -> service.updatePastClub(9L, clubRequest(2025)));
        verify(pastClubs, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void deletePastClubValidatesSeasonAndPublishesChange() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var entity = new PastClub();
        entity.setSeason(2025);
        when(pastClubs.findById(9L)).thenReturn(Optional.of(entity));
        service.deletePastClub(9L);
        verify(pastClubs).delete(entity);
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void invalidSeasonCannotCreatePastClub() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2024);
        assertThrows(IllegalArgumentException.class, () -> service.createPastClub(clubRequest(2025)));
        verifyNoInteractions(pastClubs, events);
    }

    @Test
    void missingPastClubUpdateAndDeleteReturnNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.updatePastClub(9L, clubRequest(2025)));
        assertThrows(ResourceNotFoundException.class, () -> service.deletePastClub(9L));
        verifyNoInteractions(events);
    }

    @Test
    void getPastClubByIdReturnsTheStoredEntityOrNotFound() {
        var entity = new PastClub();
        entity.setId(7L);
        when(pastClubs.findById(7L)).thenReturn(Optional.of(entity));
        assertSame(entity, service.getPastClubById(7L));
        assertThrows(ResourceNotFoundException.class, () -> service.getPastClubById(8L));
        verifyNoInteractions(events);
    }

    // ----- past club board members -----

    @Test
    void createPastClubBoardSnapshotsUnderItsParentAndPublishesChange() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        parentClubAtSeason(2025);
        when(pastClubBoards.save(any())).thenAnswer(call -> {
            PastClubBoard entity = call.getArgument(0);
            entity.setId(9L);
            return entity;
        });
        var response = service.createPastClubBoard(clubBoardRequest(2025));
        assertEquals(9L, response.getId());
        assertEquals(2025, response.getSeason());
        assertEquals("Snapshot name", response.getUserName());
        assertEquals(7L, response.getPastClubId());
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void listPastClubBoardsUsesTheOrderedFinderAndMapsInsideTheService() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var entity = new PastClubBoard();
        entity.setSeason(2025);
        entity.setName("Stored name");
        when(pastClubBoards.findBySeasonOrdered(2025)).thenReturn(List.of(entity));
        assertEquals("Stored name", service.listPastClubBoards(2025).getFirst().getUserName());
        verifyNoInteractions(events);
    }

    @Test
    void updatePastClubBoardUpdatesExistingRecordAndAllowsOptionalFieldsToBeCleared() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        parentClubAtSeason(2025);
        var entity = new PastClubBoard();
        entity.setId(9L);
        entity.setSeason(2025);
        entity.setImageUrl("https://example.org/old");
        var request = clubBoardRequest(2025);
        request.setImageUrl(null);
        when(pastClubBoards.findById(9L)).thenReturn(Optional.of(entity));
        when(pastClubBoards.save(entity)).thenReturn(entity);
        var result = service.updatePastClubBoard(9L, request);
        assertEquals(2025, result.getSeason());
        assertNull(result.getProfileImageUrl());
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void deletePastClubBoardValidatesSeasonAndPublishesChange() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var entity = new PastClubBoard();
        entity.setSeason(2025);
        when(pastClubBoards.findById(9L)).thenReturn(Optional.of(entity));
        service.deletePastClubBoard(9L);
        verify(pastClubBoards).delete(entity);
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void clubBoardRejectsMissingParent() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        when(pastClubs.findById(7L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createPastClubBoard(clubBoardRequest(2025)));
        verify(pastClubBoards, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void clubBoardRejectsParentFromAnotherSeasonForCreateAndUpdate() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        parentClubAtSeason(2024);
        assertThrows(IllegalArgumentException.class, () -> service.createPastClubBoard(clubBoardRequest(2025)));
        var entity = new PastClubBoard();
        entity.setSeason(2025);
        when(pastClubBoards.findById(9L)).thenReturn(Optional.of(entity));
        assertThrows(IllegalArgumentException.class, () -> service.updatePastClubBoard(9L, clubBoardRequest(2025)));
        verify(pastClubBoards, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void failedParentUpdateLeavesTheExistingClubMemberUnchanged() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        parentClubAtSeason(2024);
        var entity = new PastClubBoard();
        entity.setSeason(2025);
        entity.setName("Original name");
        var originalParent = new PastClub();
        entity.setPastClub(originalParent);
        when(pastClubBoards.findById(9L)).thenReturn(Optional.of(entity));

        assertThrows(IllegalArgumentException.class, () -> service.updatePastClubBoard(9L, clubBoardRequest(2025)));

        assertEquals("Original name", entity.getName());
        assertSame(originalParent, entity.getPastClub());
        verify(pastClubBoards, never()).save(any());
        verifyNoInteractions(events);
    }

    // ----- past committees -----

    @Test
    void listPastCommitteesUsesTheOrderedFinderAndMapsInsideTheService() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var entity = new PastCommittee();
        entity.setSeason(2025);
        entity.setName("Stored name");
        when(pastCommittees.findBySeasonOrderByNameAscIdAsc(2025)).thenReturn(List.of(entity));
        assertEquals("Stored name", service.listPastCommittees(2025).getFirst().getName());
        verifyNoInteractions(events);
    }

    @Test
    void createPastCommitteeSnapshotsAndPublishesChange() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var request = committeeRequest(2025);
        when(pastCommittees.save(any())).thenAnswer(call -> {
            PastCommittee entity = call.getArgument(0);
            entity.setId(9L);
            return entity;
        });
        var response = service.createPastCommittee(request);
        assertEquals(9L, response.getId());
        assertEquals(2025, response.getSeason());
        assertEquals("Snapshot name", response.getName());
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void updatePastCommitteeUpdatesExistingRecordAndAllowsOptionalFieldsToBeCleared() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var entity = new PastCommittee();
        entity.setId(9L);
        entity.setSeason(2025);
        entity.setLogoUrl("https://example.org/old");
        var request = committeeRequest(2025);
        request.setLogoUrl(null);
        when(pastCommittees.findById(9L)).thenReturn(Optional.of(entity));
        when(pastCommittees.save(entity)).thenReturn(entity);
        var result = service.updatePastCommittee(9L, request);
        assertEquals(2025, result.getSeason());
        assertNull(result.getLogoUrl());
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void updatePastCommitteeRejectsChangedSeasonBeforeSaving() {
        var entity = new PastCommittee();
        entity.setSeason(2024);
        when(pastCommittees.findById(9L)).thenReturn(Optional.of(entity));
        assertThrows(IllegalArgumentException.class, () -> service.updatePastCommittee(9L, committeeRequest(2025)));
        verify(pastCommittees, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void deletePastCommitteeValidatesSeasonAndPublishesChange() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var entity = new PastCommittee();
        entity.setSeason(2025);
        when(pastCommittees.findById(9L)).thenReturn(Optional.of(entity));
        service.deletePastCommittee(9L);
        verify(pastCommittees).delete(entity);
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void invalidSeasonCannotCreatePastCommittee() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2024);
        assertThrows(IllegalArgumentException.class, () -> service.createPastCommittee(committeeRequest(2025)));
        verifyNoInteractions(pastCommittees, events);
    }

    @Test
    void missingPastCommitteeUpdateAndDeleteReturnNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.updatePastCommittee(9L, committeeRequest(2025)));
        assertThrows(ResourceNotFoundException.class, () -> service.deletePastCommittee(9L));
        verifyNoInteractions(events);
    }

    @Test
    void getPastCommitteeByIdReturnsTheStoredEntityWithoutPublishingAChange() {
        var entity = new PastCommittee();
        entity.setId(7L);
        when(pastCommittees.findById(7L)).thenReturn(Optional.of(entity));
        assertSame(entity, service.getPastCommitteeById(7L));
        verifyNoInteractions(events);
    }

    @Test
    void getPastCommitteeByIdRejectsMissingRecords() {
        assertThrows(ResourceNotFoundException.class, () -> service.getPastCommitteeById(7L));
        verifyNoInteractions(events);
    }

    // ----- past committee board members -----

    @Test
    void createPastCommitteeBoardSnapshotsUnderItsParentAndPublishesChange() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        parentCommitteeAtSeason(2025);
        when(pastCommitteeBoards.save(any())).thenAnswer(call -> {
            PastCommitteeBoard entity = call.getArgument(0);
            entity.setId(9L);
            return entity;
        });
        var response = service.createPastCommitteeBoard(committeeBoardRequest(2025));
        assertEquals(9L, response.getId());
        assertEquals(2025, response.getSeason());
        assertEquals("Snapshot name", response.getUserName());
        assertEquals(7L, response.getPastCommitteeId());
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void listPastCommitteeBoardsUsesTheOrderedFinderAndMapsInsideTheService() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var entity = new PastCommitteeBoard();
        entity.setSeason(2025);
        entity.setName("Stored name");
        when(pastCommitteeBoards.findBySeasonOrdered(2025)).thenReturn(List.of(entity));
        assertEquals("Stored name", service.listPastCommitteeBoards(2025).getFirst().getUserName());
        verifyNoInteractions(events);
    }

    @Test
    void updatePastCommitteeBoardUpdatesExistingRecordAndAllowsOptionalFieldsToBeCleared() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        parentCommitteeAtSeason(2025);
        var entity = new PastCommitteeBoard();
        entity.setId(9L);
        entity.setSeason(2025);
        entity.setImageUrl("https://example.org/old");
        var request = committeeBoardRequest(2025);
        request.setImageUrl(null);
        when(pastCommitteeBoards.findById(9L)).thenReturn(Optional.of(entity));
        when(pastCommitteeBoards.save(entity)).thenReturn(entity);
        var result = service.updatePastCommitteeBoard(9L, request);
        assertEquals(2025, result.getSeason());
        assertNull(result.getProfileImageUrl());
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void deletePastCommitteeBoardValidatesSeasonAndPublishesChange() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var entity = new PastCommitteeBoard();
        entity.setSeason(2025);
        when(pastCommitteeBoards.findById(9L)).thenReturn(Optional.of(entity));
        service.deletePastCommitteeBoard(9L);
        verify(pastCommitteeBoards).delete(entity);
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void committeeBoardRejectsMissingParent() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        when(pastCommittees.findById(7L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createPastCommitteeBoard(committeeBoardRequest(2025)));
        verify(pastCommitteeBoards, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void committeeBoardRejectsParentFromAnotherSeasonForCreateAndUpdate() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        parentCommitteeAtSeason(2024);
        assertThrows(IllegalArgumentException.class, () -> service.createPastCommitteeBoard(committeeBoardRequest(2025)));
        var entity = new PastCommitteeBoard();
        entity.setSeason(2025);
        when(pastCommitteeBoards.findById(9L)).thenReturn(Optional.of(entity));
        assertThrows(IllegalArgumentException.class, () -> service.updatePastCommitteeBoard(9L, committeeBoardRequest(2025)));
        verify(pastCommitteeBoards, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void failedParentUpdateLeavesTheExistingCommitteeMemberUnchanged() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        parentCommitteeAtSeason(2024);
        var entity = new PastCommitteeBoard();
        entity.setSeason(2025);
        entity.setName("Original name");
        var originalParent = new PastCommittee();
        entity.setPastCommittee(originalParent);
        when(pastCommitteeBoards.findById(9L)).thenReturn(Optional.of(entity));

        assertThrows(IllegalArgumentException.class, () -> service.updatePastCommitteeBoard(9L, committeeBoardRequest(2025)));

        assertEquals("Original name", entity.getName());
        assertSame(originalParent, entity.getPastCommittee());
        verify(pastCommitteeBoards, never()).save(any());
        verifyNoInteractions(events);
    }

    // ----- past high board -----

    @Test
    void listPastHighBoardsUsesTheOrderedFinderAndMapsInsideTheService() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var entity = new PastHighBoard();
        entity.setSeason(2025);
        entity.setName("Stored name");
        when(pastHighBoards.findBySeasonOrdered(2025)).thenReturn(List.of(entity));
        assertEquals("Stored name", service.listPastHighBoards(2025).getFirst().getUserName());
        verifyNoInteractions(events);
    }

    @Test
    void createPastHighBoardSnapshotsAndPublishesChange() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var request = highRequest(2025);
        when(pastHighBoards.save(any())).thenAnswer(call -> {
            PastHighBoard entity = call.getArgument(0);
            entity.setId(9L);
            return entity;
        });
        var response = service.createPastHighBoard(request);
        assertEquals(9L, response.getId());
        assertEquals(2025, response.getSeason());
        assertEquals("Snapshot name", response.getUserName());
        assertEquals("Chair", response.getRole());
        assertEquals(2, response.getOrder());
        assertEquals("https://example.org/photo", response.getProfileImageUrl());
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void updatePastHighBoardUpdatesExistingRecordAndAllowsOptionalFieldsToBeCleared() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var entity = new PastHighBoard();
        entity.setId(9L);
        entity.setSeason(2025);
        entity.setImageUrl("https://example.org/old");
        var request = highRequest(2025);
        request.setImageUrl(null);
        when(pastHighBoards.findById(9L)).thenReturn(Optional.of(entity));
        when(pastHighBoards.save(entity)).thenReturn(entity);
        var result = service.updatePastHighBoard(9L, request);
        assertEquals(2025, result.getSeason());
        assertNull(result.getProfileImageUrl());
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void updatePastHighBoardRejectsChangedSeasonBeforeSaving() {
        var entity = new PastHighBoard();
        entity.setSeason(2024);
        when(pastHighBoards.findById(9L)).thenReturn(Optional.of(entity));
        assertThrows(IllegalArgumentException.class, () -> service.updatePastHighBoard(9L, highRequest(2025)));
        verify(pastHighBoards, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void deletePastHighBoardValidatesSeasonAndPublishesChange() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2026);
        var entity = new PastHighBoard();
        entity.setSeason(2025);
        when(pastHighBoards.findById(9L)).thenReturn(Optional.of(entity));
        service.deletePastHighBoard(9L);
        verify(pastHighBoards).delete(entity);
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void invalidSeasonCannotCreatePastHighBoard() {
        lenient().when(settings.getCurrentSeason()).thenReturn(2024);
        assertThrows(IllegalArgumentException.class, () -> service.createPastHighBoard(highRequest(2025)));
        verifyNoInteractions(pastHighBoards, events);
    }

    @Test
    void missingPastHighBoardUpdateAndDeleteReturnNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.updatePastHighBoard(9L, highRequest(2025)));
        assertThrows(ResourceNotFoundException.class, () -> service.deletePastHighBoard(9L));
        verifyNoInteractions(events);
    }

    // ----- helpers -----

    private void stubSeasons(Integer... seasons) {
        List<Integer> years = List.of(seasons);
        lenient().when(pastHighBoards.findDistinctSeasons()).thenReturn(years);
        lenient().when(pastCommittees.findDistinctSeasons()).thenReturn(years);
        lenient().when(pastCommitteeBoards.findDistinctSeasons()).thenReturn(years);
        lenient().when(pastClubs.findDistinctSeasons()).thenReturn(years);
        lenient().when(pastClubBoards.findDistinctSeasons()).thenReturn(years);
    }

    private void parentClubAtSeason(int season) {
        var parent = new PastClub();
        parent.setId(7L);
        parent.setSeason(season);
        when(pastClubs.findById(7L)).thenReturn(Optional.of(parent));
    }

    private void parentCommitteeAtSeason(int season) {
        var parent = new PastCommittee();
        parent.setId(7L);
        parent.setSeason(season);
        when(pastCommittees.findById(7L)).thenReturn(Optional.of(parent));
    }

    private PastClubRequest clubRequest(int season) {
        var request = new PastClubRequest();
        request.setSeason(season);
        request.setName("Snapshot name");
        return request;
    }

    private PastCommitteeRequest committeeRequest(int season) {
        var request = new PastCommitteeRequest();
        request.setSeason(season);
        request.setName("Snapshot name");
        return request;
    }

    private PastClubBoardRequest clubBoardRequest(int season) {
        var request = new PastClubBoardRequest();
        request.setSeason(season);
        request.setName("Snapshot name");
        request.setRole("Chair");
        request.setDisplayOrder(2);
        request.setImageUrl("https://example.org/photo");
        request.setPastClubId(7L);
        return request;
    }

    private PastCommitteeBoardRequest committeeBoardRequest(int season) {
        var request = new PastCommitteeBoardRequest();
        request.setSeason(season);
        request.setName("Snapshot name");
        request.setRole("Chair");
        request.setDisplayOrder(2);
        request.setImageUrl("https://example.org/photo");
        request.setPastCommitteeId(7L);
        return request;
    }

    private PastHighBoardRequest highRequest(int season) {
        var request = new PastHighBoardRequest();
        request.setSeason(season);
        request.setName("Snapshot name");
        request.setRole("Chair");
        request.setDisplayOrder(2);
        request.setImageUrl("https://example.org/photo");
        return request;
    }
}
