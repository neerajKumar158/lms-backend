package com.lms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles discount coupons for course payments. This entity manages coupon
 * codes, discount calculation (percentage or fixed amount), usage limits,
 * validity periods, and applicable course restrictions for promotional pricing.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Setter
@Getter
@Entity
@Table(name = "coupons")
public class Coupon {
    /**
     * Unique identifier for the coupon
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique coupon code (e.g., "SUMMER2024", "STUDENT50")
     */
    @Column(unique = true, nullable = false, length = 50)
    private String code;

    /**
     * Display name for the coupon
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description of the coupon
     */
    @Column(length = 500)
    private String description;

    /**
     * Type of discount: PERCENTAGE or FIXED_AMOUNT
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    /**
     * Discount value: percentage (0-100) or fixed amount
     */
    @Column(nullable = false)
    private BigDecimal discountValue;

    /**
     * Minimum purchase amount required to use the coupon
     */
    @Column
    private BigDecimal minimumPurchaseAmount;

    /**
     * Start date and time when the coupon becomes valid
     */
    @Column(nullable = false)
    private LocalDateTime validFrom;

    /**
     * End date and time when the coupon expires
     */
    @Column(nullable = false)
    private LocalDateTime validTo;

    /**
     * Maximum number of times the coupon can be used (null = unlimited)
     */
    @Column(nullable = false)
    private Integer maxUsageCount;

    /**
     * Current number of times the coupon has been used
     */
    @Column(nullable = false)
    private Integer currentUsageCount = 0;

    /**
     * Maximum number of times a single user can use this coupon
     */
    @Column(nullable = false)
    private Integer maxUsagePerUser = 1;

    /**
     * Whether the coupon is currently active
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Timestamp when the coupon was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when the coupon was last updated
     */
    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * List of courses this coupon applies to (empty list = applies to all courses)
     */
    @ManyToMany
    @JoinTable(
        name = "coupon_applicable_courses",
        joinColumns = @JoinColumn(name = "coupon_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> applicableCourses = new ArrayList<>();

    /**
     * List of coupon usage records
     */
    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CouponUsage> usages = new ArrayList<>();

    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT
    }

    /**
     * Calculates discount amount for a given price based on discount type.
     *
     * @param originalPrice the original price before discount
     * @return the calculated discount amount
     */
    public BigDecimal calculateDiscount(BigDecimal originalPrice) {
        if (discountType == DiscountType.PERCENTAGE) {
            return originalPrice.multiply(discountValue).divide(BigDecimal.valueOf(100));
        } else {
            // For fixed amount, don't exceed the original price
            return discountValue.compareTo(originalPrice) > 0 ? originalPrice : discountValue;
        }
    }

    /**
     * Checks if coupon is valid for a given course and user.
     *
     * @param course the course to check validity for
     * @param user the user to check validity for
     * @return true if coupon is valid, false otherwise
     */
    public boolean isValidForCourse(Course course, UserAccount user) {
        if (!active) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(validFrom) || now.isAfter(validTo)) {
            return false;
        }

        if (maxUsageCount != null && currentUsageCount != null && currentUsageCount >= maxUsageCount) {
            return false;
        }

        // Check if coupon applies to this course
        if (applicableCourses != null && !applicableCourses.isEmpty() && !applicableCourses.contains(course)) {
            return false;
        }

        // Check minimum purchase amount
        if (minimumPurchaseAmount != null && course.getPrice().compareTo(minimumPurchaseAmount) < 0) {
            return false;
        }

        return true;
    }
}

