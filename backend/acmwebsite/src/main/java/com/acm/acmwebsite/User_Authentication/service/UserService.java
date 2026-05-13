package com.acm.acmwebsite.User_Authentication.service;

import com.acm.acmwebsite.User_Authentication.dto.ResetPasswordDTO;
import com.acm.acmwebsite.User_Authentication.dto.LoginRequest;
import com.acm.acmwebsite.User_Authentication.dto.LoginResponse;
import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.dto.UserProfileDto;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;

public interface UserService {

  Map<String, Object> getUserAuthDetails(String email);

  Optional<UserDTO> getUserById(@NonNull UUID id);

  Optional<UserDTO> getUserByEmail(String email);

  UserDTO updateUserEmail(UUID id, String newEmail);

  UserDTO updateUserPassword(UUID id, String oldPassword, String newPlainPassword);

  void deleteUser(UUID id);

  boolean emailExists(String email);

  boolean verifyPassword(String email, String plainPassword);

  LoginResponse login(LoginRequest loginRequest);

  void initiatePasswordReset(String email);

  void resetPassword(ResetPasswordDTO dto);

  UserProfileDto getUserProfileById(UUID id);

  UserProfileDto updateUserProfile(UUID id, UserProfileDto profileDto);
}
