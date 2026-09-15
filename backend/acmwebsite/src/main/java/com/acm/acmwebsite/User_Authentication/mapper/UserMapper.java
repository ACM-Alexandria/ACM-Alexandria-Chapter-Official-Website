package com.acm.acmwebsite.User_Authentication.mapper;

import com.acm.acmwebsite.User_Authentication.dto.SuccessRegisterResponse;
import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.dto.UserProfileDto;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import com.acm.acmwebsite.feature.repository.CommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.ClubBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  private final HighBoardRepository highBoardRepository;
  private final CommitteeBoardRepository committeeBoardRepository;
  private final ClubBoardRepository clubBoardRepository;

  public UserDTO toDTO(User user) {
    if (user == null) {
      return null;
    }

    String boardRole = null;
    Integer boardOrder = null;
    Long committeeId = user.getCommittee() != null ? user.getCommittee().getId() : null;
    Long clubId = null;
    String associationName = user.getCommittee() != null ? user.getCommittee().getName() : null;

    if (user.getRole() == com.acm.acmwebsite.User_Authentication.enums.Role.ACM_HIGH_BOARD) {
      var hbOpt = highBoardRepository.findByUserId(user.getId());
      if (hbOpt.isPresent()) {
        boardRole = hbOpt.get().getRole();
        boardOrder = hbOpt.get().getOrder();
      }
    } else if (user.getRole() == com.acm.acmwebsite.User_Authentication.enums.Role.ACM_COMMITTEE_BOARD) {
      var cbOpt = committeeBoardRepository.findByUserId(user.getId());
      if (cbOpt.isPresent()) {
        boardRole = cbOpt.get().getRole();
        boardOrder = cbOpt.get().getOrder();
        if (cbOpt.get().getCommittee() != null) {
          committeeId = cbOpt.get().getCommittee().getId();
          associationName = cbOpt.get().getCommittee().getName();
        }
      }
    } else if (user.getRole() == com.acm.acmwebsite.User_Authentication.enums.Role.ACM_CLUB_BOARD) {
      var clbOpt = clubBoardRepository.findByUserId(user.getId());
      if (clbOpt.isPresent()) {
        boardRole = clbOpt.get().getRole();
        boardOrder = clbOpt.get().getOrder();
        if (clbOpt.get().getClub() != null) {
          clubId = clbOpt.get().getClub().getId();
          associationName = clbOpt.get().getClub().getName();
        }
      }
    }

    return UserDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .profileImageUrl(user.getProfileImageUrl())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .role(user.getRole() != null ? user.getRole().name() : null)
        .associationName(associationName)
        .boardRole(boardRole)
        .boardOrder(boardOrder)
        .committeeId(committeeId)
        .clubId(clubId)
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

  public UserProfileDto toProfileDto(User user) {
    if (user == null) {
      return null;
    }

    return UserProfileDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .role(user.getRole() != null ? user.getRole().name() : null)
        .name(user.getName())
        .phoneNumber(user.getPhoneNumber())
        .isAlexEngStudent(user.getIsAlexEngStudent())
        .department(user.getDepartment())
        .batch(user.getBatch())
        .profileImageUrl(user.getProfileImageUrl())
        .linkedinUrl(user.getLinkedinUrl())
        .committeeId(user.getCommittee() != null ? user.getCommittee().getId() : null)

        .build();
  }
}
