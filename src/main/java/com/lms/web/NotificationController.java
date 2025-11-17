package com.lms.web;

import com.lms.domain.Notification;
import com.lms.repository.UserAccountRepository;
import com.lms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lms/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal User principal) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Notification> notifications = notificationService.getUserNotifications(user.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load notifications"));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(@AuthenticationPrincipal User principal) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Notification> notifications = notificationService.getUnreadNotifications(user.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load notifications"));
        }
    }

    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal User principal) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Long count = notificationService.getUnreadCount(user.getId());
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load count"));
        }
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long id) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            notificationService.markAsRead(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to mark as read"));
        }
    }

    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal User principal) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            notificationService.markAllAsRead(user.getId());
            return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to mark all as read"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long id) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            notificationService.deleteNotification(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to delete notification"));
        }
    }
}



