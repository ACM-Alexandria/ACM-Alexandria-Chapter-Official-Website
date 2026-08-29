package com.acm.acmwebsite.User_Authentication.service.impl;

import com.acm.acmwebsite.User_Authentication.dto.UserDTO;
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

    private final UserService userService;

    @Override
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    @Override
    public boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Override
    public boolean isSelf(UUID id, Authentication authentication) {
        return findAuthenticatedUser(authentication)
                .map(u -> u.getId().equals(id))
                .orElse(false);
    }

    private Optional<UserDTO> findAuthenticatedUser(Authentication authentication) {
        return userService.getUserByEmail(authentication.getName());
    }
}