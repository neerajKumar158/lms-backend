package com.lms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * CourseReview Entity
 * Students can review and rate courses
 */
@Entity
@Table(name = "course_reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"course_id", "student_id"})
})
public class CourseReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    @Column(nullable = false)
    private Integer rating; // 1-5 stars

    @Column(length = 2000)
    private String reviewText;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;

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



