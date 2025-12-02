package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Handles course announcements by instructors. This entity manages
 * announcement creation, important flag designation, expiration dates,
 * and content distribution to enrolled students for course communication.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Setter
@Getter
@Entity
@Table(name = "course_announcements")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CourseAnnouncement {
    /**
     * Unique identifier for the announcement
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The course this announcement belongs to
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"lectures", "enrollments", "liveSessions", "assignments", "quizzes", "instructor"})
    private Course course;

    /**
     * The instructor who created the announcement
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "instructor_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"passwordHash", "roles", "emailVerificationToken", "organization", "password", "enrollments", "courses", "assignments", "quizAttempts"})
    private UserAccount instructor;

    /**
     * Title of the announcement
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Content/body of the announcement
     */
    @Column(length = 5000)
    private String content;

    /**
     * Whether this announcement is marked as important
     */
    @Column
    private Boolean isImportant = false;

    /**
     * Timestamp when the announcement was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Optional expiration date for the announcement
     */
    @Column
    private LocalDateTime expiresAt;

}

