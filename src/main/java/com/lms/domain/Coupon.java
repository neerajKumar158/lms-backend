package com.lms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Coupon Entity
 * Represents discount coupons that can be applied to course payments
 */
@Setter
@Getter
@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code; // e.g., "SUMMER2024", "STUDENT50"

    @Column(nullable = false, length = 100)
    private String name; // Display name for the coupon

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType; // PERCENTAGE or FIXED_AMOUNT

    @Column(nullable = false)
    private BigDecimal discountValue; // Percentage (0-100) or fixed amount

    @Column
    private BigDecimal minimumPurchaseAmount; // Minimum order amount to use coupon

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validTo;

    @Column(nullable = false)
    private Integer maxUsageCount; // Maximum number of times coupon can be used (null = unlimited)

    @Column(nullable = false)
    private Integer currentUsageCount = 0; // Current number of times used

    @Column(nullable = false)
    private Integer maxUsagePerUser = 1; // Maximum times a single user can use this coupon

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Many-to-many relationship with courses
    // If empty, coupon applies to all courses
    @ManyToMany
    @JoinTable(
        name = "coupon_applicable_courses",
        joinColumns = @JoinColumn(name = "coupon_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> applicableCourses = new ArrayList<>();

    // Track coupon usage
    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CouponUsage> usages = new ArrayList<>();

    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT
    }

    /**
     * Calculate discount amount for a given price
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
     * Check if coupon is valid for a given course and user
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

