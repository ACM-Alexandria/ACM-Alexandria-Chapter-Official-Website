package com.acm.acmwebsite.User_Authentication.repository;

import com.acm.acmwebsite.User_Authentication.entity.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** RefreshTokenRepository */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
