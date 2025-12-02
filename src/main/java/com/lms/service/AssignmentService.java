package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import com.lms.repository.CourseEnrollmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Handles assignment management and submissions. This service manages assignment
 * creation, submission processing, grading, late submission handling, and
 * notification sending for assignment workflow management.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Slf4j
@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentSubmissionRepository submissionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private NotificationService notificationService;

    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    /**
     * Retrieves all assignments for a specific course.
     *
     * @param courseId the course ID
     * @return the list of assignments for the course
     */
    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsByCourse(Long courseId) {
        log.info("Fetching assignments for course: {}", courseId);
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        log.info("Found {} assignments for course {}", (assignments != null ? assignments.size() : 0), courseId);
        
        // Initialize course relationship to avoid lazy loading issues
        if (assignments != null && !assignments.isEmpty()) {
            assignments.forEach(a -> {
                if (a.getCourse() != null) {
                    a.getCourse().getId(); // Trigger lazy loading
                }
                log.debug("Assignment: {} - {} (Course: {})",
                        a.getId(),
                        a.getTitle(),
                        (a.getCourse() != null ? a.getCourse().getId() : "null"));
            });
        } else {
            log.info("No assignments found for course {}", courseId);
        }
        return assignments != null ? assignments : new java.util.ArrayList<>();
    }

    /**
     * Retrieves an assignment by its ID.
     *
     * @param assignmentId the assignment ID
     * @return the Optional containing the assignment if found, empty otherwise
     */
    public Optional<Assignment> getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId);
    }

    /**
     * Creates a new assignment for a course and sends deadline reminders to enrolled students.
     *
     * @param courseId the course ID
     * @param assignment the assignment entity to create
     * @return the created assignment entity
     */
    @Transactional
    public Assignment createAssignment(Long courseId, Assignment assignment) {
        log.info("Creating assignment for course: {}, title: {}", courseId, assignment.getTitle());
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        assignment.setCourse(course);
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        
        Assignment saved = assignmentRepository.save(assignment);
        log.info("Assignment saved with ID: {}, courseId: {}",
                saved.getId(),
                (saved.getCourse() != null ? saved.getCourse().getId() : "null"));
        
        // Verify it was saved correctly
        Optional<Assignment> verify = assignmentRepository.findById(saved.getId());
        if (verify.isPresent()) {
            log.debug("Verified assignment {} exists in database with course ID: {}",
                    verify.get().getId(), verify.get().getCourse().getId());
        } else {
            log.error("Assignment was not found after saving! ID: {}", saved.getId());
        }
        
        // Send deadline reminder emails to enrolled students (if due date is in future)
        try {
            if (emailNotificationService != null && saved.getDueDate() != null && 
                saved.getDueDate().isAfter(LocalDateTime.now())) {
                List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
                for (CourseEnrollment enrollment : enrollments) {
                    emailNotificationService.sendAssignmentDeadlineReminderEmail(
                        enrollment.getStudent().getId(),
                        course.getTitle(),
                        saved.getTitle(),
                        saved.getDueDate()
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to send assignment deadline reminder emails for course {} and assignment {}: {}",
                    courseId, saved.getId(), e.getMessage(), e);
        }
        
        return saved;
    }

    /**
     * Updates an existing assignment with new details.
     *
     * @param assignmentId the assignment ID
     * @param updatedAssignment the assignment entity with updated fields
     * @return the updated assignment entity
     */
    @Transactional
    public Assignment updateAssignment(Long assignmentId, Assignment updatedAssignment) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        assignment.setTitle(updatedAssignment.getTitle());
        assignment.setDescription(updatedAssignment.getDescription());
        assignment.setInstructions(updatedAssignment.getInstructions());
        assignment.setMaxScore(updatedAssignment.getMaxScore());
        assignment.setDueDate(updatedAssignment.getDueDate());
        assignment.setStartDate(updatedAssignment.getStartDate());
        assignment.setAllowLateSubmission(updatedAssignment.getAllowLateSubmission());
        assignment.setLatePenaltyPercent(updatedAssignment.getLatePenaltyPercent());
        assignment.setType(updatedAssignment.getType());
        assignment.setAttachmentUrl(updatedAssignment.getAttachmentUrl());
        assignment.setUpdatedAt(LocalDateTime.now());
        
        return assignmentRepository.save(assignment);
    }

    /**
     * Deletes an assignment by its ID.
     *
     * @param assignmentId the assignment ID
     */
    @Transactional
    public void deleteAssignment(Long assignmentId) {
        assignmentRepository.deleteById(assignmentId);
    }

    /**
     * Submits an assignment for a student with late submission checking and notification.
     *
     * @param assignmentId the assignment ID
     * @param studentId the student user ID
     * @param submission the submission entity
     * @return the created submission entity
     */
    @Transactional
    public AssignmentSubmission submitAssignment(Long assignmentId, Long studentId, AssignmentSubmission submission) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Check if student is enrolled in the course
        if (!enrollmentService.isEnrolled(studentId, assignment.getCourse().getId())) {
            throw new RuntimeException("Student is not enrolled in this course");
        }
        
        // Check if already submitted
        Optional<AssignmentSubmission> existing = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
        if (existing.isPresent()) {
            throw new RuntimeException("Assignment already submitted. Use update instead.");
        }
        
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(AssignmentSubmission.SubmissionStatus.SUBMITTED);
        
        // Check if late submission
        if (assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate())) {
            if (!assignment.getAllowLateSubmission()) {
                throw new RuntimeException("Late submissions are not allowed for this assignment");
            }
            submission.setIsLate(true);
        }
        
        AssignmentSubmission saved = submissionRepository.save(submission);
        
        // Create notification for teacher/instructor
        try {
            if (assignment.getCourse().getInstructor() != null) {
                notificationService.createNotification(
                    assignment.getCourse().getInstructor().getId(),
                    "New Assignment Submission",
                    "Student " + student.getName() + " submitted assignment '" + assignment.getTitle() + "'",
                    com.lms.domain.Notification.NotificationType.ASSIGNMENT,
                    "/ui/lms/assignment/grade?assignmentId=" + assignment.getId()
                );
            }
        } catch (Exception e) {
            // Log but don't fail the submission
            log.error("Failed to create assignment submission notification for assignment {} and student {}: {}",
                    assignmentId, studentId, e.getMessage(), e);
        }
        
        return saved;
    }

    /**
     * Updates an existing submission with new content.
     *
     * @param submissionId the submission ID
     * @param updatedSubmission the submission entity with updated fields
     * @return the updated submission entity
     */
    @Transactional
    public AssignmentSubmission updateSubmission(Long submissionId, AssignmentSubmission updatedSubmission) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        
        submission.setSubmissionText(updatedSubmission.getSubmissionText());
        submission.setSubmissionFileUrl(updatedSubmission.getSubmissionFileUrl());
        submission.setSubmittedAt(LocalDateTime.now());
        
        return submissionRepository.save(submission);
    }

    /**
     * Grades a submission and sends notification to the student.
     *
     * @param submissionId the submission ID
     * @param score the score awarded
     * @param feedback the feedback provided
     * @return the graded submission entity
     */
    @Transactional
    public AssignmentSubmission gradeSubmission(Long submissionId, Integer score, String feedback) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        
        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setGradedAt(LocalDateTime.now());
        submission.setStatus(AssignmentSubmission.SubmissionStatus.GRADED);
        
        AssignmentSubmission saved = submissionRepository.save(submission);

        // Create notification for student
        try {
            notificationService.createNotification(
                submission.getStudent().getId(),
                "Assignment Graded",
                "Your assignment '" + submission.getAssignment().getTitle() + "' has been graded. Score: " + score + "/" + submission.getAssignment().getMaxScore(),
                com.lms.domain.Notification.NotificationType.ASSIGNMENT,
                "/ui/lms/course/detail?id=" + submission.getAssignment().getCourse().getId()
            );
            
            // Send email notification
            if (emailNotificationService != null) {
                emailNotificationService.sendGradeNotificationEmail(
                    submission.getStudent().getId(),
                    submission.getAssignment().getCourse().getTitle(),
                    submission.getAssignment().getTitle(),
                    score,
                    submission.getAssignment().getMaxScore()
                );
            }
        } catch (Exception e) {
            // Log but don't fail the grading
            log.error("Failed to create graded assignment notification for submission {}: {}",
                    submissionId, e.getMessage(), e);
        }

        return saved;
    }

    /**
     * Retrieves all submissions for a specific assignment.
     *
     * @param assignmentId the assignment ID
     * @return the list of submissions for the assignment
     */
    public List<AssignmentSubmission> getSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    /**
     * Retrieves all submissions for a specific student.
     *
     * @param studentId the student user ID
     * @return the list of submissions for the student
     */
    public List<AssignmentSubmission> getStudentSubmissions(Long studentId) {
        return submissionRepository.findByStudent(userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found")));
    }

    /**
     * Retrieves a specific submission by assignment and student.
     *
     * @param assignmentId the assignment ID
     * @param studentId the student user ID
     * @return the Optional containing the submission if found, empty otherwise
     */
    public Optional<AssignmentSubmission> getSubmission(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
    }
}

