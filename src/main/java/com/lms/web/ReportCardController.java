package com.lms.web;

import com.lms.domain.Course;
import com.lms.repository.CourseRepository;
import com.lms.repository.UserAccountRepository;
import com.lms.service.ReportCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lms/report-card")
public class ReportCardController {

    @Autowired
    private ReportCardService reportCardService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/student")
    public ResponseEntity<?> getStudentReportCard(@AuthenticationPrincipal User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> reportCard = reportCardService.getStudentReportCard(user.getId());
            return ResponseEntity.ok(reportCard);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load report card"));
        }
    }

    @GetMapping("/student/course/{courseId}")
    public ResponseEntity<?> getStudentReportCardForCourse(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId,
            @RequestParam(required = false) Long studentId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            
            if (courseId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Course ID is required"));
            }
            
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // If studentId is provided (teacher viewing student's report), verify authorization
            if (studentId != null && !studentId.equals(user.getId())) {
                // Check if user is teacher/instructor of the course
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new RuntimeException("Course not found"));
                if (course.getInstructor() == null || !course.getInstructor().getId().equals(user.getId())) {
                    return ResponseEntity.status(403).body(Map.of("error", "Not authorized to view this student's report card"));
                }
            }
            
            // If studentId is provided (teacher viewing student's report), use it; otherwise use authenticated user's ID
            Long targetStudentId = studentId != null ? studentId : user.getId();
            
            Map<String, Object> reportCard = reportCardService.getStudentReportCardForCourse(targetStudentId, courseId);
            return ResponseEntity.ok(reportCard);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load report card"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while loading the report card: " + (e.getMessage() != null ? e.getMessage() : "Unknown error")));
        }
    }

    @GetMapping("/course/{courseId}/students")
    public ResponseEntity<?> getStudentsReportCardsForCourse(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Authorization check: ensure user is teacher/instructor of the course
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            
            if (!course.getInstructor().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized to view report cards for this course"));
            }
            
            var reportCards = reportCardService.getStudentsReportCardsForCourse(courseId);
            return ResponseEntity.ok(reportCards);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load report cards"));
        }
    }
}

