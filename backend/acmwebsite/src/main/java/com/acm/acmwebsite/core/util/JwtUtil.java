package com.acm.acmwebsite.core.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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
  private long emailConfirmationExpiration;

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

  @Value("${jwt.email-confirmation-encoding-secret:#{null}}")
  private String encodingSecret;

  public String encodeTokenForUrl(String jwt) {
      if (encodingSecret == null) return jwt;
      try {
        SecretKey aesKey = new SecretKeySpec(encodingSecret.getBytes(StandardCharsets.UTF_8), 0, 32, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(jwt.getBytes(StandardCharsets.UTF_8));
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(result);
      } catch (Exception e) {
          throw new RuntimeException("Failed to encode token", e);
      }
  }

  public String decodeTokenFromUrl(String encoded) {
      if (encodingSecret == null) return encoded;
      try {
          byte[] data = Base64.getUrlDecoder().decode(encoded);
          byte[] iv = Arrays.copyOfRange(data, 0, 12);
          byte[] ciphertext = Arrays.copyOfRange(data, 12, data.length);
          SecretKey aesKey = new SecretKeySpec(encodingSecret.getBytes(StandardCharsets.UTF_8), 0, 32, "AES");
          Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
          cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
          return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
      } catch (Exception e) {
          throw new IllegalArgumentException("Invalid or tampered confirmation token");
      }
  }
}
