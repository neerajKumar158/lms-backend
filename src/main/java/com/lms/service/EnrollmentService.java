package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CoursePaymentRepository paymentRepository;

    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;

    public boolean isEnrolled(Long studentId, Long courseId) {
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        return enrollmentRepository.existsByStudentAndCourse(student, course);
    }

    @Transactional
    public CourseEnrollment enrollStudent(Long studentId, Long courseId) {
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if already enrolled
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new RuntimeException("Student is already enrolled in this course");
        }

        // Check if course is free or payment is completed
        if (course.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            // Paid course - check for successful payment
            boolean hasPayment = paymentRepository.findByStudent(student)
                    .stream()
                    .anyMatch(p -> p.getCourse().getId().equals(courseId) && "SUCCESS".equals(p.getStatus()));
            
            if (!hasPayment) {
                throw new RuntimeException("Payment required for this course");
            }
        }

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus("ACTIVE");
        enrollment.setProgressPercentage(0);
        enrollment.setLastAccessedAt(LocalDateTime.now());

        CourseEnrollment saved = enrollmentRepository.save(enrollment);

        // Send enrollment email notification
        try {
            if (emailNotificationService != null) {
                emailNotificationService.sendCourseEnrollmentEmail(studentId, course.getTitle());
            }
        } catch (Exception e) {
            System.err.println("Failed to send enrollment email: " + e.getMessage());
        }

        return saved;
    }

    public List<CourseEnrollment> getStudentEnrollments(Long studentId) {
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return enrollmentRepository.findByStudent(student);
    }

    public Optional<CourseEnrollment> getEnrollment(Long studentId, Long courseId) {
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return enrollmentRepository.findByStudentAndCourse(student, course);
    }

    @Transactional
    public void updateProgress(Long enrollmentId, Integer progressPercentage) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        
        enrollment.setProgressPercentage(Math.min(100, Math.max(0, progressPercentage)));
        enrollment.setLastAccessedAt(LocalDateTime.now());
        
        if (progressPercentage >= 100 && !"COMPLETED".equals(enrollment.getStatus())) {
            enrollment.setStatus("COMPLETED");
            enrollment.setCompletedAt(LocalDateTime.now());
        }
        
        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void markAsCompleted(Long enrollmentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        
        enrollment.setStatus("COMPLETED");
        enrollment.setProgressPercentage(100);
        enrollment.setCompletedAt(LocalDateTime.now());
        enrollmentRepository.save(enrollment);
    }
}

