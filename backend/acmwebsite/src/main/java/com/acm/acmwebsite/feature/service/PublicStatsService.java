package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.PublicStatsDto;
import com.acm.acmwebsite.feature.repository.EventRegistrationRepository;
import com.acm.acmwebsite.feature.repository.ProgramRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

/** Counts behind the public chapter totals. Each one is a single COUNT query. */
@Service
@RequiredArgsConstructor
public class PublicStatsService {

    /** Roles that make an account a chapter member: members and every board. Plain users and the super admin are not. */
    static final Set<Role> MEMBER_ROLES = EnumSet.of(Role.ACM_MEMBER, Role.ACM_COMMITTEE_BOARD, Role.ACM_CLUB_BOARD, Role.ACM_HIGH_BOARD);

    private final UserRepository userRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final ProgramRegistrationRepository programRegistrationRepository;

    @Transactional(readOnly = true)
    public PublicStatsDto getStats() {
        return new PublicStatsDto(
                userRepository.count(),
                userRepository.countByRoleIn(MEMBER_ROLES),
                eventRegistrationRepository.count(),
                programRegistrationRepository.count());
    }
}
