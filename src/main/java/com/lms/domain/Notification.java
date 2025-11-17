package com.lms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Notification Entity
 * System notifications for users
 */
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String message;

    @Column
    @Enumerated(EnumType.STRING)
    private NotificationType type = NotificationType.INFO;

    @Column
    private Boolean isRead = false;

    @Column
    private String actionUrl; // URL to navigate when clicked

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

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



