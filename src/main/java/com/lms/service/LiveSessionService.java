package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import com.lms.repository.CourseEnrollmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class LiveSessionService {

    @Autowired
    private LiveSessionRepository liveSessionRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;

    @Value("${app.base-url:http://localhost:9191}")
    private String baseUrl;

    @Transactional
    public LiveSession createLiveSession(LiveSession session, Long instructorId, Long courseId) {
        UserAccount instructor = userAccountRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        
        session.setInstructor(instructor);
        
        if (courseId != null) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            session.setCourse(course);
        }

        // Generate Jitsi Meet room URL
        String meetingId = generateMeetingId();
        session.setMeetingId(meetingId);
        session.setMeetingLink("https://meet.jit.si/" + meetingId);
        session.setStatus("SCHEDULED");

        LiveSession saved = liveSessionRepository.save(session);

        // Send reminder emails to enrolled students (if session is scheduled for future)
        try {
            if (emailNotificationService != null && saved.getCourse() != null && 
                saved.getScheduledDateTime() != null && 
                saved.getScheduledDateTime().isAfter(LocalDateTime.now())) {
                List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(saved.getCourse());
                for (CourseEnrollment enrollment : enrollments) {
                    emailNotificationService.sendLiveSessionReminderEmail(
                        enrollment.getStudent().getId(),
                        saved.getCourse().getTitle(),
                        saved.getTitle() != null ? saved.getTitle() : "Live Session",
                        saved.getScheduledDateTime()
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to send live session reminder emails for session {} and course {}: {}",
                    saved.getId(), courseId, e.getMessage(), e);
        }

        return saved;
    }

    private String generateMeetingId() {
        // Generate a unique meeting ID for Jitsi Meet
        // Format: LMS-{UUID}
        return "LMS-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public List<LiveSession> getUpcomingSessions(Long instructorId) {
        userAccountRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        
        LocalDateTime now = LocalDateTime.now();
        return liveSessionRepository.findByStatusAndScheduledDateTimeAfter("SCHEDULED", now);
    }

    public List<LiveSession> getSessionsByCourse(Long courseId) {
        return liveSessionRepository.findByCourseId(courseId);
    }

    public List<LiveSession> getSessionsByInstructor(Long instructorId) {
        UserAccount instructor = userAccountRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        return liveSessionRepository.findByInstructor(instructor);
    }

    @Transactional
    public void startSession(Long sessionId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        session.setStatus("ONGOING");
        session.setStartedAt(LocalDateTime.now());
        liveSessionRepository.save(session);
    }

    @Transactional
    public void endSession(Long sessionId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        session.setStatus("COMPLETED");
        session.setEndedAt(LocalDateTime.now());
        liveSessionRepository.save(session);
    }

    public Optional<LiveSession> getSessionById(Long sessionId) {
        return liveSessionRepository.findById(sessionId);
    }

    public List<LiveSession> getUpcomingSessionsForStudent(Long courseId) {
        if (courseId == null) {
            // Get all SCHEDULED and ONGOING sessions across all courses
            // Show all SCHEDULED sessions (not just future ones) so students can see what's coming
            List<LiveSession> scheduled = liveSessionRepository.findByStatus("SCHEDULED");
            List<LiveSession> ongoing = liveSessionRepository.findByStatus("ONGOING");
            
            // Combine and remove duplicates
            java.util.Set<Long> seenIds = new java.util.HashSet<>();
            List<LiveSession> result = new java.util.ArrayList<>();
            
            for (LiveSession session : scheduled) {
                if (!seenIds.contains(session.getId())) {
                    result.add(session);
                    seenIds.add(session.getId());
                }
            }
            
            for (LiveSession session : ongoing) {
                if (!seenIds.contains(session.getId())) {
                    result.add(session);
                    seenIds.add(session.getId());
                }
            }
            
            // Sort by scheduled date (null dates go to end)
            result.sort((s1, s2) -> {
                if (s1.getScheduledDateTime() == null && s2.getScheduledDateTime() == null) return 0;
                if (s1.getScheduledDateTime() == null) return 1;
                if (s2.getScheduledDateTime() == null) return -1;
                return s1.getScheduledDateTime().compareTo(s2.getScheduledDateTime());
            });
            
            log.debug("Found {} sessions for student dashboard (all courses)", result.size());
            return result;
        } else {
            // Get sessions for specific course
            List<LiveSession> sessions = liveSessionRepository.findByCourseId(courseId);
            List<LiveSession> filtered = sessions.stream()
                    .filter(s -> {
                        // Show all SCHEDULED and ONGOING sessions
                        if ("ONGOING".equals(s.getStatus())) {
                            return true;
                        }
                        if ("SCHEDULED".equals(s.getStatus())) {
                            return true; // Show all scheduled sessions, not just future ones
                        }
                        return false;
                    })
                    .sorted((s1, s2) -> {
                        if (s1.getScheduledDateTime() == null && s2.getScheduledDateTime() == null) return 0;
                        if (s1.getScheduledDateTime() == null) return 1;
                        if (s2.getScheduledDateTime() == null) return -1;
                        return s1.getScheduledDateTime().compareTo(s2.getScheduledDateTime());
                    })
                    .toList();
            
            log.debug("Found {} sessions for course {}", filtered.size(), courseId);
            return filtered;
        }
    }
}

