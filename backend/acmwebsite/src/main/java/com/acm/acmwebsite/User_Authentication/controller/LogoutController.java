package com.acm.acmwebsite.User_Authentication.controller;

import com.acm.acmwebsite.User_Authentication.dto.ErrorMessageResponse;
import com.acm.acmwebsite.User_Authentication.entity.RefreshToken;
import com.acm.acmwebsite.User_Authentication.repository.RefreshTokenRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class LogoutController {

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

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

    RefreshToken token = refreshTokenRepository.findByRefreshToken(refreshTokenString).orElse(null);

    if (token != null) {
      token.setSoftDelete(true);
      refreshTokenRepository.save(token);
    }

    return ResponseEntity.ok()
        .body("{\"message\": \"Logged out successfully\"}");
  }

  @Data
  public static class LogoutRequest {
    private String refreshToken;
  }
}
