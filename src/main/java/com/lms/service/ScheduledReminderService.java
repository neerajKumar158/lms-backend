package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    @Autowired(required = false)
    private NotificationService notificationService;

    /**
     * Send reminders for live sessions scheduled in the next 24 hours
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * ?") // Every hour at minute 0
    @Transactional
    public void sendLiveSessionReminders() {
        if (emailNotificationService == null && notificationService == null) return;

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
                    Long studentId = enrollment.getStudent().getId();
                    try {
                        // Email reminder
                        if (emailNotificationService != null) {
                            emailNotificationService.sendLiveSessionReminderEmail(
                                    studentId,
                                    session.getCourse().getTitle(),
                                    session.getTitle() != null ? session.getTitle() : "Live Session",
                                    session.getScheduledDateTime()
                            );
                        }

                        // In-app notification
                        if (notificationService != null) {
                            notificationService.createNotification(
                                    studentId,
                                    "Upcoming Live Session",
                                    "You have a live session '" +
                                            (session.getTitle() != null ? session.getTitle() : "Live Session") +
                                            "' scheduled for " + session.getScheduledDateTime() + ".",
                                    Notification.NotificationType.INFO,
                                    "/ui/lms/live/" + session.getId()
                            );
                        }
                    } catch (Exception e) {
                        log.error("Failed to send live session reminder for session {} and student {}: {}",
                                session.getId(), studentId, e.getMessage(), e);
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
        if (emailNotificationService == null && notificationService == null) return;

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
                    Long studentId = enrollment.getStudent().getId();
                    // Check if student has already submitted
                    boolean hasSubmitted = assignment.getSubmissions() != null &&
                            assignment.getSubmissions().stream()
                                    .anyMatch(s -> s.getStudent().getId().equals(studentId));
                    
                    if (!hasSubmitted) {
                        try {
                            // Email reminder
                            if (emailNotificationService != null) {
                                emailNotificationService.sendAssignmentDeadlineReminderEmail(
                                        studentId,
                                        assignment.getCourse().getTitle(),
                                        assignment.getTitle(),
                                        assignment.getDueDate()
                                );
                            }

                            // In-app notification
                            if (notificationService != null) {
                                notificationService.createNotification(
                                        studentId,
                                        "Assignment Due Soon",
                                        "Your assignment '" + assignment.getTitle() +
                                                "' for course '" + assignment.getCourse().getTitle() +
                                                "' is due on " + assignment.getDueDate() + ".",
                                        Notification.NotificationType.WARNING,
                                        "/ui/lms/assignment/submit"
                                );
                            }
                        } catch (Exception e) {
                            log.error("Failed to send assignment reminder for assignment {} and student {}: {}",
                                    assignment.getId(), studentId, e.getMessage(), e);
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

