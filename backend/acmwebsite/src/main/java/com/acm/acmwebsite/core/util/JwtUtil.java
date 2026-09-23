package com.acm.acmwebsite.core.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** JwtUtil */
@Component
public class JwtUtil {
  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration}")
  private long expiration;

  @Value("${jwt.email-confirmation-expiration}")
  private int emailConfirmationExpiration;

  private SecretKey key;

  @PostConstruct
  public void generateSecretKey() {
    this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(String email, String role) {

    return Jwts.builder()
        .subject(email)
        .claim("role", role)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(key)
        .compact();
  }

  public String getRole(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("role", String.class);
  }

  public String getEmail(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  public void validateAccessToken(String token) {
    Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token);
  }

  public String generateEmailConfirmationToken(String email) {
    return Jwts.builder()
        .subject(email)
        .claim("purpose", "email_confirmation")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + emailConfirmationExpiration))
        .signWith(key)
        .compact();
  }

  public String validateEmailConfirmationToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    String purpose = claims.get("purpose", String.class);
    if(!("email_confirmation").equals(purpose)) {
        throw new RuntimeException("Invalid email confirmation token");
    }

    return claims.getSubject();
  }
}
