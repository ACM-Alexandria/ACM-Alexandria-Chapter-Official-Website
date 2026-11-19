package com.acm.acmwebsite.feature.badges;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.ClubBoard;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.repository.ClubBoardRepository;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;

import lombok.RequiredArgsConstructor;

// The groups a badge is won in, each with its own winners:
//  - MEMBER: a committee's ACM members;
//  - BOARD: a committee's board (committee board members), or a club's board (its club_board entries).
@Service
@RequiredArgsConstructor
public class BadgeGroupService {

    private final CommitteeRepository committeeRepository;
    private final ClubRepository clubRepository;
    private final ClubBoardRepository clubBoardRepository;
    private final UserRepository userRepository;
    private final BadgeAwardRepository awardRepository;

    // Exactly one of committee and club is set; a club group is always on the board track.
    public record Group(AwardTrack track, Committee committee, Club club) {

        public String name() {
            return committee != null ? committee.getName() : club.getName();
        }

        Long key() {
            return committee != null ? committee.getId() : -club.getId();
        }

        // "Technical members", "Technical board", "Chess Club board"
        String describe() {
            return name() + (track == AwardTrack.BOARD ? " board" : " members");
        }
    }

    // Points that count when ranking each track: members' points, or any board member's.
    static List<Role> pointRoles(AwardTrack track) {
        return track == AwardTrack.BOARD ? List.of(Role.values()) : List.of(Role.ACM_MEMBER);
    }

    // The group the Award screen asks for.
    public Group find(AwardTrack track, Long committeeId, Long clubId) {
        AwardTrack t = track != null ? track : AwardTrack.MEMBER;
        if ((committeeId == null) == (clubId == null)) {
            throw new IllegalArgumentException("Pick a committee or a club");
        }
        if (clubId != null) {
            if (t != AwardTrack.BOARD) {
                throw new IllegalArgumentException("Clubs only have a board; pick the Board tab to award a club");
            }
            Club club = clubRepository.findById(clubId).orElseThrow(() -> new ResourceNotFoundException("Club not found"));
            return new Group(t, null, club);
        }
        Committee committee = committeeRepository.findById(committeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Committee not found"));
        return new Group(t, committee, null);
    }

    // Everyone who can be ranked and awarded in the group.
    public List<User> candidates(Group group) {
        if (group.club() != null) {
            return clubBoardRepository.findByClubId(group.club().getId()).stream()
                    .map(ClubBoard::getUser).filter(Objects::nonNull).distinct().toList();
        }
        Role role = group.track() == AwardTrack.BOARD ? Role.ACM_COMMITTEE_BOARD : Role.ACM_MEMBER;
        return userRepository.findByCommittee_IdAndRoleIn(group.committee().getId(), List.of(role));
    }

    // This badge's current holders in the group for the period.
    public List<BadgeAward> holders(Group group, Long typeId, Long periodId) {
        return group.club() != null
                ? awardRepository.findActiveForClub(typeId, periodId, group.club().getId())
                : awardRepository.findActiveForCommittee(typeId, periodId, group.committee().getId(), group.track());
    }

    // The group a member wins in on the given track. `committeeId`/`clubId` name the group the admin picked; the
    // member must belong to it. Left out, it is the member's own committee (members, committee boards) or club
    // (club boards).
    public Group of(User member, AwardTrack track, Long committeeId, Long clubId) {
        AwardTrack t = track != null ? track : AwardTrack.MEMBER;
        String who = displayName(member);
        if (t == AwardTrack.MEMBER) {
            if (clubId != null) {
                throw new IllegalArgumentException("Clubs only have a board; pick the Board tab to award a club");
            }
            if (member.getRole() != Role.ACM_MEMBER || member.getCommittee() == null) {
                throw new IllegalArgumentException(who + " isn't an ACM member of a committee; award them on the Board tab if they're on a board");
            }
            if (committeeId != null && !committeeId.equals(member.getCommittee().getId())) {
                throw new IllegalArgumentException(who + " isn't a member of that committee");
            }
            return new Group(t, member.getCommittee(), null);
        }
        boolean committeeBoard = member.getRole() == Role.ACM_COMMITTEE_BOARD && member.getCommittee() != null;
        ClubBoard seat = clubBoardRepository.findByUserId(member.getId()).orElse(null);
        if (clubId != null) {
            if (seat == null || !clubId.equals(seat.getClub().getId())) {
                throw new IllegalArgumentException(who + " isn't on that club's board");
            }
            return new Group(t, null, seat.getClub());
        }
        if (committeeId != null) {
            if (!committeeBoard || !committeeId.equals(member.getCommittee().getId())) {
                throw new IllegalArgumentException(who + " isn't on that committee's board");
            }
            return new Group(t, member.getCommittee(), null);
        }
        if (committeeBoard) {
            return new Group(t, member.getCommittee(), null);
        }
        if (seat != null) {
            return new Group(t, null, seat.getClub());
        }
        throw new IllegalArgumentException(who + " isn't on a committee or club board, so they can't receive a board badge");
    }

    static String displayName(User u) {
        return u.getName() != null && !u.getName().isBlank() ? u.getName() : u.getEmail();
    }
}
