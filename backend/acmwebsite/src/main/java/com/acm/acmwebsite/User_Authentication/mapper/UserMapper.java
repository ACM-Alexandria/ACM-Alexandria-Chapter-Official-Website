package com.acm.acmwebsite.User_Authentication.mapper;

import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(target = "id", source = "id")
  @Mapping(target = "email", source = "email")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "updatedAt", source = "updatedAt")
  UserDTO toDTO(User user);

  @Mapping(target = "passwordHash", ignore = true)
  User toEntity(UserDTO userDTO);
}
