package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import com.lms.repository.CourseEnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsByCourse(Long courseId) {
        System.out.println("Fetching assignments for course: " + courseId);
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        System.out.println("Found " + (assignments != null ? assignments.size() : 0) + " assignments for course " + courseId);
        
        // Initialize course relationship to avoid lazy loading issues
        if (assignments != null && !assignments.isEmpty()) {
            assignments.forEach(a -> {
                if (a.getCourse() != null) {
                    a.getCourse().getId(); // Trigger lazy loading
                }
                System.out.println("Assignment: " + a.getId() + " - " + a.getTitle() + " (Course: " + (a.getCourse() != null ? a.getCourse().getId() : "null") + ")");
            });
        } else {
            System.out.println("No assignments found for course " + courseId);
        }
        return assignments != null ? assignments : new java.util.ArrayList<>();
    }

    public Optional<Assignment> getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId);
    }

    @Transactional
    public Assignment createAssignment(Long courseId, Assignment assignment) {
        System.out.println("Creating assignment for course: " + courseId);
        System.out.println("Assignment title: " + assignment.getTitle());
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        assignment.setCourse(course);
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        
        Assignment saved = assignmentRepository.save(assignment);
        System.out.println("Assignment saved with ID: " + saved.getId());
        System.out.println("Assignment course ID: " + (saved.getCourse() != null ? saved.getCourse().getId() : "null"));
        
        // Verify it was saved correctly
        Optional<Assignment> verify = assignmentRepository.findById(saved.getId());
        if (verify.isPresent()) {
            System.out.println("Verified assignment exists in database with course ID: " + verify.get().getCourse().getId());
        } else {
            System.err.println("ERROR: Assignment was not found after saving!");
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
            System.err.println("Failed to send assignment deadline reminder emails: " + e.getMessage());
        }
        
        return saved;
    }

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

    @Transactional
    public void deleteAssignment(Long assignmentId) {
        assignmentRepository.deleteById(assignmentId);
    }

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
            System.err.println("Failed to create notification for teacher: " + e.getMessage());
        }
        
        return saved;
    }

    @Transactional
    public AssignmentSubmission updateSubmission(Long submissionId, AssignmentSubmission updatedSubmission) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        
        submission.setSubmissionText(updatedSubmission.getSubmissionText());
        submission.setSubmissionFileUrl(updatedSubmission.getSubmissionFileUrl());
        submission.setSubmittedAt(LocalDateTime.now());
        
        return submissionRepository.save(submission);
    }

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
            System.err.println("Failed to create notification: " + e.getMessage());
        }

        return saved;
    }

    public List<AssignmentSubmission> getSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    public List<AssignmentSubmission> getStudentSubmissions(Long studentId) {
        return submissionRepository.findByStudent(userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found")));
    }

    public Optional<AssignmentSubmission> getSubmission(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
    }
}

