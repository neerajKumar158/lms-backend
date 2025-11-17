package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

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

    public Map<String, Object> createPaymentOrder(Long studentId, Long courseId) {
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (course.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Course is free. No payment required.");
        }

        // Create payment record
        CoursePayment payment = new CoursePayment();
        payment.setStudent(student);
        payment.setCourse(course);
        payment.setAmount(course.getPrice());
        payment.setStatus("PENDING");
        payment.setCreatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // Create Razorpay order
        Map<String, Object> razorpayOrder = razorpayService.createOrder(
                course.getPrice(),
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
            System.err.println("Enrollment error (may already exist): " + e.getMessage());
        }

        // Send payment confirmation email
        try {
            if (emailNotificationService != null) {
                emailNotificationService.sendPaymentConfirmationEmail(
                    payment.getStudent().getId(),
                    payment.getCourse().getTitle(),
                    payment.getAmount(),
                    razorpayPaymentId
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to send payment confirmation email: " + e.getMessage());
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

