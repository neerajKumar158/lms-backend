package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CourseOfferService {

    @Autowired
    private CourseOfferRepository offerRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Autowired(required = false)
    private UserAccountRepository userAccountRepository;

    /**
     * Get active offers for a specific course
     */
    public List<CourseOffer> getActiveOffersForCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return offerRepository.findActiveOffersForCourse(course, LocalDateTime.now());
    }

    /**
     * Get the best offer for a course (highest discount)
     */
    public Optional<CourseOffer> getBestOfferForCourse(Long courseId) {
        List<CourseOffer> offers = getActiveOffersForCourse(courseId);
        if (offers.isEmpty()) {
            return Optional.empty();
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        BigDecimal coursePrice = course.getPrice();

        CourseOffer bestOffer = null;
        BigDecimal bestDiscount = BigDecimal.ZERO;

        for (CourseOffer offer : offers) {
            BigDecimal discount = offer.calculateDiscount(coursePrice);
            if (discount.compareTo(bestDiscount) > 0) {
                bestDiscount = discount;
                bestOffer = offer;
            }
        }

        return Optional.ofNullable(bestOffer);
    }

    /**
     * Calculate final price with best offer applied
     */
    public OfferCalculationResult calculatePriceWithOffer(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Optional<CourseOffer> offerOpt = getBestOfferForCourse(courseId);
        
        if (offerOpt.isEmpty()) {
            return new OfferCalculationResult(
                course.getPrice(),
                BigDecimal.ZERO,
                null,
                course.getPrice()
            );
        }

        CourseOffer offer = offerOpt.get();
        BigDecimal originalPrice = course.getPrice();
        BigDecimal discount = offer.calculateDiscount(originalPrice);
        BigDecimal finalPrice = originalPrice.subtract(discount);
        
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        return new OfferCalculationResult(originalPrice, discount, offer, finalPrice);
    }

    /**
     * Get all active offers
     */
    public List<CourseOffer> getAllActiveOffers() {
        LocalDateTime now = LocalDateTime.now();
        return offerRepository.findByActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(now, now);
    }

    /**
     * Create a new offer
     */
    @Transactional
    public CourseOffer createOffer(CourseOffer offer) {
        offer.setCreatedAt(LocalDateTime.now());
        offer.setUpdatedAt(LocalDateTime.now());
        CourseOffer saved = offerRepository.save(offer);
        
        // Notify teachers when an offer is created
        notifyTeachersAboutOffer(saved);
        
        return saved;
    }

    /**
     * Notify teachers about a new offer
     */
    private void notifyTeachersAboutOffer(CourseOffer offer) {
        if (notificationService == null || userAccountRepository == null) {
            return;
        }

        try {
            // Get all courses affected by this offer
            List<Course> affectedCourses;
            if (offer.getApplicableCourses() != null && !offer.getApplicableCourses().isEmpty()) {
                // Offer applies to specific courses
                affectedCourses = offer.getApplicableCourses();
            } else {
                // Offer applies to all courses
                affectedCourses = courseRepository.findAll();
            }

            // Get unique organizations from affected courses
            java.util.Set<Organization> organizations = new java.util.HashSet<>();
            for (Course course : affectedCourses) {
                if (course.getOrganization() != null) {
                    organizations.add(course.getOrganization());
                }
            }

            // Notify all teachers in these organizations
            for (Organization org : organizations) {
                List<UserAccount> teachers = userAccountRepository.findByOrganizationId(org.getId());
                for (UserAccount teacher : teachers) {
                    if (teacher.getUserType() == UserAccount.UserType.TEACHER) {
                        String message = String.format(
                            "A new offer '%s' has been created. %s",
                            offer.getTitle(),
                            offer.getDescription() != null && offer.getDescription().length() > 100 
                                ? offer.getDescription().substring(0, 100) + "..." 
                                : (offer.getDescription() != null ? offer.getDescription() : "")
                        );
                        notificationService.createNotification(
                            teacher.getId(),
                            "New Offer Created",
                            message,
                            Notification.NotificationType.INFO,
                            "/ui/lms/admin/coupons"
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to notify teachers about offer {}: {}", offer.getId(), e.getMessage(), e);
        }
    }

    /**
     * Update offer
     */
    @Transactional
    public CourseOffer updateOffer(Long id, CourseOffer updatedOffer) {
        CourseOffer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        
        offer.setTitle(updatedOffer.getTitle());
        offer.setDescription(updatedOffer.getDescription());
        offer.setDiscountType(updatedOffer.getDiscountType());
        offer.setDiscountValue(updatedOffer.getDiscountValue());
        offer.setMinimumPurchaseAmount(updatedOffer.getMinimumPurchaseAmount());
        offer.setValidFrom(updatedOffer.getValidFrom());
        offer.setValidTo(updatedOffer.getValidTo());
        offer.setActive(updatedOffer.getActive());
        offer.setUpdatedAt(LocalDateTime.now());
        
        if (updatedOffer.getApplicableCourses() != null) {
            offer.setApplicableCourses(updatedOffer.getApplicableCourses());
        }
        
        return offerRepository.save(offer);
    }

    /**
     * Delete offer (soft delete by setting active to false)
     */
    @Transactional
    public void deleteOffer(Long id) {
        CourseOffer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        offer.setActive(false);
        offer.setUpdatedAt(LocalDateTime.now());
        offerRepository.save(offer);
    }

    /**
     * Result class for offer calculation
     */
    public static class OfferCalculationResult {
        private final BigDecimal originalPrice;
        private final BigDecimal discountAmount;
        private final CourseOffer offer;
        private final BigDecimal finalPrice;

        public OfferCalculationResult(BigDecimal originalPrice, BigDecimal discountAmount, 
                                     CourseOffer offer, BigDecimal finalPrice) {
            this.originalPrice = originalPrice;
            this.discountAmount = discountAmount;
            this.offer = offer;
            this.finalPrice = finalPrice;
        }

        public BigDecimal getOriginalPrice() { return originalPrice; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public CourseOffer getOffer() { return offer; }
        public BigDecimal getFinalPrice() { return finalPrice; }
    }
}

