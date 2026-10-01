package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.ClubBoard;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.repository.ClubBoardRepository;
import com.acm.acmwebsite.feature.repository.CommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MemberTargetingService {

    public static final Set<Role> COMMITTEE_SCOPED_ROLES = Set.of(Role.ACM_MEMBER, Role.ACM_COMMITTEE_BOARD);
    public static final Set<Role> CLUB_SCOPED_ROLES = Set.of(Role.ACM_CLUB_BOARD);

    private static final List<Role> ROLE_ORDER = List.of(
            Role.ACM_HIGH_BOARD,
            Role.ACM_COMMITTEE_BOARD,
            Role.ACM_CLUB_BOARD,
            Role.ACM_MEMBER,
            Role.SUPER_ADMIN,
            Role.USER);

    private final UserRepository userRepository;
    private final HighBoardRepository highBoardRepository;
    private final CommitteeBoardRepository committeeBoardRepository;
    private final ClubBoardRepository clubBoardRepository;

    public MemberTargetingService(UserRepository userRepository,
            HighBoardRepository highBoardRepository,
            CommitteeBoardRepository committeeBoardRepository,
            ClubBoardRepository clubBoardRepository) {
        this.userRepository = userRepository;
        this.highBoardRepository = highBoardRepository;
        this.committeeBoardRepository = committeeBoardRepository;
        this.clubBoardRepository = clubBoardRepository;
    }

    public record TargetedMember(User user, String position, Integer boardOrder, Long associationId,
            String associationName) {
    }

    @Transactional(readOnly = true)
    public List<TargetedMember> findMembers(List<Role> roles, List<Long> committeeIds, List<Long> clubIds) {
        Set<Role> selectedRoles = toRoleSet(roles);
        if (selectedRoles.isEmpty()) {
            throw new IllegalArgumentException("Select at least one role.");
        }

        Set<Long> selectedCommitteeIds = toIdSet(committeeIds);
        Set<Long> selectedClubIds = toIdSet(clubIds);
        Map<UUID, HighBoard> highBoards = selectedRoles.contains(Role.ACM_HIGH_BOARD)
                ? indexByUserId(highBoardRepository.findAllWithUser(), board -> board.getUser().getId())
                : Map.of();
        Map<UUID, CommitteeBoard> committeeBoards = selectedRoles.contains(Role.ACM_COMMITTEE_BOARD)
                ? indexByUserId(committeeBoardRepository.findAllWithUserAndCommittee(),
                        board -> board.getUser().getId())
                : Map.of();
        Map<UUID, ClubBoard> clubBoards = selectedRoles.contains(Role.ACM_CLUB_BOARD)
                ? indexByUserId(clubBoardRepository.findAllWithUserAndClub(), board -> board.getUser().getId())
                : Map.of();

        Comparator<TargetedMember> order = Comparator
                .comparingInt((TargetedMember member) -> roleRank(member.user().getRole()))
                .thenComparing(member -> member.associationName(), Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(member -> member.boardOrder(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(member -> member.user().getName(), Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

        return userRepository.findAllByRoleInWithCommittee(selectedRoles).stream()
                .map(user -> toTargetedMember(user, highBoards, committeeBoards, clubBoards))
                .filter(member -> matchesScope(member, selectedCommitteeIds, selectedClubIds))
                .sorted(order)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> findRecipientEmails(List<Role> roles, List<Long> committeeIds, List<Long> clubIds) {
        return findMembers(roles, committeeIds, clubIds).stream()
                .map(member -> member.user().getEmail())
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .toList();
    }

    private TargetedMember toTargetedMember(User user,
            Map<UUID, HighBoard> highBoards,
            Map<UUID, CommitteeBoard> committeeBoards,
            Map<UUID, ClubBoard> clubBoards) {
        return switch (user.getRole()) {
            case ACM_HIGH_BOARD -> {
                HighBoard board = highBoards.get(user.getId());
                yield new TargetedMember(user, board != null ? board.getRole() : null,
                        board != null ? board.getOrder() : null, null, null);
            }
            case ACM_COMMITTEE_BOARD -> {
                CommitteeBoard board = committeeBoards.get(user.getId());
                Committee committee = board != null && board.getCommittee() != null ? board.getCommittee()
                        : user.getCommittee();
                yield new TargetedMember(user,
                        board != null ? board.getRole() : null,
                        board != null ? board.getOrder() : null,
                        committee != null ? committee.getId() : null,
                        committee != null ? committee.getName() : null);
            }
            case ACM_CLUB_BOARD -> {
                ClubBoard board = clubBoards.get(user.getId());
                Club club = board != null ? board.getClub() : null;
                yield new TargetedMember(user,
                        board != null ? board.getRole() : null,
                        board != null ? board.getOrder() : null,
                        club != null ? club.getId() : null,
                        club != null ? club.getName() : null);
            }
            default -> {
                Committee committee = user.getCommittee();
                yield new TargetedMember(user, null, null,
                        committee != null ? committee.getId() : null,
                        committee != null ? committee.getName() : null);
            }
        };
    }

    private boolean matchesScope(TargetedMember member, Set<Long> committeeIds, Set<Long> clubIds) {
        Role role = member.user().getRole();
        if (COMMITTEE_SCOPED_ROLES.contains(role) && !committeeIds.isEmpty()) {
            return member.associationId() != null && committeeIds.contains(member.associationId());
        }
        if (CLUB_SCOPED_ROLES.contains(role) && !clubIds.isEmpty()) {
            return member.associationId() != null && clubIds.contains(member.associationId());
        }
        return true;
    }

    public static int roleRank(Role role) {
        int index = ROLE_ORDER.indexOf(role);
        return index >= 0 ? index : ROLE_ORDER.size();
    }

    public static Set<Role> toRoleSet(List<Role> roles) {
        Set<Role> result = EnumSet.noneOf(Role.class);
        if (roles != null) {
            roles.stream().filter(Objects::nonNull).forEach(result::add);
        }
        return result;
    }

    public static Set<Long> toIdSet(List<Long> ids) {
        if (ids == null) {
            return Set.of();
        }
        return ids.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static <T> Map<UUID, T> indexByUserId(List<T> boards, Function<T, UUID> userIdExtractor) {
        return boards.stream()
                .collect(Collectors.toMap(userIdExtractor, Function.identity(), (first, second) -> first));
    }
}
