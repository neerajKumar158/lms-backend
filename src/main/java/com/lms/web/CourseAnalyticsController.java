package com.lms.web;

import com.lms.repository.UserAccountRepository;
import com.lms.service.CourseAnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/lms/analytics")
public class CourseAnalyticsController {

    @Autowired
    private CourseAnalyticsService analyticsService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseAnalytics(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> analytics = analyticsService.getCourseAnalytics(courseId, user.getId());
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Failed to load course analytics for course {} (principal={}): {}",
                    courseId, principal != null ? principal.getUsername() : "unknown", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load analytics"));
        }
    }
}



