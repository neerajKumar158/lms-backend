package com.lms.web;

import com.lms.domain.Course;
import com.lms.domain.CourseCategory;
import com.lms.domain.Lecture;
import com.lms.repository.UserAccountRepository;
import com.lms.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/lms/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping
    public ResponseEntity<?> getAllPublishedCourses() {
        try {
            List<Course> courses = courseService.getAllPublishedCourses();
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            log.error("Error in getAllPublishedCourses: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to load courses",
                "message", e.getMessage() != null ? e.getMessage() : "Unknown error"
            ));
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedCourses() {
        try {
            List<Course> courses = courseService.getFeaturedCourses();
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            log.error("Error in getFeaturedCourses: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to load featured courses",
                "message", e.getMessage() != null ? e.getMessage() : "Unknown error"
            ));
        }
    }

    @GetMapping("/free")
    public ResponseEntity<?> getFreeCourses() {
        try {
            List<Course> courses = courseService.getFreeCourses();
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            log.error("Error in getFreeCourses: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to load free courses",
                "message", e.getMessage() != null ? e.getMessage() : "Unknown error"
            ));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCourses(@RequestParam String keyword) {
        try {
            List<Course> courses = courseService.searchCourses(keyword);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            log.error("Error in searchCourses with keyword '{}': {}", keyword, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to search courses",
                "message", e.getMessage() != null ? e.getMessage() : "Unknown error"
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable("id") Long id) {
        try {
            log.debug("Loading course with ID: {}", id);
            Optional<Course> courseOpt = courseService.getCourseById(id);
            if (courseOpt.isPresent()) {
                Course course = courseOpt.get();
                log.debug("Course found: {}", course.getTitle());
                return ResponseEntity.ok(course);
            } else {
                log.debug("Course not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error in getCourseById for ID {}: {}", id, e.getMessage(), e);
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = e.getClass().getSimpleName() + " occurred";
            }
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to load course", 
                "message", errorMessage,
                "type", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/category/{categoryId}")
    public List<Course> getCoursesByCategory(@PathVariable("categoryId") Long categoryId) {
        return courseService.getCoursesByCategory(categoryId);
    }

    @GetMapping("/instructor/{instructorId}")
    public List<Course> getCoursesByInstructor(@PathVariable("instructorId") Long instructorId) {
        return courseService.getCoursesByInstructor(instructorId);
    }

    @GetMapping("/my-courses")
    public ResponseEntity<?> getMyCourses(@AuthenticationPrincipal User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }
            
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Course> courses = courseService.getCoursesByInstructor(user.getId());
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/lectures")
    public ResponseEntity<List<Lecture>> getCourseLectures(@PathVariable("id") Long id) {
        List<Lecture> lectures = courseService.getCourseLectures(id);
        // Filter to show only free lectures for non-authenticated users
        // For authenticated users, show all lectures if enrolled
        return ResponseEntity.ok(lectures);
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> createCourse(
            @AuthenticationPrincipal User principal,
            @RequestBody CreateCourseRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Course course = new Course();
            course.setTitle(request.title());
            course.setDescription(request.description());
            course.setPrice(new BigDecimal(request.price()));
            if (request.level() != null) {
                try {
                    course.setLevel(Course.CourseLevel.valueOf(request.level().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid course level"));
                }
            }
            course.setThumbnailUrl(request.thumbnailUrl());
            
            if (request.categoryId() != null) {
                courseService.getAllCategories().stream()
                        .filter(c -> c.getId().equals(request.categoryId()))
                        .findFirst()
                        .ifPresent(course::setCategory);
            }

            Course created = courseService.createCourse(course, user.getId());
            return ResponseEntity.ok(Map.of("id", created.getId(), "message", "Course created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCourse(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long id,
            @RequestBody CreateCourseRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Course course = courseService.getCourseById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            
            // Check if user is the instructor
            if (!course.getInstructor().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
            }

            Course updated = new Course();
            updated.setTitle(request.title());
            updated.setDescription(request.description());
            updated.setPrice(new BigDecimal(request.price()));
            if (request.level() != null) {
                try {
                    updated.setLevel(Course.CourseLevel.valueOf(request.level().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid course level"));
                }
            }
            updated.setThumbnailUrl(request.thumbnailUrl());
            
            if (request.categoryId() != null) {
                courseService.getAllCategories().stream()
                        .filter(c -> c.getId().equals(request.categoryId()))
                        .findFirst()
                        .ifPresent(updated::setCategory);
            }

            courseService.updateCourse(id, updated);
            return ResponseEntity.ok(Map.of("message", "Course updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishCourse(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long id) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Course course = courseService.getCourseById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            
            if (!course.getInstructor().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
            }

            courseService.publishCourse(id);
            return ResponseEntity.ok(Map.of("message", "Course published successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Map<String, Object>> unpublishCourse(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long id) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Course course = courseService.getCourseById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            
            if (!course.getInstructor().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
            }

            courseService.unpublishCourse(id);
            return ResponseEntity.ok(Map.of("message", "Course unpublished successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCourse(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long id) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Course course = courseService.getCourseById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            
            if (!course.getInstructor().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
            }

            courseService.deleteCourse(id);
            return ResponseEntity.ok(Map.of("message", "Course deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public List<CourseCategory> getAllCategories() {
        return courseService.getAllCategories();
    }

    @PostMapping("/{id}/lectures")
    public ResponseEntity<Map<String, Object>> addLecture(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long id,
            @RequestBody CreateLectureRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Course course = courseService.getCourseById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            
            if (!course.getInstructor().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
            }

            Lecture lecture = new Lecture();
            lecture.setTitle(request.title());
            lecture.setDescription(request.description());
            lecture.setSequenceOrder(request.sequenceOrder());
            lecture.setDurationMinutes(request.durationMinutes());
            lecture.setIsFree(request.isFree());

            Lecture created = courseService.addLectureToCourse(id, lecture);
            return ResponseEntity.ok(Map.of("id", created.getId(), "message", "Lecture added successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    record CreateCourseRequest(String title, String description, String price, String level, 
                              String thumbnailUrl, Long categoryId) {}
    record CreateLectureRequest(String title, String description, Integer sequenceOrder, 
                               Integer durationMinutes, Boolean isFree) {}
}
