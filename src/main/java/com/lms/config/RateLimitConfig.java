package com.lms.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@Configuration
public class RateLimitConfig {

    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${app.rate-limit.api.requests-per-minute:60}")
    private int apiRequestsPerMinute;

    @Value("${app.rate-limit.api.requests-per-hour:1000}")
    private int apiRequestsPerHour;

    @Value("${app.rate-limit.auth.requests-per-minute:5}")
    private int authRequestsPerMinute;

    @Value("${app.rate-limit.auth.requests-per-hour:20}")
    private int authRequestsPerHour;

    @Value("${app.rate-limit.upload.requests-per-minute:10}")
    private int uploadRequestsPerMinute;

    @Value("${app.rate-limit.upload.requests-per-hour:100}")
    private int uploadRequestsPerHour;

    // In-memory storage for buckets (can be replaced with Redis later)
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    @Bean
    public Supplier<Bucket> apiBucketSupplier() {
        return () -> {
            if (!rateLimitEnabled) {
                return createUnlimitedBucket();
            }
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(apiRequestsPerMinute, Refill.intervally(apiRequestsPerMinute, Duration.ofMinutes(1))))
                    .addLimit(Bandwidth.classic(apiRequestsPerHour, Refill.intervally(apiRequestsPerHour, Duration.ofHours(1))))
                    .build();
        };
    }

    @Bean
    public Supplier<Bucket> authBucketSupplier() {
        return () -> {
            if (!rateLimitEnabled) {
                return createUnlimitedBucket();
            }
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(authRequestsPerMinute, Refill.intervally(authRequestsPerMinute, Duration.ofMinutes(1))))
                    .addLimit(Bandwidth.classic(authRequestsPerHour, Refill.intervally(authRequestsPerHour, Duration.ofHours(1))))
                    .build();
        };
    }

    @Bean
    public Supplier<Bucket> uploadBucketSupplier() {
        return () -> {
            if (!rateLimitEnabled) {
                return createUnlimitedBucket();
            }
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(uploadRequestsPerMinute, Refill.intervally(uploadRequestsPerMinute, Duration.ofMinutes(1))))
                    .addLimit(Bandwidth.classic(uploadRequestsPerHour, Refill.intervally(uploadRequestsPerHour, Duration.ofHours(1))))
                    .build();
        };
    }

    private Bucket createUnlimitedBucket() {
        // Create a bucket with very high limits (effectively unlimited)
        return Bucket.builder()
                .addLimit(Bandwidth.classic(Long.MAX_VALUE, Refill.intervally(Long.MAX_VALUE, Duration.ofSeconds(1))))
                .build();
    }

    /**
     * Get or create a bucket for a specific client
     */
    public Bucket getBucket(String key, Supplier<Bucket> bucketSupplier) {
        return bucketCache.computeIfAbsent(key, k -> bucketSupplier.get());
    }
}
