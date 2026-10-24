package com.acm.acmwebsite.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.acm.acmwebsite.User_Authentication.dto.LoginRequest;
import com.acm.acmwebsite.User_Authentication.dto.LoginResponse;
import com.acm.acmwebsite.User_Authentication.dto.ResetPasswordDTO;
import com.acm.acmwebsite.User_Authentication.dto.UserAssignmentDto;
import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.dto.UserProfileDto;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.exception.DuplicateEmailException;
import com.acm.acmwebsite.User_Authentication.exception.UserNotFoundException;
import com.acm.acmwebsite.User_Authentication.mapper.UserMapper;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.TokenService;
import com.acm.acmwebsite.User_Authentication.service.impl.UserServiceImpl;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.ClubBoard;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.repository.ClubBoardRepository;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import com.acm.acmwebsite.feature.repository.CommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import com.acm.acmwebsite.feature.service.ImageUploadService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private TokenService tokenService;
    @Mock private ImageUploadService imageUploadService;
    @Mock private CommitteeRepository committeeRepository;
    @Mock private ClubRepository clubRepository;
    @Mock private HighBoardRepository highBoardRepository;
    @Mock private CommitteeBoardRepository committeeBoardRepository;
    @Mock private ClubBoardRepository clubBoardRepository;

    @InjectMocks private UserServiceImpl userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private User buildUser(UUID id, String email, Role role) {
        return User.builder()
                .id(id)
                .email(email)
                .role(role)
                .passwordHash("hashedPassword")
                .emailConfirmed(true)
                .forgotPasswordCount(0)
                .build();
    }

    private UserDTO buildUserDTO(UUID id, String email) {
        return UserDTO.builder().id(id).email(email).build();
    }

    @SuppressWarnings("unchecked")
    private void setSecurityAuthority(String... roles) {
        Authentication auth = mock(Authentication.class);
        var authorities = java.util.Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .map(a -> (org.springframework.security.core.GrantedAuthority) a)
                .toList();
        when(auth.getAuthorities()).thenReturn((java.util.Collection) authorities);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @Nested
    @DisplayName("getUserAuthDetails")
    class GetUserAuthDetailsTests {

        @Test
        @DisplayName("should return map with id, email, and role")
        void shouldReturnMapWithIdEmailRole() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

            Map<String, Object> result = userService.getUserAuthDetails("user@test.com");

            assertThat(result).containsEntry("id", id)
                    .containsEntry("email", "user@test.com")
                    .containsEntry("role", "USER");
        }

        @Test
        @DisplayName("should trim and lowercase email before querying")
        void shouldTrimAndLowercaseEmail() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

            userService.getUserAuthDetails("  User@TEST.COM  ");

            verify(userRepository).findByEmail("user@test.com");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundException_whenNotFound() {
            when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserAuthDetails("missing@test.com"))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User session invalid");
        }

        @Test
        @DisplayName("should include committee details when user is ACM_MEMBER")
        void shouldIncludeCommitteeDetails_whenAcmMember() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "member@test.com", Role.ACM_MEMBER);
            UserDTO dto = buildUserDTO(id, "member@test.com");
            dto.setCommitteeId(10L);
            dto.setAssociationName("Web Committee");

            when(userRepository.findByEmail("member@test.com")).thenReturn(Optional.of(user));
            when(userMapper.toDTO(user)).thenReturn(dto);

            Map<String, Object> result = userService.getUserAuthDetails("member@test.com");

            assertThat(result).containsEntry("committee_id", 10L)
                    .containsEntry("committee_name", "Web Committee");
        }

        @Test
        @DisplayName("should include committee details when user is ACM_COMMITTEE_BOARD")
        void shouldIncludeCommitteeDetails_whenAcmCommitteeBoard() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "board@test.com", Role.ACM_COMMITTEE_BOARD);
            UserDTO dto = buildUserDTO(id, "board@test.com");
            dto.setCommitteeId(20L);
            dto.setAssociationName("HR Committee");

            when(userRepository.findByEmail("board@test.com")).thenReturn(Optional.of(user));
            when(userMapper.toDTO(user)).thenReturn(dto);

            Map<String, Object> result = userService.getUserAuthDetails("board@test.com");

            assertThat(result).containsEntry("committee_id", 20L)
                    .containsEntry("committee_name", "HR Committee");
        }

        @Test
        @DisplayName("should have null committee details when user is not committee member (e.g. USER, ADMIN)")
        void shouldHaveNullCommitteeDetails_whenNotCommitteeMember() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            UserDTO dto = buildUserDTO(id, "user@test.com");
            dto.setCommitteeId(10L);
            dto.setAssociationName("Web Committee");

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(userMapper.toDTO(user)).thenReturn(dto);

            Map<String, Object> result = userService.getUserAuthDetails("user@test.com");

            assertThat(result).containsEntry("committee_id", null)
                    .containsEntry("committee_name", null);
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("should return UserDTO when user found")
        void shouldReturnUserDTO_whenFound() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            UserDTO dto = buildUserDTO(id, "user@test.com");
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userMapper.toDTO(user)).thenReturn(dto);

            Optional<UserDTO> result = userService.getUserById(id);

            assertThat(result).isPresent().contains(dto);
        }

        @Test
        @DisplayName("should return empty when user not found")
        void shouldReturnEmpty_whenNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            Optional<UserDTO> result = userService.getUserById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserByEmail")
    class GetUserByEmailTests {

        @Test
        @DisplayName("should return UserDTO when email found")
        void shouldReturnUserDTO_whenFound() {
            User user = buildUser(UUID.randomUUID(), "user@test.com", Role.USER);
            UserDTO dto = buildUserDTO(user.getId(), "user@test.com");
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(userMapper.toDTO(user)).thenReturn(dto);

            Optional<UserDTO> result = userService.getUserByEmail("user@test.com");

            assertThat(result).isPresent().contains(dto);
        }

        @Test
        @DisplayName("should return empty when email not found")
        void shouldReturnEmpty_whenNotFound() {
            when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

            Optional<UserDTO> result = userService.getUserByEmail("missing@test.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should normalize email before querying")
        void shouldNormalizeEmail() {
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

            userService.getUserByEmail("  User@TEST.COM  ");

            verify(userRepository).findByEmail("user@test.com");
        }
    }

    @Nested
    @DisplayName("updateUserEmail")
    class UpdateUserEmailTests {

        @Test
        @DisplayName("should update email and return DTO when new email is different and available")
        void shouldUpdateAndReturnDTO() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "old@test.com", Role.USER);
            UserDTO dto = buildUserDTO(id, "new@test.com");
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(dto);

            UserDTO result = userService.updateUserEmail(id, "new@test.com");

            assertThat(result.getEmail()).isEqualTo("new@test.com");
            assertThat(user.getEmail()).isEqualTo("new@test.com");
        }

        @Test
        @DisplayName("should skip update when same email (after normalization)")
        void shouldSkipUpdate_whenSameEmail() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "same@test.com", Role.USER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(buildUserDTO(id, "same@test.com"));

            userService.updateUserEmail(id, "  Same@TEST.COM  ");

            verify(userRepository, never()).existsByEmail(any());
        }

        @Test
        @DisplayName("should throw DuplicateEmailException when new email already exists")
        void shouldThrowDuplicateEmailException() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "old@test.com", Role.USER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUserEmail(id, "taken@test.com"))
                    .isInstanceOf(DuplicateEmailException.class);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundException() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserEmail(id, "any@test.com"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateUserPassword")
    class UpdateUserPasswordTests {

        @Test
        @DisplayName("should encode new password and save when old password matches")
        void shouldEncodeAndSave() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            UserDTO dto = buildUserDTO(id, "user@test.com");
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPass", "hashedPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPass123")).thenReturn("newHashedPass");
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(dto);

            UserDTO result = userService.updateUserPassword(id, "oldPass", "newPass123");

            assertThat(result).isNotNull();
            assertThat(user.getPasswordHash()).isEqualTo("newHashedPass");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundException() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserPassword(id, "old", "new"))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when old password is incorrect")
        void shouldThrow_whenOldPasswordIncorrect() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongOld", "hashedPassword")).thenReturn(false);

            assertThatThrownBy(() -> userService.updateUserPassword(id, "wrongOld", "newPass"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Old password is incorrect");
        }

        @Test
        @DisplayName("should throw when new password is null")
        void shouldThrow_whenNewPasswordNull() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPass", "hashedPassword")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUserPassword(id, "oldPass", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("New password cannot be null or empty");
        }

        @Test
        @DisplayName("should throw when new password is blank")
        void shouldThrow_whenNewPasswordBlank() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPass", "hashedPassword")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUserPassword(id, "oldPass", "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("New password cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUserTests {

        @Test
        @DisplayName("should delete user when exists")
        void shouldDeleteById() {
            UUID id = UUID.randomUUID();
            when(userRepository.existsById(id)).thenReturn(true);

            userService.deleteUser(id);

            verify(userRepository).deleteById(id);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        void shouldThrowUserNotFoundException() {
            UUID id = UUID.randomUUID();
            when(userRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("emailExists")
    class EmailExistsTests {

        @Test
        @DisplayName("should return true when email exists")
        void shouldReturnTrue_whenExists() {
            when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

            assertThat(userService.emailExists("user@test.com")).isTrue();
        }

        @Test
        @DisplayName("should return false when email does not exist")
        void shouldReturnFalse_whenNotExists() {
            when(userRepository.existsByEmail("missing@test.com")).thenReturn(false);

            assertThat(userService.emailExists("missing@test.com")).isFalse();
        }

        @Test
        @DisplayName("should normalize email before checking")
        void shouldNormalizeEmail() {
            when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

            userService.emailExists("  User@TEST.COM  ");

            verify(userRepository).existsByEmail("user@test.com");
        }
    }

    @Nested
    @DisplayName("verifyPassword")
    class VerifyPasswordTests {

        @Test
        @DisplayName("should return true when password matches")
        void shouldReturnTrue_whenMatches() {
            User user = buildUser(UUID.randomUUID(), "user@test.com", Role.USER);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("plainPass", "hashedPassword")).thenReturn(true);

            assertThat(userService.verifyPassword("user@test.com", "plainPass")).isTrue();
        }

        @Test
        @DisplayName("should return false when password does not match")
        void shouldReturnFalse_whenNoMatch() {
            User user = buildUser(UUID.randomUUID(), "user@test.com", Role.USER);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPass", "hashedPassword")).thenReturn(false);

            assertThat(userService.verifyPassword("user@test.com", "wrongPass")).isFalse();
        }

        @Test
        @DisplayName("should return false when user not found")
        void shouldReturnFalse_whenUserNotFound() {
            when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

            assertThat(userService.verifyPassword("missing@test.com", "anyPass")).isFalse();
        }
    }

    @Nested
    @DisplayName("initiatePasswordReset")
    class InitiatePasswordResetTests {

        @Test
        @DisplayName("should generate token, hash it, save user, and send email with raw token")
        void shouldGenerateTokenAndSendEmail() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            user.setName("John");
            user.setForgotPasswordCount(0);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

            userService.initiatePasswordReset("user@test.com");

            // Token was saved (hashed)
            assertThat(user.getResetPasswordToken()).isNotNull().isNotEmpty();
            assertThat(user.getResetPasswordTokenCreatedAt()).isNotNull();
            verify(userRepository).save(user);

            // Email was sent with a raw token (different from the stored hashed token)
            ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailService).sendPasswordResetEmail(eq("user@test.com"), tokenCaptor.capture(), eq("John"));
            String rawToken = tokenCaptor.getValue();
            assertThat(rawToken).isNotNull().isNotEmpty();
            // Raw token should differ from the stored hashed token
            assertThat(rawToken).isNotEqualTo(user.getResetPasswordToken());
        }

        @Test
        @DisplayName("should increment forgot password count")
        void shouldIncrementForgotPasswordCount() {
            User user = buildUser(UUID.randomUUID(), "user@test.com", Role.USER);
            user.setForgotPasswordCount(2);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

            userService.initiatePasswordReset("user@test.com");

            assertThat(user.getForgotPasswordCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("should handle null forgot password count (sets to 1)")
        void shouldHandleNullCount() {
            User user = buildUser(UUID.randomUUID(), "user@test.com", Role.USER);
            user.setForgotPasswordCount(null);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

            userService.initiatePasswordReset("user@test.com");

            assertThat(user.getForgotPasswordCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should do nothing when user not found (no exception)")
        void shouldDoNothing_whenUserNotFound() {
            when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

            userService.initiatePasswordReset("missing@test.com");

            verify(userRepository, never()).save(any());
            verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {

        private LoginRequest buildLoginRequest(String email, String password) {
            LoginRequest req = new LoginRequest();
            req.setEmail(email);
            req.setPassword(password);
            return req;
        }

        @Test
        @DisplayName("should return LoginResponse when credentials are valid and email confirmed")
        void shouldReturnLoginResponse_whenValid() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("pass123", "hashedPassword")).thenReturn(true);
            when(tokenService.createRefreshToken(user)).thenReturn("refreshTok");
            when(tokenService.createAccessToken(user)).thenReturn("accessTok");

            LoginResponse result = userService.login(buildLoginRequest("user@test.com", "pass123"));

            assertThat(result.getEmail()).isEqualTo("user@test.com");
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getRole()).isEqualTo("USER");
            assertThat(result.getAccessToken()).isEqualTo("accessTok");
            assertThat(result.getRefreshToken()).isEqualTo("refreshTok");
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrow_whenUserNotFound() {
            when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(buildLoginRequest("missing@test.com", "pass")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Incorrect email or password");
        }

        @Test
        @DisplayName("should throw when password incorrect")
        void shouldThrow_whenPasswordIncorrect() {
            User user = buildUser(UUID.randomUUID(), "user@test.com", Role.USER);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPass", "hashedPassword")).thenReturn(false);

            assertThatThrownBy(() -> userService.login(buildLoginRequest("user@test.com", "wrongPass")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Incorrect email or password");
        }

        @Test
        @DisplayName("should throw when email not confirmed (false)")
        void shouldThrow_whenEmailNotConfirmed() {
            User user = buildUser(UUID.randomUUID(), "user@test.com", Role.USER);
            user.setEmailConfirmed(false);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("pass", "hashedPassword")).thenReturn(true);

            assertThatThrownBy(() -> userService.login(buildLoginRequest("user@test.com", "pass")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Email not confirmed");
        }

        @Test
        @DisplayName("should throw when emailConfirmed is null")
        void shouldThrow_whenEmailConfirmedNull() {
            User user = buildUser(UUID.randomUUID(), "user@test.com", Role.USER);
            user.setEmailConfirmed(null);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("pass", "hashedPassword")).thenReturn(true);

            assertThatThrownBy(() -> userService.login(buildLoginRequest("user@test.com", "pass")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("should normalize email from request")
        void shouldNormalizeEmail() {
            User user = buildUser(UUID.randomUUID(), "user@test.com", Role.USER);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("pass", "hashedPassword")).thenReturn(true);
            when(tokenService.createRefreshToken(user)).thenReturn("r");
            when(tokenService.createAccessToken(user)).thenReturn("a");

            userService.login(buildLoginRequest("  User@TEST.COM  ", "pass"));

            verify(userRepository).findByEmail("user@test.com");
        }

        @Test
        @DisplayName("should populate committee details in LoginResponse when user is committee member")
        void shouldIncludeCommitteeDetailsInLogin_whenCommitteeMember() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "member@test.com", Role.ACM_MEMBER);
            UserDTO dto = buildUserDTO(id, "member@test.com");
            dto.setCommitteeId(15L);
            dto.setAssociationName("PR Committee");

            when(userRepository.findByEmail("member@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("pass", "hashedPassword")).thenReturn(true);
            when(tokenService.createRefreshToken(user)).thenReturn("refreshTok");
            when(tokenService.createAccessToken(user)).thenReturn("accessTok");
            when(userMapper.toDTO(user)).thenReturn(dto);

            LoginResponse result = userService.login(buildLoginRequest("member@test.com", "pass"));

            assertThat(result.getCommitteeId()).isEqualTo(15L);
            assertThat(result.getCommitteeName()).isEqualTo("PR Committee");
        }

        @Test
        @DisplayName("should have null committee details in LoginResponse when user is not committee member")
        void shouldHaveNullCommitteeDetailsInLogin_whenNotCommitteeMember() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            UserDTO dto = buildUserDTO(id, "user@test.com");
            dto.setCommitteeId(15L);
            dto.setAssociationName("PR Committee");

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("pass", "hashedPassword")).thenReturn(true);
            when(tokenService.createRefreshToken(user)).thenReturn("refreshTok");
            when(tokenService.createAccessToken(user)).thenReturn("accessTok");
            when(userMapper.toDTO(user)).thenReturn(dto);

            LoginResponse result = userService.login(buildLoginRequest("user@test.com", "pass"));

            assertThat(result.getCommitteeId()).isNull();
            assertThat(result.getCommitteeName()).isNull();
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPasswordTests {

        private ResetPasswordDTO buildResetDto(String token, String newPass, String confirmPass) {
            ResetPasswordDTO dto = new ResetPasswordDTO();
            dto.setToken(token);
            dto.setNew_password(newPass);
            dto.setNew_password_confirm(confirmPass);
            return dto;
        }

        @Test
        @DisplayName("should update password when token is valid and within 1 hour")
        void shouldUpdatePassword_whenTokenValid() {
            User user = buildUser(UUID.randomUUID(), "user@test.com", Role.USER);
            user.setResetPasswordTokenCreatedAt(LocalDateTime.now().minusMinutes(30));
            // We don't know the exact hashed token, so match any string
            when(userRepository.findByResetPasswordToken(any())).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("newPass123")).thenReturn("newHashedPass");

            userService.resetPassword(buildResetDto("someRawToken", "newPass123", "newPass123"));

            assertThat(user.getPasswordHash()).isEqualTo("newHashedPass");
            assertThat(user.getResetPasswordToken()).isNull();
            assertThat(user.getResetPasswordTokenCreatedAt()).isNull();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw when passwords do not match")
        void shouldThrow_whenPasswordsMismatch() {
            assertThatThrownBy(() ->
                    userService.resetPassword(buildResetDto("tok", "pass1234", "different")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Passwords do not match.");
        }

        @Test
        @DisplayName("should throw when password is shorter than 8 characters")
        void shouldThrow_whenPasswordTooShort() {
            assertThatThrownBy(() ->
                    userService.resetPassword(buildResetDto("tok", "short", "short")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password must be at least 8 characters.");
        }

        @Test
        @DisplayName("should throw when token is invalid (no user found by hashed token)")
        void shouldThrow_whenTokenInvalid() {
            when(userRepository.findByResetPasswordToken(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userService.resetPassword(buildResetDto("badToken", "newPass123", "newPass123")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid or expired token.");
        }

        @Test
        @DisplayName("should throw when token is expired (created more than 1 hour ago)")
        void shouldThrow_whenTokenExpired() {
            User user = buildUser(UUID.randomUUID(), "user@test.com", Role.USER);
            user.setResetPasswordTokenCreatedAt(LocalDateTime.now().minusHours(2));
            when(userRepository.findByResetPasswordToken(any())).thenReturn(Optional.of(user));

            assertThatThrownBy(() ->
                    userService.resetPassword(buildResetDto("tok", "newPass123", "newPass123")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid or expired token.");
        }

        @Test
        @DisplayName("should throw when resetPasswordTokenCreatedAt is null")
        void shouldThrow_whenTokenCreatedAtNull() {
            User user = buildUser(UUID.randomUUID(), "user@test.com", Role.USER);
            user.setResetPasswordTokenCreatedAt(null);
            when(userRepository.findByResetPasswordToken(any())).thenReturn(Optional.of(user));

            assertThatThrownBy(() ->
                    userService.resetPassword(buildResetDto("tok", "newPass123", "newPass123")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid or expired token.");
        }
    }

    @Nested
    @DisplayName("getUserProfileById")
    class GetUserProfileByIdTests {

        @Test
        @DisplayName("should return profile DTO when user found")
        void shouldReturnProfileDto() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            UserProfileDto profileDto = UserProfileDto.builder().id(id).email("user@test.com").build();
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userMapper.toProfileDto(user)).thenReturn(profileDto);

            UserProfileDto result = userService.getUserProfileById(id);

            assertThat(result.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when not found")
        void shouldThrowUserNotFoundException() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserProfileById(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateUserProfile")
    class UpdateUserProfileTests {

        @Test
        @DisplayName("should update all fields for student including department and batch")
        void shouldUpdateAllFields_forStudent() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            UserProfileDto profileDto = UserProfileDto.builder()
                    .name("John Doe")
                    .phoneNumber("123456")
                    .isAlexEngStudent(true)
                    .department(Department.CSED)
                    .batch("2025")
                    .linkedinUrl("https://linkedin.com/in/johndoe")
                    .build();
            UserProfileDto resultDto = UserProfileDto.builder().id(id).name("John Doe").build();
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toProfileDto(user)).thenReturn(resultDto);

            userService.updateUserProfile(id, profileDto);

            assertThat(user.getName()).isEqualTo("John Doe");
            assertThat(user.getPhoneNumber()).isEqualTo("123456");
            assertThat(user.getIsAlexEngStudent()).isTrue();
            assertThat(user.getDepartment()).isEqualTo(Department.CSED);
            assertThat(user.getBatch()).isEqualTo("2025");
            assertThat(user.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/johndoe");
        }

        @Test
        @DisplayName("should clear department and batch for non-student")
        void shouldClearDeptAndBatch_forNonStudent() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            user.setDepartment(Department.CCE);
            user.setBatch("2024");
            UserProfileDto profileDto = UserProfileDto.builder()
                    .name("Jane")
                    .phoneNumber("789")
                    .isAlexEngStudent(false)
                    .department(Department.CSED) // should be ignored
                    .batch("2025")              // should be ignored
                    .build();
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toProfileDto(user)).thenReturn(UserProfileDto.builder().build());

            userService.updateUserProfile(id, profileDto);

            assertThat(user.getDepartment()).isNull();
            assertThat(user.getBatch()).isNull();
        }

        @Test
        @DisplayName("should treat null isAlexEngStudent as false")
        void shouldHandleNullIsAlexEngStudent() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            UserProfileDto profileDto = UserProfileDto.builder()
                    .name("Test")
                    .phoneNumber("111")
                    .isAlexEngStudent(null)
                    .build();
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toProfileDto(user)).thenReturn(UserProfileDto.builder().build());

            userService.updateUserProfile(id, profileDto);

            assertThat(user.getIsAlexEngStudent()).isFalse();
            assertThat(user.getDepartment()).isNull();
            assertThat(user.getBatch()).isNull();
        }

        @Test
        @DisplayName("should update linkedinUrl when provided")
        void shouldUpdateLinkedinUrl_whenProvided() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            user.setLinkedinUrl("old-url");
            UserProfileDto profileDto = UserProfileDto.builder()
                    .name("Test")
                    .phoneNumber("111")
                    .isAlexEngStudent(false)
                    .linkedinUrl("new-url")
                    .build();
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toProfileDto(user)).thenReturn(UserProfileDto.builder().build());

            userService.updateUserProfile(id, profileDto);

            assertThat(user.getLinkedinUrl()).isEqualTo("new-url");
        }

        @Test
        @DisplayName("should not overwrite linkedinUrl when null in DTO")
        void shouldNotOverwriteLinkedinUrl_whenNull() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            user.setLinkedinUrl("existing-url");
            UserProfileDto profileDto = UserProfileDto.builder()
                    .name("Test")
                    .phoneNumber("111")
                    .isAlexEngStudent(false)
                    .linkedinUrl(null)
                    .build();
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toProfileDto(user)).thenReturn(UserProfileDto.builder().build());

            userService.updateUserProfile(id, profileDto);

            assertThat(user.getLinkedinUrl()).isEqualTo("existing-url");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when not found")
        void shouldThrowUserNotFoundException() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserProfile(id, UserProfileDto.builder().build()))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("uploadProfileImage")
    class UploadProfileImageTests {

        @Test
        @DisplayName("should upload image, save URL to user, and return URL")
        void shouldUploadAndReturnUrl() throws IOException {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            MultipartFile file = mock(MultipartFile.class);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(imageUploadService.uploadImage(file)).thenReturn("https://cdn.example.com/img.jpg");

            String result = userService.uploadProfileImage(id, file);

            assertThat(result).isEqualTo("https://cdn.example.com/img.jpg");
            assertThat(user.getProfileImageUrl()).isEqualTo("https://cdn.example.com/img.jpg");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundException() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.uploadProfileImage(id, mock(MultipartFile.class)))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("should propagate IOException from image upload service")
        void shouldPropagateIOException() throws IOException {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            MultipartFile file = mock(MultipartFile.class);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(imageUploadService.uploadImage(file)).thenThrow(new IOException("upload failed"));

            assertThatThrownBy(() -> userService.uploadProfileImage(id, file))
                    .isInstanceOf(IOException.class)
                    .hasMessage("upload failed");
        }
    }

    @Nested
    @DisplayName("searchUsers")
    class SearchUsersTests {

        @Test
        @DisplayName("should delegate to repository and map results via mapper")
        void shouldDelegateToRepositoryAndMapResults() {
            User user = buildUser(UUID.randomUUID(), "user@test.com", Role.USER);
            UserDTO dto = buildUserDTO(user.getId(), "user@test.com");
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(Collections.singletonList(user), pageable, 1);
            when(userRepository.searchUsers("query", Role.USER, 1L, 2L, pageable)).thenReturn(userPage);
            when(userMapper.toDTO(user)).thenReturn(dto);

            Page<UserDTO> result = userService.searchUsers("query", Role.USER, 1L, 2L, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("user@test.com");
        }
    }

    @Nested
    @DisplayName("updateUserRole")
    class UpdateUserRoleTests {

        @Test
        @DisplayName("should set role and save")
        void shouldSetRoleAndSave() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.USER);
            UserDTO dto = buildUserDTO(id, "user@test.com");
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(dto);

            userService.updateUserRole(id, Role.ACM_MEMBER);

            assertThat(user.getRole()).isEqualTo(Role.ACM_MEMBER);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when not found")
        void shouldThrowUserNotFoundException() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserRole(id, Role.USER))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("assignCommitteeAndClubs")
    class AssignCommitteeAndClubsTests {

        @Test
        @DisplayName("should assign committee when committeeId is not null")
        void shouldAssignCommittee() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.ACM_MEMBER);
            Committee committee = new Committee();
            committee.setId(1L);
            UserDTO dto = buildUserDTO(id, "user@test.com");
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(dto);

            userService.assignCommitteeAndClubs(id, 1L, null);

            assertThat(user.getCommittee()).isEqualTo(committee);
        }

        @Test
        @DisplayName("should clear committee when committeeId is null")
        void shouldClearCommittee_whenIdNull() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.ACM_MEMBER);
            user.setCommittee(new Committee());
            UserDTO dto = buildUserDTO(id, "user@test.com");
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(dto);

            userService.assignCommitteeAndClubs(id, null, null);

            assertThat(user.getCommittee()).isNull();
        }

        @Test
        @DisplayName("should throw when committee not found")
        void shouldThrow_whenCommitteeNotFound() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "user@test.com", Role.ACM_MEMBER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(committeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.assignCommitteeAndClubs(id, 99L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Committee not found");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundException() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.assignCommitteeAndClubs(id, 1L, null))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("assignUser")
    class AssignUserTests {


        @Test
        @DisplayName("super admin can assign any role without restriction")
        void superAdmin_canAssignAnything() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.USER);
            UserDTO dto = buildUserDTO(userId, "user@test.com");
            setSecurityAuthority("ROLE_SUPER_ADMIN");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.SUPER_ADMIN)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(highBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(committeeBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(clubBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(dto);

            assertThatCode(() -> userService.assignUser(userId, assignDto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("high board cannot modify a super admin user")
        void highBoard_cannotModifySuperAdmin() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.SUPER_ADMIN);
            setSecurityAuthority("ROLE_ACM_HIGH_BOARD");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.ACM_MEMBER)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.assignUser(userId, assignDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("High Board members cannot modify or assign Super Admin role");
        }

        @Test
        @DisplayName("high board cannot promote anyone to super admin")
        void highBoard_cannotPromoteToSuperAdmin() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.USER);
            setSecurityAuthority("ROLE_ACM_HIGH_BOARD");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.SUPER_ADMIN)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.assignUser(userId, assignDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("High Board members cannot modify or assign Super Admin role");
        }

        @Test
        @DisplayName("committee board cannot modify a high board user")
        void committeeBoard_cannotModifyHighBoard() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.ACM_HIGH_BOARD);
            setSecurityAuthority("ROLE_ACM_COMMITTEE_BOARD");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.ACM_MEMBER)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.assignUser(userId, assignDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("You do not have permission to modify High Board or Super Admin roles");
        }

        @Test
        @DisplayName("committee board cannot promote to high board")
        void committeeBoard_cannotPromoteToHighBoard() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.USER);
            setSecurityAuthority("ROLE_ACM_COMMITTEE_BOARD");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.ACM_HIGH_BOARD)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.assignUser(userId, assignDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("You do not have permission to modify High Board or Super Admin roles");
        }

        @Test
        @DisplayName("should create HighBoard when role is ACM_HIGH_BOARD")
        void shouldCreateHighBoard_whenRoleIsHighBoard() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.USER);
            setSecurityAuthority("ROLE_SUPER_ADMIN");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.ACM_HIGH_BOARD)
                    .boardRole("President")
                    .boardOrder(1)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(highBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(committeeBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(clubBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(buildUserDTO(userId, "user@test.com"));

            userService.assignUser(userId, assignDto);

            ArgumentCaptor<HighBoard> captor = ArgumentCaptor.forClass(HighBoard.class);
            verify(highBoardRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo("President");
            assertThat(captor.getValue().getOrder()).isEqualTo(1);
            assertThat(captor.getValue().getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("should delete HighBoard when role changes away from ACM_HIGH_BOARD")
        void shouldDeleteHighBoard_whenRoleChangesAway() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.ACM_HIGH_BOARD);
            HighBoard existingHb = new HighBoard();
            setSecurityAuthority("ROLE_SUPER_ADMIN");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.ACM_MEMBER)
                    .committeeId(1L)
                    .build();

            Committee committee = new Committee();
            committee.setId(1L);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(highBoardRepository.findByUserId(userId)).thenReturn(Optional.of(existingHb));
            when(committeeBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(clubBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(buildUserDTO(userId, "user@test.com"));

            userService.assignUser(userId, assignDto);

            verify(highBoardRepository).delete(existingHb);
        }

        @Test
        @DisplayName("should create CommitteeBoard when role is ACM_COMMITTEE_BOARD")
        void shouldCreateCommitteeBoard_whenRoleIsCommitteeBoard() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.USER);
            Committee committee = new Committee();
            committee.setId(5L);
            setSecurityAuthority("ROLE_SUPER_ADMIN");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.ACM_COMMITTEE_BOARD)
                    .committeeId(5L)
                    .boardRole("Head")
                    .boardOrder(2)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(highBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(committeeBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(clubBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(committeeRepository.findById(5L)).thenReturn(Optional.of(committee));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(buildUserDTO(userId, "user@test.com"));

            userService.assignUser(userId, assignDto);

            ArgumentCaptor<CommitteeBoard> captor = ArgumentCaptor.forClass(CommitteeBoard.class);
            verify(committeeBoardRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo("Head");
            assertThat(captor.getValue().getOrder()).isEqualTo(2);
            assertThat(captor.getValue().getCommittee()).isEqualTo(committee);
        }

        @Test
        @DisplayName("should delete CommitteeBoard when role changes away")
        void shouldDeleteCommitteeBoard_whenRoleChangesAway() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.ACM_COMMITTEE_BOARD);
            CommitteeBoard existingCb = new CommitteeBoard();
            setSecurityAuthority("ROLE_SUPER_ADMIN");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.USER)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(highBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(committeeBoardRepository.findByUserId(userId)).thenReturn(Optional.of(existingCb));
            when(clubBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(buildUserDTO(userId, "user@test.com"));

            userService.assignUser(userId, assignDto);

            verify(committeeBoardRepository).delete(existingCb);
        }

        @Test
        @DisplayName("should create ClubBoard when role is ACM_CLUB_BOARD")
        void shouldCreateClubBoard_whenRoleIsClubBoard() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.USER);
            Club club = new Club();
            club.setId(3L);
            setSecurityAuthority("ROLE_SUPER_ADMIN");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.ACM_CLUB_BOARD)
                    .clubId(3L)
                    .boardRole("Lead")
                    .boardOrder(1)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(highBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(committeeBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(clubBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(clubRepository.findById(3L)).thenReturn(Optional.of(club));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(buildUserDTO(userId, "user@test.com"));

            userService.assignUser(userId, assignDto);

            ArgumentCaptor<ClubBoard> captor = ArgumentCaptor.forClass(ClubBoard.class);
            verify(clubBoardRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo("Lead");
            assertThat(captor.getValue().getOrder()).isEqualTo(1);
            assertThat(captor.getValue().getClub()).isEqualTo(club);
        }

        @Test
        @DisplayName("should delete ClubBoard when role changes away")
        void shouldDeleteClubBoard_whenRoleChangesAway() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.ACM_CLUB_BOARD);
            ClubBoard existingClb = new ClubBoard();
            setSecurityAuthority("ROLE_SUPER_ADMIN");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.USER)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(highBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(committeeBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(clubBoardRepository.findByUserId(userId)).thenReturn(Optional.of(existingClb));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(buildUserDTO(userId, "user@test.com"));

            userService.assignUser(userId, assignDto);

            verify(clubBoardRepository).delete(existingClb);
        }

        @Test
        @DisplayName("should set committee for ACM_MEMBER role")
        void shouldSetCommittee_forAcmMember() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.USER);
            Committee committee = new Committee();
            committee.setId(2L);
            setSecurityAuthority("ROLE_SUPER_ADMIN");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.ACM_MEMBER)
                    .committeeId(2L)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(committeeRepository.findById(2L)).thenReturn(Optional.of(committee));
            when(highBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(committeeBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(clubBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(buildUserDTO(userId, "user@test.com"));

            userService.assignUser(userId, assignDto);

            assertThat(user.getCommittee()).isEqualTo(committee);
        }

        @Test
        @DisplayName("should clear committee for non-member role (e.g. USER)")
        void shouldClearCommittee_forNonMemberRole() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.ACM_MEMBER);
            user.setCommittee(new Committee());
            setSecurityAuthority("ROLE_SUPER_ADMIN");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.USER)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(highBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(committeeBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(clubBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(buildUserDTO(userId, "user@test.com"));

            userService.assignUser(userId, assignDto);

            assertThat(user.getCommittee()).isNull();
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundException() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.assignUser(userId, UserAssignmentDto.builder().build()))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when committee not found for ACM_MEMBER")
        void shouldThrowIllegalArgumentException_whenCommitteeNotFound_forMemberRole() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.USER);
            setSecurityAuthority("ROLE_SUPER_ADMIN");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.ACM_MEMBER)
                    .committeeId(99L)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(committeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.assignUser(userId, assignDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Committee not found");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when committee not found for ACM_COMMITTEE_BOARD")
        void shouldThrowIllegalArgumentException_whenCommitteeNotFound_forCommitteeBoard() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.USER);
            setSecurityAuthority("ROLE_SUPER_ADMIN");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.ACM_COMMITTEE_BOARD)
                    .committeeId(99L)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(committeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.assignUser(userId, assignDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Committee not found");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when club not found for ACM_CLUB_BOARD")
        void shouldThrowIllegalArgumentException_whenClubNotFound_forClubBoard() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, "user@test.com", Role.USER);
            setSecurityAuthority("ROLE_SUPER_ADMIN");

            UserAssignmentDto assignDto = UserAssignmentDto.builder()
                    .targetRole(Role.ACM_CLUB_BOARD)
                    .clubId(99L)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(highBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(committeeBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(clubBoardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(clubRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.assignUser(userId, assignDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Club not found");
        }
    }

    @Nested
    @DisplayName("nullValidation")
    class NullValidationTests {

        @Test
        @DisplayName("getUserById should throw NullPointerException when id is null")
        void getUserById_shouldThrowNullPointerException_whenIdIsNull() {
            assertThatThrownBy(() -> userService.getUserById(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("updateUserEmail should throw NullPointerException when id is null")
        void updateUserEmail_shouldThrowNullPointerException_whenIdIsNull() {
            assertThatThrownBy(() -> userService.updateUserEmail(null, "test@test.com"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("updateUserPassword should throw NullPointerException when id is null")
        void updateUserPassword_shouldThrowNullPointerException_whenIdIsNull() {
            assertThatThrownBy(() -> userService.updateUserPassword(null, "old", "new"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deleteUser should throw NullPointerException when id is null")
        void deleteUser_shouldThrowNullPointerException_whenIdIsNull() {
            assertThatThrownBy(() -> userService.deleteUser(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("loginWithGoogle")
    class LoginWithGoogleTests {

        @Test
        @DisplayName("should throw IllegalArgumentException when token is invalid")
        void shouldThrowIllegalArgumentException_whenTokenInvalid() {
            assertThatThrownBy(() -> userService.loginWithGoogle("invalid.token.here"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void downgradesBoardUsersClearsAssociationsAndLeavesSuperAdminsUntouched() {
        var committee = new Committee();
        var member = new User();
        member.setRole(Role.ACM_CLUB_BOARD);
        member.setCommittee(committee);
        var regular = new User();
        regular.setRole(Role.USER);
        regular.setCommittee(committee);
        var admin = new User();
        admin.setRole(Role.SUPER_ADMIN);
        admin.setCommittee(committee);
        when(userRepository.findCurrentBoardMembers()).thenReturn(List.of(member, regular, admin));

        assertThat(userService.demoteBoardMembers()).isEqualTo(1);
        assertThat(member.getRole()).isEqualTo(Role.USER);
        assertThat(member.getCommittee()).isNull();
        assertThat(regular.getRole()).isEqualTo(Role.USER);
        assertThat(regular.getCommittee()).isNull();
        assertThat(admin.getRole()).isEqualTo(Role.SUPER_ADMIN);
        assertThat(admin.getCommittee()).isSameAs(committee);
    }

    @Test
    void emptyBoardsChangeNoUsers() {
        assertThat(userService.demoteBoardMembers()).isZero();
    }
}
