package com.acm.acmwebsite.feature.badges;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.Candidate;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.CommitteeSuggestion;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.SuggestionRequest;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.SuggestionResponse;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.repository.PointsLogRepository;
import com.acm.acmwebsite.feature.repository.PointsLogRepository.MemberPointsTotal;

import lombok.RequiredArgsConstructor;

// Ranks each committee's members by the points they earned in a period, so admins can pick the winners.
// Nothing is saved here.
@Service
@RequiredArgsConstructor
public class BadgeSuggestionService {

    private final BadgeTypeService typeService;
    private final BadgePeriodService periodService;
    private final PointsLogRepository pointsLogRepository;
    private final BadgeAwardRepository awardRepository;
    private final CommitteeRepository committeeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SuggestionResponse suggest(SuggestionRequest request) {
        BadgeType type = typeService.get(request.badgeTypeId());
        BadgePeriod period = periodService.get(request.periodId());
        if (type.getPeriodType() == null) {
            throw new IllegalArgumentException(type.getName() + " is awarded by hand only");
        }
        if (type.getPeriodType() != period.getType()) {
            throw new IllegalArgumentException(type.getName() + " is awarded per " + type.getPeriodType().name().toLowerCase()
                    + ", but " + period.label() + " is a " + period.getType().name().toLowerCase());
        }

        // Only ACM members are ranked; board members are not.
        List<MemberPointsTotal> totals = pointsLogRepository.sumPointsByMemberBetween(
                period.getStartDate().atStartOfDay(), period.getEndDate().plusDays(1).atStartOfDay(), Role.ACM_MEMBER);
        Set<UUID> memberIds = totals.stream().map(MemberPointsTotal::getMemberId).collect(Collectors.toSet());

        Map<UUID, User> users = userRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Set<UUID> alreadyAwarded = memberIds.isEmpty() ? Set.of()
                : awardRepository.findForMembers(type.getId(), period.getId(), memberIds).stream()
                        .filter(BadgeAward::isActive)
                        .map(a -> a.getMember().getId())
                        .collect(Collectors.toSet());

        // Totals arrive highest first, so each committee's list keeps that order.
        Map<Long, List<MemberPointsTotal>> byCommittee = totals.stream()
                .filter(t -> t.getCommitteeId() != null)
                .collect(Collectors.groupingBy(MemberPointsTotal::getCommitteeId));

        Map<Long, Integer> requested = request.winnersPerCommittee() == null ? Map.of() : request.winnersPerCommittee();
        List<CommitteeSuggestion> committees = committeeRepository.findAll().stream()
                .sorted(Comparator.comparing(Committee::getName, String.CASE_INSENSITIVE_ORDER))
                .map(c -> {
                    int winners = Math.max(0, requested.getOrDefault(c.getId(), type.getDefaultWinners()));
                    List<Candidate> candidates = rank(byCommittee.getOrDefault(c.getId(), List.of()), winners, users, alreadyAwarded);
                    return new CommitteeSuggestion(c.getId(), c.getName(), winners, candidates);
                })
                .toList();

        return new SuggestionResponse(BadgeMapper.toResponse(type), BadgeMapper.toResponse(period), committees);
    }

    // Competition ranking (1, 2, 2, 4) cut at `winners` places. Everyone tied for the last place that fits is
    // included; if that tie holds more people than places left, they are flagged so the admin chooses.
    static List<Candidate> rank(List<MemberPointsTotal> sorted, int winners, Map<UUID, User> users, Set<UUID> alreadyAwarded) {
        List<Candidate> result = new ArrayList<>();
        if (winners <= 0) {
            return result;
        }
        int i = 0;
        while (i < sorted.size()) {
            int rank = i + 1;
            if (rank > winners) {
                break;
            }
            long points = sorted.get(i).getTotal();
            int j = i;
            while (j < sorted.size() && sorted.get(j).getTotal() == points) {
                j++;
            }
            boolean tied = j > winners;
            for (int k = i; k < j; k++) {
                UUID id = sorted.get(k).getMemberId();
                User u = users.get(id);
                result.add(new Candidate(id, u != null ? u.getName() : null, u != null ? u.getProfileImageUrl() : null,
                        points, rank, tied, alreadyAwarded.contains(id)));
            }
            i = j;
        }
        return result;
    }
}
