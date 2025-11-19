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

/**
 * Admin Analytics Service
 * Provides system-wide analytics and statistics for admin dashboard
 */
@Service
public class AdminAnalyticsService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private CoursePaymentRepository paymentRepository;

    @Autowired
    private CourseReviewRepository reviewRepository;

    @Autowired
    private AssignmentSubmissionRepository assignmentSubmissionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private LiveSessionRepository liveSessionRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getSystemAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        // User Statistics
        List<UserAccount> allUsers = userAccountRepository.findAll();
        long totalUsers = allUsers.size();
        long totalStudents = allUsers.stream()
                .filter(u -> u.getUserType() == UserAccount.UserType.STUDENT)
                .count();
        long totalTeachers = allUsers.stream()
                .filter(u -> u.getUserType() == UserAccount.UserType.TEACHER)
                .count();
        long totalOrganizations = allUsers.stream()
                .filter(u -> u.getUserType() == UserAccount.UserType.ORGANIZATION)
                .count();
        long totalAdmins = allUsers.stream()
                .filter(u -> u.getUserType() == UserAccount.UserType.ADMIN)
                .count();

        analytics.put("users", Map.of(
                "total", totalUsers,
                "students", totalStudents,
                "teachers", totalTeachers,
                "organizations", totalOrganizations,
                "admins", totalAdmins
        ));

        // Course Statistics
        List<Course> allCourses = courseRepository.findAll();
        long totalCourses = allCourses.size();
        long publishedCourses = allCourses.stream()
                .filter(c -> c.getStatus() == Course.CourseStatus.PUBLISHED)
                .count();
        long draftCourses = allCourses.stream()
                .filter(c -> c.getStatus() == Course.CourseStatus.DRAFT)
                .count();
        long freeCourses = allCourses.stream()
                .filter(c -> c.getPrice() != null && c.getPrice().compareTo(BigDecimal.ZERO) == 0)
                .count();
        long paidCourses = allCourses.stream()
                .filter(c -> c.getPrice() != null && c.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .count();

        analytics.put("courses", Map.of(
                "total", totalCourses,
                "published", publishedCourses,
                "draft", draftCourses,
                "free", freeCourses,
                "paid", paidCourses
        ));

        // Enrollment Statistics
        List<CourseEnrollment> allEnrollments = enrollmentRepository.findAll();
        long totalEnrollments = allEnrollments.size();
        long activeEnrollments = allEnrollments.stream()
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .count();
        long completedEnrollments = allEnrollments.stream()
                .filter(e -> "COMPLETED".equals(e.getStatus()))
                .count();
        
        double avgProgress = allEnrollments.stream()
                .filter(e -> e.getProgressPercentage() != null)
                .mapToDouble(CourseEnrollment::getProgressPercentage)
                .average()
                .orElse(0.0);

        analytics.put("enrollments", Map.of(
                "total", totalEnrollments,
                "active", activeEnrollments,
                "completed", completedEnrollments,
                "averageProgress", Math.round(avgProgress * 100.0) / 100.0
        ));

        // Revenue Statistics
        List<CoursePayment> allPayments = paymentRepository.findAll();
        BigDecimal totalRevenue = allPayments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .map(CoursePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalDiscounts = allPayments.stream()
                .filter(p -> p.getDiscountAmount() != null)
                .map(CoursePayment::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long successfulPayments = allPayments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .count();
        long failedPayments = allPayments.stream()
                .filter(p -> "FAILED".equals(p.getStatus()))
                .count();
        long pendingPayments = allPayments.stream()
                .filter(p -> "PENDING".equals(p.getStatus()))
                .count();

        // Revenue by month (last 6 months)
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            
            BigDecimal monthRevenue = allPayments.stream()
                    .filter(p -> "SUCCESS".equals(p.getStatus()))
                    .filter(p -> p.getPaidAt() != null && 
                            p.getPaidAt().isAfter(monthStart) && 
                            p.getPaidAt().isBefore(monthEnd))
                    .map(CoursePayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            String monthKey = monthStart.getMonth().toString() + " " + monthStart.getYear();
            monthlyRevenue.put(monthKey, monthRevenue);
        }

        analytics.put("revenue", Map.of(
                "total", totalRevenue,
                "totalDiscounts", totalDiscounts,
                "successfulPayments", successfulPayments,
                "failedPayments", failedPayments,
                "pendingPayments", pendingPayments,
                "monthlyRevenue", monthlyRevenue
        ));

        // Popular Courses (by enrollment count)
        Map<Course, Long> courseEnrollmentCounts = allEnrollments.stream()
                .collect(Collectors.groupingBy(
                        CourseEnrollment::getCourse,
                        Collectors.counting()
                ));

        List<Map<String, Object>> popularCourses = courseEnrollmentCounts.entrySet().stream()
                .sorted(Map.Entry.<Course, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Course course = entry.getKey();
                    return Map.<String, Object>of(
                            "id", course.getId(),
                            "title", course.getTitle() != null ? course.getTitle() : "",
                            "enrollments", entry.getValue(),
                            "price", course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO
                    );
                })
                .collect(Collectors.toList());

        analytics.put("popularCourses", popularCourses);

        // Activity Statistics
        long totalAssignments = assignmentSubmissionRepository.count();
        long totalQuizAttempts = quizAttemptRepository.count();
        long totalLiveSessions = liveSessionRepository.count();
        long totalReviews = reviewRepository.count();

        analytics.put("activity", Map.of(
                "assignmentSubmissions", totalAssignments,
                "quizAttempts", totalQuizAttempts,
                "liveSessions", totalLiveSessions,
                "reviews", totalReviews
        ));

        // Recent Activity (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long newUsersLast30Days = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();
        long newEnrollmentsLast30Days = allEnrollments.stream()
                .filter(e -> e.getEnrolledAt() != null && e.getEnrolledAt().isAfter(thirtyDaysAgo))
                .count();
        long newCoursesLast30Days = allCourses.stream()
                .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();

        analytics.put("recentActivity", Map.of(
                "newUsers", newUsersLast30Days,
                "newEnrollments", newEnrollmentsLast30Days,
                "newCourses", newCoursesLast30Days
        ));

        return analytics;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTeacherPerformance() {
        List<UserAccount> teachers = userAccountRepository.findAll().stream()
                .filter(u -> u.getUserType() == UserAccount.UserType.TEACHER)
                .collect(Collectors.toList());

        List<Map<String, Object>> teacherStats = new ArrayList<>();

        for (UserAccount teacher : teachers) {
            List<Course> teacherCourses = courseRepository.findByInstructor(teacher);
            long totalEnrollments = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            double avgRating = 0.0;

            for (Course course : teacherCourses) {
                List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
                totalEnrollments += enrollments.size();

                List<CoursePayment> payments = paymentRepository.findByCourse(course);
                BigDecimal courseRevenue = payments.stream()
                        .filter(p -> "SUCCESS".equals(p.getStatus()))
                        .map(CoursePayment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                totalRevenue = totalRevenue.add(courseRevenue);

                List<CourseReview> reviews = reviewRepository.findByCourseIdOrderByCreatedAtDesc(course.getId());
                if (!reviews.isEmpty()) {
                    double courseAvgRating = reviews.stream()
                            .filter(r -> r.getRating() != null)
                            .mapToInt(CourseReview::getRating)
                            .average()
                            .orElse(0.0);
                    avgRating = (avgRating + courseAvgRating) / 2;
                }
            }

            teacherStats.add(Map.of(
                    "teacherId", teacher.getId(),
                    "teacherName", teacher.getName() != null ? teacher.getName() : teacher.getEmail(),
                    "email", teacher.getEmail(),
                    "totalCourses", teacherCourses.size(),
                    "totalEnrollments", totalEnrollments,
                    "totalRevenue", totalRevenue,
                    "averageRating", Math.round(avgRating * 100.0) / 100.0
            ));
        }

        return Map.of("teachers", teacherStats);
    }
}

