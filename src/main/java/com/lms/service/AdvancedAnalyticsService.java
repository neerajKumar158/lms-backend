package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Analytics Service for Organizations and Teachers
 * Provides detailed reports, revenue analytics, student progress tracking, and export-ready data
 */
@Service
public class AdvancedAnalyticsService {

    @Autowired
    private OrganizationRepository organizationRepository;

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
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentSubmissionRepository submissionRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;


    /**
     * Get comprehensive organization analytics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getOrganizationAnalytics(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        List<Course> courses = courseRepository.findByOrganizationId(organizationId);
        List<UserAccount> teachers = userAccountRepository.findByOrganizationId(organizationId)
                .stream()
                .filter(u -> u.getUserType() == UserAccount.UserType.TEACHER)
                .collect(Collectors.toList());

        // Revenue Analytics
        Map<String, Object> revenue = getRevenueAnalytics(courses);

        // Enrollment Analytics
        Map<String, Object> enrollments = getEnrollmentAnalytics(courses);

        // Teacher Performance
        List<Map<String, Object>> teacherPerformance = getTeacherPerformanceList(teachers, courses);

        // Course Performance
        List<Map<String, Object>> coursePerformance = getCoursePerformanceList(courses);

        // Student Analytics
        Map<String, Object> studentAnalytics = getStudentAnalytics(courses);

        // Time-based Trends (Last 6 months)
        Map<String, Object> trends = getTrends(courses, 6);

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("organizationId", organizationId);
        analytics.put("organizationName", organization.getName());
        analytics.put("totalCourses", courses.size());
        analytics.put("totalTeachers", teachers.size());
        analytics.put("revenue", revenue);
        analytics.put("enrollments", enrollments);
        analytics.put("teacherPerformance", teacherPerformance);
        analytics.put("coursePerformance", coursePerformance);
        analytics.put("studentAnalytics", studentAnalytics);
        analytics.put("trends", trends);
        analytics.put("generatedAt", LocalDateTime.now().toString());

        return analytics;
    }

    /**
     * Get detailed teacher analytics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTeacherAnalytics(Long teacherId) {
        UserAccount teacher = userAccountRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (teacher.getUserType() != UserAccount.UserType.TEACHER) {
            throw new RuntimeException("User is not a teacher");
        }

        List<Course> courses = courseRepository.findByInstructor(teacher);

        // Revenue Analytics
        Map<String, Object> revenue = getRevenueAnalytics(courses);

        // Enrollment Analytics
        Map<String, Object> enrollments = getEnrollmentAnalytics(courses);

        // Course Performance
        List<Map<String, Object>> coursePerformance = getCoursePerformanceList(courses);

        // Student Progress Analytics
        Map<String, Object> studentProgress = getStudentProgressAnalytics(courses);

        // Assignment & Quiz Analytics
        Map<String, Object> assessmentAnalytics = getAssessmentAnalytics(courses);

        // Time-based Trends
        Map<String, Object> trends = getTrends(courses, 6);

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("teacherId", teacherId);
        analytics.put("teacherName", teacher.getName() != null ? teacher.getName() : teacher.getEmail());
        analytics.put("email", teacher.getEmail());
        analytics.put("totalCourses", courses.size());
        analytics.put("revenue", revenue);
        analytics.put("enrollments", enrollments);
        analytics.put("coursePerformance", coursePerformance);
        analytics.put("studentProgress", studentProgress);
        analytics.put("assessmentAnalytics", assessmentAnalytics);
        analytics.put("trends", trends);
        analytics.put("generatedAt", LocalDateTime.now().toString());

        return analytics;
    }

    /**
     * Get revenue analytics for courses
     */
    private Map<String, Object> getRevenueAnalytics(List<Course> courses) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal monthlyRevenue = BigDecimal.ZERO;
        BigDecimal yearlyRevenue = BigDecimal.ZERO;
        int totalTransactions = 0;

        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate yearStart = now.withDayOfYear(1);

