package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled Reminder Service
 * Automatically sends reminders for live sessions and assignment deadlines
 */
@Service
public class ScheduledReminderService {

    @Autowired
    private LiveSessionRepository liveSessionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;

    /**
     * Send reminders for live sessions scheduled in the next 24 hours
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * ?") // Every hour at minute 0
    @Transactional
    public void sendLiveSessionReminders() {
        if (emailNotificationService == null) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusHours(24);

        // Find live sessions scheduled between now and 24 hours from now
        List<LiveSession> upcomingSessions = liveSessionRepository.findAll().stream()
                .filter(session -> session.getStatus() != null && 
                        "SCHEDULED".equals(session.getStatus()))
                .filter(session -> session.getScheduledDateTime() != null &&
                        session.getScheduledDateTime().isAfter(now) &&
                        session.getScheduledDateTime().isBefore(tomorrow))
                .filter(session -> session.getCourse() != null)
                .toList();

        for (LiveSession session : upcomingSessions) {
            // Get all enrolled students
            List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(session.getCourse());
            
            for (CourseEnrollment enrollment : enrollments) {
                if ("ACTIVE".equals(enrollment.getStatus())) {
                    try {
                        emailNotificationService.sendLiveSessionReminderEmail(
                                enrollment.getStudent().getId(),
                                session.getCourse().getTitle(),
                                session.getTitle() != null ? session.getTitle() : "Live Session",
                                session.getScheduledDateTime()
                        );
                    } catch (Exception e) {
                        System.err.println("Failed to send live session reminder: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Send reminders for assignments with deadlines in the next 48 hours
     * Runs every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * ?") // Every 6 hours
    @Transactional
    public void sendAssignmentDeadlineReminders() {
        if (emailNotificationService == null) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysLater = now.plusHours(48);

        // Find assignments with deadlines between now and 48 hours from now
        List<Assignment> upcomingAssignments = assignmentRepository.findAll().stream()
                .filter(assignment -> assignment.getDueDate() != null &&
                        assignment.getDueDate().isAfter(now) &&
                        assignment.getDueDate().isBefore(twoDaysLater))
                .filter(assignment -> assignment.getCourse() != null)
                .toList();

        for (Assignment assignment : upcomingAssignments) {
            // Get all enrolled students who haven't submitted
            List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(assignment.getCourse());
            
            for (CourseEnrollment enrollment : enrollments) {
                if ("ACTIVE".equals(enrollment.getStatus())) {
                    // Check if student has already submitted
                    boolean hasSubmitted = assignment.getSubmissions() != null &&
                            assignment.getSubmissions().stream()
                                    .anyMatch(s -> s.getStudent().getId().equals(enrollment.getStudent().getId()));
                    
                    if (!hasSubmitted) {
                        try {
                            emailNotificationService.sendAssignmentDeadlineReminderEmail(
                                    enrollment.getStudent().getId(),
                                    assignment.getCourse().getTitle(),
                                    assignment.getTitle(),
                                    assignment.getDueDate()
                            );
                        } catch (Exception e) {
                            System.err.println("Failed to send assignment reminder: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Update live session statuses (mark as ONGOING or COMPLETED)
     * Runs every 5 minutes
     */
    @Scheduled(cron = "0 */5 * * * ?") // Every 5 minutes
    @Transactional
    public void updateLiveSessionStatuses() {
        LocalDateTime now = LocalDateTime.now();

        List<LiveSession> sessions = liveSessionRepository.findAll();
        
        for (LiveSession session : sessions) {
            if (session.getScheduledDateTime() == null) continue;

            LocalDateTime startTime = session.getScheduledDateTime();
            LocalDateTime endTime = startTime.plusMinutes(
                    session.getDurationMinutes() != null ? session.getDurationMinutes() : 60);

            if ("SCHEDULED".equals(session.getStatus())) {
                // If session has started but not ended, mark as ONGOING
                if (now.isAfter(startTime) && now.isBefore(endTime)) {
                    session.setStatus("ONGOING");
                    liveSessionRepository.save(session);
                }
            } else if ("ONGOING".equals(session.getStatus())) {
                // If session has ended, mark as COMPLETED
                if (now.isAfter(endTime)) {
                    session.setStatus("COMPLETED");
                    liveSessionRepository.save(session);
                }
            }
        }
    }
}

