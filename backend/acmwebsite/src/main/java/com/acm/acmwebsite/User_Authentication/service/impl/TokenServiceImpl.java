package com.acm.acmwebsite.User_Authentication.service.impl;

import com.acm.acmwebsite.User_Authentication.entity.RefreshToken;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.RefreshTokenRepository;
import com.acm.acmwebsite.User_Authentication.service.TokenService;
import com.acm.acmwebsite.core.util.JwtUtil;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** TokenServiceImpl */
@Service
public class TokenServiceImpl implements TokenService {
  @Autowired JwtUtil jwtUtil;
  @Autowired RefreshTokenRepository refreshTokenRepository;

  @Value("${jwt.refresh-expiration}")
  private Long refreshTokenExpiration;

  @Override
  @Transactional
  public String createRefreshToken(User user) {
    Duration expire = Duration.ofMillis(refreshTokenExpiration);
    RefreshToken token =
        RefreshToken.builder()
            .user(user)
            .refreshTokenExpiry(LocalDateTime.now().plus(expire))
            .refreshToken(UUID.randomUUID().toString())
            .build();
    String savedRefreshToken = refreshTokenRepository.save(token).getRefreshToken();
    return savedRefreshToken;
  }

  @Override
  @Transactional
  public void revokeRefreshToken(String tokenString) {
    Optional<RefreshToken> token = refreshTokenRepository.findByRefreshToken(tokenString);
    token.ifPresentOrElse(
        presentRefreshToken -> {
          presentRefreshToken.setSoftDelete(true);
          refreshTokenRepository.save(presentRefreshToken);
        },
        () -> {
          // no token with this tokenId
        });
  }

  @Override
  public boolean isValidRefreshToken(String token) {
    RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(token).get();
    if (refreshToken == null) {
      return false;
    }

    if (refreshToken.getRefreshTokenExpiry().isBefore(LocalDateTime.now())
        || refreshToken.isSoftDelete()) {
      return false;
    }
    return true;
  }

  @Override
  public String createAccessToken(String username) {
    return jwtUtil.generateToken(username);
  }

  @Override
  public boolean isValidAccessToken(String accessTokenString) {
    return jwtUtil.validateAccessToken(accessTokenString);
  }
}
