package com.acm.acmwebsite.feature.badges;

import com.acm.acmwebsite.feature.seasons.SeasonPeriod;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardView;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.BadgeTypeResponse;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.MemberSummary;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.Committee;

final class BadgeMapper {

    private BadgeMapper() {
    }

    static BadgeTypeResponse toResponse(BadgeType t) {
        return new BadgeTypeResponse(t.getId(), t.getName(), t.getDescription(), t.getPeriodType(), t.getIcon(),
                t.getDefaultWinners(), t.isActive(), t.getSortOrder());
    }

    static MemberSummary summary(User u) {
        Committee c = u.getCommittee();
        return new MemberSummary(u.getId(), u.getName(), photoOf(u), c != null ? c.getName() : null);
    }

    // Only board members and admins may show a profile photo (the site-wide rule, see the frontend's
    // canShowPhoto). Everyone else, ACM members included, is shown by initials.
    static String photoOf(User u) {
        Role role = u.getRole();
        return role == null || role == Role.USER || role == Role.ACM_MEMBER ? null : u.getProfileImageUrl();
    }

    // Public and member view: no admin-only fields.
    static AwardView view(BadgeAward a) {
        return view(a, null, null);
    }

    static AwardView adminView(BadgeAward a) {
        return view(a, a.getAwardedBy().getName(), null);
    }

    static AwardView ownView(BadgeAward a) {
        return view(a, null, !a.isSeenByMember());
    }

    private static AwardView view(BadgeAward a, String awardedByName, Boolean isNew) {
        BadgeType t = a.getBadgeType();
        SeasonPeriod p = a.getPeriod();
        Committee c = a.getCommittee();
        Club club = a.getClub();
        return new AwardView(
                a.getId(),
                a.getPublicId(),
                t.getId(),
                t.getName(),
                t.getDescription(),
                t.getIcon(),
                p != null ? p.getType() : null,
                p != null ? p.getId() : null,
                p != null ? p.label() : null,
                p != null ? p.getStartDate() : null,
                p != null ? p.getEndDate() : null,
                c != null ? c.getId() : null,
                c != null ? c.getName() : null,
                club != null ? club.getId() : null,
                club != null ? club.getName() : null,
                a.groupName(),
                a.getTrack(),
                a.getRank(),
                a.getNote(),
                a.getAwardedAt(),
                summary(a.getMember()),
                awardedByName,
                isNew);
    }
}
