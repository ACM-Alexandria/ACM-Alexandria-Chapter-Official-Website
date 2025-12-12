package com.acm.acmwebsite.User_Authentication.controller;

import com.acm.acmwebsite.User_Authentication.dto.ForgotPasswordDTO;
import com.acm.acmwebsite.User_Authentication.dto.ResetPasswordDTO;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.User_Authentication.dto.RegisterDTO;
import com.acm.acmwebsite.User_Authentication.dto.SuccessRegisterResponse;
import com.acm.acmwebsite.User_Authentication.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/** RegisterController */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;

  @PostMapping("/forgot-password")
  public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordDTO request) {

    // This method returns VOID. It handles "User Found" and "User Not Found"
    // identically.
    userService.initiatePasswordReset(request.getEmail());

    // Always return the same success message
    return ResponseEntity.ok(Collections.singletonMap(
        "message", "If an account with this email exists, a password reset link has been sent."));
  }

  private final RegisterService registerService;

  @PostMapping("register")
  public ResponseEntity<SuccessRegisterResponse> registerUser(
      @RequestBody @Valid RegisterDTO registerDTO) {
    SuccessRegisterResponse savedUser = registerService.createUser(registerDTO);
    return ResponseEntity.status(201).body(savedUser);
  }

  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordDTO dto) {
    try {
      userService.resetPassword(dto);
      return ResponseEntity.ok(
              Map.of("message", "Password has been reset successfully.")
      );
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(
              Map.of("error", ex.getMessage())
      );
    }
  }

}
