package com.lms.web;

import com.lms.domain.CourseReview;
import com.lms.repository.UserAccountRepository;
import com.lms.service.CourseReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lms/reviews")
public class CourseReviewController {

    @Autowired
    private CourseReviewService reviewService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @PostMapping("/course/{courseId}")
    public ResponseEntity<?> createOrUpdateReview(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId,
            @RequestBody CreateReviewRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (request.rating() < 1 || request.rating() > 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "Rating must be between 1 and 5"));
            }

            CourseReview review = reviewService.createOrUpdateReview(
                    courseId, user.getId(), request.rating(), request.reviewText());

            return ResponseEntity.ok(Map.of(
                    "id", review.getId(),
                    "message", "Review submitted successfully"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseReviews(
            @PathVariable("courseId") Long courseId,
            @RequestParam(required = false, defaultValue = "false") boolean includeHidden) {
        try {
            List<CourseReview> reviews = reviewService.getCourseReviews(courseId, includeHidden);
            Map<String, Object> summary = reviewService.getCourseReviewSummary(courseId);
            
            return ResponseEntity.ok(Map.of(
                    "reviews", reviews,
                    "summary", summary
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}/summary")
    public ResponseEntity<?> getCourseReviewSummary(@PathVariable("courseId") Long courseId) {
        try {
            Map<String, Object> summary = reviewService.getCourseReviewSummary(courseId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @AuthenticationPrincipal User principal,
            @PathVariable("reviewId") Long reviewId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            reviewService.deleteReview(reviewId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    record CreateReviewRequest(Integer rating, String reviewText) {}
}



