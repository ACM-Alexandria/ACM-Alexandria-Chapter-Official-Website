package com.acm.acmwebsite.User_Authentication.controller;

import com.acm.acmwebsite.User_Authentication.dto.*;
import com.acm.acmwebsite.User_Authentication.service.RegisterService;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/** AuthController */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final RegisterService registerService;

  @PostMapping("/forgot-password")
  public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordDTO request) {

    // This method returns VOID. It handles "User Found" and "User Not Found"
    // identically.
    userService.initiatePasswordReset(request.getEmail());

    // Always return the same success message
    return ResponseEntity.ok(Collections.singletonMap(
        "message", "If an account with this email exists, a password reset link has been sent."));
  }

  @PostMapping("register")
  public ResponseEntity<SuccessRegisterResponse> registerUser(
      @RequestBody @Valid RegisterDTO registerDTO) {
    SuccessRegisterResponse savedUser = registerService.createUser(registerDTO);
    return ResponseEntity.status(201).body(savedUser);
  }

  @PostMapping("/login")
  public ResponseEntity<?> loginUser(@RequestBody @Valid LoginRequest loginRequest) {
    try {
      LoginResponse response = userService.login(loginRequest);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.status(401)
          .body(new ErrorMessageResponse("Incorrect email or password"));
    }
  }

}
