package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * Handles options for quiz questions. This entity manages answer options
 * for multiple choice and similar question types, including correct answer
 * designation and option ordering.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
@Table(name = "quiz_options")
public class QuizOption {
    /**
     * Unique identifier for the option
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The question this option belongs to
     */
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"options", "quiz"})
    private QuizQuestion question;

    /**
     * Text content of the option
     */
    @Column(nullable = false, length = 1000)
    private String optionText;

    /**
     * Whether this option is the correct answer
     */
    @Column
    private Boolean isCorrect = false;

    /**
     * Order index of the option within the question
     */
    @Column
    private Integer orderIndex;

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

