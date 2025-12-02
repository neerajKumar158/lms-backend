package com.lms.web;

import com.lms.domain.CourseCertificate;
import com.lms.repository.UserAccountRepository;
import com.lms.service.CertificateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/lms/certificates")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping("/student")
    public ResponseEntity<?> getStudentCertificates(@AuthenticationPrincipal User principal) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<CourseCertificate> certificates = certificateService.getStudentCertificates(user.getId());
            return ResponseEntity.ok(certificates);
        } catch (Exception e) {
            log.error("Failed to load certificates for principal {}: {}", principal != null ? principal.getUsername() : "unknown", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load certificates"));
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCertificateForCourse(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Optional<CourseCertificate> certificate = certificateService.getCertificateByCourseAndStudent(courseId, user.getId());
            if (certificate.isPresent()) {
                return ResponseEntity.ok(certificate.get());
            } else {
                return ResponseEntity.ok(Map.of("message", "Certificate not yet issued"));
            }
        } catch (Exception e) {
            log.error("Failed to load certificate for course {} (principal={}): {}", courseId,
                    principal != null ? principal.getUsername() : "unknown", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load certificate"));
        }
    }

    @PostMapping("/course/{courseId}/issue")
    public ResponseEntity<?> issueCertificate(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            CourseCertificate certificate = certificateService.issueCertificate(courseId, user.getId());
            return ResponseEntity.ok(Map.of(
                    "id", certificate.getId(),
                    "certificateNumber", certificate.getCertificateNumber(),
                    "message", "Certificate issued successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to issue certificate for course {} (principal={}): {}", courseId,
                    principal != null ? principal.getUsername() : "unknown", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to issue certificate"));
        }
    }

    @GetMapping("/verify/{certificateNumber}")
    public ResponseEntity<?> verifyCertificate(@PathVariable("certificateNumber") String certificateNumber) {
        try {
            Optional<CourseCertificate> certificate = certificateService.getCertificateByNumber(certificateNumber);
            if (certificate.isPresent()) {
                CourseCertificate cert = certificate.get();
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "studentName", cert.getStudent().getName() != null ? cert.getStudent().getName() : cert.getStudent().getEmail(),
                        "courseTitle", cert.getCourse().getTitle(),
                        "grade", cert.getGrade(),
                        "issuedAt", cert.getIssuedAt()
                ));
            } else {
                return ResponseEntity.ok(Map.of("valid", false, "message", "Certificate not found"));
            }
        } catch (Exception e) {
            log.error("Failed to verify certificate {}: {}", certificateNumber, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to verify certificate"));
        }
    }
}



