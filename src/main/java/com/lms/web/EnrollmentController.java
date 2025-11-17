package com.lms.web;

import com.lms.domain.CourseEnrollment;
import com.lms.repository.UserAccountRepository;
import com.lms.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lms/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping
    public List<CourseEnrollment> getMyEnrollments(@AuthenticationPrincipal User principal) {
        var user = userAccountRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return enrollmentService.getStudentEnrollments(user.getId());
    }

    @PostMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> enrollInCourse(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            CourseEnrollment enrollment = enrollmentService.enrollStudent(user.getId(), courseId);
            return ResponseEntity.ok(Map.of("id", enrollment.getId(), "message", "Enrolled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> checkEnrollment(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            boolean enrolled = enrollmentService.isEnrolled(user.getId(), courseId);
            return ResponseEntity.ok(Map.of("enrolled", enrolled));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{enrollmentId}/progress")
    public ResponseEntity<Map<String, Object>> updateProgress(
            @AuthenticationPrincipal User principal,
            @PathVariable("enrollmentId") Long enrollmentId,
            @RequestBody UpdateProgressRequest request) {
        try {
            enrollmentService.updateProgress(enrollmentId, request.progress());
            return ResponseEntity.ok(Map.of("message", "Progress updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{enrollmentId}/complete")
    public ResponseEntity<Map<String, Object>> completeCourse(
            @AuthenticationPrincipal User principal,
            @PathVariable("enrollmentId") Long enrollmentId) {
        try {
            enrollmentService.markAsCompleted(enrollmentId);
            return ResponseEntity.ok(Map.of("message", "Course completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    record UpdateProgressRequest(Integer progress) {}
}

