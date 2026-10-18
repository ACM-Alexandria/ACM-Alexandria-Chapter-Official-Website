package com.acm.acmwebsite.service.past;

import com.acm.acmwebsite.core.config.CacheConfig;
import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.dto.past.*;
import com.acm.acmwebsite.feature.entity.SystemSettings;
import com.acm.acmwebsite.feature.mapper.PastBoardMapperImpl;
import com.acm.acmwebsite.feature.repository.SystemSettingsRepository;
import com.acm.acmwebsite.feature.repository.past.*;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import com.acm.acmwebsite.feature.service.past.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
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
@Import({SystemSettingsService.class, PastSeasonPolicy.class, PastBoardMapperImpl.class,
        PastHighBoardService.class, PastCommitteeService.class, PastClubService.class,
        PastCommitteeBoardService.class, PastClubBoardService.class, PastSeasonService.class,
        BoardCacheInvalidationListener.class, CacheConfig.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PastBoardsIntegrationTest {
    @Autowired private PastHighBoardService highBoard;
    @Autowired private PastCommitteeService committees;
    @Autowired private PastClubService clubs;
    @Autowired private PastCommitteeBoardService committeeBoard;
    @Autowired private PastClubBoardService clubBoard;
    @Autowired private PastSeasonService seasons;
    @Autowired private PastHighBoardRepository highRepository;
    @Autowired private PastCommitteeRepository committeeRepository;
    @Autowired private PastClubRepository clubRepository;
    @Autowired private PastCommitteeBoardRepository committeeBoardRepository;
    @Autowired private PastClubBoardRepository clubBoardRepository;
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
        settingsRepository.saveAndFlush(new SystemSettings(1L, true, 2026));
        caches.getCache(CacheNames.BOARD_SEASONS).clear();
        caches.getCache(CacheNames.PAST_BOARDS).clear();
    }

    @Test
    void mapsDtosAndOrdersNullLastThenId() {
        Long first = highBoard.create(member("First", 2)).getId();
        highBoard.create(member("No order", null));
        Long second = highBoard.create(member("Second", 2)).getId();
        highBoard.create(member("Leader", 1));
        var result = highBoard.list(2025);
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
        Long committeeId = committees.create(committee).getId();
        Long clubId = clubs.create(club).getId();
        var committeeMember = new PastCommitteeBoardRequest();
        committeeMember.setSeason(2025);
        committeeMember.setName("Member");
        committeeMember.setRole("Chair");
        committeeMember.setPastCommitteeId(committeeId);
        committeeBoard.create(committeeMember);
        var clubMember = new PastClubBoardRequest();
        clubMember.setSeason(2025);
        clubMember.setName("Member");
        clubMember.setRole("Chair");
        clubMember.setPastClubId(clubId);
        clubBoard.create(clubMember);
        highBoard.create(member("Unaffected", null));
        committees.delete(committeeId);
        clubs.delete(clubId);
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
        Long parentId = committees.create(parent).getId();
        var request = new PastCommitteeBoardRequest();
        request.setSeason(2025);
        request.setName("Member");
        request.setRole("Chair");
        request.setPastCommitteeId(parentId);
        assertThrows(IllegalArgumentException.class, () -> committeeBoard.create(request));
        assertEquals(0, committeeBoardRepository.count());
    }

    @Test
    void invalidatesCachesOnlyAfterCommitAndKeepsThemOnRollback() {
        assertEquals(List.of(), seasons.getSeasons());
        caches.getCache(CacheNames.PAST_BOARDS).put(2025, "old");
        transaction.executeWithoutResult(status -> {
            highBoard.create(member("Committed", null));
            assertNotNull(caches.getCache(CacheNames.PAST_BOARDS).get(2025));
            assertEquals(List.of(), seasons.getSeasons());
        });
        assertNull(caches.getCache(CacheNames.PAST_BOARDS).get(2025));
        assertEquals(List.of(2025), seasons.getSeasons());
        caches.getCache(CacheNames.PAST_BOARDS).put(2025, "committed");
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            var request = member("Rolled back", null);
            request.setSeason(2024);
            highBoard.create(request);
            throw new IllegalStateException("Simulated failure");
        }));
        assertEquals("committed", caches.getCache(CacheNames.PAST_BOARDS).get(2025, String.class));
        assertEquals(List.of(2025), seasons.getSeasons());
        assertEquals(1, highRepository.count());
    }

    @Test
    void listReadsPersistSeasonInitializationForNullAndMissingSettings() {
        List<Runnable> reads = List.of(
                () -> highBoard.list(2000),
                () -> committees.list(2000),
                () -> clubs.list(2000),
                () -> committeeBoard.list(2000),
                () -> clubBoard.list(2000));

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
