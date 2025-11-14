package com.rfpe.estimator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.util.concurrent.RateLimiter;

@Configuration
public class RateLimitConfig {
    
    // Allow 10 requests per minute per client (adjust as needed)
    private static final int REQUESTS_PER_MINUTE = 10;
    
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(REQUESTS_PER_MINUTE / 60.0);
    }
}
