package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles quiz and exam assessments for courses. This entity manages quiz
 * creation, configuration, question management, attempt tracking, and grading
 * settings including time limits, attempts, and result visibility.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
@Table(name = "quizzes")
public class Quiz {
    /**
     * Unique identifier for the quiz
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The course this quiz belongs to
     */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"lectures", "enrollments", "liveSessions", "assignments", "quizzes"})
    private Course course;

    /**
     * Title of the quiz
     */
    @Column(nullable = false)
    private String title;

    /**
     * Description of the quiz content
     */
    @Column(length = 2000)
    private String description;

    /**
     * Type of quiz: QUIZ, EXAM, ASSIGNMENT, or PRACTICE
     */
    @Column
    @Enumerated(EnumType.STRING)
    private QuizType type = QuizType.QUIZ;

    /**
     * Total marks available for the quiz
     */
    @Column
    private Integer totalMarks;

    /**
     * Minimum marks required to pass the quiz
     */
    @Column
    private Integer passingMarks;

    /**
     * Time limit for completing the quiz in minutes
     */
    @Column
    private Integer durationMinutes;

    /**
     * Maximum number of attempts allowed for this quiz
     */
    @Column
    private Integer maxAttempts = 1;

    /**
     * Date and time when the quiz becomes available
     */
    @Column
    private LocalDateTime startDate;

    /**
     * Date and time when the quiz closes
     */
    @Column
    private LocalDateTime endDate;

    /**
     * Whether to show results immediately after submission
     */
    @Column
    private Boolean showResultsImmediately = false;

    /**
     * Whether to shuffle the order of questions
     */
    @Column
    private Boolean shuffleQuestions = false;

    /**
     * Whether to shuffle the order of options within questions
     */
    @Column
    private Boolean shuffleOptions = false;

    /**
     * Timestamp when the quiz was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when the quiz was last updated
     */
    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * List of questions in the quiz (not serialized in default views)
     */
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore // Ignored in default serialization, but included in attempt endpoint via Map response
    private List<QuizQuestion> questions = new ArrayList<>();

    /**
     * List of student attempts for this quiz (not serialized in default views)
     */
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<QuizAttempt> attempts = new ArrayList<>();

    // Enums
    public enum QuizType {
        QUIZ, EXAM, ASSIGNMENT, PRACTICE
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

    public QuizType getType() { return type; }
    public void setType(QuizType type) { this.type = type; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

    public Integer getPassingMarks() { return passingMarks; }
    public void setPassingMarks(Integer passingMarks) { this.passingMarks = passingMarks; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Boolean getShowResultsImmediately() { return showResultsImmediately; }
    public void setShowResultsImmediately(Boolean showResultsImmediately) { this.showResultsImmediately = showResultsImmediately; }

    public Boolean getShuffleQuestions() { return shuffleQuestions; }
    public void setShuffleQuestions(Boolean shuffleQuestions) { this.shuffleQuestions = shuffleQuestions; }

    public Boolean getShuffleOptions() { return shuffleOptions; }
    public void setShuffleOptions(Boolean shuffleOptions) { this.shuffleOptions = shuffleOptions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<QuizQuestion> getQuestions() { return questions; }
    public void setQuestions(List<QuizQuestion> questions) { this.questions = questions; }

    public List<QuizAttempt> getAttempts() { return attempts; }
    public void setAttempts(List<QuizAttempt> attempts) { this.attempts = attempts; }
}

