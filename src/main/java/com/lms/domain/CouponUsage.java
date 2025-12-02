package com.lms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Handles coupon usage tracking. This entity manages records of when and
 * by whom coupons were used, linking coupons to payments and courses
 * for usage analytics and limit enforcement.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Setter
@Getter
@Entity
@Table(name = "coupon_usages")
public class CouponUsage {
    /**
     * Unique identifier for the coupon usage record
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The coupon that was used
     */
    @ManyToOne
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    /**
     * The user who used the coupon
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    /**
     * The course the coupon was applied to
     */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * The payment transaction where the coupon was used
     */
    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private CoursePayment payment;

    /**
     * Timestamp when the coupon was used
     */
    @Column(nullable = false)
    private LocalDateTime usedAt = LocalDateTime.now();
}

