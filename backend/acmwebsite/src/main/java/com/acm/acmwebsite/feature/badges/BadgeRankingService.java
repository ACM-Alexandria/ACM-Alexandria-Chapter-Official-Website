package com.acm.acmwebsite.feature.badges;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardView;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.CommitteeRankingRequest;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.CommitteeRankingResponse;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.RankedMember;
import com.acm.acmwebsite.feature.repository.PointsLogRepository;
import com.acm.acmwebsite.feature.repository.PointsLogRepository.MemberPointsTotal;
import com.acm.acmwebsite.feature.seasons.SeasonPeriod;
import com.acm.acmwebsite.feature.seasons.SeasonPeriodService;
import com.acm.acmwebsite.feature.seasons.dto.SeasonDtos.PeriodResponse;

import lombok.RequiredArgsConstructor;

// The Award screen for one group (a committee's members, or a committee's or club's board): who already has the
// badge for the period, and everyone else in the group ranked by the points they earned in it, with the open places
// marked as suggested. Nothing is saved here.
@Service
@RequiredArgsConstructor
public class BadgeRankingService {

    private final BadgeTypeService typeService;
    private final SeasonPeriodService periodService;
    private final PointsLogRepository pointsLogRepository;
    private final BadgeAwardRepository awardRepository;
    private final BadgeGroupService groupService;

    @Transactional(readOnly = true)
    public CommitteeRankingResponse rank(CommitteeRankingRequest request) {
        BadgeType type = typeService.get(request.badgeTypeId());
        SeasonPeriod period = periodService.get(request.periodId());
        if (type.getPeriodType() != period.getType()) {
            throw new IllegalArgumentException(type.getName() + " is awarded per " + type.getPeriodType().name().toLowerCase()
                    + ", but " + period.label() + " is a " + period.getType().name().toLowerCase());
        }
        BadgeGroupService.Group group = groupService.find(request.track(), request.committeeId(), request.clubId());
        int winners = request.winners() != null ? request.winners() : Math.max(1, type.getDefaultWinners());

        List<AwardView> awarded = groupService.holders(group, type.getId(), period.getId())
                .stream().map(BadgeMapper::adminView).toList();
        Set<UUID> awardedIds = awarded.stream().map(a -> a.member().id()).collect(Collectors.toSet());

        // Points in the period, counted up to today for an ongoing one.
        Map<UUID, Long> points = new HashMap<>();
        for (MemberPointsTotal t : pointsLogRepository.sumPointsByMemberBetweenForRoles(
                period.getStartDate().atStartOfDay(),
                period.lastDayAsOf(LocalDate.now()).plusDays(1).atStartOfDay(),
                BadgeGroupService.pointRoles(group.track()))) {
            points.put(t.getMemberId(), t.getTotal());
        }

        List<Scored> remaining = groupService.candidates(group).stream()
                .filter(u -> !awardedIds.contains(u.getId()))
                .map(u -> new Scored(u, points.getOrDefault(u.getId(), 0L)))
                .sorted(Comparator.comparingLong(Scored::points).reversed()
                        .thenComparing(s -> displayName(s.user()), String.CASE_INSENSITIVE_ORDER))
                .toList();

        int openPlaces = Math.max(0, winners - awarded.size());
        return new CommitteeRankingResponse(BadgeMapper.toResponse(type), PeriodResponse.of(period), group.track(),
                group.committee() != null ? group.committee().getId() : null,
                group.club() != null ? group.club().getId() : null,
                group.name(), winners, awarded, rankMembers(remaining, openPlaces, group.track() == AwardTrack.BOARD));
    }

    record Scored(User user, long points) {
    }

    // Competition ranking (1, 2, 2, 4) of members sorted by points, highest first. The first `openPlaces`
    // places are suggested; everyone tied for the last open place is flagged instead when they don't all fit.
    // Members with no points are never suggested or flagged.
    static List<RankedMember> rankMembers(List<Scored> sorted, int openPlaces, boolean board) {
        List<RankedMember> result = new ArrayList<>();
        int i = 0;
        while (i < sorted.size()) {
            int rank = i + 1;
            long pts = sorted.get(i).points();
            int j = i;
            while (j < sorted.size() && sorted.get(j).points() == pts) {
                j++;
            }
            boolean inRange = pts > 0 && rank <= openPlaces;
            boolean fits = j <= openPlaces;
            for (int k = i; k < j; k++) {
                User u = sorted.get(k).user();
                result.add(new RankedMember(u.getId(), displayName(u), BadgeMapper.photoOf(u), pts, rank,
                        inRange && fits, inRange && !fits, board));
            }
            i = j;
        }
        return result;
    }

    private static String displayName(User u) {
        return u.getName() != null && !u.getName().isBlank() ? u.getName() : u.getEmail();
    }
}
