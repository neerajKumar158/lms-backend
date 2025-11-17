package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportCardService {

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

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentReportCard(Long studentId) {
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent(student);
        
        List<Map<String, Object>> courseGrades = new ArrayList<>();
        double totalWeightedScore = 0;
        int totalWeight = 0;

        for (CourseEnrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            Map<String, Object> courseGrade = calculateCourseGrade(course.getId(), studentId);
            courseGrade.put("courseId", course.getId());
            courseGrade.put("courseTitle", course.getTitle());
            courseGrade.put("courseCode", course.getTitle().substring(0, Math.min(10, course.getTitle().length())));
            courseGrade.put("enrollmentDate", enrollment.getEnrolledAt());
            courseGrade.put("progress", enrollment.getProgressPercentage() != null ? enrollment.getProgressPercentage() : 0);
            
            courseGrades.add(courseGrade);
            
            // Calculate weighted average
            int weight = 1; // Default weight
            double courseScore = (Double) courseGrade.getOrDefault("overallScore", 0.0);
            totalWeightedScore += courseScore * weight;
            totalWeight += weight;
        }

        double overallGPA = totalWeight > 0 ? totalWeightedScore / totalWeight : 0.0;
        String grade = calculateLetterGrade(overallGPA);

        Map<String, Object> reportCard = new HashMap<>();
        reportCard.put("studentId", studentId);
        reportCard.put("studentName", student.getName() != null ? student.getName() : student.getEmail());
        reportCard.put("studentEmail", student.getEmail());
        reportCard.put("overallGPA", Math.round(overallGPA * 100.0) / 100.0);
        reportCard.put("overallGrade", grade);
        reportCard.put("totalCourses", enrollments.size());
        reportCard.put("courseGrades", courseGrades);
        reportCard.put("generatedAt", new java.util.Date());

        return reportCard;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentReportCardForCourse(Long studentId, Long courseId) {
        try {
            if (studentId == null) {
                throw new RuntimeException("Student ID is required");
            }
            if (courseId == null) {
                throw new RuntimeException("Course ID is required");
            }
            
            UserAccount student = userAccountRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // Check if student is enrolled in the course
            boolean isEnrolled = enrollmentRepository.findByStudentAndCourse(student, course).isPresent();
            if (!isEnrolled) {
                // Return empty report card instead of throwing error
                Map<String, Object> emptyReport = new HashMap<>();
                emptyReport.put("courseId", courseId);
                emptyReport.put("courseTitle", course.getTitle() != null ? course.getTitle() : "Untitled Course");
                emptyReport.put("studentId", studentId);
                emptyReport.put("studentName", student.getName() != null ? student.getName() : student.getEmail());
                emptyReport.put("studentEmail", student.getEmail());
                emptyReport.put("overallScore", 0.0);
                emptyReport.put("letterGrade", "N/A");
                emptyReport.put("quizAverage", 0.0);
                emptyReport.put("assignmentAverage", 0.0);
                emptyReport.put("quizScores", new ArrayList<>());
                emptyReport.put("assignmentScores", new ArrayList<>());
                emptyReport.put("totalQuizzes", 0);
                emptyReport.put("totalAssignments", 0);
                emptyReport.put("quizzesCompleted", 0);
                emptyReport.put("assignmentsCompleted", 0);
                emptyReport.put("generatedAt", new java.util.Date());
                return emptyReport;
            }

            Map<String, Object> courseGrade = calculateCourseGrade(courseId, studentId);
            courseGrade.put("courseId", course.getId());
            courseGrade.put("courseTitle", course.getTitle() != null ? course.getTitle() : "Untitled Course");
            courseGrade.put("studentId", studentId);
            courseGrade.put("studentName", student.getName() != null ? student.getName() : student.getEmail());
            courseGrade.put("studentEmail", student.getEmail());
            courseGrade.put("generatedAt", new java.util.Date());

            return courseGrade;
        } catch (RuntimeException e) {
            System.err.println("Error in getStudentReportCardForCourse: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw RuntimeException as-is
        } catch (Exception e) {
            System.err.println("Unexpected error in getStudentReportCardForCourse: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate report card: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"), e);
        }
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudentsReportCardsForCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
        
        return enrollments.stream()
                .map(enrollment -> {
                    Long studentId = enrollment.getStudent().getId();
                    Map<String, Object> reportCard = getStudentReportCardForCourse(studentId, courseId);
                    reportCard.put("enrollmentDate", enrollment.getEnrolledAt());
                    reportCard.put("progress", enrollment.getProgressPercentage() != null ? enrollment.getProgressPercentage() : 0);
                    return reportCard;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> calculateCourseGrade(Long courseId, Long studentId) {
        try {
            // Get all quizzes for the course
            List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
            if (quizzes == null) {
                quizzes = new ArrayList<>();
            }

            // Get all assignments for the course
            List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
            if (assignments == null) {
                assignments = new ArrayList<>();
            }

            // Get quiz attempts
            UserAccount student = userAccountRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));
            
            List<QuizAttempt> allQuizAttempts;
            try {
                allQuizAttempts = quizAttemptRepository.findByStudent(student);
            } catch (Exception e) {
                System.err.println("Error fetching quiz attempts: " + e.getMessage());
                allQuizAttempts = new ArrayList<>();
            }
            
            if (allQuizAttempts == null) {
                allQuizAttempts = new ArrayList<>();
            }
            
            List<QuizAttempt> quizAttempts = new ArrayList<>();
            
            // Filter and initialize relationships within transaction
            for (QuizAttempt attempt : allQuizAttempts) {
                try {
                    // Explicitly access relationships to initialize them
                    if (attempt != null && attempt.getQuiz() != null) {
                        Quiz quiz = attempt.getQuiz();
                        if (quiz.getCourse() != null && quiz.getCourse().getId() != null && quiz.getCourse().getId().equals(courseId)) {
                            // Access course to ensure it's loaded
                            quiz.getCourse().getId();
                            quizAttempts.add(attempt);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error filtering quiz attempt: " + e.getMessage());
                    e.printStackTrace();
                    // Skip this attempt
                }
            }

            // Get assignment submissions
            List<AssignmentSubmission> allSubmissions;
            try {
                allSubmissions = assignmentSubmissionRepository.findByStudent(student);
            } catch (Exception e) {
                System.err.println("Error fetching assignment submissions: " + e.getMessage());
                allSubmissions = new ArrayList<>();
            }
            
            if (allSubmissions == null) {
                allSubmissions = new ArrayList<>();
            }
            
            List<AssignmentSubmission> submissions = new ArrayList<>();
            
            // Filter and initialize relationships within transaction
            for (AssignmentSubmission submission : allSubmissions) {
                try {
                    // Explicitly access relationships to initialize them
                    if (submission != null && submission.getAssignment() != null) {
                        Assignment assignment = submission.getAssignment();
                        if (assignment.getCourse() != null && assignment.getCourse().getId() != null && assignment.getCourse().getId().equals(courseId)) {
                            // Access course to ensure it's loaded
                            assignment.getCourse().getId();
                            submissions.add(submission);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error filtering assignment submission: " + e.getMessage());
                    e.printStackTrace();
                    // Skip this submission
                }
            }

            // Calculate quiz scores
            List<Map<String, Object>> quizScores = new ArrayList<>();
            double totalQuizScore = 0;
            double totalQuizMaxScore = 0;

            for (Quiz quiz : quizzes) {
                if (quiz == null) continue;
                
                final Long quizId = quiz.getId(); // Make effectively final for lambda
                Optional<QuizAttempt> bestAttempt = quizAttempts.stream()
                        .filter(attempt -> {
                            try {
                                return attempt != null && 
                                       attempt.getQuiz() != null && 
                                       attempt.getQuiz().getId().equals(quizId);
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .max(Comparator.comparing(attempt -> {
                            try {
                                return attempt.getScore() != null ? attempt.getScore() : 0;
                            } catch (Exception e) {
                                return 0;
                            }
                        }));

                if (bestAttempt.isPresent()) {
                    try {
                        QuizAttempt attempt = bestAttempt.get();
                        // Ensure quiz is loaded
                        Quiz attemptQuiz = attempt.getQuiz();
                        if (attemptQuiz == null) continue;
                        
                        int score = attempt.getScore() != null ? attempt.getScore() : 0;
                        int maxScore = quiz.getTotalMarks() != null ? quiz.getTotalMarks() : 100;
                        double percentage = maxScore > 0 ? (double) score / maxScore * 100 : 0;

                        Map<String, Object> quizScore = new HashMap<>();
                        quizScore.put("quizId", quiz.getId());
                        quizScore.put("quizTitle", quiz.getTitle() != null ? quiz.getTitle() : "Untitled Quiz");
                        quizScore.put("score", score);
                        quizScore.put("maxScore", maxScore);
                        quizScore.put("percentage", Math.round(percentage * 100.0) / 100.0);
                        quizScore.put("passed", quiz.getPassingMarks() != null && score >= quiz.getPassingMarks());
                        quizScore.put("attemptDate", attempt.getSubmittedAt());
                        quizScores.add(quizScore);

                        totalQuizScore += score;
                        totalQuizMaxScore += maxScore;
                    } catch (Exception e) {
                        System.err.println("Error processing quiz score for quiz " + quiz.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            // Calculate assignment scores
            List<Map<String, Object>> assignmentScores = new ArrayList<>();
            double totalAssignmentScore = 0;
            double totalAssignmentMaxScore = 0;

            for (Assignment assignment : assignments) {
                if (assignment == null) continue;
                
                final Long assignmentId = assignment.getId(); // Make effectively final for lambda
                Optional<AssignmentSubmission> submission = submissions.stream()
                        .filter(sub -> {
                            try {
                                return sub != null && 
                                       sub.getAssignment() != null && 
                                       sub.getAssignment().getId().equals(assignmentId);
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .findFirst();

                if (submission.isPresent()) {
                    try {
                        AssignmentSubmission sub = submission.get();
                        boolean isGraded = sub.getScore() != null && sub.getStatus() == AssignmentSubmission.SubmissionStatus.GRADED;
                        int score = sub.getScore() != null ? sub.getScore() : 0;
                        int maxScore = assignment.getMaxScore() != null ? assignment.getMaxScore() : 100;
                        double percentage = isGraded && maxScore > 0 ? (double) score / maxScore * 100 : 0;

                        Map<String, Object> assignmentScore = new HashMap<>();
                        assignmentScore.put("assignmentId", assignment.getId());
                        assignmentScore.put("assignmentTitle", assignment.getTitle() != null ? assignment.getTitle() : "Untitled Assignment");
                        assignmentScore.put("score", isGraded ? score : null);
                        assignmentScore.put("maxScore", maxScore);
                        assignmentScore.put("percentage", isGraded ? Math.round(percentage * 100.0) / 100.0 : null);
                        assignmentScore.put("status", sub.getStatus() != null ? sub.getStatus().toString() : "SUBMITTED");
                        assignmentScore.put("submittedDate", sub.getSubmittedAt());
                        assignmentScore.put("gradedDate", sub.getGradedAt());
                        assignmentScore.put("isGraded", isGraded);
                        assignmentScores.add(assignmentScore);

                        // Only include graded assignments in the overall score calculation
                        if (isGraded) {
                            totalAssignmentScore += score;
                            totalAssignmentMaxScore += maxScore;
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing assignment score: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            // Calculate overall course score
            double quizAverage = totalQuizMaxScore > 0 ? (totalQuizScore / totalQuizMaxScore) * 100 : 0;
            double assignmentAverage = totalAssignmentMaxScore > 0 ? (totalAssignmentScore / totalAssignmentMaxScore) * 100 : 0;
            
            // Weighted average: 40% quizzes, 60% assignments (adjustable)
            double overallScore = 0;
            if (quizzes != null && quizzes.size() > 0 && assignments != null && assignments.size() > 0) {
                overallScore = (quizAverage * 0.4) + (assignmentAverage * 0.6);
            } else if (quizzes != null && quizzes.size() > 0) {
                overallScore = quizAverage;
            } else if (assignments != null && assignments.size() > 0) {
                overallScore = assignmentAverage;
            }

            String letterGrade = calculateLetterGrade(overallScore);

            Map<String, Object> courseGrade = new HashMap<>();
            courseGrade.put("overallScore", Math.round(overallScore * 100.0) / 100.0);
            courseGrade.put("letterGrade", letterGrade);
            courseGrade.put("quizAverage", Math.round(quizAverage * 100.0) / 100.0);
            courseGrade.put("assignmentAverage", Math.round(assignmentAverage * 100.0) / 100.0);
            courseGrade.put("quizScores", quizScores != null ? quizScores : new ArrayList<>());
            courseGrade.put("assignmentScores", assignmentScores != null ? assignmentScores : new ArrayList<>());
            courseGrade.put("totalQuizzes", quizzes != null ? quizzes.size() : 0);
            courseGrade.put("totalAssignments", assignments != null ? assignments.size() : 0);
            courseGrade.put("quizzesCompleted", quizScores != null ? quizScores.size() : 0);
            courseGrade.put("assignmentsCompleted", assignmentScores != null ? assignmentScores.size() : 0);

            return courseGrade;
        } catch (Exception e) {
            System.err.println("Error in calculateCourseGrade: " + e.getMessage());
            e.printStackTrace();
            // Return a minimal report card instead of throwing
            Map<String, Object> errorReport = new HashMap<>();
            errorReport.put("overallScore", 0.0);
            errorReport.put("letterGrade", "N/A");
            errorReport.put("quizAverage", 0.0);
            errorReport.put("assignmentAverage", 0.0);
            errorReport.put("quizScores", new ArrayList<>());
            errorReport.put("assignmentScores", new ArrayList<>());
            errorReport.put("totalQuizzes", 0);
            errorReport.put("totalAssignments", 0);
            errorReport.put("quizzesCompleted", 0);
            errorReport.put("assignmentsCompleted", 0);
            return errorReport;
        }
    }

    private String calculateLetterGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 85) return "A";
        if (percentage >= 80) return "A-";
        if (percentage >= 75) return "B+";
        if (percentage >= 70) return "B";
        if (percentage >= 65) return "B-";
        if (percentage >= 60) return "C+";
        if (percentage >= 55) return "C";
        if (percentage >= 50) return "C-";
        if (percentage >= 45) return "D+";
        if (percentage >= 40) return "D";
        return "F";
    }
}

