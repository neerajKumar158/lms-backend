package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Course Announcement Entity
 * Announcements posted by instructors for their courses
 */
@Setter
@Getter
@Entity
@Table(name = "course_announcements")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CourseAnnouncement {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"lectures", "enrollments", "liveSessions", "assignments", "quizzes", "instructor"})
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "instructor_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"passwordHash", "roles", "emailVerificationToken", "organization", "password", "enrollments", "courses", "assignments", "quizAttempts"})
    private UserAccount instructor;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 5000)
    private String content;

    @Column
    private Boolean isImportant = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime expiresAt; // Optional expiration date

}

