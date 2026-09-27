package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.MemberExportPreviewDto;
import com.acm.acmwebsite.feature.dto.MemberExportRequestDto;
import com.acm.acmwebsite.feature.dto.MemberExportResultDto;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.ClubBoard;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.repository.ClubBoardRepository;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import com.acm.acmwebsite.feature.repository.CommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Exports website members to a brand-new Google Sheet on every call.
 * Unlike registration sheets, nothing is persisted: each export is a one-off snapshot of the selected filters.
 */
@Service
public class MemberExportService {

    /** Roles whose rows are narrowed by the committee filter. */
    static final Set<Role> COMMITTEE_SCOPED_ROLES = EnumSet.of(Role.ACM_MEMBER, Role.ACM_COMMITTEE_BOARD);

    /** Roles whose rows are narrowed by the club filter. */
    static final Set<Role> CLUB_SCOPED_ROLES = EnumSet.of(Role.ACM_CLUB_BOARD);

    /** Sheet ordering: leadership first, then members, then everyone else. Unlisted roles go last. */
    private static final List<Role> ROLE_ORDER = List.of(
            Role.ACM_HIGH_BOARD,
            Role.ACM_COMMITTEE_BOARD,
            Role.ACM_CLUB_BOARD,
            Role.ACM_MEMBER,
            Role.SUPER_ADMIN,
            Role.USER
    );

    private static final Map<Role, String> ROLE_LABELS = new EnumMap<>(Map.of(
            Role.SUPER_ADMIN, "Super Admin",
            Role.ACM_HIGH_BOARD, "High Board",
            Role.ACM_COMMITTEE_BOARD, "Committee Board",
            Role.ACM_CLUB_BOARD, "Club Board",
            Role.ACM_MEMBER, "ACM Member",
            Role.USER, "Standard User"
    ));

    static final List<Object> SHEET_HEADERS = List.of(
            "#", "Name", "Email", "Phone Number", "Role", "Position", "Committee / Club",
            "Is Alex Eng Student", "Batch", "Department", "LinkedIn", "Joined At"
    );

    static final int MAX_TITLE_LENGTH = 150;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TITLE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserRepository userRepository;
    private final HighBoardRepository highBoardRepository;
    private final CommitteeBoardRepository committeeBoardRepository;
    private final ClubBoardRepository clubBoardRepository;
    private final CommitteeRepository committeeRepository;
    private final ClubRepository clubRepository;
    private final GoogleSheetsService googleSheetsService;

    @Value("${google.sheets.members-folder-id:}")
    private String membersFolderId;

    public MemberExportService(UserRepository userRepository,
                               HighBoardRepository highBoardRepository,
                               CommitteeBoardRepository committeeBoardRepository,
                               ClubBoardRepository clubBoardRepository,
                               CommitteeRepository committeeRepository,
                               ClubRepository clubRepository,
                               GoogleSheetsService googleSheetsService) {
        this.userRepository = userRepository;
        this.highBoardRepository = highBoardRepository;
        this.committeeBoardRepository = committeeBoardRepository;
        this.clubBoardRepository = clubBoardRepository;
        this.committeeRepository = committeeRepository;
        this.clubRepository = clubRepository;
        this.googleSheetsService = googleSheetsService;
    }

    /**
     * A resolved member with their board title and committee/club association.
     */
    record MemberRow(User user, String position, Integer boardOrder, Long associationId, String associationName) {
    }

