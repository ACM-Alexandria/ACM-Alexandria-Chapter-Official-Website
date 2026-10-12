package com.acm.acmwebsite.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

  private JwtUtil jwtUtil;

  private static final String TEST_JWT_SECRET =
      "test-secret-key-that-is-long-enough-for-hs256-algorithm-padding";
  private static final String TEST_ENCODING_SECRET = "12345678901234567890123456789012"; // 32 bytes for AES-256
  private static final long ACCESS_TOKEN_EXPIRATION_MS = 3_600_000L; // 1 hour
  private static final long EMAIL_CONFIRMATION_EXPIRATION_MS = 900_000L; // 15 mins

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(jwtUtil, "jwtSecret", TEST_JWT_SECRET);
    ReflectionTestUtils.setField(jwtUtil, "expiration", ACCESS_TOKEN_EXPIRATION_MS);
    ReflectionTestUtils.setField(jwtUtil, "emailConfirmationExpiration", EMAIL_CONFIRMATION_EXPIRATION_MS);
    ReflectionTestUtils.setField(jwtUtil, "encodingSecret", TEST_ENCODING_SECRET);
    jwtUtil.generateSecretKey();
  }

  @Nested
  @DisplayName("generateToken")
  class GenerateTokenTests {

    @Test
    @DisplayName("should generate compact signed JWT containing email and role")
    void shouldGenerateCompactSignedJwt() {
      String email = "member@acm.org";
      String role = "ACM_MEMBER";

      String token = jwtUtil.generateToken(email, role);

      assertThat(token).isNotBlank();
      assertThat(token.split("\\.")).hasSize(3);
      assertThat(jwtUtil.getEmail(token)).isEqualTo(email);
      assertThat(jwtUtil.getRole(token)).isEqualTo(role);
    }

    @Test
    @DisplayName("should set expiration according to configured expiration duration")
    void shouldSetExpirationAccordingToConfiguredDuration() {
      long beforeMs = System.currentTimeMillis();
      String token = jwtUtil.generateToken("test@acm.org", "USER");
      long afterMs = System.currentTimeMillis();

      Date expirationDate =
          Jwts.parser()
              .verifyWith(Keys.hmacShaKeyFor(TEST_JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
              .build()
              .parseSignedClaims(token)
              .getPayload()
              .getExpiration();

      long expectedMinExpiry = beforeMs + ACCESS_TOKEN_EXPIRATION_MS - 2000;
      long expectedMaxExpiry = afterMs + ACCESS_TOKEN_EXPIRATION_MS + 2000;

      assertThat(expirationDate.getTime())
          .isGreaterThanOrEqualTo(expectedMinExpiry)
          .isLessThanOrEqualTo(expectedMaxExpiry);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "ADMIN", "SUPER_ADMIN", "ACM_BOARD", "GUEST"})
    @DisplayName("should embed correct role claim for multiple role values")
    void shouldEmbedCorrectRoleClaim(String role) {
      String token = jwtUtil.generateToken("user@acm.org", role);

      assertThat(jwtUtil.getRole(token)).isEqualTo(role);
    }
  }

  @Nested
  @DisplayName("getEmail and getRole")
  class GetEmailAndGetRoleTests {

    @Test
    @DisplayName("should correctly extract subject email from valid token")
    void shouldExtractSubjectEmail() {
      String token = jwtUtil.generateToken("president@acm.org", "ACM_HIGH_BOARD");

      String email = jwtUtil.getEmail(token);

      assertThat(email).isEqualTo("president@acm.org");
    }

    @Test
    @DisplayName("should correctly extract role claim from valid token")
    void shouldExtractRoleClaim() {
      String token = jwtUtil.generateToken("admin@acm.org", "SUPER_ADMIN");

      String role = jwtUtil.getRole(token);

      assertThat(role).isEqualTo("SUPER_ADMIN");
    }

    @Test
    @DisplayName("should throw MalformedJwtException when parsing invalid token string")
    void shouldThrowWhenParsingInvalidTokenString() {
      String corruptToken = "not.a.valid.jwt.token";

      assertThatThrownBy(() -> jwtUtil.getEmail(corruptToken))
          .isInstanceOf(MalformedJwtException.class);

      assertThatThrownBy(() -> jwtUtil.getRole(corruptToken))
          .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    @DisplayName("should throw SignatureException when token is signed with different key")
    void shouldThrowWhenTokenSignedWithDifferentKey() {
      SecretKey differentKey =
          Keys.hmacShaKeyFor(
              "different-secret-key-that-is-at-least-256-bits-long-for-testing"
                  .getBytes(StandardCharsets.UTF_8));
      String forgedToken =
          Jwts.builder()
              .subject("hacker@acm.org")
              .claim("role", "SUPER_ADMIN")
              .signWith(differentKey)
              .compact();

      assertThatThrownBy(() -> jwtUtil.getEmail(forgedToken))
          .isInstanceOf(SignatureException.class);

      assertThatThrownBy(() -> jwtUtil.getRole(forgedToken))
          .isInstanceOf(SignatureException.class);
    }
  }

  @Nested
  @DisplayName("validateAccessToken")
  class ValidateAccessTokenTests {

    @Test
    @DisplayName("should succeed without exception when token is valid and unexpired")
    void shouldSucceedWhenTokenIsValid() {
      String token = jwtUtil.generateToken("valid@acm.org", "USER");

      assertThatCode(() -> jwtUtil.validateAccessToken(token)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should throw ExpiredJwtException when access token has expired")
    void shouldThrowExpiredJwtExceptionWhenTokenExpired() {
      JwtUtil expiredJwtUtil = new JwtUtil();
      ReflectionTestUtils.setField(expiredJwtUtil, "jwtSecret", TEST_JWT_SECRET);
      ReflectionTestUtils.setField(expiredJwtUtil, "expiration", -5000L); // expired 5 seconds ago
      expiredJwtUtil.generateSecretKey();

      String expiredToken = expiredJwtUtil.generateToken("expired@acm.org", "USER");

      assertThatThrownBy(() -> jwtUtil.validateAccessToken(expiredToken))
          .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("should throw SignatureException when token signature does not match secret key")
    void shouldThrowSignatureExceptionWhenSignatureInvalid() {
      SecretKey unauthorizedKey =
          Keys.hmacShaKeyFor(
              "completely-unauthorized-key-with-sufficient-entropy-for-hs256"
                  .getBytes(StandardCharsets.UTF_8));
      String forgedToken =
          Jwts.builder()
              .subject("forged@acm.org")
              .claim("role", "USER")
              .signWith(unauthorizedKey)
              .compact();

      assertThatThrownBy(() -> jwtUtil.validateAccessToken(forgedToken))
          .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("should throw MalformedJwtException when token is tampered or corrupt")
    void shouldThrowMalformedJwtExceptionWhenTokenIsTampered() {
      String validToken = jwtUtil.generateToken("user@acm.org", "USER");
      String[] parts = validToken.split("\\.");
      String tamperedToken = parts[0] + "." + parts[1] + "corrupted." + parts[2];

      assertThatThrownBy(() -> jwtUtil.validateAccessToken(tamperedToken))
          .isInstanceOf(JwtException.class);
    }
  }

  @Nested
  @DisplayName("generateEmailConfirmationToken")
  class GenerateEmailConfirmationTokenTests {

    @Test
    @DisplayName("should generate email confirmation token with purpose claim and subject")
    void shouldGenerateEmailConfirmationToken() {
      String email = "newuser@acm.org";

      String token = jwtUtil.generateEmailConfirmationToken(email);

      assertThat(token).isNotBlank();
      String validatedEmail = jwtUtil.validateEmailConfirmationToken(token);
      assertThat(validatedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("should set expiration according to email confirmation duration")
    void shouldSetExpirationAccordingToEmailConfirmationDuration() {
      long beforeMs = System.currentTimeMillis();
      String token = jwtUtil.generateEmailConfirmationToken("verify@acm.org");
      long afterMs = System.currentTimeMillis();

      Date expirationDate =
          Jwts.parser()
              .verifyWith(Keys.hmacShaKeyFor(TEST_JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
              .build()
              .parseSignedClaims(token)
              .getPayload()
              .getExpiration();

      long expectedMinExpiry = beforeMs + EMAIL_CONFIRMATION_EXPIRATION_MS - 2000;
      long expectedMaxExpiry = afterMs + EMAIL_CONFIRMATION_EXPIRATION_MS + 2000;

      assertThat(expirationDate.getTime())
          .isGreaterThanOrEqualTo(expectedMinExpiry)
          .isLessThanOrEqualTo(expectedMaxExpiry);
    }
  }

  @Nested
  @DisplayName("validateEmailConfirmationToken")
  class ValidateEmailConfirmationTokenTests {

    @Test
    @DisplayName("should return subject email when token has valid email_confirmation purpose")
    void shouldReturnEmailWhenPurposeIsValid() {
      String token = jwtUtil.generateEmailConfirmationToken("alice@acm.org");

      String email = jwtUtil.validateEmailConfirmationToken(token);

      assertThat(email).isEqualTo("alice@acm.org");
    }

    @Test
    @DisplayName("should throw RuntimeException when purpose claim is missing")
    void shouldThrowRuntimeExceptionWhenPurposeClaimIsMissing() {
      // Normal access token generated without purpose claim
      String accessToken = jwtUtil.generateToken("user@acm.org", "USER");

      assertThatThrownBy(() -> jwtUtil.validateEmailConfirmationToken(accessToken))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Invalid email confirmation token");
    }

    @Test
    @DisplayName("should throw RuntimeException when purpose claim is different")
    void shouldThrowRuntimeExceptionWhenPurposeClaimIsDifferent() {
      SecretKey key = Keys.hmacShaKeyFor(TEST_JWT_SECRET.getBytes(StandardCharsets.UTF_8));
      String wrongPurposeToken =
          Jwts.builder()
              .subject("user@acm.org")
              .claim("purpose", "password_reset")
              .signWith(key)
              .compact();

      assertThatThrownBy(() -> jwtUtil.validateEmailConfirmationToken(wrongPurposeToken))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Invalid email confirmation token");
    }

    @Test
    @DisplayName("should throw ExpiredJwtException when email confirmation token has expired")
    void shouldThrowExpiredJwtExceptionWhenExpired() {
      JwtUtil expiredJwtUtil = new JwtUtil();
      ReflectionTestUtils.setField(expiredJwtUtil, "jwtSecret", TEST_JWT_SECRET);
      ReflectionTestUtils.setField(expiredJwtUtil, "emailConfirmationExpiration", -1000L);
      expiredJwtUtil.generateSecretKey();

      String expiredToken = expiredJwtUtil.generateEmailConfirmationToken("expired@acm.org");

      assertThatThrownBy(() -> jwtUtil.validateEmailConfirmationToken(expiredToken))
          .isInstanceOf(ExpiredJwtException.class);
    }
  }

  @Nested
  @DisplayName("encodeTokenForUrl and decodeTokenFromUrl")
  class UrlEncodingAndDecodingTests {

    @Test
    @DisplayName("should return raw JWT when encodingSecret is null")
    void shouldReturnRawJwtWhenEncodingSecretIsNull() {
      ReflectionTestUtils.setField(jwtUtil, "encodingSecret", null);
      String rawJwt = "raw.jwt.token";

      String encoded = jwtUtil.encodeTokenForUrl(rawJwt);
      String decoded = jwtUtil.decodeTokenFromUrl(encoded);

      assertThat(encoded).isEqualTo(rawJwt);
      assertThat(decoded).isEqualTo(rawJwt);
    }

    @Test
    @DisplayName("should round-trip encrypt and decrypt JWT successfully with AES-GCM")
    void shouldRoundTripEncryptAndDecrypt() {
      String originalJwt = jwtUtil.generateEmailConfirmationToken("secure@acm.org");

      String encryptedUrlToken = jwtUtil.encodeTokenForUrl(originalJwt);
      String decryptedJwt = jwtUtil.decodeTokenFromUrl(encryptedUrlToken);

      assertThat(encryptedUrlToken).isNotBlank().isNotEqualTo(originalJwt);
      // Ensure URL-safe base64: no '+', '/', or '=' padding
      assertThat(encryptedUrlToken).doesNotContain("+", "/", "=");
      assertThat(decryptedJwt).isEqualTo(originalJwt);
    }

    @Test
    @DisplayName("should produce distinct ciphertexts for same token due to random IV")
    void shouldProduceDistinctCiphertextsDueToRandomIv() {
      String originalJwt = jwtUtil.generateEmailConfirmationToken("random-iv@acm.org");

      String ciphertext1 = jwtUtil.encodeTokenForUrl(originalJwt);
      String ciphertext2 = jwtUtil.encodeTokenForUrl(originalJwt);

      assertThat(ciphertext1).isNotEqualTo(ciphertext2);
      assertThat(jwtUtil.decodeTokenFromUrl(ciphertext1)).isEqualTo(originalJwt);
      assertThat(jwtUtil.decodeTokenFromUrl(ciphertext2)).isEqualTo(originalJwt);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when decoding corrupted ciphertext")
    void shouldThrowWhenDecodingCorruptedCiphertext() {
      String originalJwt = jwtUtil.generateEmailConfirmationToken("tamper@acm.org");
      String encrypted = jwtUtil.encodeTokenForUrl(originalJwt);

      // Tamper with the base64 ciphertext
      byte[] decodedBytes = Base64.getUrlDecoder().decode(encrypted);
      decodedBytes[decodedBytes.length - 1] ^= (byte) 0xFF; // flip bits of authentication tag
      String tamperedBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(decodedBytes);

      assertThatThrownBy(() -> jwtUtil.decodeTokenFromUrl(tamperedBase64))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Invalid or tampered confirmation token");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when decoding invalid base64 string")
    void shouldThrowWhenDecodingInvalidBase64() {
      String invalidBase64 = "!!!not-valid-base64???";

      assertThatThrownBy(() -> jwtUtil.decodeTokenFromUrl(invalidBase64))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Invalid or tampered confirmation token");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when ciphertext data is shorter than IV length")
    void shouldThrowWhenDataIsShorterThanIv() {
      // 5 bytes encoded to Base64 (less than 12 bytes IV needed)
      String shortData = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[]{1, 2, 3, 4, 5});

      assertThatThrownBy(() -> jwtUtil.decodeTokenFromUrl(shortData))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Invalid or tampered confirmation token");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when decoding with different secret key")
    void shouldThrowWhenDecodingWithDifferentSecretKey() {
      String originalJwt = jwtUtil.generateEmailConfirmationToken("mismatch@acm.org");
      String encrypted = jwtUtil.encodeTokenForUrl(originalJwt);

      JwtUtil differentKeyUtil = new JwtUtil();
      ReflectionTestUtils.setField(
          differentKeyUtil, "encodingSecret", "99999999999999999999999999999999");

      assertThatThrownBy(() -> differentKeyUtil.decodeTokenFromUrl(encrypted))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Invalid or tampered confirmation token");
    }

    @Test
    @DisplayName("should wrap and throw RuntimeException when encoding fails")
    void shouldThrowRuntimeExceptionWhenEncodingFails() {
      JwtUtil invalidSecretUtil = new JwtUtil();
      // Secret key shorter than 32 bytes causes SecretKeySpec creation with length 32 to fail with IndexOutOfBoundsException
      ReflectionTestUtils.setField(invalidSecretUtil, "encodingSecret", "too-short");

      assertThatThrownBy(() -> invalidSecretUtil.encodeTokenForUrl("some-jwt"))
          .isInstanceOf(RuntimeException.class)
          .hasMessageStartingWith("Failed to encode token");
    }
  }
}
