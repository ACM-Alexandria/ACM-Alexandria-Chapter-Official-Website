package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.User_Authentication.controller.UserController;
import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.dto.UserProfileDto;
import com.acm.acmwebsite.User_Authentication.exception.TokenExceptionHandler;
import com.acm.acmwebsite.User_Authentication.exception.UserExceptionHandler;
import com.acm.acmwebsite.User_Authentication.exception.UserNotFoundException;
import com.acm.acmwebsite.User_Authentication.service.AuthorizationService;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({UserExceptionHandler.class, TokenExceptionHandler.class})
@DisplayName("UserController WebMvc Tests")
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserService userService;
  @MockitoBean private AuthorizationService authorizationService;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private RateLimitInterceptor rateLimitInterceptor;

  @BeforeEach
  void setUp() throws Exception {
    when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
  }

  @Nested
  @DisplayName("GET /api/v1/users/{id}/profile")
  class GetUserProfileTests {

    @Test
    @DisplayName("should return 401 Unauthorized when request is unauthenticated")
    void shouldReturn401WhenUnauthenticated() throws Exception {
      UUID userId = UUID.randomUUID();
      when(authorizationService.isAuthenticated(any())).thenReturn(false);

      mockMvc.perform(get("/api/v1/users/{id}/profile", userId))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error", equalTo("Unauthorized request")));

      verify(userService, never()).getUserProfileById(any());
    }

    @Test
    @DisplayName("should return 403 Forbidden when user is neither admin nor self")
    void shouldReturn403WhenNeitherAdminNorSelf() throws Exception {
      UUID userId = UUID.randomUUID();
      when(authorizationService.isAuthenticated(any())).thenReturn(true);
      when(authorizationService.isAdmin(any())).thenReturn(false);
      when(authorizationService.isSelf(eq(userId), any())).thenReturn(false);

      mockMvc.perform(get("/api/v1/users/{id}/profile", userId))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.error", equalTo("Forbidden")));

      verify(userService, never()).getUserProfileById(any());
    }

    @Test
    @DisplayName("should return 200 OK with profile when user is self")
    void shouldReturn200WhenUserIsSelf() throws Exception {
      UUID userId = UUID.randomUUID();
      UserProfileDto profile =
          UserProfileDto.builder()
              .id(userId)
              .email("user@acm.org")
              .name("Self User")
              .phoneNumber("01234567890")
              .build();

      when(authorizationService.isAuthenticated(any())).thenReturn(true);
      when(authorizationService.isAdmin(any())).thenReturn(false);
      when(authorizationService.isSelf(eq(userId), any())).thenReturn(true);
      when(userService.getUserProfileById(userId)).thenReturn(profile);

      mockMvc.perform(get("/api/v1/users/{id}/profile", userId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", equalTo(userId.toString())))
          .andExpect(jsonPath("$.email", equalTo("user@acm.org")))
          .andExpect(jsonPath("$.name", equalTo("Self User")));

      verify(userService).getUserProfileById(userId);
    }

    @Test
    @DisplayName("should return 200 OK with profile when user is admin")
    void shouldReturn200WhenUserIsAdmin() throws Exception {
      UUID userId = UUID.randomUUID();
      UserProfileDto profile =
          UserProfileDto.builder()
              .id(userId)
              .email("target@acm.org")
              .name("Target User")
              .phoneNumber("01234567890")
              .build();

      when(authorizationService.isAuthenticated(any())).thenReturn(true);
      when(authorizationService.isAdmin(any())).thenReturn(true);
      when(userService.getUserProfileById(userId)).thenReturn(profile);

      mockMvc.perform(get("/api/v1/users/{id}/profile", userId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", equalTo(userId.toString())))
          .andExpect(jsonPath("$.name", equalTo("Target User")));

      verify(userService).getUserProfileById(userId);
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/users/{id}/profile")
  class UpdateUserProfileTests {

    @Test
    @DisplayName("should return 401 Unauthorized when request is unauthenticated")
    void shouldReturn401WhenUnauthenticated() throws Exception {
      UUID userId = UUID.randomUUID();
      UserProfileDto request =
          UserProfileDto.builder().name("New Name").phoneNumber("01234567890").build();

      when(authorizationService.isAuthenticated(any())).thenReturn(false);

      mockMvc.perform(
              put("/api/v1/users/{id}/profile", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error", equalTo("Unauthorized request")));

      verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    @DisplayName("should return 403 Forbidden when user is neither admin nor self")
    void shouldReturn403WhenNeitherAdminNorSelf() throws Exception {
      UUID userId = UUID.randomUUID();
      UserProfileDto request =
          UserProfileDto.builder().name("New Name").phoneNumber("01234567890").build();

      when(authorizationService.isAuthenticated(any())).thenReturn(true);
      when(authorizationService.isAdmin(any())).thenReturn(false);
      when(authorizationService.isSelf(eq(userId), any())).thenReturn(false);

      mockMvc.perform(
              put("/api/v1/users/{id}/profile", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.error", equalTo("Forbidden")));

      verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    @DisplayName("should return 200 OK with updated profile when authorized")
    void shouldReturn200WhenAuthorized() throws Exception {
      UUID userId = UUID.randomUUID();
      UserProfileDto request =
          UserProfileDto.builder().name("Updated Name").phoneNumber("01234567890").build();

      UserProfileDto response =
          UserProfileDto.builder()
              .id(userId)
              .name("Updated Name")
              .phoneNumber("01234567890")
              .email("user@acm.org")
              .build();

      when(authorizationService.isAuthenticated(any())).thenReturn(true);
      when(authorizationService.isAdmin(any())).thenReturn(true);
      when(userService.updateUserProfile(eq(userId), any(UserProfileDto.class))).thenReturn(response);

      mockMvc.perform(
              put("/api/v1/users/{id}/profile", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name", equalTo("Updated Name")))
          .andExpect(jsonPath("$.phone_number", equalTo("01234567890")));

      verify(userService).updateUserProfile(eq(userId), any(UserProfileDto.class));
    }

    @Test
    @DisplayName("should return 400 Bad Request when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
      UUID userId = UUID.randomUUID();
      UserProfileDto invalidRequest =
          UserProfileDto.builder().name("").phoneNumber("").build();

      mockMvc.perform(
              put("/api/v1/users/{id}/profile", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).updateUserProfile(any(), any());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/users/{id}/profile/image")
  class UploadProfileImageTests {

    @Test
    @DisplayName("should return 401 Unauthorized when request is unauthenticated")
    void shouldReturn401WhenUnauthenticated() throws Exception {
      UUID userId = UUID.randomUUID();
      MockMultipartFile file =
          new MockMultipartFile("file", "test.jpg", "image/jpeg", "image-content".getBytes());

      when(authorizationService.isAuthenticated(any())).thenReturn(false);

      mockMvc.perform(multipart("/api/v1/users/{id}/profile/image", userId).file(file))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error", equalTo("Unauthorized request")));

      verify(userService, never()).uploadProfileImage(any(), any());
    }

    @Test
    @DisplayName("should return 403 Forbidden when user is neither admin nor self")
    void shouldReturn403WhenNeitherAdminNorSelf() throws Exception {
      UUID userId = UUID.randomUUID();
      MockMultipartFile file =
          new MockMultipartFile("file", "test.jpg", "image/jpeg", "image-content".getBytes());

      when(authorizationService.isAuthenticated(any())).thenReturn(true);
      when(authorizationService.isAdmin(any())).thenReturn(false);
      when(authorizationService.isSelf(eq(userId), any())).thenReturn(false);

      mockMvc.perform(multipart("/api/v1/users/{id}/profile/image", userId).file(file))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.error", equalTo("Forbidden")));

      verify(userService, never()).uploadProfileImage(any(), any());
    }

    @Test
    @DisplayName("should return 200 OK with imageUrl when upload succeeds")
    void shouldReturn200WhenUploadSucceeds() throws Exception {
      UUID userId = UUID.randomUUID();
      MockMultipartFile file =
          new MockMultipartFile("file", "test.png", "image/png", "png-content".getBytes());
      String uploadedUrl = "https://res.cloudinary.com/test/image.png";

      when(authorizationService.isAuthenticated(any())).thenReturn(true);
      when(authorizationService.isSelf(eq(userId), any())).thenReturn(true);
      when(userService.uploadProfileImage(eq(userId), any())).thenReturn(uploadedUrl);

      mockMvc.perform(multipart("/api/v1/users/{id}/profile/image", userId).file(file))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.profile_image_url", equalTo(uploadedUrl)));

      verify(userService).uploadProfileImage(eq(userId), any());
    }

    @Test
    @DisplayName("should return 400 Bad Request when uploadProfileImage throws IOException")
    void shouldReturn400WhenUploadThrowsIOException() throws Exception {
      UUID userId = UUID.randomUUID();
      MockMultipartFile file =
          new MockMultipartFile("file", "test.png", "image/png", "png-content".getBytes());

      when(authorizationService.isAuthenticated(any())).thenReturn(true);
      when(authorizationService.isAdmin(any())).thenReturn(true);
      when(userService.uploadProfileImage(eq(userId), any()))
          .thenThrow(new IOException("Cloudinary connection failed"));

      mockMvc.perform(multipart("/api/v1/users/{id}/profile/image", userId).file(file))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.error", equalTo("Image upload failed: Cloudinary connection failed")));

      verify(userService).uploadProfileImage(eq(userId), any());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/users/{id}")
  class GetUserByIdTests {

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 200 OK with UserDTO when user is found")
    void shouldReturn200WhenUserFound() throws Exception {
      UUID userId = UUID.randomUUID();
      UserDTO userDTO = UserDTO.builder().id(userId).email("user@acm.org").name("Found User").build();

      when(userService.getUserById(userId)).thenReturn(Optional.of(userDTO));

      mockMvc.perform(get("/api/v1/users/{id}", userId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", equalTo(userId.toString())))
          .andExpect(jsonPath("$.email", equalTo("user@acm.org")))
          .andExpect(jsonPath("$.name", equalTo("Found User")));

      verify(userService).getUserById(userId);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 404 Not Found when user does not exist")
    void shouldReturn404WhenUserNotFound() throws Exception {
      UUID userId = UUID.randomUUID();
      when(userService.getUserById(userId)).thenReturn(Optional.empty());

      mockMvc.perform(get("/api/v1/users/{id}", userId))
          .andExpect(status().isNotFound());

      verify(userService).getUserById(userId);
    }
  }

  @Nested
  @DisplayName("GET /api/v1/users/email/{email}")
  class GetUserByEmailTests {

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 200 OK with UserDTO when user is found by email")
    void shouldReturn200WhenUserFoundByEmail() throws Exception {
      String email = "target@acm.org";
      UUID userId = UUID.randomUUID();
      UserDTO userDTO = UserDTO.builder().id(userId).email(email).name("Email User").build();

      when(userService.getUserByEmail(email)).thenReturn(Optional.of(userDTO));

      mockMvc.perform(get("/api/v1/users/email/{email}", email))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", equalTo(userId.toString())))
          .andExpect(jsonPath("$.email", equalTo(email)));

      verify(userService).getUserByEmail(email);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 404 Not Found when user is not found by email")
    void shouldReturn404WhenUserNotFoundByEmail() throws Exception {
      String email = "missing@acm.org";
      when(userService.getUserByEmail(email)).thenReturn(Optional.empty());

      mockMvc.perform(get("/api/v1/users/email/{email}", email))
          .andExpect(status().isNotFound());

      verify(userService).getUserByEmail(email);
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/users/{id}/email")
  class UpdateEmailTests {

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 200 OK with updated UserDTO when email update succeeds")
    void shouldReturn200WhenEmailUpdateSucceeds() throws Exception {
      UUID userId = UUID.randomUUID();
      Map<String, String> request = Map.of("email", "new.email@acm.org");

      UserDTO updated =
          UserDTO.builder().id(userId).email("new.email@acm.org").name("Updated").build();

      when(userService.updateUserEmail(userId, "new.email@acm.org")).thenReturn(updated);

      mockMvc.perform(
              put("/api/v1/users/{id}/email", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email", equalTo("new.email@acm.org")));

      verify(userService).updateUserEmail(userId, "new.email@acm.org");
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 404 Not Found when user does not exist")
    void shouldReturn404WhenUserNotFound() throws Exception {
      UUID userId = UUID.randomUUID();
      Map<String, String> request = Map.of("email", "new.email@acm.org");

      when(userService.updateUserEmail(userId, "new.email@acm.org"))
          .thenThrow(new UserNotFoundException("User not found"));

      mockMvc.perform(
              put("/api/v1/users/{id}/email", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound());

      verify(userService).updateUserEmail(userId, "new.email@acm.org");
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 400 Bad Request when email format is invalid")
    void shouldReturn400WhenEmailInvalid() throws Exception {
      UUID userId = UUID.randomUUID();
      Map<String, String> request = Map.of("email", "invalid-email");

      mockMvc.perform(
              put("/api/v1/users/{id}/email", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).updateUserEmail(any(), any());
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/users/{id}/password")
  class UpdatePasswordTests {

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 200 OK with updated UserDTO when password update succeeds")
    void shouldReturn200WhenPasswordUpdateSucceeds() throws Exception {
      UUID userId = UUID.randomUUID();
      Map<String, String> request =
          Map.of("oldPassword", "OldPass123", "newPassword", "NewPass123");

      UserDTO updated = UserDTO.builder().id(userId).email("user@acm.org").build();

      when(userService.updateUserPassword(userId, "OldPass123", "NewPass123")).thenReturn(updated);

      mockMvc.perform(
              put("/api/v1/users/{id}/password", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", equalTo(userId.toString())));

      verify(userService).updateUserPassword(userId, "OldPass123", "NewPass123");
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 404 Not Found when user does not exist")
    void shouldReturn404WhenUserNotFound() throws Exception {
      UUID userId = UUID.randomUUID();
      Map<String, String> request =
          Map.of("oldPassword", "OldPass123", "newPassword", "NewPass123");

      when(userService.updateUserPassword(userId, "OldPass123", "NewPass123"))
          .thenThrow(new UserNotFoundException("User not found"));

      mockMvc.perform(
              put("/api/v1/users/{id}/password", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound());

      verify(userService).updateUserPassword(userId, "OldPass123", "NewPass123");
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 400 Bad Request when old password is incorrect")
    void shouldReturn400WhenOldPasswordIncorrect() throws Exception {
      UUID userId = UUID.randomUUID();
      Map<String, String> request =
          Map.of("oldPassword", "WrongPass", "newPassword", "NewPass123");

      when(userService.updateUserPassword(userId, "WrongPass", "NewPass123"))
          .thenThrow(new IllegalArgumentException("Old password is incorrect"));

      mockMvc.perform(
              put("/api/v1/users/{id}/password", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService).updateUserPassword(userId, "WrongPass", "NewPass123");
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 400 Bad Request when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
      UUID userId = UUID.randomUUID();
      Map<String, String> request = Map.of("oldPassword", "", "newPassword", "");

      mockMvc.perform(
              put("/api/v1/users/{id}/password", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).updateUserPassword(any(), any(), any());
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/users/{id}")
  class DeleteUserTests {

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 204 No Content when user is successfully deleted")
    void shouldReturn204WhenUserDeleted() throws Exception {
      UUID userId = UUID.randomUUID();
      doNothing().when(userService).deleteUser(userId);

      mockMvc.perform(delete("/api/v1/users/{id}", userId))
          .andExpect(status().isNoContent());

      verify(userService).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("should return 404 Not Found when user to delete does not exist")
    void shouldReturn404WhenUserNotFound() throws Exception {
      UUID userId = UUID.randomUUID();
      doThrow(new UserNotFoundException("User not found"))
          .when(userService)
          .deleteUser(userId);

      mockMvc.perform(delete("/api/v1/users/{id}", userId))
          .andExpect(status().isNotFound());

      verify(userService).deleteUser(userId);
    }
  }

  @Nested
  @DisplayName("GET /api/v1/users/check-email")
  class CheckEmailTests {

    @Test
    @DisplayName("should return 200 OK with exists=true when email exists")
    void shouldReturn200WithExistsTrueWhenEmailExists() throws Exception {
      String email = "existing@acm.org";
      when(userService.emailExists(email)).thenReturn(true);

      mockMvc.perform(get("/api/v1/users/check-email").param("email", email))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.exists", equalTo(true)));

      verify(userService).emailExists(email);
    }

    @Test
    @DisplayName("should return 200 OK with exists=false when email does not exist")
    void shouldReturn200WithExistsFalseWhenEmailDoesNotExist() throws Exception {
      String email = "new@acm.org";
      when(userService.emailExists(email)).thenReturn(false);

      mockMvc.perform(get("/api/v1/users/check-email").param("email", email))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.exists", equalTo(false)));

      verify(userService).emailExists(email);
    }
  }
}
