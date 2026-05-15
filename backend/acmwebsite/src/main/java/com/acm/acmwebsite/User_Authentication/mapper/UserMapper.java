package com.acm.acmwebsite.User_Authentication.mapper;

import com.acm.acmwebsite.User_Authentication.dto.SuccessRegisterResponse;
import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
  public UserDTO toDTO(User user) {
    if (user == null) {
      return null;
    }

    return UserDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }

  public User toEntity(UserDTO userDTO) {
    if (userDTO == null) {
      return null;
    }

    return User.builder()
        .id(userDTO.getId())
        .email(userDTO.getEmail())
        .build();
  }

  public SuccessRegisterResponse userToSuccessRegister(User user) {
    if (user == null) {
      return null;
    }

    return SuccessRegisterResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .build();
  }
}
