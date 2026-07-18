package com.acm.acmwebsite.User_Authentication.service.impl;

import com.acm.acmwebsite.User_Authentication.dto.RegisterDTO;
import com.acm.acmwebsite.User_Authentication.dto.SuccessRegisterResponse;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.exception.DuplicateEmailException;
import com.acm.acmwebsite.User_Authentication.exception.InvalidEmailException;
import com.acm.acmwebsite.User_Authentication.exception.PasswordAndConfirmationMisMatch;
import com.acm.acmwebsite.User_Authentication.mapper.UserMapper;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.User_Authentication.service.RegisterService;
import com.acm.acmwebsite.core.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RegisterServiceImpl */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterServiceImpl implements RegisterService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final EmailExitanceService emailExitanceService;
  private final EmailService emailService;

  @Override
  @Transactional
  public SuccessRegisterResponse createUser(RegisterDTO registerDTO) {
    if (!registerDTO.getPassword().equals(registerDTO.getPasswordConfirmation())) {
      throw new PasswordAndConfirmationMisMatch();
    }
    if (!emailExitanceService.isEmailReal(registerDTO.getEmail())) {
      throw new InvalidEmailException("Invalid Email ");
    }

    // Validate email doesn't already exist
    if (userRepository.existsByEmail(registerDTO.getEmail())) {
      throw new DuplicateEmailException("Email already exists: " + registerDTO.getEmail());
    }

    // Create user with hashed password
    User user = new User();
    user.setEmail(registerDTO.getEmail().trim().toLowerCase());
    user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));
    user.setRole(Role.USER);

    User savedUser = userRepository.save(user);

    // Send welcome email
    try {
      emailService.sendWelcomeEmail(user.getEmail(), "ACM Member");
    } catch (Exception e) {
      log.error("Failed to send welcome email to {}", user.getEmail(), e);
    }

    return userMapper.userToSuccessRegister(savedUser);
  }
}
