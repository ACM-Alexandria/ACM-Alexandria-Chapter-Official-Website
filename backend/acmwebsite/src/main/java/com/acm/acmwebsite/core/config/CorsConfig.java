package com.acm.acmwebsite.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class CorsConfig { // Recommended: Rename this class to SecurityConfig

  @Value("${frontend.url}")
  private String frontendUrl;

  @Value("${backend.url}")
  private String backendUrl;

  // We inject the JwtFilter here via constructor (@RequiredArgsConstructor)
  private final JwtFilter jwtFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            // 1. CORS: Use the configuration source defined below
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 2. CSRF: Disable it because we use JWTs (Stateless)
            .csrf(AbstractHttpConfigurer::disable)

            // 3. Authorization Rules
            .authorizeHttpRequests(auth -> auth
                    // Allow these specific endpoints without login
                    .requestMatchers(
                            "/api/v1/auth/login",
                            "/api/v1/auth/register",
                            "/api/v1/auth/forgot-password", // Added this for your recent task
                            "/api/v1/auth/refresh", // Allow refresh token endpoint without JWT
                            "/api/v1/user/logout"
                    ).permitAll()
                    // All other requests require a valid JWT
                    .anyRequest().authenticated()
            )

            // 4. Session: Stateless (No JSESSIONID cookies)
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 5. Filter: Add JWT Filter before the standard username/password check
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * This replaces the 'WebMvcConfigurer' bean.
   * It is safer because Spring Security applies this BEFORE the request reaches the controllers.
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Allowed Origins from application.properties
    configuration.setAllowedOrigins(List.of(frontendUrl, backendUrl, "http://localhost:3000", "http://localhost:4200"));

    // Allowed HTTP Methods
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    // Allowed Headers
    configuration.setAllowedHeaders(List.of("*"));

    // Allow Cookies/Credentials
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  /**
   * You MUST have this bean for your UserServiceImpl to work.
   */
//  @Bean
//  public PasswordEncoder passwordEncoder() {
//    return new BCryptPasswordEncoder();
//  }
}