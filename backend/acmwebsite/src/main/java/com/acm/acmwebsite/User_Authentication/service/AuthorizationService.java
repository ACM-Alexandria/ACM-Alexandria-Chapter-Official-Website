package com.acm.acmwebsite.User_Authentication.service;

import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface AuthorizationService {

    boolean isAuthenticated(Authentication authentication);
    boolean isAdmin(Authentication authentication);
    boolean isSelf(UUID id, Authentication authentication);

    /** True for an ACM member who belongs to the HR Committee (checked against the database). */
    boolean isHrCommitteeMember(Authentication authentication);
}