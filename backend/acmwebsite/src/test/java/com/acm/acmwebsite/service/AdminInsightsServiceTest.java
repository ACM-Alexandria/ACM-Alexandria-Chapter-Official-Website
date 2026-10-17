package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.AdminInsightsDto;
import com.acm.acmwebsite.feature.repository.*;
import com.acm.acmwebsite.feature.service.AdminInsightsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminInsightsServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private ProgramRepository programRepository;
    @Mock
    private HighBoardRepository highBoardRepository;
    @Mock
    private CommitteeBoardRepository committeeBoardRepository;
    @Mock
    private CommitteeRepository committeeRepository;
    @Mock
    private EventRegistrationRepository eventRegistrationRepository;
    @Mock
    private ClubRegistrationRepository clubRegistrationRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private RadioSeasonRepository radioSeasonRepository;
    @Mock
    private RadioEpisodeRepository radioEpisodeRepository;

    @InjectMocks
    private AdminInsightsService adminInsightsService;

    @Test
    @DisplayName("getInsights - successful aggregation with all counts, charts, and sorted lists")
    void getInsights_success() {
        when(userRepository.count()).thenReturn(100L);
        when(eventRepository.count()).thenReturn(10L);
        when(clubRepository.count()).thenReturn(5L);
        when(programRepository.count()).thenReturn(3L);
        when(highBoardRepository.count()).thenReturn(8L);
        when(committeeBoardRepository.count()).thenReturn(20L);
        when(committeeRepository.count()).thenReturn(6L);
        when(eventRegistrationRepository.count()).thenReturn(250L);
        when(clubRegistrationRepository.count()).thenReturn(180L);
        when(subscriptionRepository.countByUserId()).thenReturn(75L);
        when(radioSeasonRepository.count()).thenReturn(2L);
        when(radioEpisodeRepository.count()).thenReturn(15L);

        // Departments
        List<Object[]> deptData = List.of(
                new Object[]{"CCE", 40L},
                new Object[]{"CSED", 60L}
        );
        when(userRepository.countUsersByDepartment()).thenReturn(deptData);

        // Batches
        List<Object[]> batchData = List.of(
                new Object[]{"2025", 55L},
                new Object[]{"2026", 45L}
        );
        when(userRepository.countUsersByBatch()).thenReturn(batchData);

        // Created times for user growth (testing grouping and cumulative sum)
        LocalDateTime d1 = LocalDateTime.of(2026, 1, 10, 10, 0);
        LocalDateTime d2 = LocalDateTime.of(2026, 1, 10, 14, 0);
        LocalDateTime d3 = LocalDateTime.of(2026, 1, 11, 9, 30);
        when(userRepository.findAllCreatedTimes()).thenReturn(List.of(d1, d2, d3));

        // Popular events: unordered, should be sorted descending
        List<Object[]> popularEventsData = List.of(
                new Object[]{1L, "Intro to Git", 30L},
                new Object[]{2L, "Annual Hackathon", 120L},
                new Object[]{3L, "Problem Solving Session", 80L}
        );
        when(eventRegistrationRepository.countRegistrationsByEvent()).thenReturn(popularEventsData);

        // Popular clubs: unordered, should be sorted descending
        List<Object[]> popularClubsData = List.of(
                new Object[]{10L, "AI Club", 50L},
                new Object[]{20L, "Web Development Club", 90L}
        );
        when(clubRegistrationRepository.countRegistrationsByClub()).thenReturn(popularClubsData);

        AdminInsightsDto insights = adminInsightsService.getInsights();

        assertNotNull(insights);
        assertEquals(100L, insights.getTotalUsers());
        assertEquals(10L, insights.getTotalEvents());
        assertEquals(5L, insights.getTotalClubs());
        assertEquals(3L, insights.getTotalPrograms());
        assertEquals(8L, insights.getTotalBoardMembers());
        assertEquals(20L, insights.getTotalCommitteeBoardMembers());
        assertEquals(6L, insights.getTotalCommittees());
        assertEquals(250L, insights.getTotalEventRegistrations());
        assertEquals(180L, insights.getTotalClubRegistrations());
        assertEquals(75L, insights.getTotalSubscriptions());
        assertEquals(2L, insights.getTotalRadioSeasons());
        assertEquals(15L, insights.getTotalRadioEpisodes());

        // Verify departments
        assertEquals(40L, insights.getUsersByDepartment().get("CCE"));
        assertEquals(60L, insights.getUsersByDepartment().get("CSED"));

        // Verify batches
        assertEquals(55L, insights.getUsersByBatch().get("2025"));
        assertEquals(45L, insights.getUsersByBatch().get("2026"));

        // Verify user growth cumulative
        List<AdminInsightsDto.UserGrowthPoint> growth = insights.getUserGrowth();
        assertEquals(2, growth.size());
        assertEquals("2026-01-10", growth.get(0).getDate());
        assertEquals(2L, growth.get(0).getCount()); // 2 users on 2026-01-10
        assertEquals("2026-01-11", growth.get(1).getDate());
        assertEquals(3L, growth.get(1).getCount()); // cumulative 2 + 1 = 3

        // Verify popular events sorting
        List<AdminInsightsDto.RegistrationSummaryPoint> events = insights.getPopularEvents();
        assertEquals(3, events.size());
        assertEquals("Annual Hackathon", events.get(0).getName());
        assertEquals(120L, events.get(0).getCount());
        assertEquals("Problem Solving Session", events.get(1).getName());
        assertEquals(80L, events.get(1).getCount());
        assertEquals("Intro to Git", events.get(2).getName());
        assertEquals(30L, events.get(2).getCount());

        // Verify popular clubs sorting
        List<AdminInsightsDto.RegistrationSummaryPoint> clubs = insights.getPopularClubs();
        assertEquals(2, clubs.size());
        assertEquals("Web Development Club", clubs.get(0).getName());
        assertEquals(90L, clubs.get(0).getCount());
        assertEquals("AI Club", clubs.get(1).getName());
        assertEquals(50L, clubs.get(1).getCount());
    }

    @Test
    @DisplayName("getInsights - handles null rows in department and batch aggregation")
    void getInsights_handlesNullInDepartmentAndBatch() {
        when(userRepository.countUsersByDepartment()).thenReturn(Arrays.asList(
                new Object[]{null, 10L},
                new Object[]{"EM", 25L}
        ));
        when(userRepository.countUsersByBatch()).thenReturn(Arrays.asList(
                new Object[]{null, 15L},
                new Object[]{"2027", 35L}
        ));
        when(userRepository.findAllCreatedTimes()).thenReturn(Collections.emptyList());
        when(eventRegistrationRepository.countRegistrationsByEvent()).thenReturn(Collections.emptyList());
        when(clubRegistrationRepository.countRegistrationsByClub()).thenReturn(Collections.emptyList());

        AdminInsightsDto insights = adminInsightsService.getInsights();

        assertNotNull(insights);
        assertEquals(1, insights.getUsersByDepartment().size());
        assertEquals(25L, insights.getUsersByDepartment().get("EM"));
        assertFalse(insights.getUsersByDepartment().containsKey(null));

        assertEquals(1, insights.getUsersByBatch().size());
        assertEquals(35L, insights.getUsersByBatch().get("2027"));
        assertFalse(insights.getUsersByBatch().containsKey(null));
    }

    @Test
    @DisplayName("getInsights - handles null timestamps in user growth calculation")
    void getInsights_handlesNullInCreatedTimes() {
        when(userRepository.countUsersByDepartment()).thenReturn(Collections.emptyList());
        when(userRepository.countUsersByBatch()).thenReturn(Collections.emptyList());

        List<LocalDateTime> timesWithNull = new ArrayList<>();
        timesWithNull.add(null);
        timesWithNull.add(LocalDateTime.of(2026, 2, 1, 12, 0));
        timesWithNull.add(null);

        when(userRepository.findAllCreatedTimes()).thenReturn(timesWithNull);
        when(eventRegistrationRepository.countRegistrationsByEvent()).thenReturn(Collections.emptyList());
        when(clubRegistrationRepository.countRegistrationsByClub()).thenReturn(Collections.emptyList());

        AdminInsightsDto insights = adminInsightsService.getInsights();

        assertNotNull(insights);
        List<AdminInsightsDto.UserGrowthPoint> growth = insights.getUserGrowth();
        assertEquals(1, growth.size());
        assertEquals("2026-02-01", growth.get(0).getDate());
        assertEquals(1L, growth.get(0).getCount());
    }

    @Test
    @DisplayName("getInsights - handles empty database gracefully")
    void getInsights_emptyData() {
        when(userRepository.count()).thenReturn(0L);
        when(eventRepository.count()).thenReturn(0L);
        when(clubRepository.count()).thenReturn(0L);
        when(programRepository.count()).thenReturn(0L);
        when(highBoardRepository.count()).thenReturn(0L);
        when(committeeBoardRepository.count()).thenReturn(0L);
        when(committeeRepository.count()).thenReturn(0L);
        when(eventRegistrationRepository.count()).thenReturn(0L);
        when(clubRegistrationRepository.count()).thenReturn(0L);
        when(subscriptionRepository.countByUserId()).thenReturn(0L);
        when(radioSeasonRepository.count()).thenReturn(0L);
        when(radioEpisodeRepository.count()).thenReturn(0L);

        when(userRepository.countUsersByDepartment()).thenReturn(Collections.emptyList());
        when(userRepository.countUsersByBatch()).thenReturn(Collections.emptyList());
        when(userRepository.findAllCreatedTimes()).thenReturn(Collections.emptyList());
        when(eventRegistrationRepository.countRegistrationsByEvent()).thenReturn(Collections.emptyList());
        when(clubRegistrationRepository.countRegistrationsByClub()).thenReturn(Collections.emptyList());

        AdminInsightsDto insights = adminInsightsService.getInsights();

        assertNotNull(insights);
        assertEquals(0L, insights.getTotalUsers());
        assertTrue(insights.getUsersByDepartment().isEmpty());
        assertTrue(insights.getUsersByBatch().isEmpty());
        assertTrue(insights.getUserGrowth().isEmpty());
        assertTrue(insights.getPopularEvents().isEmpty());
        assertTrue(insights.getPopularClubs().isEmpty());
    }
}
