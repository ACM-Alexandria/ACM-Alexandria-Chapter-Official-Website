package com.acm.acmwebsite.User_Authentication.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LoginResponse {
  private UUID id;
  private String email;
  private String accessToken;
  private String refreshToken;
  private String role;
}
