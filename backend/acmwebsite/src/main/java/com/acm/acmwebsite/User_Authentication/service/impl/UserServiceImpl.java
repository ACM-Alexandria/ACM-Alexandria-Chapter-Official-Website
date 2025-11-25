package com.acm.acmwebsite.User_Authentication.service.impl;

import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.exception.DuplicateEmailException;
import com.acm.acmwebsite.User_Authentication.exception.UserNotFoundException;
import com.acm.acmwebsite.User_Authentication.mapper.UserMapper;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.core.service.EmailService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

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
    return userMapper.toDTO(savedUser);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDTO> getUserById(@NonNull Long id) {
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
  @Transactional(readOnly = true)
  public List<UserDTO> getAllUsers() {
    return userRepository.findAll().stream()
        .map(userMapper::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public UserDTO updateUserEmail(@NonNull Long id, String newEmail) {
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
  public UserDTO updateUserPassword(@NonNull Long id, String newPlainPassword) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

    // Validate password is not null or empty
    if (newPlainPassword == null || newPlainPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }

    user.setPasswordHash(passwordEncoder.encode(newPlainPassword));

    User updatedUser = userRepository.save(user);
    return userMapper.toDTO(updatedUser);
  }

  @Override
  @Transactional
  public void deleteUser(@NonNull Long id) {
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

    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        // 1. Find User
        Optional<User> userOptional = userRepository.findByEmail(email);


        // We do NOT throw an exception here.
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();

        // 3. Generate Secure Token (Raw)
        String rawToken = generateSecureToken();

        // 4. Hash the Token (For Storage)
        String hashedToken = hashToken(rawToken);

        // 5. Update User Entity
        user.setResetPasswordToken(hashedToken);
        user.setResetPasswordTokenCreatedAt(LocalDateTime.now());
        // Increment count (handling potential nulls if existing data wasn't migrated perfectly)
        user.setForgotPasswordCount((user.getForgotPasswordCount() == null ? 0 : user.getForgotPasswordCount()) + 1);

        userRepository.save(user);

        // 6. Send Email (Send the RAW token, NOT the hash)
        emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
    }



    // Generates a random 64-character URL-safe string
    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[48]; // 48 bytes * 1.33 base64 expansion ≈ 64 chars
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    // SHA-256 Hashing
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
}