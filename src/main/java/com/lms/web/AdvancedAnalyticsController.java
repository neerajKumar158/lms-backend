// language: java
package com.lms.web;

import com.lms.domain.Organization;
import com.lms.domain.UserAccount;
import com.lms.repository.OrganizationRepository;
import com.lms.repository.UserAccountRepository;
import com.lms.security.JwtService;
import com.lms.service.AdvancedAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Advanced Analytics Controller
 * Provides detailed analytics and reports for organizations and teachers
 */
@RestController
@RequestMapping("/api/lms/analytics/advanced")
@RequiredArgsConstructor
public class AdvancedAnalyticsController {

    private static final String ACCESS_DENIED = "Access denied";

    private final AdvancedAnalyticsService analyticsService;
    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;
    private final OrganizationRepository organizationRepository;

    /**
     * Get organization analytics
     * Accessible by: ORGANIZATION (own org), ADMIN (any org)
     */
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<Map<String, Object>> getOrganizationAnalytics(
            @PathVariable Long organizationId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String email = extractEmailFromToken(authHeader);
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        // Check authorization
        boolean isAdmin = user.getRoles() != null && user.getRoles().contains("ROLE_ADMIN");
        boolean isOrgAdmin = organization.getAdmin() != null && organization.getAdmin().getId().equals(user.getId());

        if (!isAdmin && !isOrgAdmin) {
            return ResponseEntity.status(403).body(Map.of("error", ACCESS_DENIED));
        }

        Map<String, Object> analytics = analyticsService.getOrganizationAnalytics(organizationId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get teacher analytics
     * Accessible by: TEACHER (own analytics), ORGANIZATION (teachers in org), ADMIN (any teacher)
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<Map<String, Object>> getTeacherAnalytics(
            @PathVariable Long teacherId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String email = extractEmailFromToken(authHeader);
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserAccount teacher = userAccountRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // Check authorization
        boolean isAdmin = user.getRoles() != null && user.getRoles().contains("ROLE_ADMIN");
        boolean isSelf = user.getId().equals(teacherId);
        boolean isOrgAdmin = user.getUserType() == UserAccount.UserType.ORGANIZATION
                && teacher.getOrganization() != null
                && teacher.getOrganization().getAdmin().getId().equals(user.getId());

        if (!isAdmin && !isSelf && !isOrgAdmin) {
            return ResponseEntity.status(403).body(Map.of("error", ACCESS_DENIED));
        }

        Map<String, Object> analytics = analyticsService.getTeacherAnalytics(teacherId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get export data for organization
     * Types: "enrollments", "revenue"
     */
    @GetMapping("/organization/{organizationId}/export/{type}")
    public ResponseEntity<Map<String, Object>> getExportData(
            @PathVariable Long organizationId,
            @PathVariable String type,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String email = extractEmailFromToken(authHeader);
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        // Check authorization
        boolean isAdmin = user.getRoles() != null && user.getRoles().contains("ROLE_ADMIN");
        boolean isOrgAdmin = organization.getAdmin() != null && organization.getAdmin().getId().equals(user.getId());

        if (!isAdmin && !isOrgAdmin) {
            return ResponseEntity.status(403).body(Map.of("error", ACCESS_DENIED));
        }

        if (!"enrollments".equals(type) && !"revenue".equals(type)) {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid export type. Use 'enrollments' or 'revenue'"));
        }

        Map<String, Object> exportData = analyticsService.getExportData(organizationId, type);
        return ResponseEntity.ok(exportData);
    }

    private String extractEmailFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        return jwtService.extractSubject(token);
    }
}
