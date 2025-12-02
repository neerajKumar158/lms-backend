package com.lms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles automatic offers and promotions on courses. This entity manages
 * automatic discount application (unlike coupons which require codes),
 * discount calculation, validity periods, and applicable course restrictions
 * for promotional pricing campaigns.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Setter
@Getter
@Entity
@Table(name = "course_offers")
public class CourseOffer {
    /**
     * Unique identifier for the offer
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title of the offer (e.g., "Summer Sale - 50% Off")
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Description of the offer
     */
    @Column(length = 1000)
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
     * Minimum purchase amount required for the offer
     */
    @Column
    private BigDecimal minimumPurchaseAmount;

    /**
     * Start date and time when the offer becomes valid
     */
    @Column(nullable = false)
    private LocalDateTime validFrom;

    /**
     * End date and time when the offer expires
     */
    @Column(nullable = false)
    private LocalDateTime validTo;

    /**
     * Whether the offer is currently active
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Timestamp when the offer was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when the offer was last updated
     */
    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * List of courses this offer applies to (empty list = applies to all courses)
     */
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
     * Checks if offer is valid for a given course.
     *
     * @param course the course to check validity for
     * @return true if offer is valid, false otherwise
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

