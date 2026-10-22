package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeBoardMemberDto;
import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeDto;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommitteeMapperTest {

    private final CommitteeMapper mapper = Mappers.getMapper(CommitteeMapper.class);

    @Test
    @DisplayName("Should map Committee to CommitteeDto and entity")
    void testCommitteeToDtoAndEntity() {
        Committee committee = new Committee();
        committee.setId(10L);
        committee.setName("PR Committee");
        committee.setDescription("Public Relations");

        CommitteeDto dto = mapper.toDto(committee);
        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("PR Committee");
        assertThat(dto.getDescription()).isEqualTo("Public Relations");

        Committee entity = mapper.toEntity(dto);
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("PR Committee");
    }

    @Test
    @DisplayName("Should map CommitteeBoard to CommitteeBoardMemberDto")
    void testToBoardDto() {
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setName("John");
        user.setProfileImageUrl("https://image.com");
        user.setLinkedinUrl("https://linkedin.com");

        CommitteeBoard board = new CommitteeBoard();
        board.setId(1L);
        board.setUser(user);
        board.setRole("Head");
        board.setOrder(1);

        CommitteeBoardMemberDto dto = mapper.toBoardDto(board);
        assertThat(dto).isNotNull();
        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getUserName()).isEqualTo("John");
        assertThat(dto.getRole()).isEqualTo("Head");
        assertThat(dto.getOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should update committee from DTO")
    void testUpdateCommitteeFromDto() {
        Committee committee = new Committee();
        committee.setId(10L);
        committee.setName("Old Name");

        CommitteeDto updateDto = new CommitteeDto();
        updateDto.setName("New Name");

        mapper.updateCommitteeFromDto(updateDto, committee);
        assertThat(committee.getName()).isEqualTo("New Name");
        assertThat(committee.getId()).isEqualTo(10L);
    }
}
