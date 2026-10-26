package com.acm.acmwebsite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acm.acmwebsite.User_Authentication.entity.RefreshToken;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.exception.ExpiredRefreshTokenException;
import com.acm.acmwebsite.User_Authentication.exception.InvalidRefreshTokenException;
import com.acm.acmwebsite.User_Authentication.repository.RefreshTokenRepository;
import com.acm.acmwebsite.User_Authentication.service.impl.TokenServiceImpl;
import com.acm.acmwebsite.core.util.JwtUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenServiceImpl Unit Tests")
class TokenServiceImplTest {

  @Mock private JwtUtil jwtUtil;
  @Mock private RefreshTokenRepository refreshTokenRepository;

  @InjectMocks private TokenServiceImpl tokenService;

  private static final long DEFAULT_REFRESH_EXPIRATION_MS = 604_800_000L; // 7 days in ms
  private User testUser;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(tokenService, "refreshTokenExpiration", DEFAULT_REFRESH_EXPIRATION_MS);

    testUser =
        User.builder()
            .id(UUID.randomUUID())
            .email("testuser@acm.org")
            .role(Role.USER)
            .emailConfirmed(true)
            .build();
  }

  private RefreshToken buildRefreshToken(User user, LocalDateTime expiry, boolean softDelete) {
    return RefreshToken.builder()
        .id(UUID.randomUUID())
        .user(user)
        .refreshToken(UUID.randomUUID().toString())
        .refreshTokenExpiry(expiry)
        .softDelete(softDelete)
        .build();
  }

  @Nested
  @DisplayName("createRefreshToken")
  class CreateRefreshTokenTests {

    @Test
    @DisplayName("should create and persist a new refresh token with expected expiry and uuid")
    void shouldCreateAndPersistNewRefreshToken() {
      LocalDateTime beforeCall = LocalDateTime.now();

      when(refreshTokenRepository.save(any(RefreshToken.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      String generatedToken = tokenService.createRefreshToken(testUser);

      ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
      verify(refreshTokenRepository).save(captor.capture());
      RefreshToken savedEntity = captor.getValue();

      LocalDateTime afterCall = LocalDateTime.now();

      assertThat(generatedToken).isNotNull().isEqualTo(savedEntity.getRefreshToken());
      assertThatCode(() -> UUID.fromString(generatedToken)).doesNotThrowAnyException();
      assertThat(savedEntity.getUser()).isEqualTo(testUser);
      assertThat(savedEntity.isSoftDelete()).isFalse();

      // Expiration check: should be ~7 days from invocation time
      LocalDateTime expectedMinExpiry = beforeCall.plusDays(7).minusSeconds(5);
      LocalDateTime expectedMaxExpiry = afterCall.plusDays(7).plusSeconds(5);
      assertThat(savedEntity.getRefreshTokenExpiry())
          .isAfterOrEqualTo(expectedMinExpiry)
          .isBeforeOrEqualTo(expectedMaxExpiry);
    }

    @Test
    @DisplayName("should respect custom configured refresh token expiration duration")
    void shouldRespectCustomExpirationDuration() {
      long customDurationMs = 3_600_000L; // 1 hour
      ReflectionTestUtils.setField(tokenService, "refreshTokenExpiration", customDurationMs);

      LocalDateTime beforeCall = LocalDateTime.now();

      when(refreshTokenRepository.save(any(RefreshToken.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      tokenService.createRefreshToken(testUser);

      ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
      verify(refreshTokenRepository).save(captor.capture());
      RefreshToken savedEntity = captor.getValue();

      LocalDateTime expectedMinExpiry = beforeCall.plusHours(1).minusSeconds(5);
      LocalDateTime expectedMaxExpiry = LocalDateTime.now().plusHours(1).plusSeconds(5);
      assertThat(savedEntity.getRefreshTokenExpiry())
          .isAfterOrEqualTo(expectedMinExpiry)
          .isBeforeOrEqualTo(expectedMaxExpiry);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("should successfully create refresh token for users with different roles")
    void shouldCreateRefreshTokenForAnyRole(Role role) {
      User userWithRole =
          User.builder()
              .id(UUID.randomUUID())
              .email("user-" + role.name() + "@acm.org")
              .role(role)
              .build();

      when(refreshTokenRepository.save(any(RefreshToken.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      String token = tokenService.createRefreshToken(userWithRole);

      assertThat(token).isNotBlank();
      verify(refreshTokenRepository).save(any(RefreshToken.class));
    }
  }

  @Nested
  @DisplayName("revokeRefreshToken")
  class RevokeRefreshTokenTests {

    @Test
    @DisplayName("should set softDelete to true and save when token exists")
    void shouldSoftDeleteAndSaveWhenTokenExists() {
      String tokenString = UUID.randomUUID().toString();
      RefreshToken existingToken =
          buildRefreshToken(testUser, LocalDateTime.now().plusDays(5), false);
      existingToken.setRefreshToken(tokenString);

      when(refreshTokenRepository.findByRefreshToken(tokenString))
          .thenReturn(Optional.of(existingToken));

      tokenService.revokeRefreshToken(tokenString);

      assertThat(existingToken.isSoftDelete()).isTrue();
      verify(refreshTokenRepository).save(existingToken);
    }

    @Test
    @DisplayName("should throw InvalidRefreshTokenException when token does not exist")
    void shouldThrowInvalidRefreshTokenExceptionWhenTokenNotFound() {
      String unknownToken = "unknown-refresh-token";
      when(refreshTokenRepository.findByRefreshToken(unknownToken)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> tokenService.revokeRefreshToken(unknownToken))
          .isInstanceOf(InvalidRefreshTokenException.class)
          .hasMessage("Invalid or expired refresh token");

      verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }
  }

  @Nested
  @DisplayName("validateRefreshToken")
  class ValidateRefreshTokenTests {

    @Test
    @DisplayName("should succeed without exception when token is valid and unexpired")
    void shouldPassValidationWhenTokenIsValid() {
      String tokenString = UUID.randomUUID().toString();
      RefreshToken validToken =
          buildRefreshToken(testUser, LocalDateTime.now().plusDays(3), false);

      when(refreshTokenRepository.findByRefreshToken(tokenString))
          .thenReturn(Optional.of(validToken));

      assertThatCode(() -> tokenService.validateRefreshToken(tokenString))
          .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should throw InvalidRefreshTokenException when token is not found in database")
    void shouldThrowInvalidRefreshTokenExceptionWhenTokenNotFound() {
      String unknownToken = "non-existent-token";
      when(refreshTokenRepository.findByRefreshToken(unknownToken)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> tokenService.validateRefreshToken(unknownToken))
          .isInstanceOf(InvalidRefreshTokenException.class)
          .hasMessage("Invalid or expired refresh token");
    }

    @Test
    @DisplayName("should throw ExpiredRefreshTokenException when token expiry is before now")
    void shouldThrowExpiredRefreshTokenExceptionWhenTokenIsExpired() {
      String tokenString = UUID.randomUUID().toString();
      RefreshToken expiredToken =
          buildRefreshToken(testUser, LocalDateTime.now().minusSeconds(10), false);

      when(refreshTokenRepository.findByRefreshToken(tokenString))
          .thenReturn(Optional.of(expiredToken));

      assertThatThrownBy(() -> tokenService.validateRefreshToken(tokenString))
          .isInstanceOf(ExpiredRefreshTokenException.class)
          .hasMessage("Invalid or expired refresh token");
    }

    @Test
    @DisplayName("should throw ExpiredRefreshTokenException when token is marked as soft deleted")
    void shouldThrowExpiredRefreshTokenExceptionWhenTokenIsSoftDeleted() {
      String tokenString = UUID.randomUUID().toString();
      RefreshToken softDeletedToken =
          buildRefreshToken(testUser, LocalDateTime.now().plusDays(5), true);

      when(refreshTokenRepository.findByRefreshToken(tokenString))
          .thenReturn(Optional.of(softDeletedToken));

      assertThatThrownBy(() -> tokenService.validateRefreshToken(tokenString))
          .isInstanceOf(ExpiredRefreshTokenException.class)
          .hasMessage("Invalid or expired refresh token");
    }

    @Test
    @DisplayName("should throw ExpiredRefreshTokenException when token is both expired and soft deleted")
    void shouldThrowExpiredRefreshTokenExceptionWhenTokenIsExpiredAndSoftDeleted() {
      String tokenString = UUID.randomUUID().toString();
      RefreshToken expiredAndDeletedToken =
          buildRefreshToken(testUser, LocalDateTime.now().minusDays(1), true);

      when(refreshTokenRepository.findByRefreshToken(tokenString))
          .thenReturn(Optional.of(expiredAndDeletedToken));

      assertThatThrownBy(() -> tokenService.validateRefreshToken(tokenString))
          .isInstanceOf(ExpiredRefreshTokenException.class)
          .hasMessage("Invalid or expired refresh token");
    }
  }

  @Nested
  @DisplayName("createAccessToken")
  class CreateAccessTokenTests {

    @Test
    @DisplayName("should generate JWT access token with user email and role string")
    void shouldGenerateAccessTokenWithUserEmailAndRole() {
      String expectedJwt = "mocked.jwt.token";
      when(jwtUtil.generateToken("testuser@acm.org", "USER")).thenReturn(expectedJwt);

      String result = tokenService.createAccessToken(testUser);

      assertThat(result).isEqualTo(expectedJwt);
      verify(jwtUtil).generateToken("testuser@acm.org", "USER");
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("should pass correct role name string to jwtUtil for all role types")
    void shouldPassCorrectRoleNameForAllRoles(Role role) {
      User user =
          User.builder()
              .id(UUID.randomUUID())
              .email("admin@acm.org")
              .role(role)
              .build();

      String expectedJwt = "jwt.for." + role.name();
      when(jwtUtil.generateToken("admin@acm.org", role.name())).thenReturn(expectedJwt);

      String result = tokenService.createAccessToken(user);

      assertThat(result).isEqualTo(expectedJwt);
      verify(jwtUtil).generateToken("admin@acm.org", role.name());
    }
  }

  @Nested
  @DisplayName("isValidAccessToken")
  class IsValidAccessTokenTests {

    @Test
    @DisplayName("should return true when jwtUtil successfully validates access token")
    void shouldReturnTrueWhenJwtIsValid() {
      String validToken = "valid.access.token";
      doNothing().when(jwtUtil).validateAccessToken(validToken);

      boolean isValid = tokenService.isValidAccessToken(validToken);

      assertThat(isValid).isTrue();
      verify(jwtUtil).validateAccessToken(validToken);
    }

    @Test
    @DisplayName("should propagate exception when jwtUtil throws validation exception")
    void shouldPropagateExceptionWhenJwtIsInvalid() {
      String invalidToken = "invalid.access.token";
      doThrow(new RuntimeException("Token expired or signature invalid"))
          .when(jwtUtil)
          .validateAccessToken(invalidToken);

      assertThatThrownBy(() -> tokenService.isValidAccessToken(invalidToken))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Token expired or signature invalid");

      verify(jwtUtil).validateAccessToken(invalidToken);
    }
  }

  @Nested
  @DisplayName("getUserFromRefreshToken")
  class GetUserFromRefreshTokenTests {

    @Test
    @DisplayName("should return associated user when refresh token is valid and unexpired")
    void shouldReturnUserWhenTokenIsValid() {
      String tokenString = UUID.randomUUID().toString();
      RefreshToken validToken =
          buildRefreshToken(testUser, LocalDateTime.now().plusDays(2), false);

      when(refreshTokenRepository.findByRefreshToken(tokenString))
          .thenReturn(Optional.of(validToken));

      User extractedUser = tokenService.getUserFromRefreshToken(tokenString);

      assertThat(extractedUser).isNotNull().isEqualTo(testUser);
      assertThat(extractedUser.getEmail()).isEqualTo("testuser@acm.org");
      assertThat(extractedUser.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("should throw InvalidRefreshTokenException when refresh token is not found")
    void shouldThrowInvalidRefreshTokenExceptionWhenNotFound() {
      String unknownToken = "unknown-token";
      when(refreshTokenRepository.findByRefreshToken(unknownToken)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> tokenService.getUserFromRefreshToken(unknownToken))
          .isInstanceOf(InvalidRefreshTokenException.class)
          .hasMessage("Invalid or expired refresh token");
    }

    @Test
    @DisplayName("should throw ExpiredRefreshTokenException when refresh token is expired")
    void shouldThrowExpiredRefreshTokenExceptionWhenExpired() {
      String tokenString = UUID.randomUUID().toString();
      RefreshToken expiredToken =
          buildRefreshToken(testUser, LocalDateTime.now().minusMinutes(5), false);

      when(refreshTokenRepository.findByRefreshToken(tokenString))
          .thenReturn(Optional.of(expiredToken));

      assertThatThrownBy(() -> tokenService.getUserFromRefreshToken(tokenString))
          .isInstanceOf(ExpiredRefreshTokenException.class)
          .hasMessage("Invalid or expired refresh token");
    }

    @Test
    @DisplayName("should throw ExpiredRefreshTokenException when refresh token is soft deleted")
    void shouldThrowExpiredRefreshTokenExceptionWhenSoftDeleted() {
      String tokenString = UUID.randomUUID().toString();
      RefreshToken softDeletedToken =
          buildRefreshToken(testUser, LocalDateTime.now().plusDays(3), true);

      when(refreshTokenRepository.findByRefreshToken(tokenString))
          .thenReturn(Optional.of(softDeletedToken));

      assertThatThrownBy(() -> tokenService.getUserFromRefreshToken(tokenString))
          .isInstanceOf(ExpiredRefreshTokenException.class)
          .hasMessage("Invalid or expired refresh token");
    }

    @Test
    @DisplayName("should throw ExpiredRefreshTokenException when token is both expired and soft deleted")
    void shouldThrowExpiredRefreshTokenExceptionWhenExpiredAndSoftDeleted() {
      String tokenString = UUID.randomUUID().toString();
      RefreshToken expiredAndDeletedToken =
          buildRefreshToken(testUser, LocalDateTime.now().minusDays(2), true);

      when(refreshTokenRepository.findByRefreshToken(tokenString))
          .thenReturn(Optional.of(expiredAndDeletedToken));

      assertThatThrownBy(() -> tokenService.getUserFromRefreshToken(tokenString))
          .isInstanceOf(ExpiredRefreshTokenException.class)
          .hasMessage("Invalid or expired refresh token");
    }
  }
}
