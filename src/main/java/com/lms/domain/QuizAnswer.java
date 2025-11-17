package com.lms.domain;

import jakarta.persistence.*;

/**
 * QuizAnswer Entity - Phase 1.2
 * Individual answers within a quiz attempt
 */
@Entity
@Table(name = "quiz_answers")
public class QuizAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(length = 2000)
    private String answerText; // Student's answer

    @Column
    private Long selectedOptionId; // For multiple choice questions

    @Column
    private Integer marksObtained; // Marks obtained for this answer

    @Column
    private Boolean isCorrect; // Whether answer is correct

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



