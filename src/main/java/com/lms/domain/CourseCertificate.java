package com.lms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Course Certificate Entity
 * Certificates issued to students upon course completion
 */
@Setter
@Getter
@Entity
@Table(name = "course_certificates")
public class CourseCertificate {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    @Column(nullable = false, unique = true)
    private String certificateNumber; // Unique certificate ID

    @Column
    private Double finalScore; // Final score/grade

    @Column
    private String grade; // Letter grade

    @Column(nullable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column
    private LocalDateTime completedAt; // When course was completed

    @Column
    private String certificateUrl; // URL to PDF/image of certificate

}



