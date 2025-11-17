package com.lms.web;

import com.lms.repository.UserAccountRepository;
import com.lms.service.CoursePaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lms/payments")
public class CoursePaymentController {

    @Autowired
    private CoursePaymentService paymentService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @PostMapping("/create-order/{courseId}")
    public ResponseEntity<Map<String, Object>> createPaymentOrder(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> order = paymentService.createPaymentOrder(user.getId(), courseId);
            
            if (order.containsKey("error")) {
                return ResponseEntity.badRequest().body(order);
            }
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify/{orderId}")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @PathVariable("orderId") String orderId,
            @RequestBody VerifyPaymentRequest request) {
        try {
            boolean verified = paymentService.verifyAndCompletePayment(
                    request.razorpayOrderId(),
                    request.razorpayPaymentId(),
                    request.razorpaySignature()
            );
            
            if (verified) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Payment verified and enrollment completed"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment verification failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    record VerifyPaymentRequest(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {}
}

