package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.User_Authentication.controller.UserManagementController;
import com.acm.acmwebsite.User_Authentication.dto.UserAssignmentDto;
import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.exception.TokenExceptionHandler;
import com.acm.acmwebsite.User_Authentication.exception.UserExceptionHandler;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.dto.MemberExportPreviewDto;
import com.acm.acmwebsite.feature.dto.MemberExportRequestDto;
import com.acm.acmwebsite.feature.dto.MemberExportResultDto;
import com.acm.acmwebsite.feature.service.MemberExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserManagementController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({UserExceptionHandler.class, TokenExceptionHandler.class})
@DisplayName("UserManagementController WebMvc Tests")
class UserManagementControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserService userService;
  @MockitoBean private MemberExportService memberExportService;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private RateLimitInterceptor rateLimitInterceptor;

  @BeforeEach
  void setUp() throws Exception {
    when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
  }

  @Nested
  @DisplayName("GET /api/v1/admin/users")
  class SearchUsersTests {

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 200 OK with paged users using default query parameters")
    void shouldReturn200WithPagedUsersUsingDefaults() throws Exception {
      UUID userId = UUID.randomUUID();
      UserDTO user =
          UserDTO.builder()
              .id(userId)
              .email("member@acm.org")
              .name("Member")
              .role("USER")
              .build();
      Page<UserDTO> pageResult = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

      when(userService.searchUsers(null, null, null, null, 0, 10)).thenReturn(pageResult);

      mockMvc.perform(get("/api/v1/admin/users"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content[0].id", equalTo(userId.toString())))
          .andExpect(jsonPath("$.content[0].email", equalTo("member@acm.org")))
          .andExpect(jsonPath("$.totalElements", equalTo(1)));

      verify(userService).searchUsers(null, null, null, null, 0, 10);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 200 OK with filtered users when custom query parameters are provided")
    void shouldReturn200WithFilteredUsersWhenParamsProvided() throws Exception {
      UUID userId = UUID.randomUUID();
      UserDTO user =
          UserDTO.builder()
              .id(userId)
              .email("admin@acm.org")
              .name("Admin User")
              .role("SUPER_ADMIN")
              .committeeId(5L)
              .clubId(2L)
              .build();
      Page<UserDTO> pageResult = new PageImpl<>(List.of(user), PageRequest.of(1, 20), 1);

      when(userService.searchUsers("admin", Role.SUPER_ADMIN, 5L, 2L, 1, 20))
          .thenReturn(pageResult);

      mockMvc.perform(
              get("/api/v1/admin/users")
                  .param("query", "admin")
                  .param("role", "SUPER_ADMIN")
                  .param("committeeId", "5")
                  .param("clubId", "2")
                  .param("page", "1")
                  .param("size", "20"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content[0].name", equalTo("Admin User")))
          .andExpect(jsonPath("$.content[0].role", equalTo("SUPER_ADMIN")));

      verify(userService).searchUsers("admin", Role.SUPER_ADMIN, 5L, 2L, 1, 20);
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/admin/users/{id}/role")
  class UpdateUserRoleTests {

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 200 OK with updated UserDTO when role update succeeds")
    void shouldReturn200WhenRoleUpdateSucceeds() throws Exception {
      UUID userId = UUID.randomUUID();
      UserManagementController.UpdateRoleRequest request =
          new UserManagementController.UpdateRoleRequest();
      request.setRole(Role.ACM_HIGH_BOARD);

      UserDTO response =
          UserDTO.builder()
              .id(userId)
              .email("user@acm.org")
              .role(Role.ACM_HIGH_BOARD.name())
              .build();

      when(userService.updateUserRole(userId, Role.ACM_HIGH_BOARD)).thenReturn(response);

      mockMvc.perform(
              put("/api/v1/admin/users/{id}/role", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", equalTo(userId.toString())))
          .andExpect(jsonPath("$.role", equalTo("ACM_HIGH_BOARD")));

      verify(userService).updateUserRole(userId, Role.ACM_HIGH_BOARD);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 400 Bad Request with error message when service throws exception")
    void shouldReturn400WhenServiceThrowsException() throws Exception {
      UUID userId = UUID.randomUUID();
      UserManagementController.UpdateRoleRequest request =
          new UserManagementController.UpdateRoleRequest();
      request.setRole(Role.USER);

      when(userService.updateUserRole(userId, Role.USER))
          .thenThrow(new IllegalArgumentException("Cannot demote the last super admin"));

      mockMvc.perform(
              put("/api/v1/admin/users/{id}/role", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error", equalTo("Cannot demote the last super admin")));

      verify(userService).updateUserRole(userId, Role.USER);
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/admin/users/{id}/associations")
  class AssignAssociationsTests {

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 200 OK with updated UserDTO when associations assigned successfully")
    void shouldReturn200WhenAssociationsAssigned() throws Exception {
      UUID userId = UUID.randomUUID();
      UserManagementController.AssignAssociationsRequest request =
          new UserManagementController.AssignAssociationsRequest();
      request.setCommitteeId(10L);
      request.setClubId(20L);

      UserDTO response =
          UserDTO.builder()
              .id(userId)
              .committeeId(10L)
              .clubId(20L)
              .build();

      when(userService.assignCommitteeAndClubs(userId, 10L, 20L)).thenReturn(response);

      mockMvc.perform(
              put("/api/v1/admin/users/{id}/associations", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", equalTo(userId.toString())))
          .andExpect(jsonPath("$.committee_id", equalTo(10)))
          .andExpect(jsonPath("$.club_id", equalTo(20)));

      verify(userService).assignCommitteeAndClubs(userId, 10L, 20L);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 400 Bad Request when association assignment fails")
    void shouldReturn400WhenAssignmentFails() throws Exception {
      UUID userId = UUID.randomUUID();
      UserManagementController.AssignAssociationsRequest request =
          new UserManagementController.AssignAssociationsRequest();
      request.setCommitteeId(99L);
      request.setClubId(null);

      when(userService.assignCommitteeAndClubs(userId, 99L, null))
          .thenThrow(new RuntimeException("Committee not found"));

      mockMvc.perform(
              put("/api/v1/admin/users/{id}/associations", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error", equalTo("Committee not found")));

      verify(userService).assignCommitteeAndClubs(userId, 99L, null);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/admin/users/{id}/assign")
  class AssignUserTests {

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 200 OK with updated UserDTO when assignUser succeeds")
    void shouldReturn200WhenAssignUserSucceeds() throws Exception {
      UUID userId = UUID.randomUUID();
      UserAssignmentDto request =
          UserAssignmentDto.builder()
              .targetRole(Role.ACM_COMMITTEE_BOARD)
              .boardRole("Head")
              .boardOrder(1)
              .committeeId(3L)
              .build();

      UserDTO response =
          UserDTO.builder()
              .id(userId)
              .role(Role.ACM_COMMITTEE_BOARD.name())
              .boardRole("Head")
              .boardOrder(1)
              .committeeId(3L)
              .build();

      when(userService.assignUser(eq(userId), any(UserAssignmentDto.class))).thenReturn(response);

      mockMvc.perform(
              post("/api/v1/admin/users/{id}/assign", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", equalTo(userId.toString())))
          .andExpect(jsonPath("$.role", equalTo("ACM_COMMITTEE_BOARD")))
          .andExpect(jsonPath("$.board_role", equalTo("Head")));

      verify(userService).assignUser(eq(userId), any(UserAssignmentDto.class));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 400 Bad Request when assignUser throws exception")
    void shouldReturn400WhenAssignUserThrowsException() throws Exception {
      UUID userId = UUID.randomUUID();
      UserAssignmentDto request =
          UserAssignmentDto.builder()
              .targetRole(Role.ACM_HIGH_BOARD)
              .build();

      when(userService.assignUser(eq(userId), any(UserAssignmentDto.class)))
          .thenThrow(new RuntimeException("Insufficient permissions to assign role"));

      mockMvc.perform(
              post("/api/v1/admin/users/{id}/assign", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error", equalTo("Insufficient permissions to assign role")));

      verify(userService).assignUser(eq(userId), any(UserAssignmentDto.class));
    }
  }

  @Nested
  @DisplayName("POST /api/v1/admin/users/export/preview")
  class PreviewMembersExportTests {

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 200 OK with MemberExportPreviewDto when preview succeeds")
    void shouldReturn200WhenPreviewSucceeds() throws Exception {
      MemberExportRequestDto request =
          MemberExportRequestDto.builder()
              .roles(List.of(Role.USER))
              .title("Spring 2026 Export")
              .build();

      MemberExportPreviewDto previewDto =
          MemberExportPreviewDto.builder()
              .totalMembers(42)
              .alexUniStudentCount(30)
              .roleCounts(Map.of("USER", 42L))
              .associationCounts(Map.of("Web Committee", 15L))
              .build();

      when(memberExportService.preview(any(MemberExportRequestDto.class))).thenReturn(previewDto);

      mockMvc.perform(
              post("/api/v1/admin/users/export/preview")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalMembers", equalTo(42)))
          .andExpect(jsonPath("$.alexUniStudentCount", equalTo(30)));

      verify(memberExportService).preview(any(MemberExportRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 400 Bad Request when preview service throws exception")
    void shouldReturn400WhenPreviewThrowsException() throws Exception {
      MemberExportRequestDto request = MemberExportRequestDto.builder().build();

      when(memberExportService.preview(any(MemberExportRequestDto.class)))
          .thenThrow(new RuntimeException("Failed to generate export preview"));

      mockMvc.perform(
              post("/api/v1/admin/users/export/preview")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error", equalTo("Failed to generate export preview")));

      verify(memberExportService).preview(any(MemberExportRequestDto.class));
    }
  }

  @Nested
  @DisplayName("POST /api/v1/admin/users/export/sheet")
  class ExportMembersSheetTests {

    @Test
    @WithMockUser(username = "admin@acm.org", roles = "SUPER_ADMIN")
    @DisplayName("should return 200 OK with result when exporter authentication is present")
    void shouldReturn200WhenExporterAuthenticationIsPresent() throws Exception {
      MemberExportRequestDto request =
          MemberExportRequestDto.builder()
              .title("All Active Members")
              .build();

      MemberExportResultDto result =
          MemberExportResultDto.builder()
              .googleSheetUrl("https://docs.google.com/spreadsheets/d/abc123xyz")
              .title("All Active Members")
              .totalMembers(150)
              .exportedAt(LocalDateTime.now())
              .build();

      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken("admin@acm.org", null, Collections.emptyList());

      when(memberExportService.exportToSheet(any(MemberExportRequestDto.class), eq("admin@acm.org")))
          .thenReturn(result);

      mockMvc.perform(
              post("/api/v1/admin/users/export/sheet")
                  .principal(auth)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.googleSheetUrl", equalTo("https://docs.google.com/spreadsheets/d/abc123xyz")))
          .andExpect(jsonPath("$.totalMembers", equalTo(150)));

      verify(memberExportService).exportToSheet(any(MemberExportRequestDto.class), eq("admin@acm.org"));
    }

    @Test
    @DisplayName("should return 200 OK with null exporterEmail when authentication is null")
    void shouldReturn200WhenAuthenticationIsNull() throws Exception {
      MemberExportRequestDto request =
          MemberExportRequestDto.builder()
              .title("Anonymous Export")
              .build();

      MemberExportResultDto result =
          MemberExportResultDto.builder()
              .googleSheetUrl("https://docs.google.com/spreadsheets/d/null-auth")
              .title("Anonymous Export")
              .totalMembers(5)
              .exportedAt(LocalDateTime.now())
              .build();

      when(memberExportService.exportToSheet(any(MemberExportRequestDto.class), eq(null)))
          .thenReturn(result);

      mockMvc.perform(
              post("/api/v1/admin/users/export/sheet")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.googleSheetUrl", equalTo("https://docs.google.com/spreadsheets/d/null-auth")))
          .andExpect(jsonPath("$.totalMembers", equalTo(5)));

      verify(memberExportService).exportToSheet(any(MemberExportRequestDto.class), eq(null));
    }

    @Test
    @DisplayName("should return 400 Bad Request when export service throws exception")
    void shouldReturn400WhenExportThrowsException() throws Exception {
      MemberExportRequestDto request = MemberExportRequestDto.builder().build();

      when(memberExportService.exportToSheet(any(MemberExportRequestDto.class), any()))
          .thenThrow(new RuntimeException("Google Drive quota exceeded"));

      mockMvc.perform(
              post("/api/v1/admin/users/export/sheet")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error", equalTo("Google Drive quota exceeded")));

      verify(memberExportService).exportToSheet(any(MemberExportRequestDto.class), any());
    }
  }
}
