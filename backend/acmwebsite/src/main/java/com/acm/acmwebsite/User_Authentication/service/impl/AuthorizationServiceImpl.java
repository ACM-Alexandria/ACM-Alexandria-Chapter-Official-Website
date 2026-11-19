package com.acm.acmwebsite.User_Authentication.service.impl;

import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.AuthorizationService;
import com.acm.acmwebsite.User_Authentication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private static final String HR_COMMITTEE_NAME = "HR Committee";

    private static final Set<String> ACM_ROLES = Set.of(
            "ROLE_SUPER_ADMIN", "ROLE_ACM_HIGH_BOARD", "ROLE_ACM_COMMITTEE_BOARD", "ROLE_ACM_CLUB_BOARD", "ROLE_ACM_MEMBER");

    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    @Override
    public boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    @Override
    public boolean isSelf(UUID id, Authentication authentication) {
        return findAuthenticatedUser(authentication)
                .map(u -> u.getId().equals(id))
                .orElse(false);
    }

    @Override
    public boolean isAcmMember(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(a -> ACM_ROLES.contains(a.getAuthority()));
    }

    @Override
    public boolean isHrCommitteeMember(Authentication authentication) {
        return isAuthenticated(authentication)
                && userRepository.existsByEmailAndRoleAndCommitteeNameIgnoreCase(
                        authentication.getName(), Role.ACM_MEMBER, HR_COMMITTEE_NAME);
    }

    private Optional<UserDTO> findAuthenticatedUser(Authentication authentication) {
        return userService.getUserByEmail(authentication.getName());
    }
}