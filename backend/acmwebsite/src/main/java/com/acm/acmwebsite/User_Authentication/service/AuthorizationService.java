package com.acm.acmwebsite.User_Authentication.service;

import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface AuthorizationService {

    boolean isAuthenticated(Authentication authentication);
    boolean isAdmin(Authentication authentication);
    boolean isSelf(UUID id, Authentication authentication);
}