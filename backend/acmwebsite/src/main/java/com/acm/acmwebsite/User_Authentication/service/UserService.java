package com.acm.acmwebsite.User_Authentication.service;

import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;

public interface UserService {
  UserDTO createUser(String email, String plainPassword);

  Optional<UserDTO> getUserById(@NonNull UUID id);

  Optional<UserDTO> getUserByEmail(String email);

  UserDTO updateUserEmail(UUID id, String newEmail);

  UserDTO updateUserPassword(UUID id, String oldPassword, String newPlainPassword);

  void deleteUser(UUID id);

  boolean emailExists(String email);

  boolean verifyPassword(String email, String plainPassword);
}
