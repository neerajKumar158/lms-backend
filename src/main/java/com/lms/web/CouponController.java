package com.lms.web;

import com.lms.domain.Coupon;
import com.lms.domain.UserAccount;
import com.lms.repository.UserAccountRepository;
import com.lms.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lms/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    /**
     * Check if current user is admin
     */
    private boolean isAdmin(User principal) {
        if (principal == null) {
            return false;
        }
        var user = userAccountRepository.findByEmail(principal.getUsername())
                .orElse(null);
        return user != null && user.getUserType() == UserAccount.UserType.ADMIN;
    }

    /**
     * Validate a coupon code for a course (public endpoint for students)
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCoupon(
            @AuthenticationPrincipal User principal,
            @RequestBody ValidateCouponRequest request) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            CouponService.CouponValidationResult result = couponService.validateCoupon(
                    request.couponCode(), request.courseId(), user.getId());

            if (result.isValid()) {
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "message", result.getMessage(),
                        "discountAmount", result.getDiscountAmount(),
                        "coupon", Map.of(
                                "id", result.getCoupon().getId(),
                                "code", result.getCoupon().getCode(),
                                "name", result.getCoupon().getName(),
                                "discountType", result.getCoupon().getDiscountType().toString(),
                                "discountValue", result.getCoupon().getDiscountValue()
                        )
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "valid", false,
                        "message", result.getMessage()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all active coupons (public endpoint)
     */
    @GetMapping("/active")
    public ResponseEntity<List<Coupon>> getActiveCoupons() {
        return ResponseEntity.ok(couponService.getAllActiveCoupons());
    }

    /**
     * Create a new coupon (admin only)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCoupon(
            @AuthenticationPrincipal User principal,
            @RequestBody Coupon coupon) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            Coupon created = couponService.createCoupon(coupon);
            return ResponseEntity.ok(Map.of(
                    "message", "Coupon created successfully",
                    "coupon", created
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update a coupon (admin only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCoupon(
            @AuthenticationPrincipal User principal,
            @PathVariable Long id,
            @RequestBody Coupon coupon) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            Coupon updated = couponService.updateCoupon(id, coupon);
            return ResponseEntity.ok(Map.of(
                    "message", "Coupon updated successfully",
                    "coupon", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a coupon (admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCoupon(
            @AuthenticationPrincipal User principal,
            @PathVariable Long id) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            couponService.deleteCoupon(id);
            return ResponseEntity.ok(Map.of("message", "Coupon deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    record ValidateCouponRequest(String couponCode, Long courseId) {}
}




