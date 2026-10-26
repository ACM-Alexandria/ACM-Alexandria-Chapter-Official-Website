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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private static final String HR_COMMITTEE_NAME = "HR Committee";

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
    public boolean isHrCommitteeMember(Authentication authentication) {
        return isAuthenticated(authentication)
                && userRepository.existsByEmailAndRoleAndCommitteeNameIgnoreCase(
                        authentication.getName(), Role.ACM_MEMBER, HR_COMMITTEE_NAME);
    }

    private Optional<UserDTO> findAuthenticatedUser(Authentication authentication) {
        return userService.getUserByEmail(authentication.getName());
    }
}