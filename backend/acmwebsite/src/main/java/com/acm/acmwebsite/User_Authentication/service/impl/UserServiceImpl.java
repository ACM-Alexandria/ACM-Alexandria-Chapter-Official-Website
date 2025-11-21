package com.acm.acmwebsite.User_Authentication.service.impl;

import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.exception.DuplicateEmailException;
import com.acm.acmwebsite.User_Authentication.exception.UserNotFoundException;
import com.acm.acmwebsite.User_Authentication.mapper.UserMapper;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.UserService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserDTO createUser(String email, String plainPassword) {
    // Validate email doesn't already exist
    if (userRepository.existsByEmail(email)) {
      throw new DuplicateEmailException("Email already exists: " + email);
    }

    // Validate password is not null or empty
    if (plainPassword == null || plainPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }

    // Create user with hashed password
    User user = new User();
    user.setEmail(email.trim().toLowerCase());
    user.setPasswordHash(passwordEncoder.encode(plainPassword));

    User savedUser = userRepository.save(user);
    userRepository.flush();

    return userMapper.toDTO(savedUser);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDTO> getUserById(@NonNull UUID id) {
    return userRepository.findById(id)
        .map(userMapper::toDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDTO> getUserByEmail(String email) {
    return userRepository.findByEmail(email.trim().toLowerCase())
        .map(userMapper::toDTO);
  }

  @Override
  @Transactional
  public UserDTO updateUserEmail(@NonNull UUID id, String newEmail) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

    String normalizedEmail = newEmail.trim().toLowerCase();

    // Check if new email is different and doesn't already exist
    if (!user.getEmail().equals(normalizedEmail)) {
      if (userRepository.existsByEmail(normalizedEmail)) {
        throw new DuplicateEmailException("Email already exists: " + normalizedEmail);
      }
      user.setEmail(normalizedEmail);
    }

    User updatedUser = userRepository.save(user);
    return userMapper.toDTO(updatedUser);
  }

  @Override
  @Transactional
  public UserDTO updateUserPassword(@NonNull UUID id, String oldPassword, String newPlainPassword) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

    // Validate old password matches
    if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
      throw new IllegalArgumentException("Old password is incorrect");
    }

    // Validate new password is not null or empty
    if (newPlainPassword == null || newPlainPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("New password cannot be null or empty");
    }

    user.setPasswordHash(passwordEncoder.encode(newPlainPassword));

    User updatedUser = userRepository.save(user);
    return userMapper.toDTO(updatedUser);
  }

  @Override
  @Transactional
  public void deleteUser(@NonNull UUID id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException("User not found with id: " + id);
    }
    userRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean emailExists(String email) {
    return userRepository.existsByEmail(email.trim().toLowerCase());
  }

  @Override
  @Transactional(readOnly = true)
  public boolean verifyPassword(String email, String plainPassword) {
    Optional<User> userOptional = userRepository.findByEmail(email.trim().toLowerCase());

    if (userOptional.isEmpty()) {
      return false;
    }

    return passwordEncoder.matches(plainPassword, userOptional.get().getPasswordHash());
  }
}