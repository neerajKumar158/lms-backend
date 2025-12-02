package com.lms.web;

import com.lms.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.function.Supplier;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Autowired
    private Supplier<Bucket> apiBucketSupplier;

    @Autowired
    private Supplier<Bucket> authBucketSupplier;

    @Autowired
    private Supplier<Bucket> uploadBucketSupplier;

    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!rateLimitEnabled) {
            return true;
        }

        String path = request.getRequestURI();
        String clientId = getClientId(request);

        // Determine which bucket to use based on endpoint
        String bucketKey = getBucketKey(path, clientId);
        Supplier<Bucket> bucketSupplier = getBucketSupplier(path);

        if (bucketSupplier == null) {
            // No rate limiting for this endpoint
            return true;
        }

        // Get or create bucket for this client
        Bucket bucket = rateLimitConfig.getBucket(bucketKey, bucketSupplier);

        // Try to consume a token
        if (bucket.tryConsume(1)) {
            // Add rate limit headers
            long availableTokens = bucket.getAvailableTokens();
            response.setHeader("X-RateLimit-Limit", String.valueOf(availableTokens));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(availableTokens));
            return true;
        } else {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for client: {} on path: {}", clientId, path);
            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"TOO_MANY_REQUESTS\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
            return false;
        }
    }

    private String getClientId(HttpServletRequest request) {
        // Try to get user identifier from authentication
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;
        if (username != null) {
            return "user:" + username;
        }
        // Fallback to IP address
        String ip = getClientIpAddress(request);
        return "ip:" + ip;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private String getBucketKey(String path, String clientId) {
        // Create a unique key for this client and endpoint type
        if (path.startsWith("/api/auth/")) {
            return "rate-limit:auth:" + clientId;
        } else if (path.startsWith("/api/lms/upload/") || path.contains("/upload")) {
            return "rate-limit:upload:" + clientId;
        } else {
            return "rate-limit:api:" + clientId;
        }
    }

    private Supplier<Bucket> getBucketSupplier(String path) {
        // Return appropriate bucket supplier based on endpoint
        if (path.startsWith("/api/auth/")) {
            return authBucketSupplier;
        } else if (path.startsWith("/api/lms/upload/") || path.contains("/upload")) {
            return uploadBucketSupplier;
        } else if (path.startsWith("/api/")) {
            return apiBucketSupplier;
        }
        return null; // No rate limiting for non-API endpoints
    }
}
