package com.acm.acmwebsite.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig { // Renamed from CorsConfig as recommended

    // 1. Keep the robust property injection from HEAD
    @Value("${app.frontend.urls:#{null}}")
    private String frontendUrlsRaw;

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
                        // Allow CORS preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Secure admin endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Explicitly secure registration endpoints
                        .requestMatchers(HttpMethod.POST, "/api/events/*/register").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/clubs/*/register").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/committee/*/register").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/program/*/register").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/events/*/is-registered/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/clubs/*/is-registered/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/committee/*/is-registered/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/program/*/is-registered/*").authenticated()
                        // Secure feedback submission and screenshot uploads for authenticated users only
                        .requestMatchers(HttpMethod.POST, "/api/feedback/**").authenticated()
                        // Allow these specific endpoints without login
                        .requestMatchers(
                                "/error",
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/api/v1/auth/refresh",
                                "/api/v1/user/logout",
                                "/api/clubs/**",
                                "/api/events/**",
                                "/api/highboard/**",
                                "/api/committee/**",
                                "/api/program/**",
                                "/api/socialLinks",
                                "/api/socialLinks/**",
                                "/api/gallery",
                                "/api/gallery/**",
                                "/api/radio/**")
                        .permitAll()
                        // All other requests require a valid JWT
                        .anyRequest().authenticated())

                // 4. Session: Stateless (No JSESSIONID cookies)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 5. Filter: Add JWT Filter before the standard username/password check
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Replaces 'WebMvcConfigurer'. Safer because Spring Security applies this
     * BEFORE the request reaches the controllers.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 2. Merge HEAD's dynamic URL logic into the Sprint branch's
        // CorsConfigurationSource
        if (!StringUtils.hasText(frontendUrlsRaw)) {
            // dev-friendly fallback — allow localhost ports
            configuration.setAllowedOriginPatterns(List.of("http://localhost:*"));
        } else {
            String normalized = frontendUrlsRaw.trim();
            String[] origins = StringUtils.commaDelimitedListToStringArray(normalized);
            configuration.setAllowedOrigins(List.of(origins));
        }

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

    // Note: If you need the PasswordEncoder here, uncomment it. Otherwise, ensure
    // it's defined in another @Configuration class so UserServiceImpl doesn't
    // crash.
    /*
     * @Bean
     * public PasswordEncoder passwordEncoder() {
     * return new BCryptPasswordEncoder();
     * }
     */
}