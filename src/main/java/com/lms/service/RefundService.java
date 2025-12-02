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

/**
 * Refund Service
 * Manages refund requests and processing
 */
@Service
public class RefundService {

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private CoursePaymentRepository paymentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private RazorpayService razorpayService;

    /**
     * Request a refund for a course payment
     */
    @Transactional
    public Refund requestRefund(Long studentId, Long paymentId, String reason) {
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        CoursePayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Verify payment belongs to student
        if (!payment.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Payment does not belong to this student");
        }

        // Verify payment was successful
        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new RuntimeException("Refund can only be requested for successful payments");
        }

        // Check if refund already exists
        Optional<Refund> existingRefund = refundRepository.findByPayment(payment);
        if (existingRefund.isPresent()) {
            throw new RuntimeException("Refund request already exists for this payment");
        }

        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setStudent(student);
        refund.setCourse(payment.getCourse());
        refund.setAmount(payment.getAmount());
        refund.setReason(reason);
        refund.setStatus(Refund.RefundStatus.PENDING);
        refund.setRequestedAt(LocalDateTime.now());

        return refundRepository.save(refund);
    }

    /**
     * Approve a refund request (admin only)
     */
    @Transactional
    public Refund approveRefund(Long refundId, String adminNotes) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        if (refund.getStatus() != Refund.RefundStatus.PENDING) {
            throw new RuntimeException("Refund is not in pending status");
        }

        refund.setStatus(Refund.RefundStatus.APPROVED);
        refund.setAdminNotes(adminNotes);
        refundRepository.save(refund);

        // Process refund through Razorpay if payment was made through Razorpay
        if (refund.getPayment().getRazorpayPaymentId() != null) {
            try {
                // Create refund in Razorpay
                String razorpayRefundId = razorpayService.createRefund(
                        refund.getPayment().getRazorpayPaymentId(),
                        refund.getAmount()
                );
                refund.setRazorpayRefundId(razorpayRefundId);
                refund.setStatus(Refund.RefundStatus.PROCESSED);
                refund.setProcessedAt(LocalDateTime.now());
            } catch (Exception e) {
                refund.setStatus(Refund.RefundStatus.FAILED);
                refund.setAdminNotes((adminNotes != null ? adminNotes + " " : "") + 
                        "Refund processing failed: " + e.getMessage());
            }
        } else {
            // Manual refund processing
            refund.setStatus(Refund.RefundStatus.PROCESSED);
            refund.setProcessedAt(LocalDateTime.now());
        }

        refundRepository.save(refund);

        // Unenroll student from course if refund is processed
        if (refund.getStatus() == Refund.RefundStatus.PROCESSED) {
            try {
                CourseEnrollment enrollment = enrollmentRepository
                        .findByStudentAndCourse(refund.getStudent(), refund.getCourse())
                        .orElse(null);
                if (enrollment != null) {
                    enrollment.setStatus("CANCELLED");
                    enrollmentRepository.save(enrollment);
                }
            } catch (Exception e) {
                System.err.println("Failed to unenroll student after refund: " + e.getMessage());
            }
        }

        return refund;
    }

    /**
     * Reject a refund request (admin only)
     */
    @Transactional
    public Refund rejectRefund(Long refundId, String adminNotes) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        if (refund.getStatus() != Refund.RefundStatus.PENDING) {
            throw new RuntimeException("Refund is not in pending status");
        }

        refund.setStatus(Refund.RefundStatus.REJECTED);
        refund.setAdminNotes(adminNotes);
        refund.setProcessedAt(LocalDateTime.now());
        return refundRepository.save(refund);
    }

    /**
     * Get refunds for a student
     */
    @Transactional(readOnly = true)
    public List<Refund> getStudentRefunds(Long studentId) {
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return refundRepository.findByStudent(student);
    }

    /**
     * Get all pending refunds (admin)
     */
    @Transactional(readOnly = true)
    public List<Refund> getPendingRefunds() {
        return refundRepository.findByStatus(Refund.RefundStatus.PENDING);
    }

    /**
     * Get all refunds (admin)
     */
    @Transactional(readOnly = true)
    public List<Refund> getAllRefunds() {
        return refundRepository.findAll();
    }
}



