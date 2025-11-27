package com.acm.acmwebsite.User_Authentication.service;

import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

public interface UserService {
  UserDTO createUser(String email, String plainPassword);

  Optional<UserDTO> getUserById(@NonNull Long id);

  Optional<UserDTO> getUserByEmail(String email);

  List<UserDTO> getAllUsers();

  UserDTO updateUserEmail(Long id, String newEmail);

  UserDTO updateUserPassword(Long id, String newPlainPassword);

  void deleteUser(Long id);

  boolean emailExists(String email);

  boolean verifyPassword(String email, String plainPassword);
  void initiatePasswordReset(String email);
}
