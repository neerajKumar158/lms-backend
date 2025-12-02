package com.lms.service;

import com.lms.domain.UserAccount;
import com.lms.repository.UserAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class EmailNotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private NotificationService notificationService;

    @Value("${app.email-notifications-enabled:false}")
    private boolean emailNotificationsEnabled;

    @Value("${spring.mail.username:noreply@lms.com}")
    private String fromEmail;

    public void sendCourseEnrollmentEmail(Long userId, String courseTitle) {
        if (!emailNotificationsEnabled || mailSender == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                UserAccount user = userAccountRepository.findById(userId)
                        .orElse(null);
                if (user == null || user.getEmail() == null) return;

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Course Enrollment Confirmation");
                message.setFrom(fromEmail);
                message.setText(String.format(
                        "Hello %s,\n\n" +
                        "You have successfully enrolled in the course: %s\n\n" +
                        "You can now access the course content and start learning.\n\n" +
                        "Best regards,\nLMS Team",
                        user.getName() != null ? user.getName() : "Student",
                        courseTitle
                ));

                mailSender.send(message);
            } catch (Exception e) {
                log.error("Failed to send enrollment email to user {} for course {}: {}",
                        userId, courseTitle, e.getMessage(), e);
            }
        });
    }

    public void sendGradeNotificationEmail(Long userId, String courseTitle, String assignmentTitle, Integer score, Integer maxScore) {
        if (!emailNotificationsEnabled || mailSender == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                UserAccount user = userAccountRepository.findById(userId)
                        .orElse(null);
                if (user == null || user.getEmail() == null) return;

                double percentage = maxScore > 0 ? (double) score / maxScore * 100 : 0;

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Assignment Graded: " + assignmentTitle);
                message.setFrom(fromEmail);
                message.setText(String.format(
                        "Hello %s,\n\n" +
                        "Your assignment '%s' for the course '%s' has been graded.\n\n" +
                        "Score: %d/%d (%.1f%%)\n\n" +
                        "You can view detailed feedback in your course dashboard.\n\n" +
                        "Best regards,\nLMS Team",
                        user.getName() != null ? user.getName() : "Student",
                        assignmentTitle,
                        courseTitle,
                        score,
                        maxScore,
                        percentage
                ));

                mailSender.send(message);
            } catch (Exception e) {
                log.error("Failed to send grade notification email to user {} for assignment '{}' in course '{}': {}",
                        userId, assignmentTitle, courseTitle, e.getMessage(), e);
            }
        });
    }

    public void sendQuizResultEmail(Long userId, String courseTitle, String quizTitle, Integer score, Integer maxScore) {
        if (!emailNotificationsEnabled || mailSender == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                UserAccount user = userAccountRepository.findById(userId)
                        .orElse(null);
                if (user == null || user.getEmail() == null) return;

                double percentage = maxScore > 0 ? (double) score / maxScore * 100 : 0;

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Quiz Results: " + quizTitle);
                message.setFrom(fromEmail);
                message.setText(String.format(
                        "Hello %s,\n\n" +
                        "Your quiz '%s' for the course '%s' has been graded.\n\n" +
                        "Score: %d/%d (%.1f%%)\n\n" +
                        "You can view detailed results in your course dashboard.\n\n" +
                        "Best regards,\nLMS Team",
                        user.getName() != null ? user.getName() : "Student",
                        quizTitle,
                        courseTitle,
                        score,
                        maxScore,
                        percentage
                ));

                mailSender.send(message);
            } catch (Exception e) {
                log.error("Failed to send quiz result email to user {} for quiz '{}' in course '{}': {}",
                        userId, quizTitle, courseTitle, e.getMessage(), e);
            }
        });
    }

    public void sendCourseAnnouncementEmail(Long userId, String courseTitle, String announcementTitle, String announcementContent) {
        if (!emailNotificationsEnabled || mailSender == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                UserAccount user = userAccountRepository.findById(userId)
                        .orElse(null);
                if (user == null || user.getEmail() == null) return;

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("New Announcement: " + courseTitle);
                message.setFrom(fromEmail);
                message.setText(String.format(
                        "Hello %s,\n\n" +
                        "A new announcement has been posted for the course '%s':\n\n" +
                        "Title: %s\n\n" +
                        "%s\n\n" +
                        "You can view this announcement in your course dashboard.\n\n" +
                        "Best regards,\nLMS Team",
                        user.getName() != null ? user.getName() : "Student",
                        courseTitle,
                        announcementTitle,
                        announcementContent.length() > 500 ? announcementContent.substring(0, 500) + "..." : announcementContent
                ));

                mailSender.send(message);
            } catch (Exception e) {
                log.error("Failed to send announcement email to user {} for course '{}': {}",
                        userId, courseTitle, e.getMessage(), e);
            }
        });
    }

    public void sendCertificateIssuedEmail(Long userId, String courseTitle, String certificateNumber) {
        if (!emailNotificationsEnabled || mailSender == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                UserAccount user = userAccountRepository.findById(userId)
                        .orElse(null);
                if (user == null || user.getEmail() == null) return;

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Course Completion Certificate");
                message.setFrom(fromEmail);
                message.setText(String.format(
                        "Congratulations %s!\n\n" +
                        "You have successfully completed the course '%s' and earned a certificate.\n\n" +
                        "Certificate Number: %s\n\n" +
                        "You can view and download your certificate from your dashboard.\n\n" +
                        "Best regards,\nLMS Team",
                        user.getName() != null ? user.getName() : "Student",
                        courseTitle,
                        certificateNumber
                ));

                mailSender.send(message);
            } catch (Exception e) {
                log.error("Failed to send certificate email to user {} for course '{}' (certificate {}): {}",
                        userId, courseTitle, certificateNumber, e.getMessage(), e);
            }
        });
    }

    public void sendPaymentConfirmationEmail(Long userId, String courseTitle, java.math.BigDecimal amount, String paymentId) {
        if (!emailNotificationsEnabled || mailSender == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                UserAccount user = userAccountRepository.findById(userId)
                        .orElse(null);
                if (user == null || user.getEmail() == null) return;

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Payment Confirmation - " + courseTitle);
                message.setFrom(fromEmail);
                message.setText(String.format(
                        "Hello %s,\n\n" +
                        "Your payment for the course '%s' has been successfully processed.\n\n" +
                        "Amount: ₹%.2f\n" +
                        "Payment ID: %s\n\n" +
                        "You have been automatically enrolled in the course. You can now access all course content.\n\n" +
                        "Thank you for your purchase!\n\n" +
                        "Best regards,\nLMS Team",
                        user.getName() != null ? user.getName() : "Student",
                        courseTitle,
                        amount.doubleValue(),
                        paymentId
                ));

                mailSender.send(message);
            } catch (Exception e) {
                log.error("Failed to send payment confirmation email to user {} for course '{}' (payment {}): {}",
                        userId, courseTitle, paymentId, e.getMessage(), e);
            }
        });
    }

    public void sendLiveSessionReminderEmail(Long userId, String courseTitle, String sessionTitle, java.time.LocalDateTime sessionDateTime) {
        if (!emailNotificationsEnabled || mailSender == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                UserAccount user = userAccountRepository.findById(userId)
                        .orElse(null);
                if (user == null || user.getEmail() == null) return;

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Live Session Reminder: " + sessionTitle);
                message.setFrom(fromEmail);
                message.setText(String.format(
                        "Hello %s,\n\n" +
                        "This is a reminder that you have a live session coming up:\n\n" +
                        "Course: %s\n" +
                        "Session: %s\n" +
                        "Date & Time: %s\n\n" +
                        "Please join the session on time. You can access it from your course dashboard.\n\n" +
                        "Best regards,\nLMS Team",
                        user.getName() != null ? user.getName() : "Student",
                        courseTitle,
                        sessionTitle,
                        sessionDateTime.toString()
                ));

                mailSender.send(message);
            } catch (Exception e) {
                log.error("Failed to send live session reminder email to user {} for course '{}' (session '{}'): {}",
                        userId, courseTitle, sessionTitle, e.getMessage(), e);
            }
        });
    }

    public void sendAssignmentDeadlineReminderEmail(Long userId, String courseTitle, String assignmentTitle, java.time.LocalDateTime dueDate) {
        if (!emailNotificationsEnabled || mailSender == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                UserAccount user = userAccountRepository.findById(userId)
                        .orElse(null);
                if (user == null || user.getEmail() == null) return;

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Assignment Deadline Reminder: " + assignmentTitle);
                message.setFrom(fromEmail);
                message.setText(String.format(
                        "Hello %s,\n\n" +
                        "This is a reminder that you have an assignment deadline approaching:\n\n" +
                        "Course: %s\n" +
                        "Assignment: %s\n" +
                        "Due Date: %s\n\n" +
                        "Please make sure to submit your assignment before the deadline.\n\n" +
                        "Best regards,\nLMS Team",
                        user.getName() != null ? user.getName() : "Student",
                        courseTitle,
                        assignmentTitle,
                        dueDate.toString()
                ));

                mailSender.send(message);
            } catch (Exception e) {
                log.error("Failed to send assignment deadline reminder email to user {} for course '{}' (assignment '{}'): {}",
                        userId, courseTitle, assignmentTitle, e.getMessage(), e);
            }
        });
    }
}



