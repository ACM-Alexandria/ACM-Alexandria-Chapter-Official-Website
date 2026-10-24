package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.core.config.CacheConfig;
import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastClubBoardRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastClubRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeBoardRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastHighBoardRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastHighBoardResponse;
import com.acm.acmwebsite.feature.entity.SystemSettings;
import com.acm.acmwebsite.feature.mapper.PastBoardMapperImpl;
import com.acm.acmwebsite.feature.mapper.PastBoardSnapshotMapperImpl;
import com.acm.acmwebsite.feature.repository.PastClubBoardRepository;
import com.acm.acmwebsite.feature.repository.PastClubRepository;
import com.acm.acmwebsite.feature.repository.PastCommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.PastCommitteeRepository;
import com.acm.acmwebsite.feature.repository.PastHighBoardRepository;
import com.acm.acmwebsite.feature.repository.PastSeasonRepository;
import com.acm.acmwebsite.feature.repository.SystemSettingsRepository;
import com.acm.acmwebsite.feature.service.PastSeasonsService;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:past-crud-test;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SystemSettingsService.class, PastBoardMapperImpl.class, PastBoardSnapshotMapperImpl.class,
        PastSeasonsService.class, CacheConfig.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PastSeasonsIntegrationTest {
    @MockitoBean private UserService userService;
    @Autowired private PastSeasonsService service;
    @Autowired private PastCommitteeBoardRepository committeeBoardRepository;
    @Autowired private PastClubBoardRepository clubBoardRepository;
    @Autowired private PastCommitteeRepository committeeRepository;
    @Autowired private PastClubRepository clubRepository;
    @Autowired private PastHighBoardRepository highRepository;
    @Autowired private PastSeasonRepository seasonRepository;
    @Autowired private SystemSettingsRepository settingsRepository;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private CacheManager caches;
    private TransactionTemplate transaction;

    @BeforeEach
    void reset() {
        transaction = new TransactionTemplate(transactionManager);
        committeeBoardRepository.deleteAllInBatch();
        clubBoardRepository.deleteAllInBatch();
        committeeRepository.deleteAllInBatch();
        clubRepository.deleteAllInBatch();
        highRepository.deleteAllInBatch();
        seasonRepository.deleteAllInBatch();
        settingsRepository.saveAndFlush(new SystemSettings(1L, true, 2026));
        caches.getCache(CacheNames.BOARD_SEASONS).clear();
        caches.getCache(CacheNames.PAST_BOARDS).clear();
    }

    @Test
    void mapsDtosAndOrdersNullLastThenId() {
        Long first = service.createPastHighBoard(member("First", 2)).getId();
        service.createPastHighBoard(member("No order", null));
        Long second = service.createPastHighBoard(member("Second", 2)).getId();
        service.createPastHighBoard(member("Leader", 1));
        var result = service.listPastHighBoards(2025);
        assertEquals(List.of("Leader", "First", "Second", "No order"),
                result.stream().map(PastHighBoardResponse::getUserName).toList());
        assertEquals(List.of(first, second), result.subList(1, 3).stream().map(PastHighBoardResponse::getId).toList());
        assertEquals("https://example.org/photo", result.getFirst().getProfileImageUrl());
    }

    @Test
    void deletesPastParentsAndTheirMembersOnly() {
        var committee = new PastCommitteeRequest();
        committee.setSeason(2025);
        committee.setName("Committee");
        var club = new PastClubRequest();
        club.setSeason(2025);
        club.setName("Club");
        Long committeeId = service.createPastCommittee(committee).getId();
        Long clubId = service.createPastClub(club).getId();
        var committeeMember = new PastCommitteeBoardRequest();
        committeeMember.setSeason(2025);
        committeeMember.setName("Member");
        committeeMember.setRole("Chair");
        committeeMember.setPastCommitteeId(committeeId);
        service.createPastCommitteeBoard(committeeMember);
        var clubMember = new PastClubBoardRequest();
        clubMember.setSeason(2025);
        clubMember.setName("Member");
        clubMember.setRole("Chair");
        clubMember.setPastClubId(clubId);
        service.createPastClubBoard(clubMember);
        service.createPastHighBoard(member("Unaffected", null));
        service.deletePastCommittee(committeeId);
        service.deletePastClub(clubId);
        assertEquals(0, committeeBoardRepository.count());
        assertEquals(0, clubBoardRepository.count());
        assertEquals(1, highRepository.count());
        assertEquals(2026, settingsRepository.findById(1L).orElseThrow().getCurrentSeason());
    }

    @Test
    void rejectsCrossSeasonRelationsAndKeepsDatabaseUnchanged() {
        var parent = new PastCommitteeRequest();
        parent.setSeason(2024);
        parent.setName("Committee");
        Long parentId = service.createPastCommittee(parent).getId();
        var request = new PastCommitteeBoardRequest();
        request.setSeason(2025);
        request.setName("Member");
        request.setRole("Chair");
        request.setPastCommitteeId(parentId);
        assertThrows(IllegalArgumentException.class, () -> service.createPastCommitteeBoard(request));
        assertEquals(0, committeeBoardRepository.count());
    }

    @Test
    void invalidatesCachesOnlyAfterCommitAndKeepsThemOnRollback() {
        assertEquals(List.of(), service.getSeasons());
        caches.getCache(CacheNames.PAST_BOARDS).put(2025, "old");
        transaction.executeWithoutResult(status -> {
            service.createPastHighBoard(member("Committed", null));
            assertNotNull(caches.getCache(CacheNames.PAST_BOARDS).get(2025));
            assertEquals(List.of(), service.getSeasons());
        });
        assertNull(caches.getCache(CacheNames.PAST_BOARDS).get(2025));
        assertEquals(List.of(2025), service.getSeasons());
        caches.getCache(CacheNames.PAST_BOARDS).put(2025, "committed");
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            var request = member("Rolled back", null);
            request.setSeason(2024);
            service.createPastHighBoard(request);
            throw new IllegalStateException("Simulated failure");
        }));
        assertEquals("committed", caches.getCache(CacheNames.PAST_BOARDS).get(2025, String.class));
        assertEquals(List.of(2025), service.getSeasons());
        assertEquals(1, highRepository.count());
    }

    @Test
    void emptySeasonIsSavedAndDeletingItRemovesItsRecords() {
        service.addPastSeason(2025);
        assertEquals(List.of(2025), service.getSeasons());
        service.createPastHighBoard(member("Chair", null));
        var committee = new PastCommitteeRequest();
        committee.setSeason(2025);
        committee.setName("Committee");
        Long committeeId = service.createPastCommittee(committee).getId();
        var member = new PastCommitteeBoardRequest();
        member.setSeason(2025);
        member.setName("Member");
        member.setRole("Chair");
        member.setPastCommitteeId(committeeId);
        service.createPastCommitteeBoard(member);

        service.deleteSeason(2025);

        assertEquals(List.of(), service.getSeasons());
        assertEquals(0, highRepository.count());
        assertEquals(0, committeeBoardRepository.count());
        assertEquals(0, committeeRepository.count());
        assertEquals(0, seasonRepository.count());
    }

    @Test
    void listReadsPersistSeasonInitializationForNullAndMissingSettings() {
        List<Runnable> reads = List.of(
                () -> service.listPastHighBoards(2000),
                () -> service.listPastCommittees(2000),
                () -> service.listPastClubs(2000),
                () -> service.listPastCommitteeBoards(2000),
                () -> service.listPastClubBoards(2000));

        for (Runnable read : reads) {
            settingsRepository.saveAndFlush(new SystemSettings(1L, false, null));
            read.run();
            SystemSettings initialized = settingsRepository.findById(1L).orElseThrow();
            assertNotNull(initialized.getCurrentSeason());
            assertFalse(initialized.isEmailsEnabled());

            settingsRepository.deleteAllInBatch();
            read.run();
            SystemSettings created = settingsRepository.findById(1L).orElseThrow();
            assertNotNull(created.getCurrentSeason());
            assertTrue(created.isEmailsEnabled());
        }
    }

    private PastHighBoardRequest member(String name, Integer order) {
        var request = new PastHighBoardRequest();
        request.setSeason(2025);
        request.setName(name);
        request.setRole("Chair");
        request.setDisplayOrder(order);
        request.setImageUrl("https://example.org/photo");
        return request;
    }
}
