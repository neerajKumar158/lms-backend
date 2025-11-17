package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * QuizOption Entity - Phase 1.2
 * Options for quiz questions (for multiple choice, etc.)
 */
@Entity
@Table(name = "quiz_options")
public class QuizOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"options", "quiz"})
    private QuizQuestion question;

    @Column(nullable = false, length = 1000)
    private String optionText;

    @Column
    private Boolean isCorrect = false; // Whether this is the correct option

    @Column
    private Integer orderIndex; // Order of option

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QuizQuestion getQuestion() { return question; }
    public void setQuestion(QuizQuestion question) { this.question = question; }

    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}

