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

  LoginResponse loginWithGoogle(String credential) throws Exception;

  void initiatePasswordReset(String email);

  void resetPassword(ResetPasswordDTO dto);

  UserProfileDto getUserProfileById(UUID id);

  UserProfileDto updateUserProfile(UUID id, UserProfileDto profileDto);

  String uploadProfileImage(UUID id, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException;

  org.springframework.data.domain.Page<UserDTO> searchUsers(String query, com.acm.acmwebsite.User_Authentication.enums.Role role, int page, int size);

  UserDTO updateUserRole(UUID userId, com.acm.acmwebsite.User_Authentication.enums.Role role);

  UserDTO assignCommitteeAndClubs(UUID userId, Long committeeId, java.util.List<Long> clubIds);
}