        for (Course course : courses) {
            List<CoursePayment> payments = paymentRepository.findByCourse(course);
            for (CoursePayment payment : payments) {
                if ("SUCCESS".equals(payment.getStatus())) {
                    BigDecimal amount = payment.getAmount();
                    totalRevenue = totalRevenue.add(amount);
                    totalTransactions++;

                    LocalDateTime paymentDate = payment.getCreatedAt();
                    if (paymentDate != null) {
                        LocalDate date = paymentDate.toLocalDate();
                        if (date.isAfter(monthStart.minusDays(1))) {
                            monthlyRevenue = monthlyRevenue.add(amount);
                        }
                        if (date.isAfter(yearStart.minusDays(1))) {
                            yearlyRevenue = yearlyRevenue.add(amount);
                        }
                    }
                }
            }
        }

        Map<String, Object> revenue = new HashMap<>();
        revenue.put("total", totalRevenue);
        revenue.put("monthly", monthlyRevenue);
        revenue.put("yearly", yearlyRevenue);
        revenue.put("totalTransactions", totalTransactions);
        revenue.put("averageTransactionValue", totalTransactions > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(totalTransactions), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        return revenue;
    }

    /**
     * Get enrollment analytics
     */
    private Map<String, Object> getEnrollmentAnalytics(List<Course> courses) {
        long totalEnrollments = 0;
        long activeEnrollments = 0;
        long completedEnrollments = 0;
        long uniqueStudents = 0;
        Set<Long> studentIds = new HashSet<>();

        for (Course course : courses) {
            List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
            totalEnrollments += enrollments.size();

            for (CourseEnrollment enrollment : enrollments) {
                studentIds.add(enrollment.getStudent().getId());
                if ("ACTIVE".equals(enrollment.getStatus())) {
                    activeEnrollments++;
                } else if ("COMPLETED".equals(enrollment.getStatus())) {
                    completedEnrollments++;
                }
            }
        }

        uniqueStudents = studentIds.size();

        Map<String, Object> enrollments = new HashMap<>();
        enrollments.put("total", totalEnrollments);
        enrollments.put("active", activeEnrollments);
        enrollments.put("completed", completedEnrollments);
        enrollments.put("uniqueStudents", uniqueStudents);
        enrollments.put("completionRate", totalEnrollments > 0 
                ? (double) completedEnrollments / totalEnrollments * 100 
                : 0.0);

        return enrollments;
    }

