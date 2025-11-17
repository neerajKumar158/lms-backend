package com.lms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AssignmentSubmission Entity - Phase 1.2
 * Student submissions for assignments
 */
@Entity
@Table(name = "assignment_submissions")
public class AssignmentSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    @Column(length = 10000)
    private String submissionText; // Text submission

    @Column
    private String submissionFileUrl; // File submission URL

    @Column
    private Integer score; // Graded score

    @Column(length = 2000)
    private String feedback; // Instructor feedback

    @Column
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column
    private LocalDateTime gradedAt;

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



