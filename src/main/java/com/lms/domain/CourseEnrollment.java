package com.lms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Handles student enrollment in courses. This entity manages enrollment records,
 * tracking enrollment status, student progress, and access timestamps for course
 * participation and completion monitoring.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
public class CourseEnrollment {
    /**
     * Unique identifier for the enrollment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The student who enrolled in the course
     */
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    /**
     * The course the student enrolled in
     */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * Timestamp when the student enrolled in the course
     */
    @Column(nullable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();

    /**
     * Enrollment status: ACTIVE, COMPLETED, CANCELLED
     */
    @Column
    private String status;

    /**
     * Progress percentage (0-100) indicating how much of the course has been completed
     */
    @Column
    private Integer progressPercentage = 0;

    /**
     * Timestamp when the course was completed
     */
    @Column
    private LocalDateTime completedAt;

    /**
     * Timestamp of the last time the student accessed the course
     */
    @Column
    private LocalDateTime lastAccessedAt;

    /**
     * Gets the unique identifier of the enrollment
     *
     * @return the enrollment ID
     */
    public Long getId() { return id; }

    /**
     * Sets the unique identifier of the enrollment
     *
     * @param id the enrollment ID to set
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Gets the student who enrolled in the course
     *
     * @return the student user account
     */
    public UserAccount getStudent() { return student; }

    /**
     * Sets the student who enrolled in the course
     *
     * @param student the student user account to set
     */
    public void setStudent(UserAccount student) { this.student = student; }

    /**
     * Gets the course the student enrolled in
     *
     * @return the course entity
     */
    public Course getCourse() { return course; }

    /**
     * Sets the course the student enrolled in
     *
     * @param course the course entity to set
     */
    public void setCourse(Course course) { this.course = course; }

    /**
     * Gets the timestamp when the student enrolled
     *
     * @return the enrollment timestamp
     */
    public LocalDateTime getEnrolledAt() { return enrolledAt; }

    /**
     * Sets the timestamp when the student enrolled
     *
     * @param enrolledAt the enrollment timestamp to set
     */
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }

    /**
     * Gets the enrollment status (ACTIVE, COMPLETED, CANCELLED)
     *
     * @return the enrollment status
     */
    public String getStatus() { return status; }

    /**
     * Sets the enrollment status
     *
     * @param status the enrollment status to set
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Gets the progress percentage (0-100)
     *
     * @return the progress percentage
     */
    public Integer getProgressPercentage() { return progressPercentage; }

    /**
     * Sets the progress percentage (0-100)
     *
     * @param progressPercentage the progress percentage to set
     */
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }

    /**
     * Gets the timestamp when the course was completed
     *
     * @return the completion timestamp, or null if not completed
     */
    public LocalDateTime getCompletedAt() { return completedAt; }

    /**
     * Sets the timestamp when the course was completed
     *
     * @param completedAt the completion timestamp to set
     */
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    /**
     * Gets the timestamp of the last access to the course
     *
     * @return the last access timestamp, or null if never accessed
     */
    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }

    /**
     * Sets the timestamp of the last access to the course
     *
     * @param lastAccessedAt the last access timestamp to set
     */
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
}

