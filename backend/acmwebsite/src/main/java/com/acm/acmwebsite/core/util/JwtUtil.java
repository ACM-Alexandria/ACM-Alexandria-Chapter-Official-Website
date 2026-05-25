package com.acm.acmwebsite.core.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
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
  private int expiration;

  private SecretKey key;

  @PostConstruct
  public void generateSecretKey() {
    this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }

  public String generateToken(String email, String role) {

    return Jwts.builder()
        .setSubject(email)
        .claim("role", role)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String getRole(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .get("role", String.class);
  }

  public String getEmail(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public void validateAccessToken(String token) {
    Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
  }
}
