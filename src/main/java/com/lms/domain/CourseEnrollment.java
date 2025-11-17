package com.lms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class CourseEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Column
    private String status; // ACTIVE, COMPLETED, CANCELLED

    @Column
    private Integer progressPercentage = 0; // 0-100

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime lastAccessedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserAccount getStudent() { return student; }
    public void setStudent(UserAccount student) { this.student = student; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
}

