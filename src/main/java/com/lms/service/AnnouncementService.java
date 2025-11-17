package com.lms.service;

import com.lms.domain.Course;
import com.lms.domain.CourseAnnouncement;
import com.lms.domain.CourseEnrollment;
import com.lms.domain.UserAccount;
import com.lms.repository.CourseAnnouncementRepository;
import com.lms.repository.CourseEnrollmentRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService {

    @Autowired
    private CourseAnnouncementRepository announcementRepository;

    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public List<CourseAnnouncement> getAnnouncementsByCourse(Long courseId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            // Get all announcements for the course, then filter out expired ones
            List<CourseAnnouncement> allAnnouncements = announcementRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
            
            System.out.println("Found " + allAnnouncements.size() + " total announcements for course " + courseId);
            
            // Initialize relationships to avoid lazy loading issues
            if (allAnnouncements != null) {
                allAnnouncements.forEach(announcement -> {
                    if (announcement.getCourse() != null) {
                        announcement.getCourse().getId(); // Trigger lazy loading
                    }
                    if (announcement.getInstructor() != null) {
                        announcement.getInstructor().getId(); // Trigger lazy loading
                    }
                });
            }
            
            // Filter out expired announcements
            List<CourseAnnouncement> activeAnnouncements = allAnnouncements.stream()
                    .filter(announcement -> {
                        if (announcement.getExpiresAt() == null) {
                            return true; // No expiration date, always show
                        }
                        boolean isActive = announcement.getExpiresAt().isAfter(now);
                        if (!isActive) {
                            System.out.println("Filtering out expired announcement: " + announcement.getTitle() + " (expired: " + announcement.getExpiresAt() + ")");
                        }
                        return isActive;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            System.out.println("Returning " + activeAnnouncements.size() + " active announcements for course " + courseId);
            if (!activeAnnouncements.isEmpty()) {
                System.out.println("First announcement title: " + activeAnnouncements.get(0).getTitle());
            }
            return activeAnnouncements;
        } catch (Exception e) {
            System.err.println("Error loading announcements for course " + courseId + ": " + e.getMessage());
            e.printStackTrace();
            // Fallback: return all announcements for the course
            return announcementRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
        }
    }

    @Transactional
    public CourseAnnouncement createAnnouncement(Long courseId, Long instructorId, CourseAnnouncement announcement) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        UserAccount instructor = userAccountRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        // Verify instructor owns the course
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("Only the course instructor can create announcements");
        }

        announcement.setCourse(course);
        announcement.setInstructor(instructor);
        announcement.setCreatedAt(LocalDateTime.now());
        
        CourseAnnouncement saved = announcementRepository.save(announcement);
        
        // Send email notifications to all enrolled students
        try {
            if (emailNotificationService != null) {
                List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
                for (CourseEnrollment enrollment : enrollments) {
                    emailNotificationService.sendCourseAnnouncementEmail(
                        enrollment.getStudent().getId(),
                        course.getTitle(),
                        saved.getTitle(),
                        saved.getContent()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send announcement emails: " + e.getMessage());
        }
        
        return saved;
    }

    @Transactional
    public void deleteAnnouncement(Long announcementId, Long instructorId) {
        CourseAnnouncement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("Announcement not found"));

        if (!announcement.getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("Only the announcement creator can delete it");
        }

        announcementRepository.delete(announcement);
    }
}

