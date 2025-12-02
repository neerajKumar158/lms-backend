package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles questions within quizzes. This entity manages question content,
 * question types (multiple choice, true/false, essay, etc.), options,
 * correct answers, and explanations for quiz assessment.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {
    /**
     * Unique identifier for the question
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The quiz this question belongs to
     */
    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonIgnoreProperties({"questions", "attempts", "course"})
    private Quiz quiz;

    /**
     * The question text/content
     */
    @Column(nullable = false, length = 2000)
    private String questionText;

    /**
     * Type of question: MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER, ESSAY, MATCHING, or FILL_BLANK
     */
    @Column
    @Enumerated(EnumType.STRING)
    private QuestionType type = QuestionType.MULTIPLE_CHOICE;

    /**
     * Points/marks awarded for this question
     */
    @Column
    private Integer marks;

    /**
     * Order index of the question within the quiz
     */
    @Column
    private Integer orderIndex;

    /**
     * List of answer options for multiple choice, true/false, etc.
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<QuizOption> options = new ArrayList<>();

    /**
     * Correct answer identifier (can be option ID, text, etc. for auto-grading)
     */
    @Column
    private String correctAnswer;

    /**
     * Explanation of the correct answer
     */
    @Column(length = 2000)
    private String explanation;

    // Enums
    public enum QuestionType {
        MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER, ESSAY, MATCHING, FILL_BLANK
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public List<QuizOption> getOptions() { return options; }
    public void setOptions(List<QuizOption> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}

