package com.lms.web;

import com.lms.domain.CourseAnnouncement;
import com.lms.repository.UserAccountRepository;
import com.lms.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/lms/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getAnnouncementsByCourse(@PathVariable("courseId") Long courseId) {
        try {
            log.debug("Fetching announcements for course {}", courseId);
            List<CourseAnnouncement> announcements = announcementService.getAnnouncementsByCourse(courseId);
            log.debug("Returning {} announcements for course {}", announcements.size(), courseId);
            return ResponseEntity.ok(announcements);
        } catch (Exception e) {
            log.error("Failed to load announcements for course {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load announcements"));
        }
    }

    @PostMapping("/course/{courseId}")
    public ResponseEntity<?> createAnnouncement(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId,
            @RequestBody CreateAnnouncementRequest request) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            CourseAnnouncement announcement = new CourseAnnouncement();
            announcement.setTitle(request.title());
            announcement.setContent(request.content());
            announcement.setIsImportant(request.isImportant() != null ? request.isImportant() : false);
            if (request.expiresAt() != null) {
                announcement.setExpiresAt(request.expiresAt());
            }

            CourseAnnouncement created = announcementService.createAnnouncement(courseId, user.getId(), announcement);
            return ResponseEntity.ok(Map.of("id", created.getId(), "message", "Announcement created successfully"));
        } catch (Exception e) {
            log.error("Failed to create announcement for course {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to create announcement"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long id) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            announcementService.deleteAnnouncement(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Announcement deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete announcement {} for user {}: {}", id,
                    principal != null ? principal.getUsername() : "unknown", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to delete announcement"));
        }
    }

    record CreateAnnouncementRequest(String title, String content, Boolean isImportant, LocalDateTime expiresAt) {}
}



