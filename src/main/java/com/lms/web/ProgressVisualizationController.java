package com.lms.web;

import com.lms.repository.UserAccountRepository;
import com.lms.service.ProgressVisualizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lms/progress")
public class ProgressVisualizationController {

    @Autowired
    private ProgressVisualizationService progressService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getStudentProgress(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId,
            @RequestParam(required = false) Long studentId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // If studentId is provided (teacher viewing), use it; otherwise use authenticated user's ID
            Long targetStudentId = studentId != null ? studentId : user.getId();

            Map<String, Object> progressData = progressService.getStudentProgressData(targetStudentId, courseId);
            return ResponseEntity.ok(progressData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load progress data"));
        }
    }
}



