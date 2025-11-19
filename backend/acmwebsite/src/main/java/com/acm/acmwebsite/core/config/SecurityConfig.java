package com.acm.acmwebsite.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // disable CSRF for testing POST/PUT/DELETE
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/clubs/**").permitAll() // public endpoints
                        .anyRequest().authenticated() // everything else requires auth
                );

        return http.build();
    }
}
