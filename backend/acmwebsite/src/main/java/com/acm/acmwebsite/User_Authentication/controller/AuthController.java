package com.acm.acmwebsite.User_Authentication.controller;

import com.acm.acmwebsite.User_Authentication.dto.*;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.service.RegisterService;
import com.acm.acmwebsite.User_Authentication.service.TokenService;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.User_Authentication.service.impl.TokenServiceImpl;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** RegisterController */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final TokenService tokenService;

  @PostMapping("/forgot-password")
  public ResponseEntity<Map<String, String>> forgotPassword(
      @Valid @RequestBody ForgotPasswordDTO request) {

    // This method returns VOID. It handles "User Found" and "User Not Found"
    // identically.
    userService.initiatePasswordReset(request.getEmail());

    // Always return the same success message
    return ResponseEntity.ok(
        Collections.singletonMap(
            "message",
            "If an account with this email exists, a password reset link has been sent."));
  }

  private final RegisterService registerService;

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
  @PostMapping("/refresh")
  @Transactional
    public ResponseEntity<RefreshTokenResponse> refreshAccessToken(@RequestBody @Valid RefreshTokenRequest request) {

          tokenService.validateRefreshToken(request.getRefreshToken());
          User user = tokenService.getUserFromRefreshToken(request.getRefreshToken());
          tokenService.revokeRefreshToken(request.getRefreshToken());
          String newAccessToken = tokenService.createAccessToken(user.getEmail());
          String newRefreshToken = tokenService.createRefreshToken(user);
            RefreshTokenResponse response = new RefreshTokenResponse(newAccessToken, newRefreshToken);
          return ResponseEntity.ok(response);
  }

}
