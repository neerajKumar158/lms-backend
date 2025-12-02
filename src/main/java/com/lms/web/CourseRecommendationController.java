package com.lms.web;

import com.lms.domain.Course;
import com.lms.repository.UserAccountRepository;
import com.lms.service.CourseRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lms/recommendations")
public class CourseRecommendationController {

    @Autowired
    private CourseRecommendationService recommendationService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getRecommendedCourses(@AuthenticationPrincipal User principal) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<Course> recommendations = recommendationService.getRecommendedCourses(user.getId());
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/trending")
    public ResponseEntity<List<Course>> getTrendingCourses(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Course> trending = recommendationService.getTrendingCourses(limit);
            return ResponseEntity.ok(trending);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}




