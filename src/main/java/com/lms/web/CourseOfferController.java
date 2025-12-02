package com.lms.web;

import com.lms.domain.CourseOffer;
import com.lms.domain.UserAccount;
import com.lms.repository.UserAccountRepository;
import com.lms.service.CourseOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/lms/offers")
public class CourseOfferController {

    @Autowired
    private CourseOfferService offerService;

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
     * Get active offers for a course (public endpoint)
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseOffer>> getOffersForCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(offerService.getActiveOffersForCourse(courseId));
    }

    /**
     * Get best offer for a course (public endpoint)
     */
    @GetMapping("/course/{courseId}/best")
    public ResponseEntity<Map<String, Object>> getBestOfferForCourse(@PathVariable Long courseId) {
        Optional<CourseOffer> offerOpt = offerService.getBestOfferForCourse(courseId);
        
        if (offerOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("hasOffer", false));
        }

        CourseOffer offer = offerOpt.get();
        CourseOfferService.OfferCalculationResult calculation = offerService.calculatePriceWithOffer(courseId);
        
        return ResponseEntity.ok(Map.of(
                "hasOffer", true,
                "offer", Map.of(
                        "id", offer.getId(),
                        "title", offer.getTitle(),
                        "description", offer.getDescription(),
                        "discountType", offer.getDiscountType().toString(),
                        "discountValue", offer.getDiscountValue()
                ),
                "originalPrice", calculation.getOriginalPrice(),
                "discountAmount", calculation.getDiscountAmount(),
                "finalPrice", calculation.getFinalPrice()
        ));
    }

    /**
     * Calculate price with offer for a course (public endpoint)
     */
    @GetMapping("/course/{courseId}/calculate")
    public ResponseEntity<CourseOfferService.OfferCalculationResult> calculatePriceWithOffer(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(offerService.calculatePriceWithOffer(courseId));
    }

    /**
     * Get all active offers (public endpoint)
     */
    @GetMapping("/active")
    public ResponseEntity<List<CourseOffer>> getActiveOffers() {
        return ResponseEntity.ok(offerService.getAllActiveOffers());
    }

    /**
     * Create a new offer (admin only)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOffer(
            @AuthenticationPrincipal User principal,
            @RequestBody CourseOffer offer) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            CourseOffer created = offerService.createOffer(offer);
            return ResponseEntity.ok(Map.of(
                    "message", "Offer created successfully",
                    "offer", created
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update an offer (admin only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateOffer(
            @AuthenticationPrincipal User principal,
            @PathVariable Long id,
            @RequestBody CourseOffer offer) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            CourseOffer updated = offerService.updateOffer(id, offer);
            return ResponseEntity.ok(Map.of(
                    "message", "Offer updated successfully",
                    "offer", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete an offer (admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOffer(
            @AuthenticationPrincipal User principal,
            @PathVariable Long id) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            offerService.deleteOffer(id);
            return ResponseEntity.ok(Map.of("message", "Offer deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}




