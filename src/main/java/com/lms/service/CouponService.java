package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUsageRepository couponUsageRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    /**
     * Validate and get coupon by code
     */
    public Optional<Coupon> getCouponByCode(String code) {
        return couponRepository.findByCodeAndActiveTrue(code);
    }

    /**
     * Validate coupon for a specific course and user
     */
    public CouponValidationResult validateCoupon(String code, Long courseId, Long userId) {
        Optional<Coupon> couponOpt = couponRepository.findByCodeAndActiveTrue(code);
        
        if (couponOpt.isEmpty()) {
            return new CouponValidationResult(false, "Invalid coupon code", null, null);
        }

        Coupon coupon = couponOpt.get();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if coupon is valid for this course
        if (!coupon.isValidForCourse(course, user)) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(coupon.getValidFrom())) {
                return new CouponValidationResult(false, "Coupon is not yet valid", null, null);
            }
            if (now.isAfter(coupon.getValidTo())) {
                return new CouponValidationResult(false, "Coupon has expired", null, null);
            }
            if (coupon.getMaxUsageCount() != null && coupon.getCurrentUsageCount() >= coupon.getMaxUsageCount()) {
                return new CouponValidationResult(false, "Coupon usage limit reached", null, null);
            }
            if (!coupon.getApplicableCourses().isEmpty() && !coupon.getApplicableCourses().contains(course)) {
                return new CouponValidationResult(false, "Coupon is not applicable to this course", null, null);
            }
            if (coupon.getMinimumPurchaseAmount() != null && 
                course.getPrice().compareTo(coupon.getMinimumPurchaseAmount()) < 0) {
                return new CouponValidationResult(false, 
                    "Minimum purchase amount not met", null, null);
            }
            return new CouponValidationResult(false, "Coupon is not valid for this course", null, null);
        }

        // Check user usage limit
        long userUsageCount = couponUsageRepository.countByCouponAndUser(coupon, user);
        if (userUsageCount >= coupon.getMaxUsagePerUser()) {
            return new CouponValidationResult(false, "You have already used this coupon maximum times", null, null);
        }

        // Calculate discount
        BigDecimal discount = coupon.calculateDiscount(course.getPrice());
        BigDecimal finalPrice = course.getPrice().subtract(discount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        return new CouponValidationResult(true, "Coupon is valid", coupon, discount);
    }

    /**
     * Apply coupon to a payment (record usage)
     */
    @Transactional
    public void applyCoupon(Coupon coupon, UserAccount user, Course course, CoursePayment payment) {
        // Record usage
        CouponUsage usage = new CouponUsage();
        usage.setCoupon(coupon);
        usage.setUser(user);
        usage.setCourse(course);
        usage.setPayment(payment);
        usage.setUsedAt(LocalDateTime.now());
        couponUsageRepository.save(usage);

        // Update coupon usage count
        coupon.setCurrentUsageCount(coupon.getCurrentUsageCount() + 1);
        couponRepository.save(coupon);
    }

    /**
     * Get all active coupons
     */
    public List<Coupon> getAllActiveCoupons() {
        return couponRepository.findByActiveTrue();
    }

    /**
     * Create a new coupon
     */
    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());
        return couponRepository.save(coupon);
    }

    /**
     * Update coupon
     */
    @Transactional
    public Coupon updateCoupon(Long id, Coupon updatedCoupon) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        
        coupon.setName(updatedCoupon.getName());
        coupon.setDescription(updatedCoupon.getDescription());
        coupon.setDiscountType(updatedCoupon.getDiscountType());
        coupon.setDiscountValue(updatedCoupon.getDiscountValue());
        coupon.setMinimumPurchaseAmount(updatedCoupon.getMinimumPurchaseAmount());
        coupon.setValidFrom(updatedCoupon.getValidFrom());
        coupon.setValidTo(updatedCoupon.getValidTo());
        coupon.setMaxUsageCount(updatedCoupon.getMaxUsageCount());
        coupon.setMaxUsagePerUser(updatedCoupon.getMaxUsagePerUser());
        coupon.setActive(updatedCoupon.getActive());
        coupon.setUpdatedAt(LocalDateTime.now());
        
        if (updatedCoupon.getApplicableCourses() != null) {
            coupon.setApplicableCourses(updatedCoupon.getApplicableCourses());
        }
        
        return couponRepository.save(coupon);
    }

    /**
     * Delete coupon (soft delete by setting active to false)
     */
    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        coupon.setActive(false);
        coupon.setUpdatedAt(LocalDateTime.now());
        couponRepository.save(coupon);
    }

    /**
     * Result class for coupon validation
     */
    public static class CouponValidationResult {
        private final boolean valid;
        private final String message;
        private final Coupon coupon;
        private final BigDecimal discountAmount;

        public CouponValidationResult(boolean valid, String message, Coupon coupon, BigDecimal discountAmount) {
            this.valid = valid;
            this.message = message;
            this.coupon = coupon;
            this.discountAmount = discountAmount;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public Coupon getCoupon() { return coupon; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
    }
}




