package com.acm.acmwebsite.integration;

import com.acm.acmwebsite.User_Authentication.dto.UserProfileDto;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.badges.BadgeType;
import com.acm.acmwebsite.feature.badges.BadgeTypeRepository;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardView;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.Candidate;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.CommitteeSuggestion;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.PeriodResponse;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.SuggestionResponse;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.PointsLog;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.repository.PointsLogRepository;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Badges: periods, suggestions, awards and the public pages")
class BadgeIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private CommitteeRepository committeeRepository;
    @Autowired private PointsLogRepository pointsLogRepository;
    @Autowired private BadgeTypeRepository badgeTypeRepository;
    @Autowired private UserService userService;
    @Autowired private EntityManager entityManager;
    @Autowired private JwtUtil jwtUtil;

    @MockitoBean private EmailService emailService;
    @MockitoBean private EmailExitanceService emailExitanceService;
    @MockitoBean private GoogleSheetsService googleSheetsService;

    private Committee technical;
    private Committee media;
    private User highBoard;
    private User committeeBoard;
    private User nour;
    private User salma;
    private User ahmed;
    private User youssef;
    private User noCommittee;

    @BeforeEach
    void setUp() {
        when(emailExitanceService.isEmailReal(anyString())).thenReturn(true);

        technical = saveCommittee("Technical");
        media = saveCommittee("Media");

        highBoard = saveUser("high.board@example.com", "High Board", Role.ACM_HIGH_BOARD, null);
        committeeBoard = saveUser("tech.board@example.com", "Tech Board", Role.ACM_COMMITTEE_BOARD, technical);
        nour = saveUser("nour@example.com", "Nour Hassan", Role.ACM_MEMBER, technical);
        salma = saveUser("salma@example.com", "Salma Ibrahim", Role.ACM_MEMBER, technical);
        ahmed = saveUser("ahmed@example.com", "Ahmed Fathy", Role.ACM_MEMBER, technical);
        youssef = saveUser("youssef@example.com", "Youssef Adel", Role.ACM_MEMBER, media);
        noCommittee = saveUser("free@example.com", "Free Member", Role.ACM_MEMBER, null);

        // This month's points. The committee board member's points must not rank.
        logPoints(nour, 128);
        logPoints(salma, 90);
        logPoints(ahmed, 90);
        logPoints(youssef, 115);
        logPoints(committeeBoard, 999);
        entityManager.flush();
    }

    private Committee saveCommittee(String name) {
        Committee c = new Committee();
        c.setName(name);
        c.setDescription(name + " committee");
        return committeeRepository.save(c);
    }

    private User saveUser(String email, String name, Role role, Committee committee) {
        return userRepository.save(User.builder()
                .email(email)
                .name(name)
                .phoneNumber("01012345678")
                .isAlexEngStudent(false)
                .role(role)
                .committee(committee)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build());
    }

    private void logPoints(User member, int points) {
        pointsLogRepository.save(new PointsLog(null, member, highBoard, points, "Task", null, null, highBoard));
    }

    private String bearer(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }

    private ResultActions postAs(User user, String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private <T> T read(ResultActions result, TypeReference<T> type) throws Exception {
        return objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), type);
    }

    private Long typeId(String name) {
        return badgeTypeRepository.findAll().stream().filter(t -> t.getName().equals(name))
                .map(BadgeType::getId).findFirst().orElseThrow();
    }

    private Long currentMonthId() throws Exception {
        PeriodResponse month = read(postAs(highBoard, "/api/admin/badges/periods/months",
                Map.of("month", YearMonth.now().toString())).andExpect(status().isOk()), new TypeReference<>() {});
        return month.id();
    }

    private static Map<String, Object> recipient(User u, Integer rank) {
        return rank == null ? Map.of("memberId", u.getId()) : Map.of("memberId", u.getId(), "rank", rank);
    }

    @Test
    @DisplayName("The three starter badge types are seeded")
    void starterTypesSeeded() {
        List<String> names = badgeTypeRepository.findAllByOrderBySortOrderAscIdAsc().stream().map(BadgeType::getName).toList();
        assertTrue(names.containsAll(List.of("Monthly MVP", "Quarter MVP", "Season Champion")));
    }

    @Test
    @DisplayName("Suggestions rank members per committee, flag ties and skip board members")
    void suggestionsPerCommittee() throws Exception {
        Long monthId = currentMonthId();
        SuggestionResponse response = read(postAs(highBoard, "/api/admin/badges/suggestions", Map.of(
                "badgeTypeId", typeId("Monthly MVP"),
                "periodId", monthId,
                "winnersPerCommittee", Map.of(String.valueOf(technical.getId()), 2)))
                .andExpect(status().isOk()), new TypeReference<>() {});

        CommitteeSuggestion tech = response.committees().stream()
                .filter(c -> c.committeeId().equals(technical.getId())).findFirst().orElseThrow();
        assertEquals(2, tech.winners());
        assertEquals(List.of(nour.getId(), salma.getId(), ahmed.getId()).size(), tech.candidates().size());
        assertEquals(nour.getId(), tech.candidates().get(0).memberId());
        assertEquals(128, tech.candidates().get(0).points());
        assertFalse(tech.candidates().get(0).tied());
        assertTrue(tech.candidates().get(1).tied());
        assertTrue(tech.candidates().get(2).tied());
        assertTrue(tech.candidates().stream().noneMatch(c -> c.memberId().equals(committeeBoard.getId())));

        CommitteeSuggestion med = response.committees().stream()
                .filter(c -> c.committeeId().equals(media.getId())).findFirst().orElseThrow();
        assertEquals(1, med.winners());
        assertEquals(List.of(youssef.getId()), med.candidates().stream().map(Candidate::memberId).toList());
    }

    @Test
    @DisplayName("Awarding, the member's own list, the public pages, revoking and restoring")
    void awardLifecycle() throws Exception {
        Long monthId = currentMonthId();
        Long mvp = typeId("Monthly MVP");

        List<AwardView> awarded = read(postAs(highBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", mvp, "periodId", monthId, "note", "Great month",
                "recipients", List.of(recipient(nour, 1), recipient(salma, 2))))
                .andExpect(status().isCreated()), new TypeReference<>() {});
        assertEquals(2, awarded.size());
        AwardView nourAward = awarded.get(0);
        assertEquals("Technical", nourAward.committeeName());
        assertEquals("High Board", nourAward.awardedByName());

        // The same badge for the same month can't be given twice.
        postAs(highBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", mvp, "periodId", monthId, "recipients", List.of(recipient(nour, 1))))
                .andExpect(status().isConflict());

        // Suggestions now mark Nour as already awarded.
        SuggestionResponse again = read(postAs(highBoard, "/api/admin/badges/suggestions", Map.of(
                "badgeTypeId", mvp, "periodId", monthId)).andExpect(status().isOk()), new TypeReference<>() {});
        assertTrue(again.committees().stream().filter(c -> c.committeeId().equals(technical.getId()))
                .findFirst().orElseThrow().candidates().get(0).alreadyAwarded());

        // The member sees it as new until they open their badges.
        mockMvc.perform(get("/api/badges/me").header("Authorization", bearer(nour)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isNew").value(true))
                .andExpect(jsonPath("$[0].awardedByName").doesNotExist());
        mockMvc.perform(post("/api/badges/me/seen").header("Authorization", bearer(nour)))
                .andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();
        mockMvc.perform(get("/api/badges/me").header("Authorization", bearer(nour)))
                .andExpect(jsonPath("$[0].isNew").value(false));

        // Anyone can open the badge page and the member's badge wall, without signing in.
        mockMvc.perform(get("/api/public/badges/" + nourAward.publicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Monthly MVP"))
                .andExpect(jsonPath("$.committeeName").value("Technical"))
                .andExpect(jsonPath("$.note").value("Great month"))
                .andExpect(jsonPath("$.member.name").value("Nour Hassan"))
                .andExpect(jsonPath("$.awardedByName").doesNotExist());
        mockMvc.perform(get("/api/public/members/" + nour.getId() + "/badges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.committeeName").value("Technical"))
                .andExpect(jsonPath("$.badges.length()").value(1));

        mockMvc.perform(get("/api/admin/badges/awards").param("periodId", monthId.toString())
                        .header("Authorization", bearer(highBoard)))
                .andExpect(jsonPath("$.length()").value(2));

        // Revoking hides it everywhere.
        mockMvc.perform(delete("/api/admin/badges/awards/" + nourAward.id()).header("Authorization", bearer(highBoard)))
                .andExpect(status().isNoContent());
        entityManager.flush();
        mockMvc.perform(get("/api/public/badges/" + nourAward.publicId())).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/public/members/" + nour.getId() + "/badges"))
                .andExpect(jsonPath("$.badges.length()").value(0));

        // Awarding it again restores the same award, with its share link, as new.
        List<AwardView> restored = read(postAs(highBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", mvp, "periodId", monthId, "recipients", List.of(recipient(nour, 1))))
                .andExpect(status().isCreated()), new TypeReference<>() {});
        assertEquals(nourAward.id(), restored.get(0).id());
        assertEquals(nourAward.publicId(), restored.get(0).publicId());
        entityManager.flush();
        mockMvc.perform(get("/api/public/badges/" + nourAward.publicId())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Award requests are checked against the badge's period and the member's committee")
    void awardValidation() throws Exception {
        Long monthId = currentMonthId();
        Long mvp = typeId("Monthly MVP");

        // A monthly badge needs a month.
        postAs(highBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", mvp, "recipients", List.of(recipient(nour, 1))))
                .andExpect(status().isBadRequest());

        // A member with no committee needs one picked.
        postAs(highBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", mvp, "periodId", monthId, "recipients", List.of(recipient(noCommittee, null))))
                .andExpect(status().isBadRequest());
        postAs(highBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", mvp, "periodId", monthId,
                "recipients", List.of(Map.of("memberId", noCommittee.getId(), "committeeId", media.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].committeeName").value("Media"));

        // A manual-only badge has no period, and an awarded type can't be deleted.
        Long hero = read(postAs(highBoard, "/api/admin/badges/types", Map.of(
                "name", "Event Hero", "icon", "STAR", "defaultWinners", 0, "active", true, "sortOrder", 40))
                .andExpect(status().isCreated()), new TypeReference<Map<String, Object>>() {})
                .get("id") instanceof Number n ? n.longValue() : null;
        postAs(highBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", hero, "periodId", monthId, "recipients", List.of(recipient(youssef, null))))
                .andExpect(status().isBadRequest());
        postAs(highBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", hero, "recipients", List.of(recipient(youssef, null))))
                .andExpect(status().isCreated());
        postAs(highBoard, "/api/admin/badges/suggestions", Map.of("badgeTypeId", hero, "periodId", monthId))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/admin/badges/awards").header("Authorization", bearer(highBoard)))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Event Hero"));
        mockMvc.perform(delete("/api/admin/badges/types/" + hero).header("Authorization", bearer(highBoard)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Seasons and quarters: dates must fit, nothing may overlap")
    void periodRules() throws Exception {
        String url = "/api/admin/badges/periods";
        Long seasonId = read(postAs(highBoard, url, period("SEASON", null, "2026-09-01", "2027-08-31", 2026))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Season 2026/27")), new TypeReference<PeriodResponse>() {}).id();

        postAs(highBoard, url, period("QUARTER", "Q1", "2026-09-01", "2026-11-15", 2026))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Q1 · 2026/27"));
        // Overlaps Q1.
        postAs(highBoard, url, period("QUARTER", "Q2", "2026-11-01", "2027-01-31", 2026)).andExpect(status().isConflict());
        // Runs past the season's end.
        postAs(highBoard, url, period("QUARTER", "Q4", "2027-08-01", "2027-09-30", 2026)).andExpect(status().isBadRequest());
        // No such season.
        postAs(highBoard, url, period("QUARTER", "Q1", "2030-09-01", "2030-10-01", 2030)).andExpect(status().isBadRequest());
        // A second 2026 season, and a season overlapping this one.
        postAs(highBoard, url, period("SEASON", null, "2028-09-01", "2029-08-31", 2026)).andExpect(status().isConflict());
        postAs(highBoard, url, period("SEASON", null, "2027-06-01", "2028-05-31", 2027)).andExpect(status().isConflict());
        // Months are automatic; dates must be in order.
        postAs(highBoard, url, period("MONTH", null, "2026-09-01", "2026-09-30", 2026)).andExpect(status().isBadRequest());
        postAs(highBoard, url, period("SEASON", null, "2028-09-01", "2028-08-01", 2028)).andExpect(status().isBadRequest());

        // Shrinking the season would leave Q1 outside it; a season with quarters can't be deleted.
        mockMvc.perform(put(url + "/" + seasonId).header("Authorization", bearer(highBoard))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(period("SEASON", null, "2026-10-01", "2027-08-31", 2026))))
                .andExpect(status().isConflict());
        mockMvc.perform(delete(url + "/" + seasonId).header("Authorization", bearer(highBoard)))
                .andExpect(status().isConflict());

        mockMvc.perform(get(url).header("Authorization", bearer(highBoard)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private static Map<String, Object> period(String type, String name, String start, String end, int season) {
        return name == null
                ? Map.of("type", type, "startDate", start, "endDate", end, "season", season)
                : Map.of("type", type, "name", name, "startDate", start, "endDate", end, "season", season);
    }

    @Test
    @DisplayName("Only super admins and the high board can manage badges")
    void adminAccess() throws Exception {
        mockMvc.perform(get("/api/admin/badges/types").header("Authorization", bearer(highBoard)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/badges/types").header("Authorization", bearer(committeeBoard)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/badges/types").header("Authorization", bearer(nour)))
                .andExpect(status().isForbidden());
        postAs(committeeBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", typeId("Monthly MVP"), "periodId", 1, "recipients", List.of(recipient(nour, 1))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Members can hide themselves from the leaderboard from their profile")
    void leaderboardOptOut() {
        assertFalse(userService.getUserProfileById(nour.getId()).getLeaderboardOptOut());

        UserProfileDto update = userService.getUserProfileById(nour.getId());
        update.setLeaderboardOptOut(true);
        assertTrue(userService.updateUserProfile(nour.getId(), update).getLeaderboardOptOut());

        // Leaving it out of an update keeps the setting.
        UserProfileDto partial = userService.getUserProfileById(nour.getId());
        partial.setLeaderboardOptOut(null);
        assertTrue(userService.updateUserProfile(nour.getId(), partial).getLeaderboardOptOut());

        UUID id = nour.getId();
        entityManager.flush();
        entityManager.clear();
        assertTrue(userRepository.findById(id).orElseThrow().getLeaderboardOptOut());
    }
}
