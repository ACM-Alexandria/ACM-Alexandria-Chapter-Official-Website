package com.acm.acmwebsite.User_Authentication.controller;

import com.acm.acmwebsite.User_Authentication.dto.RegisterDTO;
import com.acm.acmwebsite.User_Authentication.dto.SuccessRegisterResponse;
import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.exception.UserNotFoundException;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("register")
  public ResponseEntity<SuccessRegisterResponse> registerUser(
      @RequestBody @Valid RegisterDTO registerDTO) {
    SuccessRegisterResponse savedUser = userService.createUser(registerDTO);
    return ResponseEntity.status(201).body(savedUser);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
    return userService
        .getUserById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/email/{email}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
    return userService
        .getUserByEmail(email)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}/email")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDTO> updateEmail(
      @PathVariable UUID id, @Valid @RequestBody UpdateEmailRequest request) {
    try {
      UserDTO updated = userService.updateUserEmail(id, request.getEmail());
      return ResponseEntity.ok(updated);
    } catch (UserNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping("/{id}/password")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDTO> updatePassword(
      @PathVariable UUID id, @Valid @RequestBody UpdatePasswordRequest request) {
    try {
      UserDTO updated =
          userService.updateUserPassword(id, request.getOldPassword(), request.getNewPassword());
      return ResponseEntity.ok(updated);
    } catch (UserNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
    try {
      userService.deleteUser(id);
      return ResponseEntity.noContent().build();
    } catch (UserNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/check-email")
  public ResponseEntity<EmailCheckResponse> checkEmail(@RequestParam String email) {
    boolean exists = userService.emailExists(email);
    return ResponseEntity.ok(new EmailCheckResponse(exists));
  }

  // Request/Response DTOs
  @Data
  static class UpdateEmailRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
  }

  @Data
  static class UpdatePasswordRequest {
    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;
  }

  @Data
  static class EmailCheckResponse {
    private boolean exists;

    public EmailCheckResponse(boolean exists) {
      this.exists = exists;
    }
  }
}

