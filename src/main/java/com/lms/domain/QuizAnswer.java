package com.lms.domain;

import jakarta.persistence.*;

/**
 * Handles individual answers within quiz attempts. This entity manages
 * student responses to quiz questions, including text answers, selected
 * options, and grading results for each answer.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
@Table(name = "quiz_answers")
public class QuizAnswer {
    /**
     * Unique identifier for the answer
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The quiz attempt this answer belongs to
     */
    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    /**
     * The question this answer is for
     */
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    /**
     * Text answer provided by the student
     */
    @Column(length = 2000)
    private String answerText;

    /**
     * ID of the selected option (for multiple choice questions)
     */
    @Column
    private Long selectedOptionId;

    /**
     * Marks obtained for this answer
     */
    @Column
    private Integer marksObtained;

    /**
     * Whether the answer is correct
     */
    @Column
    private Boolean isCorrect;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QuizAttempt getAttempt() { return attempt; }
    public void setAttempt(QuizAttempt attempt) { this.attempt = attempt; }

    public QuizQuestion getQuestion() { return question; }
    public void setQuestion(QuizQuestion question) { this.question = question; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public Long getSelectedOptionId() { return selectedOptionId; }
    public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }

    public Integer getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
}



