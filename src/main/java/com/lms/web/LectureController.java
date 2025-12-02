package com.lms.web;

import com.lms.domain.StudyMaterial;
import com.lms.repository.UserAccountRepository;
import com.lms.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/lms/lectures")
public class LectureController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping("/{id}/materials")
    public ResponseEntity<List<StudyMaterial>> getLectureMaterials(@PathVariable("id") Long id) {
        List<StudyMaterial> materials = courseService.getLectureMaterials(id);
        return ResponseEntity.ok(materials);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLecture(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long id) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Verify user is the instructor of the course that contains this lecture
            // This check is done in the service layer
            courseService.deleteLecture(id);
            return ResponseEntity.ok(Map.of("message", "Lecture deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete lecture {} for user {}: {}", id,
                    principal != null ? principal.getUsername() : "unknown", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
