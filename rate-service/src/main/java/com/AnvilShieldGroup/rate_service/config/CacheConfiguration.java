package com.AnvilShieldGroup.rate_service.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfiguration {
    @Value("${cache.ttl}")
    private Long cacheTtl;
    @Value("${cache.maximum.size}")
    private Long cacheMaximumSize;
    @Bean
    public Cache<String, Map<String, Double>> cache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(cacheTtl, TimeUnit.MINUTES)
                .maximumSize(cacheMaximumSize)
                .build();
    }
}
