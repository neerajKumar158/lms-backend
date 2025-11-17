package com.lms.web;

import com.lms.domain.LiveSession;
import com.lms.repository.UserAccountRepository;
import com.lms.service.LiveSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
            
            System.out.println("Found " + sessions.size() + " sessions for instructor: " + user.getId());
            if (!sessions.isEmpty()) {
                System.out.println("First session ID: " + sessions.get(0).getId() + ", Title: " + sessions.get(0).getTitle());
            }
            
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getSessionsByCourse(@PathVariable("courseId") Long courseId) {
        try {
            List<LiveSession> sessions = liveSessionService.getSessionsByCourse(courseId);
            System.out.println("Found " + sessions.size() + " sessions for course: " + courseId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            e.printStackTrace();
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
                System.out.println("Received dateTime string: " + dateTimeStr);
                
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
                    
                    System.out.println("Parsing dateTime: " + dateTimeStr);
                    scheduledDateTime = LocalDateTime.parse(dateTimeStr);
                    System.out.println("Parsed successfully: " + scheduledDateTime);
                } else {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format. Please use format: YYYY-MM-DDTHH:MM"));
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            
            System.out.println("Live session created successfully with ID: " + created.getId());
            
            return ResponseEntity.ok(Map.of(
                    "id", created.getId(),
                    "meetingLink", created.getMeetingLink() != null ? created.getMeetingLink() : "",
                    "meetingId", created.getMeetingId() != null ? created.getMeetingId() : "",
                    "message", "Live session created successfully"
            ));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to create live session"));
        } catch (Exception e) {
            e.printStackTrace();
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

