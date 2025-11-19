package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Course Recommendation Service
 * Provides personalized course recommendations based on user behavior
 */
@Service
public class CourseRecommendationService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseReviewRepository reviewRepository;

    @Autowired
    private CourseCategoryRepository categoryRepository;

    @Autowired
    private CourseWishlistRepository wishlistRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    /**
     * Get recommended courses for a user
     */
    @Transactional(readOnly = true)
    public List<Course> getRecommendedCourses(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Course> recommendedCourses = new ArrayList<>();
        Set<Long> excludedCourseIds = new HashSet<>();

        // Get courses user is already enrolled in
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent(user);
        excludedCourseIds.addAll(enrollments.stream()
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet()));

        // Strategy 1: Based on enrolled course categories
        Set<Long> preferredCategoryIds = enrollments.stream()
                .map(e -> e.getCourse().getCategory())
                .filter(Objects::nonNull)
                .map(CourseCategory::getId)
                .collect(Collectors.toSet());

        if (!preferredCategoryIds.isEmpty()) {
            List<Course> categoryBasedCourses = courseRepository.findAll().stream()
                    .filter(c -> c.getStatus() == Course.CourseStatus.PUBLISHED)
                    .filter(c -> !excludedCourseIds.contains(c.getId()))
                    .filter(c -> c.getCategory() != null && preferredCategoryIds.contains(c.getCategory().getId()))
                    .sorted(Comparator.comparing((Course c) -> {
                        // Sort by rating if available
                        List<CourseReview> reviews = reviewRepository.findByCourseIdOrderByCreatedAtDesc(c.getId());
                        if (reviews.isEmpty()) return 0.0;
                        return reviews.stream()
                                .filter(r -> r.getRating() != null)
                                .mapToInt(CourseReview::getRating)
                                .average()
                                .orElse(0.0);
                    }).reversed())
                    .limit(5)
                    .collect(Collectors.toList());
            recommendedCourses.addAll(categoryBasedCourses);
            excludedCourseIds.addAll(categoryBasedCourses.stream()
                    .map(Course::getId)
                    .collect(Collectors.toSet()));
        }

        // Strategy 2: Popular courses (by enrollment count)
        Map<Course, Long> courseEnrollmentCounts = enrollmentRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        CourseEnrollment::getCourse,
                        Collectors.counting()
                ));

        List<Course> popularCourses = courseEnrollmentCounts.entrySet().stream()
                .filter(entry -> !excludedCourseIds.contains(entry.getKey().getId()))
                .filter(entry -> entry.getKey().getStatus() == Course.CourseStatus.PUBLISHED)
                .sorted(Map.Entry.<Course, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        recommendedCourses.addAll(popularCourses);
        excludedCourseIds.addAll(popularCourses.stream()
                .map(Course::getId)
                .collect(Collectors.toSet()));

        // Strategy 3: Highly rated courses
        List<Course> highlyRatedCourses = courseRepository.findAll().stream()
                .filter(c -> c.getStatus() == Course.CourseStatus.PUBLISHED)
                .filter(c -> !excludedCourseIds.contains(c.getId()))
                .filter(c -> {
                    List<CourseReview> reviews = reviewRepository.findByCourseIdOrderByCreatedAtDesc(c.getId());
                    if (reviews.isEmpty()) return false;
                    double avgRating = reviews.stream()
                            .filter(r -> r.getRating() != null)
                            .mapToInt(CourseReview::getRating)
                            .average()
                            .orElse(0.0);
                    return avgRating >= 4.0; // 4+ star rating
                })
                .sorted(Comparator.comparing((Course c) -> {
                    List<CourseReview> reviews = reviewRepository.findByCourseIdOrderByCreatedAtDesc(c.getId());
                    return reviews.stream()
                            .filter(r -> r.getRating() != null)
                            .mapToInt(CourseReview::getRating)
                            .average()
                            .orElse(0.0);
                }).reversed())
                .limit(5)
                .collect(Collectors.toList());
        recommendedCourses.addAll(highlyRatedCourses);

        // Remove duplicates and limit to 10
        return recommendedCourses.stream()
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Get trending courses (courses with recent enrollments)
     */
    @Transactional(readOnly = true)
    public List<Course> getTrendingCourses(int limit) {
        // Get enrollments from last 30 days
        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        
        Map<Course, Long> recentEnrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getEnrolledAt() != null && e.getEnrolledAt().isAfter(thirtyDaysAgo))
                .collect(Collectors.groupingBy(
                        CourseEnrollment::getCourse,
                        Collectors.counting()
                ));

        return recentEnrollments.entrySet().stream()
                .filter(entry -> entry.getKey().getStatus() == Course.CourseStatus.PUBLISHED)
                .sorted(Map.Entry.<Course, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}