    /**
     * Get teacher performance list
     */
    private List<Map<String, Object>> getTeacherPerformanceList(List<UserAccount> teachers, List<Course> allCourses) {
        List<Map<String, Object>> performance = new ArrayList<>();

        for (UserAccount teacher : teachers) {
            List<Course> teacherCourses = allCourses.stream()
                    .filter(c -> c.getInstructor().getId().equals(teacher.getId()))
                    .collect(Collectors.toList());

            long enrollments = 0;
            BigDecimal revenue = BigDecimal.ZERO;
            double avgRating = 0.0;
            int ratingCount = 0;

            for (Course course : teacherCourses) {
                enrollments += enrollmentRepository.findByCourse(course).size();

                List<CoursePayment> payments = paymentRepository.findByCourse(course);
                revenue = revenue.add(payments.stream()
                        .filter(p -> "SUCCESS".equals(p.getStatus()))
                        .map(CoursePayment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

                List<CourseReview> reviews = reviewRepository.findByCourseIdOrderByCreatedAtDesc(course.getId());
                if (!reviews.isEmpty()) {
                    double courseAvg = reviews.stream()
                            .filter(r -> r.getRating() != null)
                            .mapToInt(CourseReview::getRating)
                            .average()
                            .orElse(0.0);
                    avgRating = (avgRating * ratingCount + courseAvg * reviews.size()) / (ratingCount + reviews.size());
                    ratingCount += reviews.size();
                }
            }

            Map<String, Object> teacherStats = new HashMap<>();
            teacherStats.put("teacherId", teacher.getId());
            teacherStats.put("teacherName", teacher.getName() != null ? teacher.getName() : teacher.getEmail());
            teacherStats.put("email", teacher.getEmail());
            teacherStats.put("totalCourses", teacherCourses.size());
            teacherStats.put("totalEnrollments", enrollments);
            teacherStats.put("totalRevenue", revenue);
            teacherStats.put("averageRating", Math.round(avgRating * 100.0) / 100.0);
            teacherStats.put("ratingCount", ratingCount);

            performance.add(teacherStats);
        }

        return performance;
    }

    /**
     * Get course performance list
     */
    private List<Map<String, Object>> getCoursePerformanceList(List<Course> courses) {
        List<Map<String, Object>> performance = new ArrayList<>();

        for (Course course : courses) {
            List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
            List<CoursePayment> payments = paymentRepository.findByCourse(course);
            List<CourseReview> reviews = reviewRepository.findByCourseIdOrderByCreatedAtDesc(course.getId());

            BigDecimal revenue = payments.stream()
                    .filter(p -> "SUCCESS".equals(p.getStatus()))
                    .map(CoursePayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double avgRating = reviews.stream()
                    .filter(r -> r.getRating() != null)
                    .mapToInt(CourseReview::getRating)
                    .average()
                    .orElse(0.0);

            double avgProgress = enrollments.stream()
                    .mapToDouble(e -> e.getProgressPercentage() != null ? e.getProgressPercentage() : 0)
                    .average()
                    .orElse(0.0);

            Map<String, Object> courseStats = new HashMap<>();
            courseStats.put("courseId", course.getId());
            courseStats.put("courseTitle", course.getTitle());
            courseStats.put("status", course.getStatus().name());
            courseStats.put("totalEnrollments", enrollments.size());
            courseStats.put("totalRevenue", revenue);
            courseStats.put("averageRating", Math.round(avgRating * 100.0) / 100.0);
            courseStats.put("reviewCount", reviews.size());
            courseStats.put("averageProgress", Math.round(avgProgress * 100.0) / 100.0);

            performance.add(courseStats);
        }

        return performance;
    }

    /**
     * Get student analytics
     */
    private Map<String, Object> getStudentAnalytics(List<Course> courses) {
        Set<Long> studentIds = new HashSet<>();
        Map<String, Integer> enrollmentByStatus = new HashMap<>();
        double totalProgress = 0.0;
        int progressCount = 0;

        for (Course course : courses) {
            List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
            for (CourseEnrollment enrollment : enrollments) {
                studentIds.add(enrollment.getStudent().getId());
                String status = enrollment.getStatus() != null ? enrollment.getStatus() : "UNKNOWN";
                enrollmentByStatus.put(status, enrollmentByStatus.getOrDefault(status, 0) + 1);

                if (enrollment.getProgressPercentage() != null) {
                    totalProgress += enrollment.getProgressPercentage();
                    progressCount++;
                }
            }
        }

        Map<String, Object> studentAnalytics = new HashMap<>();
        studentAnalytics.put("uniqueStudents", studentIds.size());
        studentAnalytics.put("enrollmentByStatus", enrollmentByStatus);
        studentAnalytics.put("averageProgress", progressCount > 0 ? totalProgress / progressCount : 0.0);

        return studentAnalytics;
    }

    /**
     * Get student progress analytics
     */
    private Map<String, Object> getStudentProgressAnalytics(List<Course> courses) {
        Map<String, Integer> progressDistribution = new HashMap<>();
        progressDistribution.put("0-25%", 0);
        progressDistribution.put("26-50%", 0);
        progressDistribution.put("51-75%", 0);
        progressDistribution.put("76-100%", 0);

        int totalEnrollments = 0;
        double totalProgress = 0.0;

        for (Course course : courses) {
            List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
            for (CourseEnrollment enrollment : enrollments) {
                totalEnrollments++;
                Integer progress = enrollment.getProgressPercentage() != null ? enrollment.getProgressPercentage() : 0;
                totalProgress += progress;

                if (progress <= 25) {
                    progressDistribution.put("0-25%", progressDistribution.get("0-25%") + 1);
                } else if (progress <= 50) {
                    progressDistribution.put("26-50%", progressDistribution.get("26-50%") + 1);
                } else if (progress <= 75) {
                    progressDistribution.put("51-75%", progressDistribution.get("51-75%") + 1);
                } else {
                    progressDistribution.put("76-100%", progressDistribution.get("76-100%") + 1);
                }
            }
        }

        Map<String, Object> progressAnalytics = new HashMap<>();
        progressAnalytics.put("totalEnrollments", totalEnrollments);
        progressAnalytics.put("averageProgress", totalEnrollments > 0 ? totalProgress / totalEnrollments : 0.0);
        progressAnalytics.put("progressDistribution", progressDistribution);

        return progressAnalytics;
    }

    /**
     * Get assessment analytics (assignments and quizzes)
     */
    private Map<String, Object> getAssessmentAnalytics(List<Course> courses) {
        int totalAssignments = 0;
        int totalSubmissions = 0;
        int totalQuizzes = 0;
        int totalQuizAttempts = 0;
        double avgAssignmentScore = 0.0;
        double avgQuizScore = 0.0;
        int assignmentScoreCount = 0;
        int quizScoreCount = 0;

        for (Course course : courses) {
            List<Assignment> assignments = assignmentRepository.findByCourseId(course.getId());
            totalAssignments += assignments.size();

            for (Assignment assignment : assignments) {
                List<AssignmentSubmission> submissions = submissionRepository.findByAssignmentId(assignment.getId());
                totalSubmissions += submissions.size();

                Integer maxScore = assignment.getMaxScore();
                for (AssignmentSubmission submission : submissions) {
                    if (submission.getScore() != null && maxScore != null && maxScore > 0) {
                        double percentage = (double) submission.getScore() / maxScore * 100;
                        avgAssignmentScore = (avgAssignmentScore * assignmentScoreCount + percentage) / (assignmentScoreCount + 1);
                        assignmentScoreCount++;
                    }
                }
            }

            List<Quiz> quizzes = quizRepository.findByCourseId(course.getId());
            totalQuizzes += quizzes.size();

            for (Quiz quiz : quizzes) {
                List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quiz.getId());
                totalQuizAttempts += attempts.size();

                Integer totalMarks = quiz.getTotalMarks();
                for (QuizAttempt attempt : attempts) {
                    if (attempt.getScore() != null && totalMarks != null && totalMarks > 0) {
                        double percentage = (double) attempt.getScore() / totalMarks * 100;
                        avgQuizScore = (avgQuizScore * quizScoreCount + percentage) / (quizScoreCount + 1);
                        quizScoreCount++;
                    } else if (attempt.getPercentage() != null) {
                        // Use percentage if available
                        avgQuizScore = (avgQuizScore * quizScoreCount + attempt.getPercentage()) / (quizScoreCount + 1);
                        quizScoreCount++;
                    }
                }
            }
        }

        Map<String, Object> assessmentAnalytics = new HashMap<>();
        assessmentAnalytics.put("totalAssignments", totalAssignments);
        assessmentAnalytics.put("totalSubmissions", totalSubmissions);
        assessmentAnalytics.put("submissionRate", totalAssignments > 0 ? (double) totalSubmissions / totalAssignments : 0.0);
        assessmentAnalytics.put("averageAssignmentScore", Math.round(avgAssignmentScore * 100.0) / 100.0);
        assessmentAnalytics.put("totalQuizzes", totalQuizzes);
        assessmentAnalytics.put("totalQuizAttempts", totalQuizAttempts);
        assessmentAnalytics.put("averageQuizScore", Math.round(avgQuizScore * 100.0) / 100.0);

        return assessmentAnalytics;
    }

    /**
     * Get time-based trends (monthly data for last N months)
     */
    private Map<String, Object> getTrends(List<Course> courses, int months) {
        LocalDate now = LocalDate.now();
        Map<String, Long> enrollmentTrend = new LinkedHashMap<>();
        Map<String, BigDecimal> revenueTrend = new LinkedHashMap<>();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            String monthKey = monthStart.getYear() + "-" + String.format("%02d", monthStart.getMonthValue());

            long enrollments = 0;
            BigDecimal revenue = BigDecimal.ZERO;

            for (Course course : courses) {
                List<CourseEnrollment> courseEnrollments = enrollmentRepository.findByCourse(course);
                for (CourseEnrollment enrollment : courseEnrollments) {
                    if (enrollment.getEnrolledAt() != null) {
                        LocalDate enrolledDate = enrollment.getEnrolledAt().toLocalDate();
                        if (!enrolledDate.isBefore(monthStart) && !enrolledDate.isAfter(monthEnd)) {
                            enrollments++;
                        }
                    }
                }

                List<CoursePayment> payments = paymentRepository.findByCourse(course);
                for (CoursePayment payment : payments) {
                    if ("SUCCESS".equals(payment.getStatus()) && payment.getCreatedAt() != null) {
                        LocalDate paymentDate = payment.getCreatedAt().toLocalDate();
                        if (!paymentDate.isBefore(monthStart) && !paymentDate.isAfter(monthEnd)) {
                            revenue = revenue.add(payment.getAmount());
                        }
                    }
                }
            }

            enrollmentTrend.put(monthKey, enrollments);
            revenueTrend.put(monthKey, revenue);
        }

        Map<String, Object> trends = new HashMap<>();
        trends.put("enrollmentTrend", enrollmentTrend);
        trends.put("revenueTrend", revenueTrend);
        trends.put("period", months + " months");

        return trends;
    }

    /**
     * Get export-ready data for CSV/PDF generation
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getExportData(Long organizationId, String type) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        List<Course> courses = courseRepository.findByOrganizationId(organizationId);
        List<Map<String, Object>> exportData = new ArrayList<>();

        if ("enrollments".equals(type)) {
            for (Course course : courses) {
                List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
                for (CourseEnrollment enrollment : enrollments) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("courseTitle", course.getTitle());
                    row.put("studentName", enrollment.getStudent().getName() != null 
                            ? enrollment.getStudent().getName() 
                            : enrollment.getStudent().getEmail());
                    row.put("studentEmail", enrollment.getStudent().getEmail());
                    row.put("enrolledDate", enrollment.getEnrolledAt() != null 
                            ? enrollment.getEnrolledAt().toString() 
                            : "");
                    row.put("status", enrollment.getStatus());
                    row.put("progress", enrollment.getProgressPercentage() != null 
                            ? enrollment.getProgressPercentage() + "%" 
                            : "0%");
                    exportData.add(row);
                }
            }
        } else if ("revenue".equals(type)) {
            for (Course course : courses) {
                List<CoursePayment> payments = paymentRepository.findByCourse(course);
                for (CoursePayment payment : payments) {
                    if ("SUCCESS".equals(payment.getStatus())) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("courseTitle", course.getTitle());
                        row.put("studentEmail", payment.getStudent().getEmail());
                        row.put("amount", payment.getAmount().toString());
                        row.put("paymentDate", payment.getCreatedAt() != null 
                                ? payment.getCreatedAt().toString() 
                                : "");
                        row.put("paymentMethod", "Razorpay"); // Default payment method
                        exportData.add(row);
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("organizationName", organization.getName());
        result.put("exportType", type);
        result.put("data", exportData);
        result.put("generatedAt", LocalDateTime.now().toString());

        return result;
    }
}

