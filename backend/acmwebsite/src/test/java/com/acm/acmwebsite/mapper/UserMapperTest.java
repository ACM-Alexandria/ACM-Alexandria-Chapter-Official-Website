package com.acm.acmwebsite.mapper;

import com.acm.acmwebsite.User_Authentication.dto.SuccessRegisterResponse;
import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.dto.UserProfileDto;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.mapper.UserMapper;
import com.acm.acmwebsite.feature.entity.*;
import com.acm.acmwebsite.feature.repository.ClubBoardRepository;
import com.acm.acmwebsite.feature.repository.CommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {

    @Mock
    private HighBoardRepository highBoardRepository;

    @Mock
    private CommitteeBoardRepository committeeBoardRepository;

    @Mock
    private ClubBoardRepository clubBoardRepository;

    @InjectMocks
    private UserMapper userMapper;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("alex@acm.org")
                .name("Alex")
                .role(Role.USER)
                .phoneNumber("+20123456789")
                .isAlexEngStudent(true)
                .department(Department.CCE)
                .batch("2027")
                .profileImageUrl("https://image.url/pic.jpg")
                .linkedinUrl("https://linkedin.com/in/alex")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("toDTO")
    class ToDtoTests {

        @Test
        @DisplayName("Should return null when user is null")
        void shouldReturnNullWhenUserIsNull() {
            assertThat(userMapper.toDTO(null)).isNull();
        }

        @Test
        @DisplayName("Should map basic User to UserDTO")
        void shouldMapBasicUser() {
            UserDTO dto = userMapper.toDTO(user);

            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(userId);
            assertThat(dto.getEmail()).isEqualTo("alex@acm.org");
            assertThat(dto.getName()).isEqualTo("Alex");
            assertThat(dto.getRole()).isEqualTo(Role.USER.name());
            assertThat(dto.getBoardRole()).isNull();
            assertThat(dto.getBoardOrder()).isNull();
        }

        @Test
        @DisplayName("Should map High Board user with board role and order")
        void shouldMapHighBoardUser() {
            user.setRole(Role.ACM_HIGH_BOARD);
            HighBoard hb = new HighBoard();
            hb.setRole("President");
            hb.setOrder(1);

            when(highBoardRepository.findByUserId(userId)).thenReturn(Optional.of(hb));

            UserDTO dto = userMapper.toDTO(user);

            assertThat(dto.getBoardRole()).isEqualTo("President");
            assertThat(dto.getBoardOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should map Committee Board user with committee association")
        void shouldMapCommitteeBoardUser() {
            user.setRole(Role.ACM_COMMITTEE_BOARD);
            Committee committee = new Committee();
            committee.setId(10L);
            committee.setName("Technical Committee");

            CommitteeBoard cb = new CommitteeBoard();
            cb.setRole("Head");
            cb.setOrder(2);
            cb.setCommittee(committee);

            when(committeeBoardRepository.findByUserId(userId)).thenReturn(Optional.of(cb));

            UserDTO dto = userMapper.toDTO(user);

            assertThat(dto.getBoardRole()).isEqualTo("Head");
            assertThat(dto.getBoardOrder()).isEqualTo(2);
            assertThat(dto.getCommitteeId()).isEqualTo(10L);
            assertThat(dto.getAssociationName()).isEqualTo("Technical Committee");
        }

        @Test
        @DisplayName("Should map Club Board user with club association")
        void shouldMapClubBoardUser() {
            user.setRole(Role.ACM_CLUB_BOARD);
            Club club = new Club();
            club.setId(20L);
            club.setName("CP Club");

            ClubBoard clb = new ClubBoard();
            clb.setRole("Vice Head");
            clb.setOrder(3);
            clb.setClub(club);

            when(clubBoardRepository.findByUserId(userId)).thenReturn(Optional.of(clb));

            UserDTO dto = userMapper.toDTO(user);

            assertThat(dto.getBoardRole()).isEqualTo("Vice Head");
            assertThat(dto.getBoardOrder()).isEqualTo(3);
            assertThat(dto.getClubId()).isEqualTo(20L);
            assertThat(dto.getAssociationName()).isEqualTo("CP Club");
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntityTests {

        @Test
        @DisplayName("Should return null when userDTO is null")
        void shouldReturnNullWhenUserDtoIsNull() {
            assertThat(userMapper.toEntity(null)).isNull();
        }

        @Test
        @DisplayName("Should map UserDTO to User entity")
        void shouldMapDtoToEntity() {
            UserDTO dto = UserDTO.builder()
                    .id(userId)
                    .email("dto@acm.org")
                    .build();

            User entity = userMapper.toEntity(dto);

            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(userId);
            assertThat(entity.getEmail()).isEqualTo("dto@acm.org");
        }
    }

    @Nested
    @DisplayName("userToSuccessRegister")
    class UserToSuccessRegisterTests {

        @Test
        @DisplayName("Should return null when user is null")
        void shouldReturnNullWhenUserIsNull() {
            assertThat(userMapper.userToSuccessRegister(null)).isNull();
        }

        @Test
        @DisplayName("Should map user to SuccessRegisterResponse")
        void shouldMapUserToSuccessRegisterResponse() {
            SuccessRegisterResponse response = userMapper.userToSuccessRegister(user);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(userId);
            assertThat(response.getEmail()).isEqualTo("alex@acm.org");
        }
    }

    @Nested
    @DisplayName("toProfileDto")
    class ToProfileDtoTests {

        @Test
        @DisplayName("Should return null when user is null")
        void shouldReturnNullWhenUserIsNull() {
            assertThat(userMapper.toProfileDto(null)).isNull();
        }

        @Test
        @DisplayName("Should map user to UserProfileDto with committee")
        void shouldMapUserProfileWithCommittee() {
            Committee committee = new Committee();
            committee.setId(5L);
            committee.setName("Logistics");
            user.setCommittee(committee);

            UserProfileDto profile = userMapper.toProfileDto(user);

            assertThat(profile).isNotNull();
            assertThat(profile.getId()).isEqualTo(userId);
            assertThat(profile.getEmail()).isEqualTo("alex@acm.org");
            assertThat(profile.getCommitteeId()).isEqualTo(5L);
            assertThat(profile.getCommitteeName()).isEqualTo("Logistics");
            assertThat(profile.getDepartment()).isEqualTo(Department.CCE);
        }

        @Test
        @DisplayName("Should map user to UserProfileDto without committee")
        void shouldMapUserProfileWithoutCommittee() {
            user.setCommittee(null);

            UserProfileDto profile = userMapper.toProfileDto(user);

            assertThat(profile).isNotNull();
            assertThat(profile.getCommitteeId()).isNull();
            assertThat(profile.getCommitteeName()).isNull();
        }
    }
}
