package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProgressVisualizationService {

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private AssignmentSubmissionRepository assignmentSubmissionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentProgressData(Long studentId, Long courseId) {
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseEnrollment enrollment = enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new RuntimeException("Student not enrolled in course"));

        // Get all quizzes and assignments
        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);

        // Quiz progress
        List<Map<String, Object>> quizProgress = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quiz.getId()).stream()
                    .filter(a -> a.getStudent().getId().equals(studentId))
                    .collect(Collectors.toList());
            
            Optional<QuizAttempt> bestAttempt = attempts.stream()
                    .max(Comparator.comparing(a -> a.getScore() != null ? a.getScore() : 0));
            
            Map<String, Object> quizData = new HashMap<>();
            quizData.put("quizId", quiz.getId());
            quizData.put("quizTitle", quiz.getTitle());
            quizData.put("totalMarks", quiz.getTotalMarks());
            quizData.put("attemptsCount", attempts.size());
            quizData.put("completed", !attempts.isEmpty());
            if (bestAttempt.isPresent()) {
                quizData.put("bestScore", bestAttempt.get().getScore());
                quizData.put("bestPercentage", bestAttempt.get().getPercentage());
            } else {
                quizData.put("bestScore", 0);
                quizData.put("bestPercentage", 0);
            }
            quizProgress.add(quizData);
        }

        // Assignment progress
        List<Map<String, Object>> assignmentProgress = new ArrayList<>();
        for (Assignment assignment : assignments) {
            Optional<AssignmentSubmission> submission = assignmentSubmissionRepository
                    .findByAssignmentIdAndStudentId(assignment.getId(), studentId);
            
            Map<String, Object> assignmentData = new HashMap<>();
            assignmentData.put("assignmentId", assignment.getId());
            assignmentData.put("assignmentTitle", assignment.getTitle());
            assignmentData.put("maxScore", assignment.getMaxScore());
            assignmentData.put("submitted", submission.isPresent());
            if (submission.isPresent()) {
                AssignmentSubmission sub = submission.get();
                assignmentData.put("score", sub.getScore() != null ? sub.getScore() : 0);
                assignmentData.put("graded", sub.getStatus() == AssignmentSubmission.SubmissionStatus.GRADED);
            } else {
                assignmentData.put("score", 0);
                assignmentData.put("graded", false);
            }
            assignmentProgress.add(assignmentData);
        }

        // Overall progress
        int totalQuizzes = quizzes.size();
        int completedQuizzes = (int) quizProgress.stream().filter(q -> (Boolean) q.get("completed")).count();
        int totalAssignments = assignments.size();
        int submittedAssignments = (int) assignmentProgress.stream().filter(a -> (Boolean) a.get("submitted")).count();

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("courseId", courseId);
        progressData.put("courseTitle", course.getTitle());
        progressData.put("overallProgress", enrollment.getProgressPercentage() != null ? enrollment.getProgressPercentage() : 0);
        progressData.put("quizzes", quizProgress);
        progressData.put("assignments", assignmentProgress);
        progressData.put("quizCompletion", Map.of(
                "completed", completedQuizzes,
                "total", totalQuizzes,
                "percentage", totalQuizzes > 0 ? Math.round((double) completedQuizzes / totalQuizzes * 100) : 0
        ));
        progressData.put("assignmentCompletion", Map.of(
                "submitted", submittedAssignments,
                "total", totalAssignments,
                "percentage", totalAssignments > 0 ? Math.round((double) submittedAssignments / totalAssignments * 100) : 0
        ));

        return progressData;
    }

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;
}



