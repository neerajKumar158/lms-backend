package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles student assignments within courses. This entity manages assignment creation,
 * configuration, and submission tracking, supporting various assignment types (essay,
 * project, presentation, etc.) with late submission and penalty management.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
@Table(name = "assignments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Assignment {
    /**
     * Unique identifier for the assignment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The course this assignment belongs to
     */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"lectures", "enrollments", "liveSessions", "assignments", "quizzes", "instructor"})
    private Course course;

    /**
     * Title of the assignment
     */
    @Column(nullable = false)
    private String title;

    /**
     * Description of the assignment
     */
    @Column(length = 2000)
    private String description;

    /**
     * Detailed instructions for completing the assignment
     */
    @Column(length = 5000)
    private String instructions;

    /**
     * Maximum points/score for this assignment
     */
    @Column
    private Integer maxScore;

    /**
     * Due date and time for the assignment
     */
    @Column(nullable = false)
    private LocalDateTime dueDate;

    /**
     * Start date when the assignment becomes available to students
     */
    @Column
    private LocalDateTime startDate;

    /**
     * Whether late submissions are allowed
     */
    @Column
    private Boolean allowLateSubmission = false;

    /**
     * Penalty percentage applied to late submissions (0-100)
     */
    @Column
    private Integer latePenaltyPercent;

    /**
     * Type of assignment (ESSAY, PROJECT, PRESENTATION, CODE, QUIZ, OTHER)
     */
    @Column
    @Enumerated(EnumType.STRING)
    private AssignmentType type = AssignmentType.ESSAY;

    /**
     * URL to attachment file containing instructions or template
     */
    @Column
    private String attachmentUrl;

    /**
     * Timestamp when the assignment was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when the assignment was last updated
     */
    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * List of student submissions for this assignment
     */
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    @JsonIgnore // Don't include submissions in list view - use separate endpoint
    private List<AssignmentSubmission> submissions = new ArrayList<>();

    /**
     * Enumeration of assignment types
     */
    public enum AssignmentType {
        /** Essay assignment */
        ESSAY,
        /** Project assignment */
        PROJECT,
        /** Presentation assignment */
        PRESENTATION,
        /** Code/programming assignment */
        CODE,
        /** Quiz assignment */
        QUIZ,
        /** Other type of assignment */
        OTHER
    }

    /**
     * Gets the unique identifier of the assignment
     *
     * @return the assignment ID
     */
    public Long getId() { return id; }

    /**
     * Sets the unique identifier of the assignment
     *
     * @param id the assignment ID to set
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Gets the course this assignment belongs to
     *
     * @return the course entity
     */
    public Course getCourse() { return course; }

    /**
     * Sets the course this assignment belongs to
     *
     * @param course the course entity to set
     */
    public void setCourse(Course course) { this.course = course; }

    /**
     * Gets the title of the assignment
     *
     * @return the assignment title
     */
    public String getTitle() { return title; }

    /**
     * Sets the title of the assignment
     *
     * @param title the assignment title to set
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Gets the description of the assignment
     *
     * @return the description
     */
    public String getDescription() { return description; }

    /**
     * Sets the description of the assignment
     *
     * @param description the description to set
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Gets the instructions for the assignment
     *
     * @return the instructions
     */
    public String getInstructions() { return instructions; }

    /**
     * Sets the instructions for the assignment
     *
     * @param instructions the instructions to set
     */
    public void setInstructions(String instructions) { this.instructions = instructions; }

    /**
     * Gets the maximum score for this assignment
     *
     * @return the maximum score
     */
    public Integer getMaxScore() { return maxScore; }

    /**
     * Sets the maximum score for this assignment
     *
     * @param maxScore the maximum score to set
     */
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }

    /**
     * Gets the due date and time for the assignment
     *
     * @return the due date
     */
    public LocalDateTime getDueDate() { return dueDate; }

    /**
     * Sets the due date and time for the assignment
     *
     * @param dueDate the due date to set
     */
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    /**
     * Gets the start date when the assignment becomes available
     *
     * @return the start date, or null if available immediately
     */
    public LocalDateTime getStartDate() { return startDate; }

    /**
     * Sets the start date when the assignment becomes available
     *
     * @param startDate the start date to set
     */
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    /**
     * Checks if late submissions are allowed
     *
     * @return true if late submissions are allowed, false otherwise
     */
    public Boolean getAllowLateSubmission() { return allowLateSubmission; }

    /**
     * Sets whether late submissions are allowed
     *
     * @param allowLateSubmission true to allow late submissions, false otherwise
     */
    public void setAllowLateSubmission(Boolean allowLateSubmission) { this.allowLateSubmission = allowLateSubmission; }

    /**
     * Gets the late penalty percentage (0-100)
     *
     * @return the late penalty percentage
     */
    public Integer getLatePenaltyPercent() { return latePenaltyPercent; }

    /**
     * Sets the late penalty percentage (0-100)
     *
     * @param latePenaltyPercent the late penalty percentage to set
     */
    public void setLatePenaltyPercent(Integer latePenaltyPercent) { this.latePenaltyPercent = latePenaltyPercent; }

    /**
     * Gets the type of assignment
     *
     * @return the assignment type
     */
    public AssignmentType getType() { return type; }

    /**
     * Sets the type of assignment
     *
     * @param type the assignment type to set
     */
    public void setType(AssignmentType type) { this.type = type; }

    /**
     * Gets the URL to the attachment file
     *
     * @return the attachment URL
     */
    public String getAttachmentUrl() { return attachmentUrl; }

    /**
     * Sets the URL to the attachment file
     *
     * @param attachmentUrl the attachment URL to set
     */
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    /**
     * Gets the timestamp when the assignment was created
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Sets the timestamp when the assignment was created
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Gets the timestamp when the assignment was last updated
     *
     * @return the update timestamp
     */
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /**
     * Sets the timestamp when the assignment was last updated
     *
     * @param updatedAt the update timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Gets the list of student submissions for this assignment
     *
     * @return the list of submissions
     */
    public List<AssignmentSubmission> getSubmissions() { return submissions; }

    /**
     * Sets the list of student submissions for this assignment
     *
     * @param submissions the list of submissions to set
     */
    public void setSubmissions(List<AssignmentSubmission> submissions) { this.submissions = submissions; }
}



