package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.dto.RegisterDTO;
import com.acm.acmwebsite.User_Authentication.dto.SuccessRegisterResponse;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.exception.DuplicateEmailException;
import com.acm.acmwebsite.User_Authentication.exception.InvalidEmailException;
import com.acm.acmwebsite.User_Authentication.exception.PasswordAndConfirmationMisMatch;
import com.acm.acmwebsite.User_Authentication.mapper.UserMapper;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailConfirmationService;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.User_Authentication.service.impl.RegisterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegisterServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailExitanceService emailExitanceService;

    @Mock
    private EmailConfirmationService emailConfirmationService;

    @InjectMocks
    private RegisterServiceImpl registerService;

    private RegisterDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = RegisterDTO.builder()
                .email("test@acm.org")
                .password("Password123")
                .passwordConfirmation("Password123")
                .build();
    }

    @Nested
    @DisplayName("Password mismatch validation")
    class PasswordMismatchTests {

        @Test
        @DisplayName("Should throw PasswordAndConfirmationMisMatch when passwords do not match")
        void shouldThrowWhenPasswordAndConfirmationMismatch() {
            validDto.setPasswordConfirmation("DifferentPassword123");

            assertThatThrownBy(() -> registerService.createUser(validDto))
                    .isInstanceOf(PasswordAndConfirmationMisMatch.class);

            verifyNoInteractions(emailExitanceService, userRepository, passwordEncoder, emailConfirmationService, userMapper);
        }
    }

    @Nested
    @DisplayName("Email validity & existence validation")
    class EmailValidationTests {

        @Test
        @DisplayName("Should throw InvalidEmailException when email existence service returns false")
        void shouldThrowWhenEmailIsNotReal() {
            when(emailExitanceService.isEmailReal(validDto.getEmail())).thenReturn(false);

            assertThatThrownBy(() -> registerService.createUser(validDto))
                    .isInstanceOf(InvalidEmailException.class)
                    .hasMessageContaining("Invalid Email");

            verify(emailExitanceService).isEmailReal(validDto.getEmail());
            verifyNoInteractions(userRepository, passwordEncoder, emailConfirmationService, userMapper);
        }

        @Test
        @DisplayName("Should throw DuplicateEmailException when email already exists in repository")
        void shouldThrowWhenEmailAlreadyExists() {
            when(emailExitanceService.isEmailReal(validDto.getEmail())).thenReturn(true);
            when(userRepository.existsByEmail(validDto.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> registerService.createUser(validDto))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("Email already exists");

            verify(userRepository).existsByEmail(validDto.getEmail());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Successful Registration Flow")
    class SuccessfulRegistrationTests {

        @Test
        @DisplayName("Should successfully create and save user, send confirmation email, and return response")
        void shouldSuccessfullyCreateUser() {
            validDto.setEmail("   User@Example.COM   ");
            when(emailExitanceService.isEmailReal(validDto.getEmail())).thenReturn(true);
            when(userRepository.existsByEmail(validDto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode("Password123")).thenReturn("encodedPasswordHash");

            UUID userId = UUID.randomUUID();
            User savedUser = new User();
            savedUser.setId(userId);
            savedUser.setEmail("user@example.com");
            savedUser.setPasswordHash("encodedPasswordHash");
            savedUser.setRole(Role.USER);

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            SuccessRegisterResponse expectedResponse = SuccessRegisterResponse.builder()
                    .id(userId)
                    .email("user@example.com")
                    .build();
            when(userMapper.userToSuccessRegister(savedUser)).thenReturn(expectedResponse);

            SuccessRegisterResponse result = registerService.createUser(validDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.getEmail()).isEqualTo("user@example.com");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getEmail()).isEqualTo("user@example.com");
            assertThat(capturedUser.getPasswordHash()).isEqualTo("encodedPasswordHash");
            assertThat(capturedUser.getRole()).isEqualTo(Role.USER);

            verify(emailConfirmationService).sendConfirmationEmail("user@example.com");
            verify(userMapper).userToSuccessRegister(savedUser);
        }

        @Test
        @DisplayName("Should still succeed when emailConfirmationService throws exception")
        void shouldSucceedEvenIfConfirmationEmailFails() {
            when(emailExitanceService.isEmailReal(validDto.getEmail())).thenReturn(true);
            when(userRepository.existsByEmail(validDto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(validDto.getPassword())).thenReturn("encodedPasswordHash");

            UUID userId = UUID.randomUUID();
            User savedUser = new User();
            savedUser.setId(userId);
            savedUser.setEmail("test@acm.org");

            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            doThrow(new RuntimeException("Mail server down"))
                    .when(emailConfirmationService).sendConfirmationEmail("test@acm.org");

            SuccessRegisterResponse expectedResponse = SuccessRegisterResponse.builder()
                    .id(userId)
                    .email("test@acm.org")
                    .build();
            when(userMapper.userToSuccessRegister(savedUser)).thenReturn(expectedResponse);

            SuccessRegisterResponse result = registerService.createUser(validDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId);
            verify(userRepository).save(any(User.class));
            verify(emailConfirmationService).sendConfirmationEmail("test@acm.org");
            verify(userMapper).userToSuccessRegister(savedUser);
        }
    }
}
