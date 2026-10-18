package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
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
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import com.acm.acmwebsite.feature.service.MemberExportService;
import com.acm.acmwebsite.feature.service.MemberTargetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MemberExportServiceTest {

    private static final int HEADER_ROW_INDEX = 7; // 6 metadata rows + 1 blank separator

    @Mock
    private UserRepository userRepository;

    @Mock
    private HighBoardRepository highBoardRepository;

    @Mock
    private CommitteeBoardRepository committeeBoardRepository;

    @Mock
    private ClubBoardRepository clubBoardRepository;

    @Mock
    private CommitteeRepository committeeRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private GoogleSheetsService googleSheetsService;
    @InjectMocks
    private MemberExportService memberExportService;

    private Committee tech;
    private Committee media;
    private Club cpClub;
    private Club roboticsClub;

    @BeforeEach
    void setUp() {
        MemberTargetingService memberTargetingService = new MemberTargetingService(
                                userRepository, highBoardRepository, committeeBoardRepository, clubBoardRepository);
        memberExportService = new MemberExportService(
                                memberTargetingService, committeeRepository, clubRepository, googleSheetsService);
        ReflectionTestUtils.setField(memberExportService, "membersFolderId", "members-folder");
        tech = committee(1L, "Tech");
        media = committee(2L, "Media");
        cpClub = new Club(10L, "CP Club", "desc", "img", List.of());
        roboticsClub = new Club(11L, "Robotics Club", "desc", "img", List.of());
    }

    private Committee committee(long id, String name) {
        Committee committee = new Committee();
        committee.setId(id);
        committee.setName(name);
        return committee;
    }

    private User user(String name, Role role, Committee committee) {
        return User.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(name.toLowerCase().replace(" ", ".") + "@acm.org")
                .phoneNumber("0100")
                .isAlexEngStudent(true)
                .department(Department.CSED)
                .batch("2026")
                .role(role)
                .committee(committee)
                .createdAt(LocalDateTime.of(2026, 1, 15, 10, 30))
                .build();
    }

    private MemberExportRequestDto request(List<Role> roles, List<Long> committeeIds, List<Long> clubIds, String title) {
        return MemberExportRequestDto.builder()
                .roles(roles)
                .committeeIds(committeeIds)
                .clubIds(clubIds)
                .title(title)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<List<Object>> captureExportedRows(String expectedTitle) {
        ArgumentCaptor<List<List<Object>>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(googleSheetsService).createSpreadsheetWithData(
                expectedTitle != null ? eq(expectedTitle) : anyString(), eq("members-folder"), rowsCaptor.capture());
        return rowsCaptor.getValue();
    }

    @Test
    void preview_requiresAtLeastOneRole() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> memberExportService.preview(request(List.of(), null, null, null)));

        assertEquals("Select at least one role to export.", ex.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void exportToSheet_requiresAtLeastOneRole() {
        assertThrows(IllegalArgumentException.class,
                () -> memberExportService.exportToSheet(request(null, null, null, null), "admin@acm.org"));

        verifyNoInteractions(googleSheetsService);
    }

    @Test
    void preview_onlyLoadsBoardsForSelectedRoles() {
        User member = user("Mona", Role.ACM_MEMBER, tech);
        when(userRepository.findAllByRoleInWithCommittee(any())).thenReturn(List.of(member));

        MemberExportPreviewDto preview = memberExportService.preview(request(List.of(Role.ACM_MEMBER), null, null, null));

        assertEquals(1, preview.getTotalMembers());
        verifyNoInteractions(highBoardRepository, committeeBoardRepository, clubBoardRepository);
    }

    @Test
    void preview_committeeFilterNarrowsOnlyCommitteeScopedRoles() {
        User chairman = user("Chairman", Role.ACM_HIGH_BOARD, null);
        User techMember = user("Tech Member", Role.ACM_MEMBER, tech);
        User mediaMember = user("Media Member", Role.ACM_MEMBER, media);
        when(userRepository.findAllByRoleInWithCommittee(any())).thenReturn(List.of(chairman, techMember, mediaMember));
        when(highBoardRepository.findAllWithUser()).thenReturn(List.of(new HighBoard(1L, "Chairman", 1, chairman)));

        MemberExportPreviewDto preview = memberExportService.preview(
                request(List.of(Role.ACM_HIGH_BOARD, Role.ACM_MEMBER), List.of(tech.getId()), null, null));

        assertEquals(2, preview.getTotalMembers());
        assertEquals(Map.of("High Board", 1L, "ACM Member", 1L), preview.getRoleCounts());
        assertEquals(Map.of("Tech", 1L), preview.getAssociationCounts());
    }

    @Test
    void preview_committeeBoardUsesCommitteeFromBoardRow() {
        // The board row is the source of truth for heads, even if users.committee_id drifted.
        User head = user("Head", Role.ACM_COMMITTEE_BOARD, media);
        when(userRepository.findAllByRoleInWithCommittee(any())).thenReturn(List.of(head));
        when(committeeBoardRepository.findAllWithUserAndCommittee())
                .thenReturn(List.of(new CommitteeBoard(1L, "Head", 1, tech, head)));

        MemberExportPreviewDto preview = memberExportService.preview(
                request(List.of(Role.ACM_COMMITTEE_BOARD), List.of(tech.getId()), null, null));

        assertEquals(1, preview.getTotalMembers());
        assertEquals(Map.of("Tech", 1L), preview.getAssociationCounts());
    }

    @Test
    void preview_clubFilterKeepsOnlyBoardsOfSelectedClubs() {
        User cpHead = user("CP Head", Role.ACM_CLUB_BOARD, null);
        User roboticsHead = user("Robotics Head", Role.ACM_CLUB_BOARD, null);
        when(userRepository.findAllByRoleInWithCommittee(any())).thenReturn(List.of(cpHead, roboticsHead));
        when(clubBoardRepository.findAllWithUserAndClub()).thenReturn(List.of(
                new ClubBoard(1L, "Head", 1, cpClub, cpHead),
                new ClubBoard(2L, "Head", 1, roboticsClub, roboticsHead)));

        MemberExportPreviewDto preview = memberExportService.preview(
                request(List.of(Role.ACM_CLUB_BOARD), null, List.of(cpClub.getId()), null));

        assertEquals(1, preview.getTotalMembers());
        assertEquals(Map.of("CP Club", 1L), preview.getAssociationCounts());
    }

    @Test
    void preview_countsAlexStudentsAndOrdersRolesByRank() {
        User member = user("Member", Role.ACM_MEMBER, tech);
        User nonAlexMember = user("Other Member", Role.ACM_MEMBER, tech);
        nonAlexMember.setIsAlexEngStudent(false);
        User chairman = user("Chairman", Role.ACM_HIGH_BOARD, null);
        when(userRepository.findAllByRoleInWithCommittee(any())).thenReturn(List.of(member, nonAlexMember, chairman));
        when(highBoardRepository.findAllWithUser()).thenReturn(List.of());

        MemberExportPreviewDto preview = memberExportService.preview(
                request(List.of(Role.ACM_MEMBER, Role.ACM_HIGH_BOARD), null, null, null));

        assertEquals(3, preview.getTotalMembers());
        assertEquals(2, preview.getAlexUniStudentCount());
        assertEquals(List.of("High Board", "ACM Member"), new ArrayList<>(preview.getRoleCounts().keySet()));
    }

    @Test
    void preview_noMatchesReturnsZero() {
        when(userRepository.findAllByRoleInWithCommittee(any())).thenReturn(List.of());

        MemberExportPreviewDto preview = memberExportService.preview(request(List.of(Role.ACM_MEMBER), null, null, null));

        assertEquals(0, preview.getTotalMembers());
        assertTrue(preview.getRoleCounts().isEmpty());
    }

    @Test
    void exportToSheet_noMatchesThrowsWithoutCreatingSheet() {
        when(userRepository.findAllByRoleInWithCommittee(any())).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> memberExportService.exportToSheet(request(List.of(Role.ACM_MEMBER), null, null, null), "admin@acm.org"));

        assertEquals("No members match the selected filters.", ex.getMessage());
        verify(googleSheetsService, never()).createSpreadsheetWithData(anyString(), anyString(), anyList());
    }

    @Test
    void exportToSheet_writesMetadataHeaderAndSortedRowsToMembersFolder() {
        User zeinaMember = user("Zeina", Role.ACM_MEMBER, tech);
        User adamMember = user("Adam", Role.ACM_MEMBER, tech);
        User viceHead = user("Vice Head", Role.ACM_COMMITTEE_BOARD, tech);
        User head = user("Head", Role.ACM_COMMITTEE_BOARD, tech);
        User chairman = user("Chairman", Role.ACM_HIGH_BOARD, null);
        chairman.setLinkedinUrl("https://linkedin.com/in/chairman");
        User mediaMember = user("Media Member", Role.ACM_MEMBER, media);

        when(userRepository.findAllByRoleInWithCommittee(any()))
                .thenReturn(List.of(zeinaMember, viceHead, mediaMember, chairman, adamMember, head));
        when(highBoardRepository.findAllWithUser()).thenReturn(List.of(new HighBoard(1L, "Chairman", 1, chairman)));
        when(committeeBoardRepository.findAllWithUserAndCommittee()).thenReturn(List.of(
                new CommitteeBoard(1L, "Vice Head", 2, tech, viceHead),
                new CommitteeBoard(2L, "Head", 1, tech, head)));
        when(committeeRepository.findAllById(any())).thenReturn(List.of(tech));
        doCallRealMethod().when(googleSheetsService).buildMetadataRows(anyList());
        when(googleSheetsService.createSpreadsheetWithData(anyString(), anyString(), anyList()))
                .thenReturn("https://docs.google.com/spreadsheets/d/abc/edit");

        MemberExportResultDto result = memberExportService.exportToSheet(
                request(List.of(Role.ACM_MEMBER, Role.ACM_COMMITTEE_BOARD, Role.ACM_HIGH_BOARD),
                        List.of(tech.getId()), null, "  Tech Committee  "),
                "admin@acm.org");

        assertEquals("https://docs.google.com/spreadsheets/d/abc/edit", result.getGoogleSheetUrl());
        assertEquals("Tech Committee", result.getTitle());
        assertEquals(5, result.getTotalMembers());
        assertNotNull(result.getExportedAt());

        List<List<Object>> rows = captureExportedRows("Tech Committee");
        assertEquals(List.of("Exported By:", "admin@acm.org"), rows.get(1));
        assertEquals(List.of("Roles:", "High Board, Committee Board, ACM Member"), rows.get(2));
        assertEquals(List.of("Committees:", "Tech"), rows.get(3));
        assertEquals(List.of("Clubs:", "N/A"), rows.get(4));
        assertEquals(List.of("Total Members:", "5"), rows.get(5));
        assertTrue(rows.get(6).isEmpty());
        assertEquals("#", rows.get(HEADER_ROW_INDEX).get(0));
        assertEquals("Joined At", rows.get(HEADER_ROW_INDEX).get(11));

        List<List<Object>> dataRows = rows.subList(HEADER_ROW_INDEX + 1, rows.size());
        assertEquals(List.of("Chairman", "Head", "Vice Head", "Adam", "Zeina"),
                dataRows.stream().map(r -> r.get(1)).toList());
        assertEquals(List.of(1, "Chairman", "chairman@acm.org", "0100", "High Board", "Chairman", "",
                        "Yes", "2026", "CSED", "https://linkedin.com/in/chairman", "2026-01-15 10:30:00"),
                dataRows.get(0));
        assertEquals("Committee Board", dataRows.get(1).get(4));
        assertEquals("Head", dataRows.get(1).get(5));
        assertEquals("Tech", dataRows.get(1).get(6));
        verify(userRepository, never()).save(any());
    }

    @Test
    void exportToSheet_blankTitleGeneratesDefaultName() {
        User member = user("Mona", Role.ACM_MEMBER, tech);
        when(userRepository.findAllByRoleInWithCommittee(any())).thenReturn(List.of(member));
        when(committeeRepository.findAllById(any())).thenReturn(List.of(tech));
        when(googleSheetsService.buildMetadataRows(anyList())).thenReturn(new ArrayList<>());
        when(googleSheetsService.createSpreadsheetWithData(anyString(), anyString(), anyList())).thenReturn("url");

        MemberExportResultDto result = memberExportService.exportToSheet(
                request(List.of(Role.ACM_MEMBER), List.of(tech.getId()), null, "   "), "admin@acm.org");

        assertTrue(result.getTitle().matches("ACM Alexandria - Members - Tech - ACM Member - \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"),
                result.getTitle());
        captureExportedRows(result.getTitle());
    }

    @Test
    void exportToSheet_capsTitleLength() {
        User member = user("Mona", Role.ACM_MEMBER, tech);
        when(userRepository.findAllByRoleInWithCommittee(any())).thenReturn(List.of(member));
        when(googleSheetsService.buildMetadataRows(anyList())).thenReturn(new ArrayList<>());
        when(googleSheetsService.createSpreadsheetWithData(anyString(), anyString(), anyList())).thenReturn("url");

        MemberExportResultDto result = memberExportService.exportToSheet(
                request(List.of(Role.ACM_MEMBER), null, null, "x".repeat(300)), "admin@acm.org");

        assertEquals(150, result.getTitle().length());
    }

    @Test
    void preview_throwsWhenRequestIsNull() {
        assertThrows(IllegalArgumentException.class, () -> memberExportService.preview(null));
    }

    @Test
    void exportToSheet_handlesNullExporterEmail_nullTitle_allScopes_andNullUserFields() {
        User sparseUser = User.builder()
                .id(UUID.randomUUID())
                .role(Role.ACM_COMMITTEE_BOARD)
                .name(null)
                .email(null)
                .phoneNumber(null)
                .isAlexEngStudent(null)
                .batch(null)
                .department(null)
                .linkedinUrl(null)
                .createdAt(null)
                .build();

        when(userRepository.findAllByRoleInWithCommittee(any())).thenReturn(List.of(sparseUser));
        when(committeeBoardRepository.findAllWithUserAndCommittee()).thenReturn(List.of());
        doCallRealMethod().when(googleSheetsService).buildMetadataRows(anyList());
        when(googleSheetsService.createSpreadsheetWithData(anyString(), anyString(), anyList())).thenReturn("url");

        MemberExportResultDto result = memberExportService.exportToSheet(
                request(List.of(Role.ACM_COMMITTEE_BOARD), null, null, null), null);

        assertNotNull(result);
        assertTrue(result.getTitle().contains("All"));

        List<List<Object>> rows = captureExportedRows(result.getTitle());
        assertEquals(List.of("Exported By:", ""), rows.get(1));
        assertEquals(List.of("Committees:", "All Committees"), rows.get(3));
        assertEquals(List.of("Clubs:", "N/A"), rows.get(4));

        List<Object> userRow = rows.get(HEADER_ROW_INDEX + 1);
        assertEquals(1, userRow.get(0));
        assertEquals("", userRow.get(1)); // name
        assertEquals("", userRow.get(2)); // email
        assertEquals("", userRow.get(3)); // phone
        assertEquals("Committee Board", userRow.get(4)); // role
        assertEquals("", userRow.get(5)); // position
        assertEquals("", userRow.get(6)); // association
        assertEquals("No", userRow.get(7)); // alex student
        assertEquals("", userRow.get(8)); // batch
        assertEquals("", userRow.get(9)); // department
        assertEquals("", userRow.get(10)); // linkedin
        assertEquals("", userRow.get(11)); // createdAt
    }

    @Test
    void exportToSheet_describesClubScope_whenClubScopedRolesAndClubsPresent() {
        User clubHead = user("Club Head", Role.ACM_CLUB_BOARD, null);
        when(userRepository.findAllByRoleInWithCommittee(any())).thenReturn(List.of(clubHead));
        when(clubBoardRepository.findAllWithUserAndClub()).thenReturn(List.of(
                new ClubBoard(1L, "Head", 1, cpClub, clubHead)));

        Club unnamedClub = new Club(99L, null, "desc", "img", List.of());
        when(clubRepository.findAllById(any())).thenReturn(List.of(cpClub, unnamedClub));
        doCallRealMethod().when(googleSheetsService).buildMetadataRows(anyList());
        when(googleSheetsService.createSpreadsheetWithData(anyString(), anyString(), anyList())).thenReturn("url");

        MemberExportResultDto result = memberExportService.exportToSheet(
                request(List.of(Role.ACM_CLUB_BOARD), null, List.of(cpClub.getId()), "Clubs Export"), "admin@acm.org");

        List<List<Object>> rows = captureExportedRows("Clubs Export");
        assertEquals(List.of("Committees:", "N/A"), rows.get(3));
        assertEquals(List.of("Clubs:", "CP Club"), rows.get(4));
    }

    @Test
    void exportToSheet_describesAllClubs_whenClubScopedRolesPresentAndNoClubsSelected() {
        User clubHead = user("Club Head", Role.ACM_CLUB_BOARD, null);
        when(userRepository.findAllByRoleInWithCommittee(any())).thenReturn(List.of(clubHead));
        when(clubBoardRepository.findAllWithUserAndClub()).thenReturn(List.of());
        when(clubRepository.findAllById(any())).thenReturn(List.of());
        doCallRealMethod().when(googleSheetsService).buildMetadataRows(anyList());
        when(googleSheetsService.createSpreadsheetWithData(anyString(), anyString(), anyList())).thenReturn("url");

        MemberExportResultDto result = memberExportService.exportToSheet(
                request(List.of(Role.ACM_CLUB_BOARD), null, null, "Clubs All Export"), "admin@acm.org");

        List<List<Object>> rows = captureExportedRows("Clubs All Export");
        assertEquals(List.of("Clubs:", "All Clubs"), rows.get(4));
    }
}
