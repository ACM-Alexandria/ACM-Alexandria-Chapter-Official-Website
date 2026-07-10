package com.acm.acmwebsite.feature.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminInsightsDto {
    private long totalUsers;
    private long totalEvents;
    private long totalClubs;
    private long totalPrograms;
    private long totalBoardMembers;
    private long totalCommitteeBoardMembers;
    private long totalCommittees;
    private long totalEventRegistrations;
    private long totalClubRegistrations;
    private long totalSubscriptions;
    private long totalRadioSeasons;
    private long totalRadioEpisodes;

    private Map<String, Long> usersByDepartment;
    private Map<String, Long> usersByBatch;
    private List<UserGrowthPoint> userGrowth;
    private List<RegistrationSummaryPoint> popularEvents;
    private List<RegistrationSummaryPoint> popularClubs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserGrowthPoint {
        private String date;
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegistrationSummaryPoint {
        private Long id;
        private String name;
        private long count;
    }
}
