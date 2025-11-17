package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseAnalyticsService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private AssignmentSubmissionRepository assignmentSubmissionRepository;

    @Autowired
    private CoursePaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getCourseAnalytics(Long courseId, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Verify instructor owns the course
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("Not authorized to view analytics for this course");
        }

        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);

        // Enrollment statistics
        long totalEnrollments = enrollments.size();
        long activeEnrollments = enrollments.stream()
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .count();
        long completedEnrollments = enrollments.stream()
                .filter(e -> "COMPLETED".equals(e.getStatus()))
                .count();

        // Revenue statistics
        List<CoursePayment> payments = paymentRepository.findByCourse(course);
        BigDecimal totalRevenue = payments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .map(CoursePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Progress statistics
        double avgProgress = enrollments.stream()
                .mapToDouble(e -> e.getProgressPercentage() != null ? e.getProgressPercentage() : 0)
                .average()
                .orElse(0.0);

        // Quiz statistics
        long totalQuizAttempts = 0;
        double avgQuizScore = 0.0;
        if (!quizzes.isEmpty()) {
            List<QuizAttempt> allAttempts = new ArrayList<>();
            for (Quiz quiz : quizzes) {
                allAttempts.addAll(quizAttemptRepository.findByQuizId(quiz.getId()));
            }
            totalQuizAttempts = allAttempts.size();
            if (!allAttempts.isEmpty()) {
                avgQuizScore = allAttempts.stream()
                        .filter(a -> a.getScore() != null)
                        .mapToInt(QuizAttempt::getScore)
                        .average()
                        .orElse(0.0);
            }
        }

        // Assignment statistics
        long totalSubmissions = 0;
        double avgAssignmentScore = 0.0;
        if (!assignments.isEmpty()) {
            List<AssignmentSubmission> allSubmissions = new ArrayList<>();
            for (Assignment assignment : assignments) {
                allSubmissions.addAll(assignmentSubmissionRepository.findByAssignmentId(assignment.getId()));
            }
            totalSubmissions = allSubmissions.size();
            if (!allSubmissions.isEmpty()) {
                avgAssignmentScore = allSubmissions.stream()
                        .filter(s -> s.getScore() != null)
                        .mapToInt(AssignmentSubmission::getScore)
                        .average()
                        .orElse(0.0);
            }
        }

        // Enrollment trend (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long enrollmentsLast30Days = enrollments.stream()
                .filter(e -> e.getEnrolledAt() != null && e.getEnrolledAt().isAfter(thirtyDaysAgo))
                .count();

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("courseId", courseId);
        analytics.put("courseTitle", course.getTitle());
        analytics.put("totalEnrollments", totalEnrollments);
        analytics.put("activeEnrollments", activeEnrollments);
        analytics.put("completedEnrollments", completedEnrollments);
        analytics.put("totalRevenue", totalRevenue);
        analytics.put("avgProgress", Math.round(avgProgress * 100.0) / 100.0);
        analytics.put("totalQuizzes", quizzes.size());
        analytics.put("totalQuizAttempts", totalQuizAttempts);
        analytics.put("avgQuizScore", Math.round(avgQuizScore * 100.0) / 100.0);
        analytics.put("totalAssignments", assignments.size());
        analytics.put("totalSubmissions", totalSubmissions);
        analytics.put("avgAssignmentScore", Math.round(avgAssignmentScore * 100.0) / 100.0);
        analytics.put("enrollmentsLast30Days", enrollmentsLast30Days);
        analytics.put("completionRate", totalEnrollments > 0 ? 
                Math.round((double) completedEnrollments / totalEnrollments * 10000.0) / 100.0 : 0.0);

        return analytics;
    }

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;
}

