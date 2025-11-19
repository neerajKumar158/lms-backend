package com.lms.web;

import com.lms.domain.UserAccount;
import com.lms.repository.UserAccountRepository;
import com.lms.service.AdminAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lms/admin/analytics")
public class AdminAnalyticsController {

    @Autowired
    private AdminAnalyticsService analyticsService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private boolean isAdmin(User principal) {
        if (principal == null) return false;
        var user = userAccountRepository.findByEmail(principal.getUsername()).orElse(null);
        return user != null && user.getUserType() == UserAccount.UserType.ADMIN;
    }

    @GetMapping("/system")
    public ResponseEntity<?> getSystemAnalytics(@AuthenticationPrincipal User principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            Map<String, Object> analytics = analyticsService.getSystemAnalytics();
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/teachers/performance")
    public ResponseEntity<?> getTeacherPerformance(@AuthenticationPrincipal User principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            Map<String, Object> performance = analyticsService.getTeacherPerformance();
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

