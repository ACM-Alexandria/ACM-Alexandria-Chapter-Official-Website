package com.acm.acmwebsite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.User_Authentication.service.impl.AuthorizationServiceImpl;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationServiceImpl Unit Tests")
class AuthorizationServiceImplTest {

  @Mock private UserService userService;

  @InjectMocks private AuthorizationServiceImpl authorizationService;

  private Authentication createMockAuthentication(String... roles) {
    Authentication authentication = mock(Authentication.class);
    List<GrantedAuthority> authorities =
        Arrays.stream(roles).map(SimpleGrantedAuthority::new).map(a -> (GrantedAuthority) a).toList();
    doReturn(authorities).when(authentication).getAuthorities();
    return authentication;
  }

  @Nested
  @DisplayName("isAuthenticated")
  class IsAuthenticatedTests {

    @Test
    @DisplayName("should return false when authentication object is null")
    void shouldReturnFalseWhenAuthenticationIsNull() {
      boolean result = authorizationService.isAuthenticated(null);

      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("should return false when authentication is not authenticated")
    void shouldReturnFalseWhenAuthenticationIsNotAuthenticated() {
      Authentication authentication = mock(Authentication.class);
      when(authentication.isAuthenticated()).thenReturn(false);

      boolean result = authorizationService.isAuthenticated(authentication);

      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("should return true when authentication is authenticated")
    void shouldReturnTrueWhenAuthenticationIsAuthenticated() {
      Authentication authentication = mock(Authentication.class);
      when(authentication.isAuthenticated()).thenReturn(true);

      boolean result = authorizationService.isAuthenticated(authentication);

      assertThat(result).isTrue();
    }
  }

  @Nested
  @DisplayName("isAdmin")
  class IsAdminTests {

    @Test
    @DisplayName("should return false when authentication object is null")
    void shouldReturnFalseWhenAuthenticationIsNull() {
      boolean result = authorizationService.isAdmin(null);

      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("should return false when authorities list is empty")
    void shouldReturnFalseWhenAuthoritiesListIsEmpty() {
      Authentication authentication = mock(Authentication.class);
      when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

      boolean result = authorizationService.isAdmin(authentication);

      assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "ROLE_USER",
          "ROLE_ADMIN",
          "ROLE_ACM_MEMBER",
          "ROLE_ACM_CLUB_BOARD",
          "ROLE_ACM_COMMITTEE_BOARD",
          "ROLE_ACM_HIGH_BOARD"
        })
    @DisplayName("should return false when authorities do not include ROLE_SUPER_ADMIN")
    void shouldReturnFalseWhenAuthoritiesDoNotIncludeRoleSuperAdmin(String role) {
      Authentication authentication = createMockAuthentication(role);

      boolean result = authorizationService.isAdmin(authentication);

      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("should return true when authorities include ROLE_SUPER_ADMIN")
    void shouldReturnTrueWhenAuthoritiesIncludeRoleSuperAdmin() {
      Authentication authentication = createMockAuthentication("ROLE_USER", "ROLE_SUPER_ADMIN");

      boolean result = authorizationService.isAdmin(authentication);

      assertThat(result).isTrue();
    }
  }

  @Nested
  @DisplayName("isSelf")
  class IsSelfTests {

    @Test
    @DisplayName("should return true when authenticated user ID matches the target ID")
    void shouldReturnTrueWhenAuthenticatedUserMatchesTargetId() {
      UUID targetId = UUID.randomUUID();
      String email = "target@acm.org";

      Authentication authentication = mock(Authentication.class);
      when(authentication.getName()).thenReturn(email);

      UserDTO userDTO = UserDTO.builder().id(targetId).email(email).build();
      when(userService.getUserByEmail(email)).thenReturn(Optional.of(userDTO));

      boolean result = authorizationService.isSelf(targetId, authentication);

      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("should return false when authenticated user ID differs from target ID")
    void shouldReturnFalseWhenAuthenticatedUserHasDifferentId() {
      UUID targetId = UUID.randomUUID();
      UUID actualUserId = UUID.randomUUID();
      String email = "other@acm.org";

      Authentication authentication = mock(Authentication.class);
      when(authentication.getName()).thenReturn(email);

      UserDTO userDTO = UserDTO.builder().id(actualUserId).email(email).build();
      when(userService.getUserByEmail(email)).thenReturn(Optional.of(userDTO));

      boolean result = authorizationService.isSelf(targetId, authentication);

      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("should return false when user is not found by authenticated email")
    void shouldReturnFalseWhenUserNotFoundByEmail() {
      UUID targetId = UUID.randomUUID();
      String email = "ghost@acm.org";

      Authentication authentication = mock(Authentication.class);
      when(authentication.getName()).thenReturn(email);

      when(userService.getUserByEmail(email)).thenReturn(Optional.empty());

      boolean result = authorizationService.isSelf(targetId, authentication);

      assertThat(result).isFalse();
    }
  }
}
