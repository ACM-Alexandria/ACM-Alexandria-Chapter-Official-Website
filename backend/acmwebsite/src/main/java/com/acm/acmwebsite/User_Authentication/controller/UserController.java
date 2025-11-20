package com.acm.acmwebsite.User_Authentication.controller;

import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.exception.UserNotFoundException;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public ResponseEntity<List<UserDTO>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
    return userService.getUserById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/email/{email}")
  public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
    return userService.getUserByEmail(email)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}/email")
  public ResponseEntity<UserDTO> updateEmail(
      @PathVariable Long id,
      @Valid @RequestBody UpdateEmailRequest request) {
    try {
      UserDTO updated = userService.updateUserEmail(id, request.getEmail());
      return ResponseEntity.ok(updated);
    } catch (UserNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping("/{id}/password")
  public ResponseEntity<UserDTO> updatePassword(
      @PathVariable Long id,
      @Valid @RequestBody UpdatePasswordRequest request) {
    try {
      UserDTO updated = userService.updateUserPassword(id, request.getPassword());
      return ResponseEntity.ok(updated);
    } catch (UserNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
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
    @NotBlank(message = "Password is required")
    private String password;
  }

  @Data
  static class EmailCheckResponse {
    private boolean exists;

    public EmailCheckResponse(boolean exists) {
      this.exists = exists;
    }
  }
}