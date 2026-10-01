package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.repository.ClubBoardRepository;
import com.acm.acmwebsite.feature.repository.CommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import com.acm.acmwebsite.feature.service.MemberTargetingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberTargetingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HighBoardRepository highBoardRepository;

    @Mock
    private CommitteeBoardRepository committeeBoardRepository;

    @Mock
    private ClubBoardRepository clubBoardRepository;

    @InjectMocks
    private MemberTargetingService memberTargetingService;

    @Test
    void findRecipientEmailsAppliesCommitteeScope() {
        Committee tech = committee(1L);
        Committee media = committee(2L);
        User techMember = user("tech@acm.org", tech);
        User mediaMember = user("media@acm.org", media);
        when(userRepository.findAllByRoleInWithCommittee(any())).thenReturn(List.of(techMember, mediaMember));

        List<String> recipientEmails = memberTargetingService.findRecipientEmails(
                List.of(Role.ACM_MEMBER), List.of(tech.getId()), List.of());

        assertEquals(List.of("tech@acm.org"), recipientEmails);
    }

    private Committee committee(long id) {
        Committee committee = new Committee();
        committee.setId(id);
        committee.setName("Committee " + id);
        return committee;
    }

    private User user(String email, Committee committee) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .name(email)
                .role(Role.ACM_MEMBER)
                .committee(committee)
                .build();
    }
}