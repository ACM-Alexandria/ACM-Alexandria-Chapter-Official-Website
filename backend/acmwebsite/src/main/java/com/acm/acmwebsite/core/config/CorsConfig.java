package com.acm.acmwebsite.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    // accepts either a single URL or comma-separated list; fallback empty string if unset
    @Value("${app.frontend.urls:#{null}}")
    private String frontendUrlsRaw;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        final String[] origins;
        if (!StringUtils.hasText(frontendUrlsRaw)) {
            origins = new String[0];
        } else {
            // support YAML list or comma-separated single-line
            String normalized = frontendUrlsRaw.trim();
            // if YAML list is used (Spring can inject comma-joined string), this still works
            origins = StringUtils.commaDelimitedListToStringArray(normalized);
        }

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                var mapping = registry.addMapping("/**")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);

                if (origins.length == 0) {
                    // dev-friendly fallback — allow localhost ports. Remove in prod if you want fail-fast.
                    mapping.allowedOriginPatterns("http://localhost:*");
                } else {
                    mapping.allowedOrigins(origins);
                }
            }
        };
    }
}
