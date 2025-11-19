package com.lms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Refund Entity
 * Tracks refund requests and processing for course payments
 */
@Setter
@Getter
@Entity
@Table(name = "refunds")
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private CoursePayment payment;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 1000)
    private String reason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RefundStatus status = RefundStatus.PENDING;

    @Column
    private String adminNotes;

    @Column(nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column
    private LocalDateTime processedAt;

    @Column
    private String razorpayRefundId; // If processed through Razorpay

    public enum RefundStatus {
        PENDING, APPROVED, REJECTED, PROCESSED, FAILED
    }
}

