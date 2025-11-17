package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Assignment Entity - Phase 1.2
 * Student assignments within a course
 */
@Entity
@Table(name = "assignments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"lectures", "enrollments", "liveSessions", "assignments", "quizzes", "instructor"})
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 5000)
    private String instructions;

    @Column
    private Integer maxScore; // Maximum points for this assignment

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Column
    private LocalDateTime startDate; // When assignment becomes available

    @Column
    private Boolean allowLateSubmission = false;

    @Column
    private Integer latePenaltyPercent; // Penalty percentage for late submissions

    @Column
    @Enumerated(EnumType.STRING)
    private AssignmentType type = AssignmentType.ESSAY;

    @Column
    private String attachmentUrl; // Instructions or template file

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Student submissions
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    @JsonIgnore // Don't include submissions in list view - use separate endpoint
    private List<AssignmentSubmission> submissions = new ArrayList<>();

    // Enums
    public enum AssignmentType {
        ESSAY, PROJECT, PRESENTATION, CODE, QUIZ, OTHER
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public Boolean getAllowLateSubmission() { return allowLateSubmission; }
    public void setAllowLateSubmission(Boolean allowLateSubmission) { this.allowLateSubmission = allowLateSubmission; }

    public Integer getLatePenaltyPercent() { return latePenaltyPercent; }
    public void setLatePenaltyPercent(Integer latePenaltyPercent) { this.latePenaltyPercent = latePenaltyPercent; }

    public AssignmentType getType() { return type; }
    public void setType(AssignmentType type) { this.type = type; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<AssignmentSubmission> getSubmissions() { return submissions; }
    public void setSubmissions(List<AssignmentSubmission> submissions) { this.submissions = submissions; }
}



