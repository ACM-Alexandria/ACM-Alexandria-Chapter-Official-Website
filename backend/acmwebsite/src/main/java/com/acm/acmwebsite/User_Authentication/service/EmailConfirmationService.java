package com.acm.acmwebsite.User_Authentication.service;

public interface EmailConfirmationService {
    void sendConfirmationEmail(String email);
    void confirmEmail(String token);
}
