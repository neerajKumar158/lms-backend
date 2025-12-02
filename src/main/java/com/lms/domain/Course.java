package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles course management and organization. This entity manages course
 * creation, publishing workflows, pricing, categorization, instructor
 * assignment, course status, and relationships with lectures, enrollments,
 * and other course-related entities.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Setter
@Getter
@Entity
@Table(name = "courses")
public class Course {
    /**
     * Unique identifier for the course
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title of the course
     */
    @Column(nullable = false)
    private String title;

    /**
     * Detailed description of the course content
     */
    @Column(length = 5000)
    private String description;

    /**
     * Price of the course (0.00 for free courses)
     */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * Teacher who created and instructs the course
     */
    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "roles", "emailVerificationToken", "organization"}) // Only expose safe fields
    private UserAccount instructor;

    /**
     * Organization the course belongs to (optional)
     */
    @ManyToOne
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({"teachers", "courses", "students"}) // Prevent circular reference
    private Organization organization;

    /**
     * Category classification of the course
     */
    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"courses"}) // Prevent circular reference
    private CourseCategory category;

    /**
     * URL to the course thumbnail image
     */
    @Column
    private String thumbnailUrl;

    /**
     * Current status of the course: DRAFT, PUBLISHED, ARCHIVED, or SUSPENDED
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CourseStatus status = CourseStatus.DRAFT;

    /**
     * Difficulty level of the course: BEGINNER, INTERMEDIATE, ADVANCED, or EXPERT
     */
    @Column
    @Enumerated(EnumType.STRING)
    private CourseLevel level;

    /**
     * Total duration of the course in hours
     */
    @Column
    private Integer durationHours;

    /**
     * Language in which the course is delivered
     */
    @Column
    private String language;

    /**
     * Maximum number of students that can enroll in the course
     */
    @Column
    private Integer maxEnrollments;

    /**
     * Whether the course is featured and shown prominently
     */
    @Column
    private Boolean featured = false;

    /**
     * Timestamp when the course was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when the course was last updated
     */
    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Timestamp when the course was published
     */
    @Column
    private LocalDateTime publishedAt;

    /**
     * List of lectures/lessons in the course (not serialized in list views)
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore // Never serialize lectures in list views - use separate endpoint
    private List<Lecture> lectures = new ArrayList<>();

    /**
     * List of student enrollments in the course (not exposed in public API)
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Hide enrollments from public API
    private List<CourseEnrollment> enrollments = new ArrayList<>();

    /**
     * List of live sessions scheduled for the course (not serialized in list views)
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Never serialize live sessions in list views
    private List<LiveSession> liveSessions = new ArrayList<>();

    /**
     * List of assignments in the course (not serialized in list views)
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Never serialize assignments in list views
    private List<Assignment> assignments = new ArrayList<>();

    /**
     * List of quizzes/exams in the course (not serialized in list views)
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Never serialize quizzes in list views
    private List<Quiz> quizzes = new ArrayList<>();

    // Enums
    public enum CourseStatus {
        DRAFT, PUBLISHED, ARCHIVED, SUSPENDED
    }

    public enum CourseLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

}
