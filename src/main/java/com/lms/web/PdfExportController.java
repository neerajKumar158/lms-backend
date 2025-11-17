package com.lms.web;

import com.lms.repository.UserAccountRepository;
import com.lms.service.PdfExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lms/export")
public class PdfExportController {

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping("/report-card/course/{courseId}")
    public ResponseEntity<?> exportReportCard(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId,
            @RequestParam(value = "studentId", required = false) Long studentId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            
            if (courseId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Course ID is required"));
            }

            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Long targetStudentId = studentId != null ? studentId : user.getId();

            byte[] pdfBytes = pdfExportService.exportReportCardToPdf(targetStudentId, courseId);

            HttpHeaders headers = new HttpHeaders();
            // Note: Currently returns HTML content, not actual PDF binary
            // For production, use a PDF library like iText or Apache PDFBox
            headers.setContentType(MediaType.TEXT_HTML);
            headers.setContentDispositionFormData("attachment", "report-card-" + courseId + ".html");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/certificate/{certificateNumber}")
    public ResponseEntity<?> exportCertificate(@PathVariable("certificateNumber") String certificateNumber) {
        try {
            byte[] pdfBytes = pdfExportService.exportCertificateToPdf(certificateNumber);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "certificate-" + certificateNumber + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}



