package com.acm.acmwebsite.User_Authentication.controller;

import com.acm.acmwebsite.User_Authentication.dto.ForgotPasswordDTO;
import com.acm.acmwebsite.User_Authentication.dto.ResetPasswordDTO;

import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.User_Authentication.dto.RegisterDTO;
import com.acm.acmwebsite.User_Authentication.dto.SuccessRegisterResponse;
import com.acm.acmwebsite.User_Authentication.dto.*;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.service.RegisterService;
import com.acm.acmwebsite.User_Authentication.service.TokenService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

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

  @PostMapping("/logout")
  public ResponseEntity<?> logout(
          @AuthenticationPrincipal UserDetails currentUser,
          @RequestBody LogoutRequest logoutRequest) {

    if (currentUser == null) {
      return ResponseEntity.status(401)
              .body(new ErrorMessageResponse("Unauthorized request"));
    }

    String refreshTokenString = logoutRequest.getRefreshToken();
    if (refreshTokenString == null || refreshTokenString.isEmpty()) {
      return ResponseEntity.status(400)
              .body(new ErrorMessageResponse("refresh_token is required"));
    }

    try{
      tokenService.revokeRefreshToken(refreshTokenString);
      return ResponseEntity.ok()
              .body("{\"message\": \"Logged out successfully\"}");
    }catch (Exception ex){
      return ResponseEntity.status(400)
              .body(ex.getMessage());
    }


  }

}
