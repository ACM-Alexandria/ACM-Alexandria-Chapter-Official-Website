package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.PublicStatsDto;
import com.acm.acmwebsite.feature.repository.EventRegistrationRepository;
import com.acm.acmwebsite.feature.repository.ProgramRegistrationRepository;
import com.acm.acmwebsite.feature.service.PublicStatsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicStatsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRegistrationRepository eventRegistrationRepository;

    @Mock
    private ProgramRegistrationRepository programRegistrationRepository;

    @InjectMocks
    private PublicStatsService publicStatsService;

    @Test
    void getStats_shouldCombineTheFourCounts() {
        when(userRepository.count()).thenReturn(1200L);
        when(userRepository.countByRoleIn(anyCollection())).thenReturn(85L);
        when(eventRegistrationRepository.count()).thenReturn(3400L);
        when(programRegistrationRepository.count()).thenReturn(640L);

        assertEquals(new PublicStatsDto(1200, 85, 3400, 640), publicStatsService.getStats());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getStats_shouldCountMembersAndBoardsButNotPlainUsersOrTheSuperAdmin() {
        ArgumentCaptor<Collection<Role>> roles = ArgumentCaptor.forClass(Collection.class);

        publicStatsService.getStats();

        verify(userRepository).countByRoleIn(roles.capture());
        assertEquals(4, roles.getValue().size());
        assertTrue(roles.getValue().containsAll(List.of(Role.ACM_MEMBER, Role.ACM_COMMITTEE_BOARD, Role.ACM_CLUB_BOARD, Role.ACM_HIGH_BOARD)));
    }
}
