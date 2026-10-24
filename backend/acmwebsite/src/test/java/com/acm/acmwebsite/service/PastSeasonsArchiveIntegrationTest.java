package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.User_Authentication.service.impl.UserServiceImpl;
import com.acm.acmwebsite.User_Authentication.mapper.UserMapper;
import com.acm.acmwebsite.User_Authentication.service.TokenService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.feature.service.ImageUploadService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.acm.acmwebsite.core.config.CacheConfig;
import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.ClubBoard;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.entity.PastClub;
import com.acm.acmwebsite.feature.entity.PastClubBoard;
import com.acm.acmwebsite.feature.entity.PastCommitteeBoard;
import com.acm.acmwebsite.feature.entity.PastHighBoard;
import com.acm.acmwebsite.feature.entity.SystemSettings;
import com.acm.acmwebsite.feature.exception.BoardArchiveConflictException;
import com.acm.acmwebsite.feature.mapper.PastBoardMapperImpl;
import com.acm.acmwebsite.feature.mapper.PastBoardSnapshotMapperImpl;
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
import com.acm.acmwebsite.feature.repository.SystemSettingsRepository;
import com.acm.acmwebsite.feature.service.PastSeasonsService;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:archive-integration;DB_CLOSE_DELAY=-1;MODE=MySQL;LOCK_TIMEOUT=10000",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "google.sheets.client-id=test-client"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PastSeasonsService.class, PastBoardMapperImpl.class, PastBoardSnapshotMapperImpl.class,
        CacheConfig.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PastSeasonsArchiveIntegrationTest {
    @TestConfiguration
    static class SettingsConfiguration {
        @Bean
        UserService userService(UserRepository users, CommitteeRepository committees, ClubRepository clubs,
                                HighBoardRepository high, CommitteeBoardRepository committeeBoard,
                                ClubBoardRepository clubBoard) {
            return new UserServiceImpl(users, mock(UserMapper.class), mock(PasswordEncoder.class),
                    mock(EmailService.class), mock(TokenService.class), mock(ImageUploadService.class),
                    committees, clubs, high, committeeBoard, clubBoard);
        }

        @Bean
        SystemSettingsService systemSettingsService(SystemSettingsRepository repository) {
            return new SystemSettingsService(repository,
                    Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC));
        }
    }

    @Autowired private PastSeasonsService service;
    @MockitoSpyBean private SystemSettingsService settings;
    @MockitoSpyBean private UserService demotion;
    @Autowired private UserRepository users;
    @Autowired private SystemSettingsRepository settingsRepository;
    @Autowired private HighBoardRepository high;
    @Autowired private CommitteeBoardRepository committeeBoard;
    @Autowired private ClubBoardRepository clubBoard;
    @Autowired private CommitteeRepository committees;
    @Autowired private ClubRepository clubs;
    @Autowired private PastHighBoardRepository pastHigh;
    @Autowired private PastCommitteeBoardRepository pastCommitteeBoard;
    @Autowired private PastClubBoardRepository pastClubBoard;
    @Autowired private PastCommitteeRepository pastCommittees;
    @Autowired private PastClubRepository pastClubs;
    @Autowired private CacheManager caches;
    // Stubs go on the unproxied spies: calling a proxied @Transactional method outside a transaction would throw.
    private UserService demotionTarget;
    private SystemSettingsService settingsTarget;
    private UUID alice;
    private UUID zoe;
    private UUID bob;
    private UUID root;
    private UUID carol;
    private UUID unrelated;
    private long committeeId;
    private static final List<String> AFFECTED_CACHES = List.of(CacheNames.HIGH_BOARD, CacheNames.COMMITTEES,
            CacheNames.CLUBS, CacheNames.PAST_BOARDS, CacheNames.BOARD_SEASONS);

    @BeforeEach
    void fixture() {
        demotionTarget = AopTestUtils.getUltimateTargetObject(demotion);
        settingsTarget = AopTestUtils.getUltimateTargetObject(settings);
        high.deleteAllInBatch();
        committeeBoard.deleteAllInBatch();
        clubBoard.deleteAllInBatch();
        pastHigh.deleteAllInBatch();
        pastCommitteeBoard.deleteAllInBatch();
        pastClubBoard.deleteAllInBatch();
        pastCommittees.deleteAllInBatch();
        pastClubs.deleteAllInBatch();
        users.deleteAllInBatch();
        committees.deleteAllInBatch();
        clubs.deleteAllInBatch();
        settingsRepository.saveAndFlush(new SystemSettings(1L, false, 2026));
        var committee = new Committee();
        committee.setName("Main committee");
        committee.setDescription("Committee description");
        committee.setLogoUrl("https://example.org/committee");
        committee = committees.saveAndFlush(committee);
        committeeId = committee.getId();
        var emptyCommittee = new Committee();
        emptyCommittee.setName("Boardless committee");
        committees.saveAndFlush(emptyCommittee);
        var club = new Club();
        club.setName("Main club");
        club.setDescription("Club description");
        club.setImageUrl("https://example.org/club");
        club = clubs.saveAndFlush(club);
        var emptyClub = new Club();
        emptyClub.setName("Boardless club");
        clubs.saveAndFlush(emptyClub);
        var a = user("Alice", Role.ACM_HIGH_BOARD, committee);
        alice = a.getId();
        var z = user("Zoe", Role.ACM_HIGH_BOARD, committee);
        zoe = z.getId();
        var b = user("Bob", Role.ACM_CLUB_BOARD, committee);
        bob = b.getId();
        var r = user("Root", Role.SUPER_ADMIN, committee);
        root = r.getId();
        var c = user("Carol", Role.USER, committee);
        carol = c.getId();
        unrelated = user("Unrelated", Role.ACM_MEMBER, committee).getId();
        high.saveAndFlush(new HighBoard(null, "Chair", 1, a));
        high.saveAndFlush(new HighBoard(null, "Vice chair", 1, z));
        high.saveAndFlush(new HighBoard(null, "Advisor", null, r));
        committeeBoard.saveAndFlush(new CommitteeBoard(null, "Head", 2, committee, a));
        committeeBoard.saveAndFlush(new CommitteeBoard(null, "Mentor", null, committee, r));
        clubBoard.saveAndFlush(new ClubBoard(null, "Coordinator", 3, club, b));
        clubBoard.saveAndFlush(new ClubBoard(null, "Treasurer", null, club, c));
        for (String name : AFFECTED_CACHES) {
            caches.getCache(name).clear();
            caches.getCache(name).put("probe", "cached");
        }
    }

    @Test
    void archivesEveryTableWithCorrectLinksOrderingDemotionAndSeasonAdvance() {
        var summary = service.archive(2026);
        assertEquals(2026, summary.getArchivedSeason());
        assertEquals(2027, summary.getNewCurrentSeason());
        assertEquals(3, summary.getHighBoardMembers());
        assertEquals(2, summary.getCommittees());
        assertEquals(2, summary.getCommitteeBoardMembers());
        assertEquals(2, summary.getClubs());
        assertEquals(2, summary.getClubBoardMembers());
        assertEquals(3, summary.getUsersDowngraded());
        assertEquals(2027, settings.getCurrentSeason());
        assertFalse(settingsRepository.findById(1L).orElseThrow().isEmailsEnabled());
        assertEquals(0, high.count());
        assertEquals(0, committeeBoard.count());
        assertEquals(0, clubBoard.count());
        assertEquals(2, committees.count());
        assertEquals(2, clubs.count());
        var savedHigh = pastHigh.findBySeasonOrdered(2026);
        assertEquals(List.of("Alice", "Zoe", "Root"), savedHigh.stream().map(PastHighBoard::getName).toList());
        assertEquals(List.of("Chair", "Vice chair", "Advisor"), savedHigh.stream().map(PastHighBoard::getRole).toList());
        assertEquals("https://example.org/Alice", savedHigh.getFirst().getImageUrl());
        assertEquals("https://linkedin.com/in/Alice", savedHigh.getFirst().getLinkedinUrl());
        assertNull(savedHigh.getLast().getDisplayOrder());
        var savedCommittee = pastCommittees.findBySeasonOrderByNameAscIdAsc(2026).stream()
                .filter(parent -> parent.getName().equals("Main committee")).findFirst().orElseThrow();
        assertEquals("Committee description", savedCommittee.getDescription());
        assertEquals("https://example.org/committee", savedCommittee.getLogoUrl());
        var savedCommitteeBoard = pastCommitteeBoard.findBySeasonOrdered(2026);
        assertEquals(List.of("Head", "Mentor"), savedCommitteeBoard.stream().map(PastCommitteeBoard::getRole).toList());
        assertEquals(savedCommittee.getId(), savedCommitteeBoard.getFirst().getPastCommittee().getId());
        assertEquals(2026, savedCommitteeBoard.getFirst().getPastCommittee().getSeason());
        assertEquals(2, savedCommitteeBoard.getFirst().getDisplayOrder());
        var savedClub = pastClubs.findBySeasonOrderByNameAscIdAsc(2026).stream()
                .filter(parent -> parent.getName().equals("Main club")).findFirst().orElseThrow();
        assertEquals("Club description", savedClub.getDescription());
        assertEquals("https://example.org/club", savedClub.getLogoUrl());
        var savedClubBoard = pastClubBoard.findBySeasonOrdered(2026);
        assertEquals(savedClub.getId(), savedClubBoard.getFirst().getPastClub().getId());
        assertEquals("Coordinator", savedClubBoard.getFirst().getRole());
        assertEquals(3, savedClubBoard.getFirst().getDisplayOrder());
        for (UUID id : List.of(alice, zoe, bob, carol)) {
            var user = users.findById(id).orElseThrow();
            assertEquals(Role.USER, user.getRole());
            assertNull(user.getCommittee());
        }
        assertEquals(Role.SUPER_ADMIN, users.findById(root).orElseThrow().getRole());
        assertEquals(committeeId, users.findById(root).orElseThrow().getCommittee().getId());
        assertEquals(Role.ACM_MEMBER, users.findById(unrelated).orElseThrow().getRole());
        assertEquals(committeeId, users.findById(unrelated).orElseThrow().getCommittee().getId());
        for (String name : AFFECTED_CACHES) assertNull(caches.getCache(name).get("probe"));
        var changed = users.findById(alice).orElseThrow();
        changed.setName("Changed later");
        users.saveAndFlush(changed);
        assertEquals("Alice", pastHigh.findBySeasonOrdered(2026).getFirst().getName());
    }

    @Test
    void rejectsStaleConfirmationBeforeChangingAnything() {
        assertThrows(BoardArchiveConflictException.class, () -> service.archive(2025));
        assertOriginalBoardsAndUsers();
        assertEquals(2026, settings.getCurrentSeason());
        assertEquals(0, pastHigh.count());
        for (String name : AFFECTED_CACHES) assertNotNull(caches.getCache(name).get("probe"));
    }

    @Test
    void rejectsASeasonWithExistingPastData() {
        var existing = new PastClub();
        existing.setSeason(2026);
        existing.setName("Existing");
        pastClubs.saveAndFlush(existing);
        assertThrows(BoardArchiveConflictException.class, () -> service.archive(2026));
        assertOriginalBoardsAndUsers();
        assertEquals(2026, settings.getCurrentSeason());
        assertEquals(1, pastClubs.count());
        assertEquals(0, pastHigh.count());
        assertEquals(0, pastCommittees.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"existing", "null", "missing"})
    void failureAfterBulkDeletionRollsBackSnapshotsUsersBoardsSettingsAndCaches(String state) {
        if (state.equals("null")) settingsRepository.saveAndFlush(new SystemSettings(1L, false, null));
        if (state.equals("missing")) settingsRepository.deleteAllInBatch();
        doAnswer(invocation -> {
            invocation.callRealMethod();
            throw new IllegalStateException("Failure after all bulk deletes");
        }).when(settingsTarget).incrementCurrentSeason(any());
        assertThrows(IllegalStateException.class, () -> service.archive(2026));
        assertOriginalBoardsAndUsers();
        assertEquals(0, pastHigh.count());
        assertEquals(0, pastCommitteeBoard.count());
        assertEquals(0, pastClubBoard.count());
        assertEquals(0, pastCommittees.count());
        assertEquals(0, pastClubs.count());
        if (state.equals("missing")) assertFalse(settingsRepository.existsById(1L));
        else assertEquals(state.equals("null") ? null : 2026, settingsRepository.findById(1L).orElseThrow().getCurrentSeason());
        for (String name : AFFECTED_CACHES) assertNotNull(caches.getCache(name).get("probe"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void initializesNullOrMissingSettingsInsideTheArchiveTransaction(boolean missing) {
        if (missing) settingsRepository.deleteAllInBatch();
        else settingsRepository.saveAndFlush(new SystemSettings(1L, false, null));
        assertEquals(2027, service.archive(2026).getNewCurrentSeason());
        var stored = settingsRepository.findById(1L).orElseThrow();
        assertEquals(2027, stored.getCurrentSeason());
        assertEquals(missing, stored.isEmailsEnabled());
        assertEquals(1, settingsRepository.count());
    }

    @Test
    void collaboratorsRejectCallsOutsideAnArchiveTransaction() {
        assertThrows(IllegalTransactionStateException.class, () -> service.snapshot(2026));
        assertThrows(IllegalTransactionStateException.class, demotion::demoteBoardMembers);
        assertThrows(IllegalTransactionStateException.class, service::clearBoards);
        assertThrows(IllegalTransactionStateException.class, settings::lockForArchive);
        assertOriginalBoardsAndUsers();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void secondArchiveWaitsForFirstThenRejectsItsStaleConfirmation(boolean missingSettings) throws Exception {
        if (missingSettings) settingsRepository.deleteAllInBatch();
        CountDownLatch firstInDemotion = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        doAnswer(invocation -> {
            firstInDemotion.countDown();
            if (!releaseFirst.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Timed out holding archive");
            return invocation.callRealMethod();
        }).when(demotionTarget).demoteBoardMembers();
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> service.archive(2026));
            try {
                assertTrue(firstInDemotion.await(5, TimeUnit.SECONDS));
                var second = executor.submit(() -> {
                    secondStarted.countDown();
                    return service.archive(2026);
                });
                assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
                assertThrows(TimeoutException.class, () -> second.get(200, TimeUnit.MILLISECONDS));
                releaseFirst.countDown();
                assertEquals(2027, first.get(5, TimeUnit.SECONDS).getNewCurrentSeason());
                var failure = assertThrows(ExecutionException.class, () -> second.get(5, TimeUnit.SECONDS));
                assertInstanceOf(BoardArchiveConflictException.class, failure.getCause());
                assertEquals(2027, settings.getCurrentSeason());
                assertEquals(3, pastHigh.count());
            } finally {
                releaseFirst.countDown();
            }
        }
    }

    private User user(String name, Role role, Committee committee) {
        var user = new User();
        user.setName(name);
        user.setEmail(name.toLowerCase() + "@example.org");
        user.setPasswordHash("test-password-hash");
        user.setRole(role);
        user.setCommittee(committee);
        user.setProfileImageUrl("https://example.org/" + name);
        user.setLinkedinUrl("https://linkedin.com/in/" + name);
        return users.saveAndFlush(user);
    }

    private void assertOriginalBoardsAndUsers() {
        assertEquals(3, high.count());
        assertEquals(2, committeeBoard.count());
        assertEquals(2, clubBoard.count());
        assertEquals(2, committees.count());
        assertEquals(2, clubs.count());
        assertEquals(Role.ACM_HIGH_BOARD, users.findById(alice).orElseThrow().getRole());
        assertEquals(Role.ACM_CLUB_BOARD, users.findById(bob).orElseThrow().getRole());
        assertEquals(committeeId, users.findById(alice).orElseThrow().getCommittee().getId());
        assertEquals(committeeId, users.findById(carol).orElseThrow().getCommittee().getId());
        assertEquals(Role.SUPER_ADMIN, users.findById(root).orElseThrow().getRole());
    }
}
