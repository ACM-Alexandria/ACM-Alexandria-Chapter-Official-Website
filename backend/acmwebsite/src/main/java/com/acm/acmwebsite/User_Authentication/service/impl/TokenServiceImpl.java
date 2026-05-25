package com.acm.acmwebsite.User_Authentication.service.impl;

import com.acm.acmwebsite.User_Authentication.entity.RefreshToken;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.exception.ExpiredRefreshTokenException;
import com.acm.acmwebsite.User_Authentication.exception.InvalidRefreshTokenException;
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
          throw new InvalidRefreshTokenException("Invalid or expired refresh token");
        });
  }

  @Override
  public void validateRefreshToken(String token) {
    Optional<RefreshToken> refreshToken = refreshTokenRepository.findByRefreshToken(token);

    if (refreshToken.isEmpty()) {
      // create special excpetion type and throw it then handle it in the global handler
      throw new InvalidRefreshTokenException("Invalid or expired refresh token");
    }

    if (refreshToken.get().getRefreshTokenExpiry().isBefore(LocalDateTime.now())
        || refreshToken.get().isSoftDelete()) {
      // create special excpetion type and throw it
        throw new ExpiredRefreshTokenException("Invalid or expired refresh token");
    }
  }

  @Override
  public String createAccessToken(User user) {
    return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
  }

  @Override
  public boolean isValidAccessToken(String accessTokenString) {
    jwtUtil.validateAccessToken(accessTokenString);
    return true;
  }

  @Override
  public User getUserFromRefreshToken(String refreshToken) {
    Optional<RefreshToken> token = refreshTokenRepository.findByRefreshToken(refreshToken);

    if (token.isEmpty()) {
      throw new InvalidRefreshTokenException("Invalid or expired refresh token");
    }

    if (token.get().getRefreshTokenExpiry().isBefore(LocalDateTime.now())
        || token.get().isSoftDelete()) {
      throw new ExpiredRefreshTokenException("Invalid or expired refresh token");
    }

    return token.get().getUser();

  }

}
