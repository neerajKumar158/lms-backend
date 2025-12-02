package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CoursePaymentService {

    @Autowired
    private CoursePaymentRepository paymentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CourseOfferService offerService;

    public Map<String, Object> createPaymentOrder(Long studentId, Long courseId) {
        return createPaymentOrder(studentId, courseId, null);
    }

    public Map<String, Object> createPaymentOrder(Long studentId, Long courseId, String couponCode) {
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (course.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Course is free. No payment required.");
        }

        BigDecimal originalPrice = course.getPrice();
        BigDecimal finalPrice = originalPrice;
        BigDecimal discountAmount = BigDecimal.ZERO;
        Coupon appliedCoupon = null;
        CourseOffer appliedOffer = null;

        // Apply coupon if provided
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            CouponService.CouponValidationResult validation = couponService.validateCoupon(
                    couponCode, courseId, studentId);
            
            if (!validation.isValid()) {
                return Map.of("error", validation.getMessage());
            }
            
            appliedCoupon = validation.getCoupon();
            discountAmount = validation.getDiscountAmount();
            finalPrice = originalPrice.subtract(discountAmount);
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                finalPrice = BigDecimal.ZERO;
            }
        } else {
            // If no coupon, check for active offers
            Optional<CourseOffer> offerOpt = offerService.getBestOfferForCourse(courseId);
            if (offerOpt.isPresent()) {
                CourseOffer offer = offerOpt.get();
                appliedOffer = offer;
                discountAmount = offer.calculateDiscount(originalPrice);
                finalPrice = originalPrice.subtract(discountAmount);
                if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                    finalPrice = BigDecimal.ZERO;
                }
            }
        }

        // Create payment record
        CoursePayment payment = new CoursePayment();
        payment.setStudent(student);
        payment.setCourse(course);
        payment.setOriginalAmount(originalPrice);
        payment.setDiscountAmount(discountAmount);
        payment.setAmount(finalPrice);
        payment.setCoupon(appliedCoupon);
        payment.setOffer(appliedOffer);
        payment.setStatus("PENDING");
        payment.setCreatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // If coupon was applied, record the usage
        if (appliedCoupon != null) {
            couponService.applyCoupon(appliedCoupon, student, course, payment);
        }

        // Create Razorpay order with final price
        Map<String, Object> razorpayOrder = razorpayService.createOrder(
                finalPrice,
                "INR",
                payment.getId().toString(),
                student.getName() != null ? student.getName() : "Student",
                student.getEmail(),
                student.getPhone()
        );

        if (razorpayOrder.containsKey("error")) {
            payment.setStatus("FAILED");
            payment.setFailureReason(razorpayOrder.get("error").toString());
            paymentRepository.save(payment);
            return razorpayOrder;
        }

        // Update payment with Razorpay order ID
        String razorpayOrderId = razorpayOrder.get("id").toString();
        payment.setRazorpayOrderId(razorpayOrderId);
        paymentRepository.save(payment);

        // Add user info and payment ID to response
        Map<String, Object> response = new java.util.HashMap<>(razorpayOrder);
        response.put("orderId", payment.getId());
        response.put("razorpayOrderId", razorpayOrderId); // Add this for frontend
        response.put("userEmail", student.getEmail());
        response.put("userName", student.getName());
        response.put("originalPrice", originalPrice);
        response.put("discountAmount", discountAmount);
        response.put("finalPrice", finalPrice);
        if (appliedCoupon != null) {
            response.put("couponCode", appliedCoupon.getCode());
            response.put("couponName", appliedCoupon.getName());
        }
        if (appliedOffer != null) {
            response.put("offerTitle", appliedOffer.getTitle());
        }
        
        return response;
    }

    @Transactional
    public boolean verifyAndCompletePayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        Optional<CoursePayment> paymentOpt = paymentRepository.findByRazorpayOrderId(razorpayOrderId);
        
        if (paymentOpt.isEmpty()) {
            return false;
        }

        CoursePayment payment = paymentOpt.get();

        // Verify payment signature
        boolean isValid = razorpayService.verifyPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature);
        
        if (!isValid) {
            payment.setStatus("FAILED");
            payment.setFailureReason("Payment signature verification failed");
            paymentRepository.save(payment);
            return false;
        }

        // Update payment record
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(razorpaySignature);
        payment.setStatus("SUCCESS");
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Enroll student in course
        try {
            enrollmentService.enrollStudent(payment.getStudent().getId(), payment.getCourse().getId());
        } catch (Exception e) {
            // Enrollment might already exist, which is fine
            log.warn("Enrollment error after successful payment (may already exist) for student {} and course {}: {}",
                    payment.getStudent().getId(), payment.getCourse().getId(), e.getMessage());
        }

        // Send payment confirmation email
        try {
            // In-app notification
            try {
                notificationService.createNotification(
                        payment.getStudent().getId(),
                        "Payment Successful",
                        "Your payment for the course '" + payment.getCourse().getTitle() + "' has been completed successfully.",
                        com.lms.domain.Notification.NotificationType.SUCCESS,
                        "/ui/lms/courses/" + payment.getCourse().getId()
                );
            } catch (Exception ex) {
                log.error("Failed to create payment notification for payment {}: {}",
                        payment.getId(), ex.getMessage(), ex);
            }

            // Email notification
            if (emailNotificationService != null) {
                emailNotificationService.sendPaymentConfirmationEmail(
                        payment.getStudent().getId(),
                        payment.getCourse().getTitle(),
                        payment.getAmount(),
                        razorpayPaymentId
                );
            }
        } catch (Exception e) {
            log.error("Failed to send payment notifications for payment {}: {}", payment.getId(), e.getMessage(), e);
        }

        return true;
    }

    public Optional<CoursePayment> getPaymentByOrderId(String razorpayOrderId) {
        return paymentRepository.findByRazorpayOrderId(razorpayOrderId);
    }

    public Optional<CoursePayment> getPaymentByPaymentId(String razorpayPaymentId) {
        return paymentRepository.findByRazorpayPaymentId(razorpayPaymentId);
    }
}

