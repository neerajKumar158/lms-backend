package com.lms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * QuizAttempt Entity - Phase 1.2
 * Student attempts at quizzes
 */
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    @Column
    private Integer attemptNumber; // Which attempt this is (1, 2, 3, etc.)

    @Column
    private Integer score; // Total score achieved

    @Column
    private Integer percentage; // Percentage score

    @Column
    @Enumerated(EnumType.STRING)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column
    private LocalDateTime submittedAt;

    @Column
    private Integer timeSpentSeconds; // Time taken in seconds

    // Answers
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizAnswer> answers = new ArrayList<>();

    // Enums
    public enum AttemptStatus {
        IN_PROGRESS, SUBMITTED, GRADED, EXPIRED
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public UserAccount getStudent() { return student; }
    public void setStudent(UserAccount student) { this.student = student; }

    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getPercentage() { return percentage; }
    public void setPercentage(Integer percentage) { this.percentage = percentage; }

    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Integer getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(Integer timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

    public List<QuizAnswer> getAnswers() { return answers; }
    public void setAnswers(List<QuizAnswer> answers) { this.answers = answers; }
}

