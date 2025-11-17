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
 * Course Entity - Phase 1.2
 * Core LMS entity with:
 * - Title, description, price, category, instructor, status
 */
@Setter
@Getter
@Entity
@Table(name = "courses")
public class Course {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price; // 0.00 for free courses

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "roles", "emailVerificationToken", "organization"}) // Only expose safe fields
    private UserAccount instructor; // Teacher who created the course

    @ManyToOne
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({"teachers", "courses", "students"}) // Prevent circular reference
    private Organization organization; // Optional: if course belongs to organization

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"courses"}) // Prevent circular reference
    private CourseCategory category;

    @Column
    private String thumbnailUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CourseStatus status = CourseStatus.DRAFT;

    @Column
    @Enumerated(EnumType.STRING)
    private CourseLevel level; // BEGINNER, INTERMEDIATE, ADVANCED

    @Column
    private Integer durationHours; // Total course duration

    @Column
    private String language; // Course language

    @Column
    private Integer maxEnrollments; // Maximum number of students

    @Column
    private Boolean featured = false; // Featured courses shown prominently

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column
    private LocalDateTime publishedAt;

    // Relationships - All collections are ignored in JSON to prevent lazy loading issues
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore // Never serialize lectures in list views - use separate endpoint
    private List<Lecture> lectures = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Hide enrollments from public API
    private List<CourseEnrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Never serialize live sessions in list views
    private List<LiveSession> liveSessions = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Never serialize assignments in list views
    private List<Assignment> assignments = new ArrayList<>();

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
