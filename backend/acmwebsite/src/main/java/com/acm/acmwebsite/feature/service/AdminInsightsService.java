package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.AdminInsightsDto;
import com.acm.acmwebsite.feature.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInsightsService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ClubRepository clubRepository;
    private final ProgramRepository programRepository;
    private final HighBoardRepository highBoardRepository;
    private final CommitteeBoardRepository committeeBoardRepository;
    private final CommitteeRepository committeeRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final ClubRegistrationRepository clubRegistrationRepository;
    private final SubscriptionRepository subscriptionRepository;

    public AdminInsightsDto getInsights() {
        return AdminInsightsDto.builder()
                .totalUsers(userRepository.count())
                .totalEvents(eventRepository.count())
                .totalClubs(clubRepository.count())
                .totalPrograms(programRepository.count())
                .totalBoardMembers(highBoardRepository.count())
                .totalCommitteeBoardMembers(committeeBoardRepository.count())
                .totalCommittees(committeeRepository.count())
                .totalEventRegistrations(eventRegistrationRepository.count())
                .totalClubRegistrations(clubRegistrationRepository.count())
                .totalSubscriptions(subscriptionRepository.countByUserId())
                .usersByDepartment(getUsersByDepartment())
                .usersByBatch(getUsersByBatch())
                .userGrowth(getUserGrowth())
                .popularEvents(getPopularEvents())
                .popularClubs(getPopularClubs())
                .build();
    }

    private Map<String, Long> getUsersByDepartment() {
        Map<String, Long> usersByDept = new HashMap<>();
        List<Object[]> deptCounts = userRepository.countUsersByDepartment();
        for (Object[] row : deptCounts) {
            if (row[0] != null) {
                usersByDept.put(row[0].toString(), (Long) row[1]);
            }
        }
        return usersByDept;
    }

    private Map<String, Long> getUsersByBatch() {
        Map<String, Long> usersByBatch = new HashMap<>();
        List<Object[]> batchCounts = userRepository.countUsersByBatch();
        for (Object[] row : batchCounts) {
            if (row[0] != null) {
                usersByBatch.put(row[0].toString(), (Long) row[1]);
            }
        }
        return usersByBatch;
    }

    private List<AdminInsightsDto.UserGrowthPoint> getUserGrowth() {
        List<LocalDateTime> createdTimes = userRepository.findAllCreatedTimes();
        Map<String, Long> dailyCounts = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (LocalDateTime time : createdTimes) {
            if (time != null) {
                String dateStr = time.format(formatter);
                dailyCounts.put(dateStr, dailyCounts.getOrDefault(dateStr, 0L) + 1);
            }
        }

        List<AdminInsightsDto.UserGrowthPoint> userGrowth = new ArrayList<>();
        long cumulativeSum = 0;
        for (Map.Entry<String, Long> entry : dailyCounts.entrySet()) {
            cumulativeSum += entry.getValue();
            userGrowth.add(new AdminInsightsDto.UserGrowthPoint(entry.getKey(), cumulativeSum));
        }
        return userGrowth;
    }

    private List<AdminInsightsDto.RegistrationSummaryPoint> getPopularEvents() {
        return eventRegistrationRepository.countRegistrationsByEvent()
                .stream()
                .map(arr -> new AdminInsightsDto.RegistrationSummaryPoint((Long) arr[0], (String) arr[1], (Long) arr[2]))
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());
    }

    private List<AdminInsightsDto.RegistrationSummaryPoint> getPopularClubs() {
        return clubRegistrationRepository.countRegistrationsByClub()
                .stream()
                .map(arr -> new AdminInsightsDto.RegistrationSummaryPoint((Long) arr[0], (String) arr[1], (Long) arr[2]))
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());
    }
}
