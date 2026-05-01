package com.acm.acmwebsite.service;

import static org.mockito.Mockito.*;

import com.acm.acmwebsite.User_Authentication.dto.RegisterDTO;
import com.acm.acmwebsite.User_Authentication.dto.SuccessRegisterResponse;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.mapper.UserMapper;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.impl.RegisterServiceImpl;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/** UserServiceTest */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
  @Mock UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock UserMapper userMapper;
  @Mock com.acm.acmwebsite.User_Authentication.service.EmailExitanceService emailExitanceService;
  @InjectMocks RegisterServiceImpl registerService;

  @Test
  public void userService_createUser_shouldReturnUser() {
    // arrange
    RegisterDTO registerDTO = new RegisterDTO("test@email.com", "pass123@", "pass123@");

    User savedUser = new User();
    UUID id = mock(UUID.class);
    SuccessRegisterResponse response = new SuccessRegisterResponse(id, "test@email.com");

    when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);
    when(emailExitanceService.isEmailReal(registerDTO.getEmail())).thenReturn(true);
    when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("hashedPass");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(userMapper.userToSuccessRegister(savedUser)).thenReturn(response);

    // ACT
    SuccessRegisterResponse result = registerService.createUser(registerDTO);

    // ASSERT
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getEmail()).isEqualTo("test@email.com");
  }
}
