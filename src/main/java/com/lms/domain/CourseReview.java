package com.lms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Handles student reviews and ratings for courses. This entity manages
 * course feedback, rating system (1-5 stars), review visibility, and
 * ensures one review per student per course for authentic feedback.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
@Table(name = "course_reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"course_id", "student_id"})
})
public class CourseReview {
    /**
     * Unique identifier for the review
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The course being reviewed
     */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * The student who wrote the review
     */
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    /**
     * Rating given to the course (1-5 stars)
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * Text content of the review
     */
    @Column(length = 2000)
    private String reviewText;

    /**
     * Timestamp when the review was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when the review was last updated
     */
    @Column
    private LocalDateTime updatedAt;

    /**
     * Whether the review is visible to other users
     */
    @Column
    private Boolean isVisible = true;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public UserAccount getStudent() { return student; }
    public void setStudent(UserAccount student) { this.student = student; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { 
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating; 
    }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }
}



