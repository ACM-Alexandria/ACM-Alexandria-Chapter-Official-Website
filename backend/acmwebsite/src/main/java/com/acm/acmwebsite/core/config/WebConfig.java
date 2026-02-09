package com.acm.acmwebsite.core.config;

import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //Protect ONLY the forgot password endpoint (only for this task)
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/v1/auth/forgot-password");

        // Protect ALL Auth endpoints (Login, Register, Forgot Password)
        // registry.addInterceptor(rateLimitInterceptor)
        //        .addPathPatterns("/api/v1/auth/**");
    }
}