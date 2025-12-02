package com.lms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Handles system notifications for users. This entity manages notification
 * creation, delivery, read status tracking, notification types, and action
 * URLs for user engagement and system communication.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
@Table(name = "notifications")
public class Notification {
    /**
     * Unique identifier for the notification
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who receives this notification
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    /**
     * Title of the notification
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Message content of the notification
     */
    @Column(length = 1000)
    private String message;

    /**
     * Type of notification: INFO, SUCCESS, WARNING, ERROR, ASSIGNMENT, QUIZ, ANNOUNCEMENT, or CERTIFICATE
     */
    @Column
    @Enumerated(EnumType.STRING)
    private NotificationType type = NotificationType.INFO;

    /**
     * Whether the notification has been read by the user
     */
    @Column
    private Boolean isRead = false;

    /**
     * URL to navigate to when the notification is clicked
     */
    @Column
    private String actionUrl;

    /**
     * Timestamp when the notification was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when the notification was read
     */
    @Column
    private LocalDateTime readAt;

    // Enums
    public enum NotificationType {
        INFO, SUCCESS, WARNING, ERROR, ASSIGNMENT, QUIZ, ANNOUNCEMENT, CERTIFICATE
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}



