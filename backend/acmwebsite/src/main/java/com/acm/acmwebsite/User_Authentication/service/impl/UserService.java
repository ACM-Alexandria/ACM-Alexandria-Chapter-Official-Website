package com.acm.acmwebsite.User_Authentication.service.impl;
import com.acm.acmwebsite.User_Authentication.dto.LoginRequest;
import com.acm.acmwebsite.User_Authentication.dto.LoginResponse;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public LoginResponse login(LoginRequest request) {

        // 1. Validate email format
        if (!EMAIL_REGEX.matcher(request.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // 2. Check user exists
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new SecurityException("Incorrect email or password");
        }

        // 3. Verify password
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!matches) {
            throw new SecurityException("Incorrect email or password");
        }

        // 4. Return response (no password!)
        return new LoginResponse(user.getId(), user.getEmail());
    }
}
