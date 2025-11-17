package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CertificateService {

    @Autowired
    private CourseCertificateRepository certificateRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private ReportCardService reportCardService;

    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;

    @Transactional(readOnly = true)
    public List<CourseCertificate> getStudentCertificates(Long studentId) {
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return certificateRepository.findByStudent(student);
    }

    @Transactional(readOnly = true)
    public Optional<CourseCertificate> getCertificateByCourseAndStudent(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return certificateRepository.findByCourseAndStudent(course, student);
    }

    @Transactional
    public CourseCertificate issueCertificate(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Check if certificate already exists
        Optional<CourseCertificate> existing = certificateRepository.findByCourseAndStudent(course, student);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Check if student is enrolled and has completed the course
        CourseEnrollment enrollment = enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new RuntimeException("Student is not enrolled in this course"));

        // Get student's final grade
        Map<String, Object> reportCard = reportCardService.getStudentReportCardForCourse(studentId, courseId);
        Double finalScore = (Double) reportCard.getOrDefault("overallScore", 0.0);
        String grade = (String) reportCard.getOrDefault("letterGrade", "F");

        // Check if student passed (grade should be at least D)
        if (finalScore < 40) {
            throw new RuntimeException("Student has not met the minimum passing grade (40%) to receive a certificate");
        }

        // Generate unique certificate number
        String certificateNumber = "CERT-" + courseId + "-" + studentId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        CourseCertificate certificate = new CourseCertificate();
        certificate.setCourse(course);
        certificate.setStudent(student);
        certificate.setCertificateNumber(certificateNumber);
        certificate.setFinalScore(finalScore);
        certificate.setGrade(grade);
        certificate.setIssuedAt(LocalDateTime.now());
        certificate.setCompletedAt(enrollment.getLastAccessedAt() != null ? enrollment.getLastAccessedAt() : LocalDateTime.now());

        CourseCertificate saved = certificateRepository.save(certificate);

        // Send certificate email notification
        try {
            if (emailNotificationService != null) {
                emailNotificationService.sendCertificateIssuedEmail(studentId, course.getTitle(), certificateNumber);
            }
        } catch (Exception e) {
            System.err.println("Failed to send certificate email: " + e.getMessage());
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<CourseCertificate> getCertificateByNumber(String certificateNumber) {
        return certificateRepository.findByCertificateNumber(certificateNumber);
    }
}

