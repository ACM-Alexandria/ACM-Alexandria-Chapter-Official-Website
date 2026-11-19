package com.acm.acmwebsite.integration;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.PointsLog;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.repository.PointsLogRepository;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
@DisplayName("Points log: only points managers can change points, never their own")
class PointsLogAccessIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private CommitteeRepository committeeRepository;
    @Autowired private PointsLogRepository pointsLogRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private JwtUtil jwtUtil;

    @MockitoBean private EmailService emailService;
    @MockitoBean private EmailExitanceService emailExitanceService;
    @MockitoBean private GoogleSheetsService googleSheetsService;

    private User board;
    private User hrMember;
    private User plainMember;
    private User target;

    @BeforeEach
    void setUp() {
        when(emailExitanceService.isEmailReal(anyString())).thenReturn(true);

        Committee hr = new Committee();
        hr.setName("HR Committee");
        hr.setDescription("People and culture");
        hr = committeeRepository.save(hr);

        Committee media = new Committee();
        media.setName("Media Committee");
        media.setDescription("Design and content");
        media = committeeRepository.save(media);

        board = saveUser("board@example.com", Role.ACM_COMMITTEE_BOARD, media);
        hrMember = saveUser("hr.member@example.com", Role.ACM_MEMBER, hr);
        plainMember = saveUser("media.member@example.com", Role.ACM_MEMBER, media);
        target = saveUser("target@example.com", Role.ACM_MEMBER, media);
        entityManager.flush();
    }

    private User saveUser(String email, Role role, Committee committee) {
        return userRepository.save(User.builder()
                .email(email)
                .name(email)
                .phoneNumber("01012345678")
                .isAlexEngStudent(false)
                .role(role)
                .committee(committee)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build());
    }

    private String bearer(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }

    private String addBody(UUID member, UUID assigner, int points) {
        return "{\"member\":\"" + member + "\",\"assigner\":\"" + assigner + "\",\"points\":" + points + ",\"reason\":\"Task\"}";
    }

    @Test
    @DisplayName("A plain member cannot add points, even to someone else")
    void plainMemberCannotAddPoints() throws Exception {
        mockMvc.perform(post("/api/pointsLog/add")
                        .header("Authorization", bearer(plainMember))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody(target.getId(), board.getId(), 50)))
                .andExpect(status().isForbidden());

        assertEquals(0, pointsLogRepository.count());
    }

    @Test
    @DisplayName("A board member can add points, and is recorded as the assigner whatever the body says")
    void boardAddsPointsAsThemselves() throws Exception {
        mockMvc.perform(post("/api/pointsLog/add")
                        .header("Authorization", bearer(board))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody(target.getId(), hrMember.getId(), 40)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assigner").value(board.getId().toString()))
                .andExpect(jsonPath("$.points").value(40));

        entityManager.flush();
        entityManager.clear();
        assertEquals(40, userRepository.findById(target.getId()).orElseThrow().getSeasonPoints());
    }

    @Test
    @DisplayName("An HR committee member can add points to another member")
    void hrMemberCanAddPoints() throws Exception {
        mockMvc.perform(post("/api/pointsLog/add")
                        .header("Authorization", bearer(hrMember))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody(target.getId(), hrMember.getId(), 15)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Nobody can give points to themselves")
    void cannotAddPointsToSelf() throws Exception {
        mockMvc.perform(post("/api/pointsLog/add")
                        .header("Authorization", bearer(hrMember))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody(hrMember.getId(), board.getId(), 100)))
                .andExpect(status().isForbidden());

        assertEquals(0, pointsLogRepository.count());
    }

    @Test
    @DisplayName("A plain member cannot edit or delete a points log, but can still read logs")
    void plainMemberCannotEditOrDelete() throws Exception {
        PointsLog log = pointsLogRepository.save(new PointsLog(null, target, board, 10, "Task", null, null, board));
        entityManager.flush();

        mockMvc.perform(put("/api/pointsLog/update/" + log.getId())
                        .header("Authorization", bearer(plainMember))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"points\":999,\"reason\":\"Hacked\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/pointsLog/delete/" + log.getId())
                        .header("Authorization", bearer(plainMember)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/pointsLog/" + target.getId() + "/logs")
                        .header("Authorization", bearer(plainMember)))
                .andExpect(status().isOk());

        List<PointsLog> logs = pointsLogRepository.findAll();
        assertEquals(1, logs.size());
        assertEquals(10, logs.get(0).getPoints());
    }

    @Test
    @DisplayName("A board member updating a log is recorded as the last updater")
    void boardUpdatesAsThemselves() throws Exception {
        PointsLog log = pointsLogRepository.save(new PointsLog(null, target, hrMember, 10, "Task", null, null, hrMember));
        entityManager.flush();

        mockMvc.perform(put("/api/pointsLog/update/" + log.getId())
                        .header("Authorization", bearer(board))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"points\":25,\"reason\":\"Task, revised\",\"lastUpdater\":\"" + hrMember.getId() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastUpdater").value(board.getId().toString()))
                .andExpect(jsonPath("$.points").value(25));
    }
}
