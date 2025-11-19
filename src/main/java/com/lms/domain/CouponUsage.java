package com.lms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * CouponUsage Entity
 * Tracks when and by whom a coupon was used
 */
@Setter
@Getter
@Entity
@Table(name = "coupon_usages")
public class CouponUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private CoursePayment payment;

    @Column(nullable = false)
    private LocalDateTime usedAt = LocalDateTime.now();
}

