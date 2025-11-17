package com.lms.web;

import com.lms.domain.Assignment;
import com.lms.domain.AssignmentSubmission;
import com.lms.repository.UserAccountRepository;
import com.lms.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lms/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping("/course/{courseId}")
    public List<Assignment> getAssignmentsByCourse(@PathVariable("courseId") Long courseId) {
        return assignmentService.getAssignmentsByCourse(courseId);
    }

    @GetMapping("/course/{courseId}/with-status")
    public ResponseEntity<?> getAssignmentsByCourseWithStatus(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Assignment> assignments = assignmentService.getAssignmentsByCourse(courseId);
            List<Map<String, Object>> assignmentsWithStatus = assignments.stream()
                    .map(assignment -> {
                        Map<String, Object> assignmentMap = new java.util.HashMap<>();
                        assignmentMap.put("id", assignment.getId());
                        assignmentMap.put("title", assignment.getTitle());
                        assignmentMap.put("description", assignment.getDescription());
                        assignmentMap.put("instructions", assignment.getInstructions());
                        assignmentMap.put("maxScore", assignment.getMaxScore());
                        assignmentMap.put("dueDate", assignment.getDueDate());
                        assignmentMap.put("startDate", assignment.getStartDate());
                        assignmentMap.put("allowLateSubmission", assignment.getAllowLateSubmission());
                        assignmentMap.put("latePenaltyPercent", assignment.getLatePenaltyPercent());
                        assignmentMap.put("type", assignment.getType() != null ? assignment.getType().name() : null);
                        assignmentMap.put("attachmentUrl", assignment.getAttachmentUrl());
                        
                        // Check if student has submitted
                        var submission = assignmentService.getSubmission(assignment.getId(), user.getId());
                        assignmentMap.put("isSubmitted", submission.isPresent());
                        assignmentMap.put("isGraded", submission.isPresent() && 
                                submission.get().getStatus() == AssignmentSubmission.SubmissionStatus.GRADED);
                        if (submission.isPresent()) {
                            assignmentMap.put("score", submission.get().getScore());
                            assignmentMap.put("status", submission.get().getStatus().name());
                        }
                        
                        return assignmentMap;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(assignmentsWithStatus);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load assignments"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable("id") Long id) {
        return assignmentService.getAssignmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> createAssignment(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId,
            @RequestBody CreateAssignmentRequest request) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Assignment assignment = new Assignment();
            assignment.setTitle(request.title());
            assignment.setDescription(request.description());
            assignment.setInstructions(request.instructions());
            assignment.setMaxScore(request.maxScore());
            assignment.setDueDate(request.dueDate());
            assignment.setStartDate(request.startDate());
            assignment.setAllowLateSubmission(request.allowLateSubmission());
            assignment.setLatePenaltyPercent(request.latePenaltyPercent());
            if (request.type() != null) {
                assignment.setType(Assignment.AssignmentType.valueOf(request.type()));
            }
            assignment.setAttachmentUrl(request.attachmentUrl());

            Assignment created = assignmentService.createAssignment(courseId, assignment);
            return ResponseEntity.ok(Map.of("id", created.getId(), "message", "Assignment created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAssignment(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long id,
            @RequestBody CreateAssignmentRequest request) {
        try {
            Assignment assignment = assignmentService.getAssignmentById(id)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));
            
            Assignment updated = new Assignment();
            updated.setTitle(request.title());
            updated.setDescription(request.description());
            updated.setInstructions(request.instructions());
            updated.setMaxScore(request.maxScore());
            updated.setDueDate(request.dueDate());
            updated.setStartDate(request.startDate());
            updated.setAllowLateSubmission(request.allowLateSubmission());
            updated.setLatePenaltyPercent(request.latePenaltyPercent());
            if (request.type() != null) {
                updated.setType(Assignment.AssignmentType.valueOf(request.type()));
            }
            updated.setAttachmentUrl(request.attachmentUrl());

            assignmentService.updateAssignment(id, updated);
            return ResponseEntity.ok(Map.of("message", "Assignment updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAssignment(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.ok(Map.of("message", "Assignment deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, Object>> submitAssignment(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long assignmentId,
            @RequestBody SubmitAssignmentRequest request) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            AssignmentSubmission submission = new AssignmentSubmission();
            submission.setSubmissionText(request.content());
            submission.setSubmissionFileUrl(request.attachmentUrl());

            AssignmentSubmission created = assignmentService.submitAssignment(assignmentId, user.getId(), submission);
            return ResponseEntity.ok(Map.of("id", created.getId(), "message", "Assignment submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/submissions")
    public List<AssignmentSubmission> getSubmissions(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long assignmentId) {
        return assignmentService.getSubmissionsByAssignment(assignmentId);
    }

    @GetMapping("/my-submissions")
    public List<AssignmentSubmission> getMySubmissions(@AuthenticationPrincipal User principal) {
        var user = userAccountRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return assignmentService.getStudentSubmissions(user.getId());
    }

    @PostMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<Map<String, Object>> gradeSubmission(
            @AuthenticationPrincipal User principal,
            @PathVariable("submissionId") Long submissionId,
            @RequestBody GradeSubmissionRequest request) {
        try {
            assignmentService.gradeSubmission(submissionId, request.score(), request.feedback());
            return ResponseEntity.ok(Map.of("message", "Submission graded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    record CreateAssignmentRequest(String title, String description, String instructions,
                                  Integer maxScore,
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                  Boolean allowLateSubmission, Integer latePenaltyPercent,
                                  String type, String attachmentUrl) {}
    
    record SubmitAssignmentRequest(String content, String attachmentUrl) {}
    
    record GradeSubmissionRequest(Integer score, String feedback) {}
}

