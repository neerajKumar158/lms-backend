package com.lms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CourseOffer Entity
 * Represents automatic offers/promotions on courses
 * Unlike coupons, offers are automatically applied and don't require a code
 */
@Setter
@Getter
@Entity
@Table(name = "course_offers")
public class CourseOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title; // e.g., "Summer Sale - 50% Off"

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType; // PERCENTAGE or FIXED_AMOUNT

    @Column(nullable = false)
    private BigDecimal discountValue; // Percentage (0-100) or fixed amount

    @Column
    private BigDecimal minimumPurchaseAmount; // Minimum order amount

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validTo;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Many-to-many relationship with courses
    // If empty, offer applies to all courses
    @ManyToMany
    @JoinTable(
        name = "offer_applicable_courses",
        joinColumns = @JoinColumn(name = "offer_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> applicableCourses = new ArrayList<>();

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
     * Check if offer is valid for a given course
     */
    public boolean isValidForCourse(Course course) {
        if (!active) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(validFrom) || now.isAfter(validTo)) {
            return false;
        }

        // Check if offer applies to this course
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

