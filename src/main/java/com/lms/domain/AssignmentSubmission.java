package com.lms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Handles student submissions for assignments. This entity manages submission
 * content (text and files), grading, feedback, and submission status tracking
 * for assignment evaluation and student assessment.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
@Table(name = "assignment_submissions")
public class AssignmentSubmission {
    /**
     * Unique identifier for the submission
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The assignment this submission is for
     */
    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    /**
     * The student who submitted the assignment
     */
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    /**
     * Text content of the submission
     */
    @Column(length = 10000)
    private String submissionText;

    /**
     * URL to the submitted file (if file submission)
     */
    @Column
    private String submissionFileUrl;

    /**
     * Score awarded for this submission
     */
    @Column
    private Integer score;

    /**
     * Feedback provided by the instructor
     */
    @Column(length = 2000)
    private String feedback;

    /**
     * Current status of the submission: DRAFT, SUBMITTED, GRADED, RETURNED, or RESUBMITTED
     */
    @Column
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    /**
     * Timestamp when the submission was submitted
     */
    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    /**
     * Timestamp when the submission was graded
     */
    @Column
    private LocalDateTime gradedAt;

    /**
     * Whether the submission was submitted after the due date
     */
    @Column
    private Boolean isLate = false;

    // Enums
    public enum SubmissionStatus {
        DRAFT, SUBMITTED, GRADED, RETURNED, RESUBMITTED
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }

    public UserAccount getStudent() { return student; }
    public void setStudent(UserAccount student) { this.student = student; }

    public String getSubmissionText() { return submissionText; }
    public void setSubmissionText(String submissionText) { this.submissionText = submissionText; }

    public String getSubmissionFileUrl() { return submissionFileUrl; }
    public void setSubmissionFileUrl(String submissionFileUrl) { this.submissionFileUrl = submissionFileUrl; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public Boolean getIsLate() { return isLate; }
    public void setIsLate(Boolean isLate) { this.isLate = isLate; }
}



