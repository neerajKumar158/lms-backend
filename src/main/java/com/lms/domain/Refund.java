package com.lms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Handles refund requests and processing for course payments. This entity
 * manages refund creation, status tracking, approval workflow, Razorpay
 * integration, and refund amount calculation for payment reversals.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Setter
@Getter
@Entity
@Table(name = "refunds")
public class Refund {
    /**
     * Unique identifier for the refund
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The payment transaction being refunded
     */
    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private CoursePayment payment;

    /**
     * The student requesting the refund
     */
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    /**
     * The course associated with the refund
     */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * Amount to be refunded
     */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * Reason for the refund request
     */
    @Column(length = 1000)
    private String reason;

    /**
     * Current status of the refund: PENDING, APPROVED, REJECTED, PROCESSED, or FAILED
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RefundStatus status = RefundStatus.PENDING;

    /**
     * Admin notes regarding the refund
     */
    @Column
    private String adminNotes;

    /**
     * Timestamp when the refund was requested
     */
    @Column(nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    /**
     * Timestamp when the refund was processed
     */
    @Column
    private LocalDateTime processedAt;

    /**
     * Razorpay refund ID (if processed through Razorpay)
     */
    @Column
    private String razorpayRefundId;

    public enum RefundStatus {
        PENDING, APPROVED, REJECTED, PROCESSED, FAILED
    }
}


