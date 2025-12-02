package com.lms.web;

import com.lms.domain.LiveSession;
import com.lms.repository.UserAccountRepository;
import com.lms.service.LiveSessionService;
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
@RequestMapping("/api/lms/live-sessions")
public class LiveSessionController {

    @Autowired
    private LiveSessionService liveSessionService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping
    public ResponseEntity<?> getUpcomingSessions(
            @AuthenticationPrincipal User principal,
            @RequestParam(value = "courseId", required = false) Long courseId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }
            
            List<LiveSession> sessions;
            if (courseId != null) {
                sessions = liveSessionService.getUpcomingSessionsForStudent(courseId);
            } else {
                sessions = liveSessionService.getUpcomingSessionsForStudent(null);
            }
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/instructor")
    public ResponseEntity<?> getMySessions(@AuthenticationPrincipal User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }
            
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<LiveSession> sessions = liveSessionService.getSessionsByInstructor(user.getId());
            
            log.debug("Found {} sessions for instructor {}", sessions.size(), user.getId());
            
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Failed to load instructor sessions for user {}: {}", principal != null ? principal.getUsername() : "unknown", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getSessionsByCourse(@PathVariable("courseId") Long courseId) {
        try {
            List<LiveSession> sessions = liveSessionService.getSessionsByCourse(courseId);
            log.debug("Found {} sessions for course {}", sessions.size(), courseId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Failed to load live sessions for course {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<LiveSession> getSessionById(@PathVariable("id") Long id) {
        return liveSessionService.getSessionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSession(
            @AuthenticationPrincipal User principal,
            @RequestBody CreateSessionRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }
            
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (request.title() == null || request.title().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Session title is required"));
            }
            
            if (request.scheduledDateTime() == null || request.scheduledDateTime().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Scheduled date and time is required"));
            }
            
            // Parse the scheduled date time
            LocalDateTime scheduledDateTime;
            try {
                String dateTimeStr = request.scheduledDateTime().trim();
                log.debug("Received live session scheduledDateTime string: {}", dateTimeStr);
                
                // Handle different date formats
                if (dateTimeStr.contains("T")) {
                    // ISO format: "2025-11-14T17:00" or "2025-11-14T17:00:00"
                    // Remove timezone info if present
                    if (dateTimeStr.contains("Z")) {
                        dateTimeStr = dateTimeStr.replace("Z", "");
                    }
                    if (dateTimeStr.contains("+")) {
                        dateTimeStr = dateTimeStr.substring(0, dateTimeStr.indexOf("+"));
                    }
                    if (dateTimeStr.contains("-") && dateTimeStr.lastIndexOf("-") > 10) {
                        // Has timezone offset like -05:00
                        int tzIndex = dateTimeStr.lastIndexOf("-");
                        if (tzIndex > 10) {
                            dateTimeStr = dateTimeStr.substring(0, tzIndex);
                        }
                    }
                    // Count colons in the entire string to determine format
                    long totalColons = dateTimeStr.chars().filter(ch -> ch == ':').count();
                    if (totalColons == 1) {
                        // Format: "2025-11-14T17:00" - add seconds
                        dateTimeStr = dateTimeStr + ":00";
                    }
                    
                    scheduledDateTime = LocalDateTime.parse(dateTimeStr);
                    log.debug("Parsed scheduledDateTime successfully: {}", scheduledDateTime);
                } else {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format. Please use format: YYYY-MM-DDTHH:MM"));
                }
            } catch (Exception e) {
                log.error("Failed to parse scheduledDateTime '{}' for live session: {}", request.scheduledDateTime(), e.getMessage(), e);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format. Please use format: YYYY-MM-DDTHH:MM. Error: " + e.getMessage()));
            }
            
            // Validate scheduled date is not in the past
            if (scheduledDateTime.isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Scheduled date and time cannot be in the past"));
            }
            
            LiveSession session = new LiveSession();
            session.setTitle(request.title());
            session.setDescription(request.description());
            session.setScheduledDateTime(scheduledDateTime);
            session.setDurationMinutes(request.durationMinutes() != null ? request.durationMinutes() : 60);
            session.setMaxParticipants(request.maxParticipants() != null ? request.maxParticipants() : 50);

            LiveSession created = liveSessionService.createLiveSession(
                    session, 
                    user.getId(), 
                    request.courseId()
            );
            
            log.info("Live session created successfully with ID {} for course {}", created.getId(), request.courseId());
            
            return ResponseEntity.ok(Map.of(
                    "id", created.getId(),
                    "meetingLink", created.getMeetingLink() != null ? created.getMeetingLink() : "",
                    "meetingId", created.getMeetingId() != null ? created.getMeetingId() : "",
                    "message", "Live session created successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Failed to create live session (validation/runtime error) for user {}: {}", principal != null ? principal.getUsername() : "unknown", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to create live session"));
        } catch (Exception e) {
            log.error("Unexpected error while creating live session for user {}: {}", principal != null ? principal.getUsername() : "unknown", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred: " + (e.getMessage() != null ? e.getMessage() : "Unknown error")));
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startSession(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long id) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            LiveSession session = liveSessionService.getSessionById(id)
                    .orElseThrow(() -> new RuntimeException("Session not found"));
            
            if (!session.getInstructor().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
            }

            liveSessionService.startSession(id);
            return ResponseEntity.ok(Map.of("message", "Session started"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<Map<String, Object>> endSession(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long id) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            LiveSession session = liveSessionService.getSessionById(id)
                    .orElseThrow(() -> new RuntimeException("Session not found"));
            
            if (!session.getInstructor().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
            }

            liveSessionService.endSession(id);
            return ResponseEntity.ok(Map.of("message", "Session ended"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    record CreateSessionRequest(String title, String description, 
                               String scheduledDateTime, // Accept as String and parse manually
                               Integer durationMinutes, Integer maxParticipants, Long courseId) {}
}

