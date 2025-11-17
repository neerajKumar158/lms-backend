package com.lms.service;

import com.lms.domain.Course;
import com.lms.domain.CourseEnrollment;
import com.lms.domain.CourseReview;
import com.lms.domain.UserAccount;
import com.lms.repository.CourseRepository;
import com.lms.repository.CourseReviewRepository;
import com.lms.repository.CourseEnrollmentRepository;
import com.lms.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CourseReviewService {

    @Autowired
    private CourseReviewRepository reviewRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Transactional
    public CourseReview createOrUpdateReview(Long courseId, Long studentId, Integer rating, String reviewText) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Check if student is enrolled
        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
        boolean isEnrolled = enrollments.stream()
                .anyMatch(e -> e.getStudent().getId().equals(studentId));
        if (!isEnrolled) {
            throw new RuntimeException("You must be enrolled in the course to review it");
        }

        Optional<CourseReview> existingReview = reviewRepository.findByCourseIdAndStudentId(courseId, studentId);
        
        CourseReview review;
        if (existingReview.isPresent()) {
            review = existingReview.get();
            review.setRating(rating);
            review.setReviewText(reviewText);
            review.setUpdatedAt(LocalDateTime.now());
        } else {
            review = new CourseReview();
            review.setCourse(course);
            review.setStudent(student);
            review.setRating(rating);
            review.setReviewText(reviewText);
        }

        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<CourseReview> getCourseReviews(Long courseId, boolean includeHidden) {
        if (includeHidden) {
            return reviewRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
        }
        return reviewRepository.findByCourseIdAndIsVisibleTrueOrderByCreatedAtDesc(courseId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCourseReviewSummary(Long courseId) {
        Double avgRating = reviewRepository.findAverageRatingByCourseId(courseId);
        Long totalReviews = reviewRepository.countByCourseId(courseId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        summary.put("totalReviews", totalReviews);
        summary.put("ratingDistribution", getRatingDistribution(courseId));

        return summary;
    }

    @Transactional(readOnly = true)
    private Map<Integer, Long> getRatingDistribution(Long courseId) {
        List<CourseReview> reviews = reviewRepository.findByCourseIdAndIsVisibleTrueOrderByCreatedAtDesc(courseId);
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            long count = reviews.stream().filter(r -> r.getRating().equals(rating)).count();
            distribution.put(rating, count);
        }
        return distribution;
    }

    @Transactional
    public void deleteReview(Long reviewId, Long studentId) {
        CourseReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Not authorized to delete this review");
        }

        reviewRepository.delete(review);
    }
}

