package com.acm.acmwebsite.feature.badges;

import com.acm.acmwebsite.feature.seasons.SeasonPeriodService;
import com.acm.acmwebsite.feature.seasons.SeasonPeriod;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardRequest;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardView;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.MemberBadgesResponse;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.Recipient;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BadgeAwardService {

    private final BadgeAwardRepository awardRepository;
    private final BadgeTypeService typeService;
    private final SeasonPeriodService periodService;
    private final UserRepository userRepository;

    // Gives one badge to several members at once, all or nothing. A previously revoked award is restored.
    @Transactional
    public List<AwardView> award(AwardRequest request, String actorEmail) {
        User actor = findActor(actorEmail);
        BadgeType type = typeService.get(request.badgeTypeId());
        if (!type.isActive()) {
            throw new IllegalArgumentException(type.getName() + " is inactive and can't be awarded");
        }
        SeasonPeriod period = resolvePeriod(type, request.periodId());

        Set<UUID> seen = new HashSet<>();
        for (Recipient r : request.recipients()) {
            if (!seen.add(r.memberId())) {
                throw new IllegalArgumentException("A member is listed twice");
            }
        }
        Map<UUID, User> members = userRepository.findAllById(seen).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        if (members.size() != seen.size()) {
            throw new ResourceNotFoundException("One or more members were not found");
        }
        Map<UUID, BadgeAward> existing = awardRepository.findForMembers(type.getId(), period.getId(), seen).stream()
                        .collect(Collectors.toMap(a -> a.getMember().getId(), Function.identity()));

        List<BadgeAward> saved = new ArrayList<>();
        for (Recipient r : request.recipients()) {
            User member = members.get(r.memberId());
            Committee committee = committeeOf(member);
            BadgeAward award = existing.get(member.getId());
            if (award != null && award.isActive()) {
                throw new BadgeConflictException(displayName(member) + " already has " + type.getName()
                        + " for " + period.label());
            }
            if (award == null) {
                award = new BadgeAward();
                award.setBadgeType(type);
                award.setMember(member);
                award.setPeriod(period);
            } else {
                award.setRevokedAt(null);
                award.setRevokedBy(null);
                award.setAwardedAt(LocalDateTime.now());
                award.setSeenByMember(false);
            }
            award.setCommittee(committee);
            award.setRank(r.rank());
            award.setNote(r.note() != null && !r.note().isBlank() ? r.note().trim() : blankToNull(request.note()));
            award.setAwardedBy(actor);
            saved.add(award);
        }
        checkWinnerLimit(type, period, saved,
                request.winners() != null ? request.winners() : Math.max(1, type.getDefaultWinners()));
        return awardRepository.saveAll(saved).stream().map(BadgeMapper::adminView).toList();
    }

    // A committee can't have more holders of the badge for the period than its number of winners.
    private void checkWinnerLimit(BadgeType type, SeasonPeriod period, List<BadgeAward> newAwards, int winners) {
        Map<Long, List<BadgeAward>> picked = newAwards.stream()
                .collect(Collectors.groupingBy(a -> a.getCommittee().getId()));
        picked.values().forEach(group -> {
            Committee committee = group.get(0).getCommittee();
            int count = group.size();
            int holders = awardRepository.findActiveForCommittee(type.getId(), period.getId(), committee.getId()).size();
            int open = Math.max(0, winners - holders);
            if (count > open) {
                String where = type.getName() + " in " + committee.getName() + " for " + period.label();
                throw new BadgeConflictException(open == 0
                        ? "All " + winners + " places for " + where + " are taken. Revoke one or raise the number of winners."
                        : "Only " + open + " of " + winners + " places for " + where + " are open, but " + count
                                + " members were picked.");
            }
        });
    }

    // The member's description on an awarded badge; blank removes it.
    @Transactional
    public AwardView updateNote(Long awardId, String note) {
        BadgeAward award = awardRepository.findById(awardId)
                .filter(BadgeAward::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Badge award not found"));
        award.setNote(blankToNull(note));
        return BadgeMapper.adminView(award);
    }

    @Transactional
    public void revoke(Long awardId, String actorEmail) {
        BadgeAward award = awardRepository.findById(awardId)
                .orElseThrow(() -> new ResourceNotFoundException("Badge award not found"));
        if (award.isActive()) {
            award.setRevokedAt(LocalDateTime.now());
            award.setRevokedBy(findActor(actorEmail));
        }
    }

    @Transactional(readOnly = true)
    public List<AwardView> listForPeriod(Long periodId) {
        return awardRepository.findActiveByPeriod(periodService.get(periodId).getId()).stream()
                .map(BadgeMapper::adminView).toList();
    }

    @Transactional(readOnly = true)
    public List<AwardView> listMine(String email) {
        User me = findActor(email);
        return awardRepository.findActiveByMember(me.getId()).stream().map(BadgeMapper::ownView).toList();
    }

    @Transactional
    public void markMineSeen(String email) {
        awardRepository.markSeen(findActor(email).getId());
    }

    @Transactional(readOnly = true)
    public MemberBadgesResponse memberBadges(UUID userId) {
        User member = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        List<AwardView> badges = awardRepository.findActiveByMember(userId).stream().map(BadgeMapper::view).toList();
        return new MemberBadgesResponse(BadgeMapper.summary(member), badges);
    }

    @Transactional(readOnly = true)
    public AwardView publicBadge(UUID publicId) {
        return awardRepository.findActiveByPublicId(publicId).map(BadgeMapper::view)
                .orElseThrow(() -> new ResourceNotFoundException("Badge not found"));
    }

    private SeasonPeriod resolvePeriod(BadgeType type, Long periodId) {
        if (periodId == null) {
            throw new IllegalArgumentException("Pick the period " + type.getName() + " is for");
        }
        SeasonPeriod period = periodService.get(periodId);
        if (period.getType() != type.getPeriodType()) {
            throw new IllegalArgumentException(type.getName() + " is awarded per " + type.getPeriodType().name().toLowerCase()
                    + ", but " + period.label() + " is a " + period.getType().name().toLowerCase());
        }
        return period;
    }

    // Every badge belongs to the committee the member is in when it is awarded; it can't be chosen.
    private Committee committeeOf(User member) {
        if (member.getCommittee() == null) {
            throw new IllegalArgumentException(displayName(member) + " is not in a committee, so they can't receive a badge");
        }
        return member.getCommittee();
    }

    private User findActor(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }

    private static String displayName(User u) {
        return u.getName() != null && !u.getName().isBlank() ? u.getName() : u.getEmail();
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
