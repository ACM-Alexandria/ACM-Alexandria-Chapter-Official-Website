package com.acm.acmwebsite.core.config;

import com.acm.acmwebsite.core.constants.CacheNames;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                CacheNames.CLUBS, CacheNames.EVENTS, CacheNames.PROGRAMS,
                CacheNames.COMMITTEES, CacheNames.HIGH_BOARD, CacheNames.PARTNERS,
                CacheNames.GALLERY, CacheNames.SOCIAL_LINKS, CacheNames.RADIO,
                CacheNames.EXCLUSIVE_FORMS);
    }
}
