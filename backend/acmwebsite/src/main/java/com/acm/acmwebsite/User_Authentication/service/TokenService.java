package com.acm.acmwebsite.User_Authentication.service;

import com.acm.acmwebsite.User_Authentication.entity.User;

/** TokenService */
public interface TokenService {
  String createRefreshToken(User user);

  void revokeRefreshToken(String token);

  void validateRefreshToken(String refreshTokenString);

  String createAccessToken(String username);

  boolean isValidAccessToken(String accessTokenString);
}
