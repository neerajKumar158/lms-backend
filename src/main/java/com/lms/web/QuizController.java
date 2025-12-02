package com.lms.web;

import com.lms.domain.Quiz;
import com.lms.domain.QuizAttempt;
import com.lms.domain.QuizAnswer;
import com.lms.domain.QuizOption;
import com.lms.domain.QuizQuestion;
import com.lms.repository.UserAccountRepository;
import com.lms.service.QuizService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/lms/quizzes")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getQuizzesByCourse(@PathVariable("courseId") Long courseId) {
        try {
            List<Quiz> quizzes = quizService.getQuizzesByCourse(courseId);
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            log.error("Failed to load quizzes for course {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load quizzes"));
        }
    }

    @GetMapping("/course/{courseId}/with-status")
    public ResponseEntity<?> getQuizzesByCourseWithStatus(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Quiz> quizzes = quizService.getQuizzesByCourse(courseId);
            List<Map<String, Object>> quizzesWithStatus = quizzes.stream()
                    .map(quiz -> {
                        Map<String, Object> quizMap = new java.util.HashMap<>();
                        quizMap.put("id", quiz.getId());
                        quizMap.put("title", quiz.getTitle());
                        quizMap.put("description", quiz.getDescription());
                        quizMap.put("type", quiz.getType() != null ? quiz.getType().name() : null);
                        quizMap.put("totalMarks", quiz.getTotalMarks());
                        quizMap.put("passingMarks", quiz.getPassingMarks());
                        quizMap.put("durationMinutes", quiz.getDurationMinutes());
                        quizMap.put("maxAttempts", quiz.getMaxAttempts());
                        quizMap.put("startDate", quiz.getStartDate());
                        quizMap.put("endDate", quiz.getEndDate());
                        quizMap.put("showResultsImmediately", quiz.getShowResultsImmediately());
                        quizMap.put("shuffleQuestions", quiz.getShuffleQuestions());
                        quizMap.put("shuffleOptions", quiz.getShuffleOptions());
                        
                        // Check if student has attempted
                        List<QuizAttempt> attempts = quizService.getAttemptsByQuiz(quiz.getId()).stream()
                                .filter(a -> a.getStudent().getId().equals(user.getId()))
                                .collect(java.util.stream.Collectors.toList());
                        
                        quizMap.put("isCompleted", !attempts.isEmpty());
                        quizMap.put("attemptsCount", attempts.size());
                        if (!attempts.isEmpty()) {
                            QuizAttempt bestAttempt = attempts.stream()
                                    .max(java.util.Comparator.comparing(a -> a.getScore() != null ? a.getScore() : 0))
                                    .orElse(attempts.get(0));
                            quizMap.put("bestScore", bestAttempt.getScore());
                            quizMap.put("bestPercentage", bestAttempt.getPercentage());
                            quizMap.put("passed", bestAttempt.getScore() != null && 
                                    quiz.getPassingMarks() != null && 
                                    bestAttempt.getScore() >= quiz.getPassingMarks());
                        }
                        
                        return quizMap;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(quizzesWithStatus);
        } catch (Exception e) {
            log.error("Failed to load quizzes with status for course {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load quizzes"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable("id") Long id) {
        return quizService.getQuizById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/attempt")
    public ResponseEntity<?> getQuizForAttempt(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long quizId) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Quiz quiz = quizService.getQuizForAttempt(quizId, user.getId());
            
            // Create a response DTO that includes questions
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", quiz.getId());
            response.put("title", quiz.getTitle());
            response.put("description", quiz.getDescription());
            response.put("type", quiz.getType());
            response.put("totalMarks", quiz.getTotalMarks());
            response.put("passingMarks", quiz.getPassingMarks());
            response.put("durationMinutes", quiz.getDurationMinutes());
            response.put("maxAttempts", quiz.getMaxAttempts());
            response.put("startDate", quiz.getStartDate());
            response.put("endDate", quiz.getEndDate());
            response.put("showResultsImmediately", quiz.getShowResultsImmediately());
            response.put("shuffleQuestions", quiz.getShuffleQuestions());
            response.put("shuffleOptions", quiz.getShuffleOptions());
            response.put("questions", quiz.getQuestions()); // Include questions
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to load quiz {} for attempt: {}", quizId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load quiz"));
        }
    }

    @PostMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> createQuiz(
            @AuthenticationPrincipal User principal,
            @PathVariable("courseId") Long courseId,
            @RequestBody CreateQuizRequest request) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Quiz quiz = new Quiz();
            quiz.setTitle(request.title());
            quiz.setDescription(request.description());
            if (request.type() != null) {
                quiz.setType(Quiz.QuizType.valueOf(request.type()));
            }
            quiz.setTotalMarks(request.totalMarks());
            quiz.setPassingMarks(request.passingMarks());
            quiz.setDurationMinutes(request.durationMinutes());
            quiz.setMaxAttempts(request.maxAttempts());
            quiz.setStartDate(request.startDate());
            quiz.setEndDate(request.endDate());
            quiz.setShowResultsImmediately(request.showResultsImmediately());
            quiz.setShuffleQuestions(request.shuffleQuestions());
            quiz.setShuffleOptions(request.shuffleOptions());

            Quiz created = quizService.createQuiz(courseId, quiz);
            return ResponseEntity.ok(Map.of("id", created.getId(), "message", "Quiz created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startQuizAttempt(
            @AuthenticationPrincipal User principal,
            @PathVariable("id") Long quizId) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            QuizAttempt attempt = quizService.startQuizAttempt(quizId, user.getId());
            return ResponseEntity.ok(Map.of("attemptId", attempt.getId(), "message", "Quiz attempt started"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<Map<String, Object>> submitQuizAttempt(
            @AuthenticationPrincipal User principal,
            @PathVariable("attemptId") Long attemptId,
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> answers = (List<Map<String, Object>>) request.get("answers");
            
            if (answers == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Answers are required"));
            }
            
            QuizAttempt attempt = quizService.submitQuizAttempt(attemptId, answers);
            Quiz quiz = attempt.getQuiz();
            boolean passed = quiz.getPassingMarks() != null && attempt.getScore() != null && 
                            attempt.getScore() >= quiz.getPassingMarks();
            return ResponseEntity.ok(Map.of(
                    "attemptId", attempt.getId(),
                    "score", attempt.getScore() != null ? attempt.getScore() : 0,
                    "passed", passed,
                    "message", "Quiz submitted successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to submit quiz attempt {}: {}", attemptId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to submit quiz"));
        }
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<QuizAttempt> getAttemptById(@PathVariable("attemptId") Long attemptId) {
        return quizService.getAttemptById(attemptId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-attempts")
    public List<QuizAttempt> getMyAttempts(@AuthenticationPrincipal User principal) {
        var user = userAccountRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return quizService.getStudentAttempts(user.getId());
    }

    record CreateQuizRequest(String title, String description, String type,
                            Integer totalMarks, Integer passingMarks, Integer durationMinutes,
                            Integer maxAttempts,
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                            Boolean showResultsImmediately, Boolean shuffleQuestions, Boolean shuffleOptions) {}
    
    record SubmitQuizRequest(List<QuizAnswer> answers) {}
    
    // Question Management Endpoints
    @GetMapping("/{quizId}/questions")
    public ResponseEntity<?> getQuizQuestions(@PathVariable("quizId") Long quizId) {
        try {
            List<QuizQuestion> questions = quizService.getQuestionsByQuiz(quizId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            log.error("Failed to load quiz questions for quiz {}: {}", quizId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load questions"));
        }
    }

    @PostMapping("/{quizId}/questions")
    public ResponseEntity<Map<String, Object>> addQuestionToQuiz(
            @AuthenticationPrincipal User principal,
            @PathVariable("quizId") Long quizId,
            @RequestBody CreateQuestionRequest request) {
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            QuizQuestion question = new QuizQuestion();
            question.setQuestionText(request.questionText());
            if (request.type() != null) {
                question.setType(QuizQuestion.QuestionType.valueOf(request.type()));
            }
            question.setMarks(request.marks());
            question.setOrderIndex(request.orderIndex());
            question.setCorrectAnswer(request.correctAnswer());
            question.setExplanation(request.explanation());
            
            // Convert options from request
            List<QuizOption> options = null;
            if (request.options() != null && !request.options().isEmpty()) {
                options = request.options().stream()
                        .map(opt -> {
                            QuizOption option = new QuizOption();
                            option.setOptionText(opt.optionText());
                            option.setIsCorrect(opt.isCorrect() != null ? opt.isCorrect() : false);
                            option.setOrderIndex(opt.orderIndex());
                            return option;
                        })
                        .collect(java.util.stream.Collectors.toList());
            }
            
            QuizQuestion created = quizService.addQuestionToQuiz(quizId, question, options);
            return ResponseEntity.ok(Map.of("id", created.getId(), "message", "Question added successfully"));
        } catch (Exception e) {
            log.error("Failed to add question to quiz {}: {}", quizId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{quizId}/questions/{questionId}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(
            @AuthenticationPrincipal User principal,
            @PathVariable("quizId") Long quizId,
            @PathVariable("questionId") Long questionId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }
            
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Verify user is authorized (instructor of the course)
            Quiz quiz = quizService.getQuizById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));
            
            if (quiz.getCourse() == null || quiz.getCourse().getInstructor() == null || 
                !quiz.getCourse().getInstructor().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized to delete questions from this quiz"));
            }
            
            // Verify question belongs to this quiz
            QuizQuestion question = quizService.getQuestionById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question not found"));
            
            if (question.getQuiz() == null || !question.getQuiz().getId().equals(quizId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Question does not belong to this quiz"));
            }
            
            quizService.deleteQuestion(questionId);
            return ResponseEntity.ok(Map.of("message", "Question deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Failed to delete quiz question {} for quiz {}: {}", questionId, quizId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error deleting quiz question {} for quiz {}: {}", questionId, quizId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while deleting the question: " + (e.getMessage() != null ? e.getMessage() : "Unknown error")));
        }
    }

    record CreateQuestionRequest(
            String questionText,
            String type,
            Integer marks,
            Integer orderIndex,
            String correctAnswer,
            String explanation,
            List<QuestionOptionRequest> options
    ) {}
    
    record QuestionOptionRequest(
            String optionText,
            Boolean isCorrect,
            Integer orderIndex
    ) {}
}

