package com.lms.web;

import com.lms.domain.Refund;
import com.lms.domain.UserAccount;
import com.lms.repository.UserAccountRepository;
import com.lms.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lms/refunds")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private boolean isAdmin(User principal) {
        if (principal == null) return false;
        var user = userAccountRepository.findByEmail(principal.getUsername()).orElse(null);
        return user != null && user.getUserType() == UserAccount.UserType.ADMIN;
    }

    @PostMapping("/request/{paymentId}")
    public ResponseEntity<Map<String, Object>> requestRefund(
            @AuthenticationPrincipal User principal,
            @PathVariable Long paymentId,
            @RequestBody RefundRequest request) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Refund refund = refundService.requestRefund(user.getId(), paymentId, request.reason());
            return ResponseEntity.ok(Map.of(
                    "message", "Refund request submitted",
                    "refundId", refund.getId(),
                    "status", refund.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-refunds")
    public ResponseEntity<List<Refund>> getMyRefunds(@AuthenticationPrincipal User principal) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Refund> refunds = refundService.getStudentRefunds(user.getId());
            return ResponseEntity.ok(refunds);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRefunds(@AuthenticationPrincipal User principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            List<Refund> refunds = refundService.getPendingRefunds();
            return ResponseEntity.ok(refunds);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllRefunds(@AuthenticationPrincipal User principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            List<Refund> refunds = refundService.getAllRefunds();
            return ResponseEntity.ok(refunds);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{refundId}/approve")
    public ResponseEntity<Map<String, Object>> approveRefund(
            @AuthenticationPrincipal User principal,
            @PathVariable Long refundId,
            @RequestBody(required = false) ApproveRefundRequest request) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            Refund refund = refundService.approveRefund(refundId, 
                    request != null ? request.adminNotes() : null);
            return ResponseEntity.ok(Map.of(
                    "message", "Refund approved and processed",
                    "refundId", refund.getId(),
                    "status", refund.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{refundId}/reject")
    public ResponseEntity<Map<String, Object>> rejectRefund(
            @AuthenticationPrincipal User principal,
            @PathVariable Long refundId,
            @RequestBody RejectRefundRequest request) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            Refund refund = refundService.rejectRefund(refundId, request.adminNotes());
            return ResponseEntity.ok(Map.of(
                    "message", "Refund rejected",
                    "refundId", refund.getId(),
                    "status", refund.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    record RefundRequest(String reason) {}
    record ApproveRefundRequest(String adminNotes) {}
    record RejectRefundRequest(String adminNotes) {}
}

