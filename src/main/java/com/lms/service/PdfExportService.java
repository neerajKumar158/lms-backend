package com.lms.service;

import com.lms.domain.Course;
import com.lms.domain.CourseCertificate;
import com.lms.domain.UserAccount;
import com.lms.repository.CourseRepository;
import com.lms.repository.CourseCertificateRepository;
import com.lms.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class PdfExportService {

    @Autowired
    private ReportCardService reportCardService;

    @Autowired
    private CourseCertificateRepository certificateRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    /**
     * Export report card as PDF
     * Note: This is a simplified implementation. For production, use a library like iText or Apache PDFBox
     */
    public byte[] exportReportCardToPdf(Long studentId, Long courseId) throws IOException {
        Map<String, Object> reportCard = reportCardService.getStudentReportCardForCourse(studentId, courseId);
        
        // Generate HTML content for the report card
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        htmlContent.append("<style>");
        htmlContent.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        htmlContent.append("h1 { color: #333; border-bottom: 2px solid #333; padding-bottom: 10px; }");
        htmlContent.append("h2 { color: #666; margin-top: 20px; }");
        htmlContent.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        htmlContent.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        htmlContent.append("th { background-color: #f2f2f2; font-weight: bold; }");
        htmlContent.append(".summary { background-color: #f9f9f9; padding: 15px; margin: 20px 0; border-radius: 5px; }");
        htmlContent.append(".grade { font-size: 24px; font-weight: bold; color: #28a745; }");
        htmlContent.append("</style></head><body>");
        
        htmlContent.append("<h1>REPORT CARD</h1>");
        htmlContent.append("<div class='summary'>");
        htmlContent.append("<p><strong>Course:</strong> ").append(escapeHtml(String.valueOf(reportCard.get("courseTitle")))).append("</p>");
        htmlContent.append("<p><strong>Student:</strong> ").append(escapeHtml(String.valueOf(reportCard.get("studentName")))).append("</p>");
        htmlContent.append("<p><strong>Date:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("</p>");
        htmlContent.append("<p><strong>Overall Score:</strong> <span class='grade'>").append(reportCard.get("overallScore")).append("%</span></p>");
        htmlContent.append("<p><strong>Letter Grade:</strong> <span class='grade'>").append(reportCard.get("letterGrade")).append("</span></p>");
        htmlContent.append("</div>");
        
        // Quiz scores
        if (reportCard.get("quizScores") != null) {
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> quizScores = (java.util.List<Map<String, Object>>) reportCard.get("quizScores");
            if (quizScores != null && !quizScores.isEmpty()) {
                htmlContent.append("<h2>Quiz Scores</h2>");
                htmlContent.append("<table>");
                htmlContent.append("<tr><th>Quiz</th><th>Score</th><th>Max Score</th><th>Percentage</th><th>Status</th><th>Date</th></tr>");
                for (Map<String, Object> quiz : quizScores) {
                    htmlContent.append("<tr>");
                    htmlContent.append("<td>").append(escapeHtml(String.valueOf(quiz.get("quizTitle")))).append("</td>");
                    htmlContent.append("<td>").append(quiz.get("score")).append("</td>");
                    htmlContent.append("<td>").append(quiz.get("maxScore")).append("</td>");
                    htmlContent.append("<td>").append(quiz.get("percentage")).append("%</td>");
                    htmlContent.append("<td>").append(Boolean.TRUE.equals(quiz.get("passed")) ? "Passed" : "Failed").append("</td>");
                    htmlContent.append("<td>").append(formatDate(quiz.get("attemptDate"))).append("</td>");
                    htmlContent.append("</tr>");
                }
                htmlContent.append("</table>");
            }
        }
        
        // Assignment scores
        if (reportCard.get("assignmentScores") != null) {
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> assignmentScores = (java.util.List<Map<String, Object>>) reportCard.get("assignmentScores");
            if (assignmentScores != null && !assignmentScores.isEmpty()) {
                htmlContent.append("<h2>Assignment Scores</h2>");
                htmlContent.append("<table>");
                htmlContent.append("<tr><th>Assignment</th><th>Score</th><th>Max Score</th><th>Percentage</th><th>Status</th><th>Submitted</th><th>Graded</th></tr>");
                for (Map<String, Object> assignment : assignmentScores) {
                    htmlContent.append("<tr>");
                    htmlContent.append("<td>").append(escapeHtml(String.valueOf(assignment.get("assignmentTitle")))).append("</td>");
                    htmlContent.append("<td>").append(assignment.get("score")).append("</td>");
                    htmlContent.append("<td>").append(assignment.get("maxScore")).append("</td>");
                    htmlContent.append("<td>").append(assignment.get("percentage")).append("%</td>");
                    htmlContent.append("<td>").append(assignment.get("status")).append("</td>");
                    htmlContent.append("<td>").append(formatDate(assignment.get("submittedDate"))).append("</td>");
                    htmlContent.append("<td>").append(formatDate(assignment.get("gradedDate"), "Not Graded")).append("</td>");
                    htmlContent.append("</tr>");
                }
                htmlContent.append("</table>");
            }
        }
        
        htmlContent.append("</body></html>");
        
        // Convert HTML to PDF-like format (simplified - in production use iText or Apache PDFBox)
        // For now, return HTML content that browsers can render as PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(htmlContent.toString().getBytes("UTF-8"));
        return baos.toByteArray();
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    private String formatDate(Object dateObj) {
        return formatDate(dateObj, "N/A");
    }
    
    private String formatDate(Object dateObj, String defaultValue) {
        if (dateObj == null) return defaultValue;
        try {
            if (dateObj instanceof java.time.LocalDateTime) {
                return ((java.time.LocalDateTime) dateObj).format(DateTimeFormatter.ISO_LOCAL_DATE);
            } else if (dateObj instanceof java.util.Date) {
                return ((java.util.Date) dateObj).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
            } else if (dateObj instanceof String) {
                // Try to parse as ISO date
                try {
                    return java.time.LocalDateTime.parse(dateObj.toString()).format(DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    return dateObj.toString();
                }
            }
            return dateObj.toString();
        } catch (Exception e) {
            return dateObj.toString();
        }
    }

    /**
     * Export certificate as PDF
     */
    public byte[] exportCertificateToPdf(String certificateNumber) throws IOException {
        CourseCertificate certificate = certificateRepository.findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        
        // For now, return a simple text representation
        // In production, use iText or Apache PDFBox to generate actual PDF
        StringBuilder pdfContent = new StringBuilder();
        pdfContent.append("CERTIFICATE OF COMPLETION\n");
        pdfContent.append("=========================\n\n");
        pdfContent.append("This is to certify that\n\n");
        pdfContent.append(certificate.getStudent().getName() != null ? 
                certificate.getStudent().getName() : certificate.getStudent().getEmail()).append("\n\n");
        pdfContent.append("has successfully completed the course\n\n");
        pdfContent.append(certificate.getCourse().getTitle()).append("\n\n");
        pdfContent.append("Certificate Number: ").append(certificateNumber).append("\n");
        pdfContent.append("Grade: ").append(certificate.getGrade()).append("\n");
        pdfContent.append("Score: ").append(certificate.getFinalScore()).append("%\n");
        pdfContent.append("Date: ").append(certificate.getIssuedAt().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(pdfContent.toString().getBytes());
        return baos.toByteArray();
    }
}



