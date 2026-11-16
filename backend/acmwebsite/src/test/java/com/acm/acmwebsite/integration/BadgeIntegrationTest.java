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
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.CommitteeRankingResponse;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.RankedMember;
import com.acm.acmwebsite.feature.seasons.dto.SeasonDtos.PeriodResponse;
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
@DisplayName("Badges: committee ranking, awards and the public pages")
class BadgeIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private CommitteeRepository committeeRepository;
    @Autowired private PointsLogRepository pointsLogRepository;
    @Autowired private BadgeTypeRepository badgeTypeRepository;
    @Autowired private com.acm.acmwebsite.feature.badges.BadgeAwardRepository badgeAwardRepository;
    @Autowired private com.acm.acmwebsite.feature.seasons.SeasonPeriodRepository seasonPeriodRepository;
    @Autowired private UserService userService;
    @Autowired private EntityManager entityManager;
    @Autowired private JwtUtil jwtUtil;

    @MockitoBean private EmailService emailService;
    @MockitoBean private EmailExitanceService emailExitanceService;
    @MockitoBean private GoogleSheetsService googleSheetsService;

    private Committee technical;
    private Committee media;
    private User superAdmin;
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

        superAdmin = saveUser("super@example.com", "Super Admin", Role.SUPER_ADMIN, null);
        highBoard = saveUser("high.board@example.com", "High Board", Role.ACM_HIGH_BOARD, null);
        committeeBoard = saveUser("tech.board@example.com", "Tech Board", Role.ACM_COMMITTEE_BOARD, technical);
        nour = saveUser("nour@example.com", "Nour Hassan", Role.ACM_MEMBER, technical);
        salma = saveUser("salma@example.com", "Salma Ibrahim", Role.ACM_MEMBER, technical);
        ahmed = saveUser("ahmed@example.com", "Ahmed Fathy", Role.ACM_MEMBER, technical);
        youssef = saveUser("youssef@example.com", "Youssef Adel", Role.ACM_MEMBER, media);
        noCommittee = saveUser("free@example.com", "Free Member", Role.ACM_MEMBER, null);

        // Start from an empty calendar; the app creates a current season at startup.
        badgeAwardRepository.deleteAll();
        seasonPeriodRepository.deleteAll();

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
                .profileImageUrl("https://example.org/" + name.replace(' ', '-') + ".jpg")
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
        PeriodResponse month = read(postAs(highBoard, "/api/admin/seasons/months",
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

    private CommitteeRankingResponse ranking(Long typeId, Long periodId, Committee committee, Integer winners) throws Exception {
        Map<String, Object> body = winners == null
                ? Map.of("badgeTypeId", typeId, "periodId", periodId, "committeeId", committee.getId())
                : Map.of("badgeTypeId", typeId, "periodId", periodId, "committeeId", committee.getId(), "winners", winners);
        return read(postAs(highBoard, "/api/admin/badges/committee-ranking", body).andExpect(status().isOk()),
                new TypeReference<>() {});
    }

    @Test
    @DisplayName("A committee's members are ranked by points; the top places are suggested, ties flagged, boards left out")
    void committeeRanking() throws Exception {
        Long monthId = currentMonthId();
        CommitteeRankingResponse tech = ranking(typeId("Monthly MVP"), monthId, technical, 2);

        assertEquals("Technical", tech.committeeName());
        assertEquals(2, tech.winners());
        assertTrue(tech.awarded().isEmpty());
        assertEquals(List.of(nour.getId(), salma.getId(), ahmed.getId()).size(), tech.members().size());
        RankedMember first = tech.members().get(0);
        assertEquals(nour.getId(), first.memberId());
        assertEquals(128, first.points());
        assertTrue(first.suggested());
        // Salma and Ahmed tie for the second place, so the admin picks.
        assertTrue(tech.members().get(1).tied() && tech.members().get(2).tied());
        assertFalse(tech.members().get(1).suggested() || tech.members().get(2).suggested());
        assertTrue(tech.members().stream().noneMatch(m -> m.memberId().equals(committeeBoard.getId())));

        // Without a number, the default of 3 winners; Media's only member is suggested.
        CommitteeRankingResponse med = ranking(typeId("Monthly MVP"), monthId, media, null);
        assertEquals(3, med.winners());
        assertEquals(List.of(youssef.getId()), med.members().stream().map(RankedMember::memberId).toList());
        assertTrue(med.members().get(0).suggested());
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

        // The ranking now lists Nour and Salma as awarded; Ahmed takes the one place left of the default 3.
        CommitteeRankingResponse again = ranking(mvp, monthId, technical, null);
        assertEquals(List.of(nour.getId(), salma.getId()), again.awarded().stream().map(a -> a.member().id()).toList());
        assertEquals(List.of(ahmed.getId()), again.members().stream().map(RankedMember::memberId).toList());
        assertTrue(again.members().get(0).suggested());

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

        // Anyone can open the badge page without signing in.
        mockMvc.perform(get("/api/public/badges/" + nourAward.publicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Monthly MVP"))
                .andExpect(jsonPath("$.committeeName").value("Technical"))
                .andExpect(jsonPath("$.note").value("Great month"))
                .andExpect(jsonPath("$.member.name").value("Nour Hassan"))
                .andExpect(jsonPath("$.awardedByName").doesNotExist());
        // Admins and HR see a member's badges in the member view; there is no public member page.
        mockMvc.perform(get("/api/public/members/" + nour.getId() + "/badges"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/badges/members/" + nour.getId()).header("Authorization", bearer(salma)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/badges/members/" + nour.getId()).header("Authorization", bearer(committeeBoard)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.committeeName").value("Technical"))
                // Members' photos stay private; only board members and admins show one.
                .andExpect(jsonPath("$.member.profileImageUrl").doesNotExist())
                .andExpect(jsonPath("$.badges.length()").value(1));

        mockMvc.perform(get("/api/admin/badges/awards").param("periodId", monthId.toString())
                        .header("Authorization", bearer(highBoard)))
                .andExpect(jsonPath("$.length()").value(2));

        // Revoking hides it everywhere.
        mockMvc.perform(delete("/api/admin/badges/awards/" + nourAward.id()).header("Authorization", bearer(highBoard)))
                .andExpect(status().isNoContent());
        entityManager.flush();
        mockMvc.perform(get("/api/public/badges/" + nourAward.publicId())).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/badges/members/" + nour.getId()).header("Authorization", bearer(committeeBoard)))
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
    @DisplayName("Shared links: a public preview page with LinkedIn tags and a 1200x627 image")
    void shareLinks() throws Exception {
        AwardView award = read(postAs(highBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", typeId("Monthly MVP"), "periodId", currentMonthId(),
                "recipients", List.of(recipient(nour, 1))))
                .andExpect(status().isCreated()), new TypeReference<List<AwardView>>() {}).get(0);
        entityManager.flush();

        String html = mockMvc.perform(get("/share/badges/" + award.publicId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("<meta property=\"og:title\" content=\"Nour Hassan · Monthly MVP"));
        assertTrue(html.contains("<meta property=\"og:image\" content=\"http://localhost:8080/share/badges/" + award.publicId() + "/card.png\">"));
        assertTrue(html.contains("<meta property=\"og:url\" content=\"http://localhost:8080/share/badges/" + award.publicId() + "\">"));
        assertTrue(html.contains("http://localhost:5173/badges/" + award.publicId()));
        assertTrue(html.contains("of the Technical for"));

        byte[] png = mockMvc.perform(get("/share/badges/" + award.publicId() + "/card.png"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentType("image/png"))
                .andReturn().getResponse().getContentAsByteArray();
        var image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(png));
        assertEquals(1200, image.getWidth());
        assertEquals(627, image.getHeight());

        // Unknown or revoked badges aren't shared.
        mockMvc.perform(get("/share/badges/" + UUID.randomUUID())).andExpect(status().isNotFound());
        mockMvc.perform(get("/share/badges/" + UUID.randomUUID() + "/card.png")).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/admin/badges/awards/" + award.id()).header("Authorization", bearer(highBoard)))
                .andExpect(status().isNoContent());
        entityManager.flush();
        mockMvc.perform(get("/share/badges/" + award.publicId())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("A name with HTML in it is escaped on the share page")
    void sharePageEscapes() throws Exception {
        User odd = saveUser("odd@example.com", "Sam <b>\"Odd\"</b>", Role.ACM_MEMBER, technical);
        entityManager.flush();
        AwardView award = read(postAs(highBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", typeId("Monthly MVP"), "periodId", currentMonthId(),
                "recipients", List.of(recipient(odd, null))))
                .andExpect(status().isCreated()), new TypeReference<List<AwardView>>() {}).get(0);
        entityManager.flush();

        String html = mockMvc.perform(get("/share/badges/" + award.publicId()))
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("Sam &lt;b&gt;&quot;Odd&quot;&lt;/b&gt;"));
        assertFalse(html.contains("<b>"));
        // The image still renders.
        mockMvc.perform(get("/share/badges/" + award.publicId() + "/card.png")).andExpect(status().isOk());
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

        // A member with no committee can't receive a badge.
        postAs(highBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", mvp, "periodId", monthId, "recipients", List.of(recipient(noCommittee, null))))
                .andExpect(status().isBadRequest());

        // The badge always goes to the member's own committee; a committee sent with the request is ignored.
        postAs(highBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", mvp, "periodId", monthId,
                "recipients", List.of(Map.of("memberId", youssef.getId(), "committeeId", technical.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].committeeName").value("Media"));

        // A monthly badge can't be given for a quarter or season.
        Long seasonId = read(postAs(superAdmin, "/api/admin/seasons/periods",
                period("SEASON", null, "2026-09-01", "2027-08-31", 2026)).andExpect(status().isCreated()),
                new TypeReference<PeriodResponse>() {}).id();
        postAs(highBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", mvp, "periodId", seasonId, "recipients", List.of(recipient(youssef, null))))
                .andExpect(status().isBadRequest());
        postAs(highBoard, "/api/admin/badges/committee-ranking",
                Map.of("badgeTypeId", mvp, "periodId", seasonId, "committeeId", technical.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Badge types are fixed: they can be listed but not created, edited or deleted")
    void badgeTypesAreReadOnly() throws Exception {
        mockMvc.perform(get("/api/admin/badges/types").header("Authorization", bearer(highBoard)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Monthly MVP"))
                .andExpect(jsonPath("$[2].icon").value("CROWN"));
        postAs(highBoard, "/api/admin/badges/types", Map.of("name", "Event Hero", "icon", "STAR"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(delete("/api/admin/badges/types/" + typeId("Monthly MVP")).header("Authorization", bearer(highBoard)))
                .andExpect(status().is4xxClientError());
        assertEquals(3, badgeTypeRepository.count());
    }

    private static Map<String, Object> period(String type, String name, String start, String end, int season) {
        return name == null
                ? Map.of("type", type, "startDate", start, "endDate", end, "season", season)
                : Map.of("type", type, "name", name, "startDate", start, "endDate", end, "season", season);
    }

    @Test
    @DisplayName("Every admin-page role can manage badges; members can't")
    void adminAccess() throws Exception {
        User clubBoard = saveUser("club.board@example.com", "Club Board", Role.ACM_CLUB_BOARD, null);
        mockMvc.perform(get("/api/admin/badges/types").header("Authorization", bearer(highBoard)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/badges/types").header("Authorization", bearer(clubBoard)))
                .andExpect(status().isOk());
        postAs(committeeBoard, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", typeId("Monthly MVP"), "periodId", currentMonthId(), "recipients", List.of(recipient(nour, 1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].awardedByName").value("Tech Board"));

        mockMvc.perform(get("/api/admin/badges/types").header("Authorization", bearer(nour)))
                .andExpect(status().isForbidden());
        postAs(nour, "/api/admin/badges/awards", Map.of(
                "badgeTypeId", typeId("Monthly MVP"), "periodId", 1, "recipients", List.of(recipient(salma, 1))))
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
