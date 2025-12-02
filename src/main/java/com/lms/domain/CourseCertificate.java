package com.lms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Handles course completion certificates for students. This entity manages
 * certificate issuance, unique certificate numbers, final scores, grades,
 * and certificate document URLs for course completion recognition.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Setter
@Getter
@Entity
@Table(name = "course_certificates")
public class CourseCertificate {
    /**
     * Unique identifier for the certificate
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The course for which the certificate was issued
     */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * The student who received the certificate
     */
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    /**
     * Unique certificate number/ID
     */
    @Column(nullable = false, unique = true)
    private String certificateNumber;

    /**
     * Final score/grade achieved in the course
     */
    @Column
    private Double finalScore;

    /**
     * Letter grade assigned (e.g., A, B, C, etc.)
     */
    @Column
    private String grade;

    /**
     * Timestamp when the certificate was issued
     */
    @Column(nullable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    /**
     * Timestamp when the course was completed
     */
    @Column
    private LocalDateTime completedAt;

    /**
     * URL to the certificate document (PDF/image)
     */
    @Column
    private String certificateUrl;

}