    @Transactional(readOnly = true)
    public MemberExportPreviewDto preview(MemberExportRequestDto request) {
        List<MemberRow> members = resolveMembers(request);

        long alexCount = members.stream()
                .filter(m -> Boolean.TRUE.equals(m.user().getIsAlexEngStudent()))
                .count();

        // Members are already sorted by role rank, so a LinkedHashMap keeps the leadership-first order.
        Map<String, Long> roleCounts = members.stream()
                .collect(Collectors.groupingBy(m -> roleLabel(m.user().getRole()), LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> associationCounts = members.stream()
                .filter(m -> m.associationName() != null)
                .collect(Collectors.groupingBy(MemberRow::associationName, TreeMap::new, Collectors.counting()));

        return MemberExportPreviewDto.builder()
                .totalMembers(members.size())
                .alexUniStudentCount(alexCount)
                .roleCounts(roleCounts)
                .associationCounts(associationCounts)
                .build();
    }

    /**
     * Creates a new spreadsheet in the members folder. Intentionally not transactional so the
     * Google API calls don't hold a DB transaction open; all associations are fetched eagerly.
     */
    public MemberExportResultDto exportToSheet(MemberExportRequestDto request, String exporterEmail) {
        List<MemberRow> members = resolveMembers(request);
        if (members.isEmpty()) {
            throw new IllegalArgumentException("No members match the selected filters.");
        }

        Set<Role> roles = toRoleSet(request.getRoles());
        List<String> committeeNames = namesOf(committeeRepository.findAllById(toIdSet(request.getCommitteeIds())), Committee::getName);
        List<String> clubNames = namesOf(clubRepository.findAllById(toIdSet(request.getClubIds())), Club::getName);
        String rolesLabel = describeRoles(roles);
        LocalDateTime exportedAt = LocalDateTime.now();

        String title = resolveTitle(request.getTitle(), rolesLabel, committeeNames, clubNames, exportedAt);

        List<List<Object>> rows = googleSheetsService.buildMetadataRows(Arrays.asList(
                "Exported At:", exportedAt.format(DATE_TIME_FORMAT),
                "Exported By:", exporterEmail != null ? exporterEmail : "",
                "Roles:", rolesLabel,
                "Committees:", describeScope(roles, COMMITTEE_SCOPED_ROLES, committeeNames, "All Committees"),
                "Clubs:", describeScope(roles, CLUB_SCOPED_ROLES, clubNames, "All Clubs"),
                "Total Members:", String.valueOf(members.size())
        ));
        rows.add(new ArrayList<>(SHEET_HEADERS));

        int seqNum = 1;
        for (MemberRow member : members) {
            rows.add(toSheetRow(seqNum++, member));
        }

        String url = googleSheetsService.createSpreadsheetWithData(title, membersFolderId, rows);

        return MemberExportResultDto.builder()
                .googleSheetUrl(url)
                .title(title)
                .totalMembers(members.size())
                .exportedAt(exportedAt)
                .build();
    }

    List<MemberRow> resolveMembers(MemberExportRequestDto request) {
        Set<Role> roles = toRoleSet(request != null ? request.getRoles() : null);
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("Select at least one role to export.");
        }
        Set<Long> committeeIds = toIdSet(request.getCommitteeIds());
        Set<Long> clubIds = toIdSet(request.getClubIds());

        Map<UUID, HighBoard> highBoards = roles.contains(Role.ACM_HIGH_BOARD)
                ? indexByUserId(highBoardRepository.findAllWithUser(), hb -> hb.getUser().getId())
                : Map.of();
        Map<UUID, CommitteeBoard> committeeBoards = roles.contains(Role.ACM_COMMITTEE_BOARD)
                ? indexByUserId(committeeBoardRepository.findAllWithUserAndCommittee(), cb -> cb.getUser().getId())
                : Map.of();
        Map<UUID, ClubBoard> clubBoards = roles.contains(Role.ACM_CLUB_BOARD)
                ? indexByUserId(clubBoardRepository.findAllWithUserAndClub(), cb -> cb.getUser().getId())
                : Map.of();

        Comparator<MemberRow> order = Comparator
                .comparingInt((MemberRow m) -> roleRank(m.user().getRole()))
                .thenComparing(MemberRow::associationName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(MemberRow::boardOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(m -> m.user().getName(), Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

        return userRepository.findAllByRoleInWithCommittee(roles).stream()
                .map(user -> toMemberRow(user, highBoards, committeeBoards, clubBoards))
                .filter(member -> matchesScope(member, committeeIds, clubIds))
                .sorted(order)
                .toList();
    }

    private MemberRow toMemberRow(User user,
                                  Map<UUID, HighBoard> highBoards,
                                  Map<UUID, CommitteeBoard> committeeBoards,
                                  Map<UUID, ClubBoard> clubBoards) {
        switch (user.getRole()) {
            case ACM_HIGH_BOARD -> {
                HighBoard hb = highBoards.get(user.getId());
                return new MemberRow(user, hb != null ? hb.getRole() : null, hb != null ? hb.getOrder() : null, null, null);
            }
            case ACM_COMMITTEE_BOARD -> {
                CommitteeBoard cb = committeeBoards.get(user.getId());
                Committee committee = cb != null && cb.getCommittee() != null ? cb.getCommittee() : user.getCommittee();
                return new MemberRow(user,
                        cb != null ? cb.getRole() : null,
                        cb != null ? cb.getOrder() : null,
                        committee != null ? committee.getId() : null,
                        committee != null ? committee.getName() : null);
            }
            case ACM_CLUB_BOARD -> {
                ClubBoard cb = clubBoards.get(user.getId());
                Club club = cb != null ? cb.getClub() : null;
                return new MemberRow(user,
                        cb != null ? cb.getRole() : null,
                        cb != null ? cb.getOrder() : null,
                        club != null ? club.getId() : null,
                        club != null ? club.getName() : null);
            }
            default -> {
                Committee committee = user.getCommittee();
                return new MemberRow(user, null, null,
                        committee != null ? committee.getId() : null,
                        committee != null ? committee.getName() : null);
            }
        }
    }

    /**
     * Committee/club filters only narrow the roles they apply to; every other selected role passes through.
     */
    private boolean matchesScope(MemberRow member, Set<Long> committeeIds, Set<Long> clubIds) {
        Role role = member.user().getRole();
        if (COMMITTEE_SCOPED_ROLES.contains(role) && !committeeIds.isEmpty()) {
            return member.associationId() != null && committeeIds.contains(member.associationId());
        }
        if (CLUB_SCOPED_ROLES.contains(role) && !clubIds.isEmpty()) {
            return member.associationId() != null && clubIds.contains(member.associationId());
        }
        return true;
    }

    private List<Object> toSheetRow(int seqNum, MemberRow member) {
        User user = member.user();
        return new ArrayList<>(Arrays.asList(
                seqNum,
                user.getName() != null ? user.getName() : "",
                user.getEmail() != null ? user.getEmail() : "",
                user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                roleLabel(user.getRole()),
                member.position() != null ? member.position() : "",
                member.associationName() != null ? member.associationName() : "",
                Boolean.TRUE.equals(user.getIsAlexEngStudent()) ? "Yes" : "No",
                user.getBatch() != null ? user.getBatch() : "",
                user.getDepartment() != null ? user.getDepartment().name() : "",
                user.getLinkedinUrl() != null ? user.getLinkedinUrl() : "",
                user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_TIME_FORMAT) : ""
        ));
    }

    private String resolveTitle(String requestedTitle, String rolesLabel, List<String> committeeNames,
                                List<String> clubNames, LocalDateTime exportedAt) {
        String title = requestedTitle != null ? requestedTitle.trim() : "";
        if (title.isEmpty()) {
            List<String> scope = new ArrayList<>(committeeNames);
            scope.addAll(clubNames);
            title = "ACM Alexandria - Members - "
                    + (scope.isEmpty() ? "All" : String.join(", ", scope))
                    + " - " + rolesLabel
                    + " - " + exportedAt.format(TITLE_DATE_FORMAT);
        }
        return title.length() > MAX_TITLE_LENGTH ? title.substring(0, MAX_TITLE_LENGTH).trim() : title;
    }

    private String describeRoles(Set<Role> roles) {
        return roles.stream()
                .sorted(Comparator.comparingInt(MemberExportService::roleRank))
                .map(MemberExportService::roleLabel)
                .collect(Collectors.joining(", "));
    }

    private String describeScope(Set<Role> roles, Set<Role> scopedRoles, List<String> names, String allLabel) {
        if (Collections.disjoint(roles, scopedRoles)) {
            return "N/A";
        }
        return names.isEmpty() ? allLabel : String.join(", ", names);
    }

    private static String roleLabel(Role role) {
        return ROLE_LABELS.getOrDefault(role, role.name());
    }

    private static int roleRank(Role role) {
        int index = ROLE_ORDER.indexOf(role);
        return index >= 0 ? index : ROLE_ORDER.size();
    }

    private static Set<Role> toRoleSet(List<Role> roles) {
        Set<Role> result = EnumSet.noneOf(Role.class);
        if (roles != null) {
            roles.stream().filter(Objects::nonNull).forEach(result::add);
        }
        return result;
    }

    private static Set<Long> toIdSet(List<Long> ids) {
        if (ids == null) {
            return Set.of();
        }
        return ids.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static <T> List<String> namesOf(List<T> entities, Function<T, String> nameExtractor) {
        return entities.stream()
                .map(nameExtractor)
                .filter(Objects::nonNull)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private static <T> Map<UUID, T> indexByUserId(List<T> boards, Function<T, UUID> userIdExtractor) {
        return boards.stream().collect(Collectors.toMap(userIdExtractor, Function.identity(), (first, second) -> first));
    }
}
