package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAttemptRepository attemptRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private QuizQuestionRepository questionRepository;

    @Autowired
    private QuizOptionRepository optionRepository;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;

    @Transactional(readOnly = true)
    public List<Quiz> getQuizzesByCourse(Long courseId) {
        return quizRepository.findByCourseId(courseId);
    }

    public Optional<Quiz> getQuizById(Long quizId) {
        return quizRepository.findById(quizId);
    }

    @Transactional
    public Quiz createQuiz(Long courseId, Quiz quiz) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        quiz.setCourse(course);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());
        
        return quizRepository.save(quiz);
    }

    @Transactional
    public Quiz updateQuiz(Long quizId, Quiz updatedQuiz) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        quiz.setTitle(updatedQuiz.getTitle());
        quiz.setDescription(updatedQuiz.getDescription());
        quiz.setType(updatedQuiz.getType());
        quiz.setTotalMarks(updatedQuiz.getTotalMarks());
        quiz.setPassingMarks(updatedQuiz.getPassingMarks());
        quiz.setDurationMinutes(updatedQuiz.getDurationMinutes());
        quiz.setMaxAttempts(updatedQuiz.getMaxAttempts());
        quiz.setStartDate(updatedQuiz.getStartDate());
        quiz.setEndDate(updatedQuiz.getEndDate());
        quiz.setShowResultsImmediately(updatedQuiz.getShowResultsImmediately());
        quiz.setShuffleQuestions(updatedQuiz.getShuffleQuestions());
        quiz.setShuffleOptions(updatedQuiz.getShuffleOptions());
        quiz.setUpdatedAt(LocalDateTime.now());
        
        return quizRepository.save(quiz);
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        quizRepository.deleteById(quizId);
    }

    @Transactional(readOnly = true)
    public Quiz getQuizForAttempt(Long quizId, Long studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        // Check enrollment
        if (!enrollmentService.isEnrolled(studentId, quiz.getCourse().getId())) {
            throw new RuntimeException("Student is not enrolled in this course");
        }
        
        // Explicitly load questions and options within transaction
        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId);
        
        // Load options for each question
        for (QuizQuestion question : questions) {
            question.getOptions().size(); // Trigger lazy load
        }
        
        // Shuffle questions if enabled
        if (quiz.getShuffleQuestions()) {
            Collections.shuffle(questions);
        }
        
        // Shuffle options if enabled
        if (quiz.getShuffleOptions()) {
            for (QuizQuestion question : questions) {
                List<QuizOption> options = new java.util.ArrayList<>(question.getOptions());
                Collections.shuffle(options);
                question.setOptions(options);
            }
        }
        
        // Set questions on quiz (this will be serialized)
        quiz.setQuestions(questions);
        
        return quiz;
    }

    @Transactional
    public QuizAttempt startQuizAttempt(Long quizId, Long studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Check enrollment
        if (!enrollmentService.isEnrolled(studentId, quiz.getCourse().getId())) {
            throw new RuntimeException("Student is not enrolled in this course");
        }
        
        // Check attempts limit
        long attemptCount = attemptRepository.countByQuizIdAndStudentId(quizId, studentId);
        if (attemptCount >= quiz.getMaxAttempts()) {
            throw new RuntimeException("Maximum attempts reached for this quiz");
        }
        
        // Check if quiz is available
        LocalDateTime now = LocalDateTime.now();
        if (quiz.getStartDate() != null && now.isBefore(quiz.getStartDate())) {
            throw new RuntimeException("Quiz is not available yet");
        }
        if (quiz.getEndDate() != null && now.isAfter(quiz.getEndDate())) {
            throw new RuntimeException("Quiz has ended");
        }
        
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setStartedAt(now);
        attempt.setStatus(QuizAttempt.AttemptStatus.IN_PROGRESS);
        
        return attemptRepository.save(attempt);
    }

    @Transactional
    public QuizAttempt submitQuizAttempt(Long attemptId, List<Map<String, Object>> answerDataList) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        
        Quiz quiz = attempt.getQuiz();
        LocalDateTime now = LocalDateTime.now();
        
        // Calculate time spent
        long timeSpentSeconds = 0;
        if (attempt.getStartedAt() != null) {
            timeSpentSeconds = java.time.Duration.between(attempt.getStartedAt(), now).getSeconds();
        }
        
        // Calculate score
        int totalScore = 0;
        List<QuizAnswer> answers = new java.util.ArrayList<>();
        
        for (Map<String, Object> answerData : answerDataList) {
            QuizAnswer answer = new QuizAnswer();
            answer.setAttempt(attempt);
            
            // Get questionId from the answer data
            Long questionId = null;
            if (answerData.get("questionId") != null) {
                questionId = Long.valueOf(answerData.get("questionId").toString());
            }
            
            if (questionId == null) {
                throw new RuntimeException("Question ID is required for each answer");
            }
            
            // Load the question with options
            final Long finalQuestionId = questionId;
            QuizQuestion question = questionRepository.findById(finalQuestionId)
                    .orElseThrow(() -> new RuntimeException("Question not found: " + finalQuestionId));
            
            // Ensure options are loaded
            question.getOptions().size(); // Trigger lazy load
            
            answer.setQuestion(question);
            
            // Set answer text or selected option
            if (answerData.get("answerText") != null) {
                answer.setAnswerText(answerData.get("answerText").toString());
            }
            
            if (answerData.get("selectedOptionId") != null) {
                answer.setSelectedOptionId(Long.valueOf(answerData.get("selectedOptionId").toString()));
            }
            
            // Grade the answer
            boolean isCorrect = false;
            int marksObtained = 0;
            
            if (question.getCorrectAnswer() != null) {
                if (answer.getSelectedOptionId() != null) {
                    // For multiple choice questions, check if selected option is correct
                    String correctAnswerStr = question.getCorrectAnswer();
                    if (correctAnswerStr.equals(answer.getSelectedOptionId().toString())) {
                        isCorrect = true;
                        marksObtained = question.getMarks() != null ? question.getMarks() : 1;
                        totalScore += marksObtained;
                    } else {
                        // Check if the option itself is marked as correct
                        Long selectedOptionId = answer.getSelectedOptionId();
                        QuizOption selectedOption = question.getOptions().stream()
                                .filter(opt -> opt.getId().equals(selectedOptionId))
                                .findFirst()
                                .orElse(null);
                        if (selectedOption != null && Boolean.TRUE.equals(selectedOption.getIsCorrect())) {
                            isCorrect = true;
                            marksObtained = question.getMarks() != null ? question.getMarks() : 1;
                            totalScore += marksObtained;
                        }
                    }
                } else if (answer.getAnswerText() != null && !answer.getAnswerText().trim().isEmpty()) {
                    // For text-based questions, compare with correct answer
                    if (question.getCorrectAnswer().equalsIgnoreCase(answer.getAnswerText().trim())) {
                        isCorrect = true;
                        marksObtained = question.getMarks() != null ? question.getMarks() : 1;
                        totalScore += marksObtained;
                    }
                }
            }
            
            answer.setIsCorrect(isCorrect);
            answer.setMarksObtained(marksObtained);
            answers.add(answer);
        }
        
        // Calculate percentage
        int totalMarks = quiz.getTotalMarks() != null ? quiz.getTotalMarks() : totalScore;
        double percentage = totalMarks > 0 ? (double) totalScore / totalMarks * 100 : 0;
        
        attempt.setScore(totalScore);
        attempt.setPercentage((int) Math.round(percentage));
        attempt.setSubmittedAt(now);
        attempt.setTimeSpentSeconds((int) timeSpentSeconds);
        attempt.setStatus(QuizAttempt.AttemptStatus.SUBMITTED);
        
        // Clear existing answers and add new ones (to avoid orphan removal error)
        attempt.getAnswers().clear();
        attempt.getAnswers().addAll(answers);
        
        // Save attempt (answers will be saved via cascade)
        QuizAttempt savedAttempt = attemptRepository.save(attempt);
        
        // Create notification and send email for quiz results
        try {
            if (notificationService != null) {
                notificationService.createNotification(
                    attempt.getStudent().getId(),
                    "Quiz Graded",
                    "Your quiz '" + quiz.getTitle() + "' has been graded. Score: " + totalScore + "/" + totalMarks + " (" + Math.round(percentage) + "%)",
                    com.lms.domain.Notification.NotificationType.QUIZ,
                    "/ui/lms/quiz/take?id=" + quiz.getId()
                );
            }
            
            // Send email notification
            if (emailNotificationService != null) {
                emailNotificationService.sendQuizResultEmail(
                    attempt.getStudent().getId(),
                    quiz.getCourse().getTitle(),
                    quiz.getTitle(),
                    totalScore,
                    totalMarks
                );
            }
        } catch (Exception e) {
            // Log but don't fail the submission
            System.err.println("Failed to send quiz notification: " + e.getMessage());
        }
        
        return savedAttempt;
    }

    public List<QuizAttempt> getAttemptsByQuiz(Long quizId) {
        return attemptRepository.findByQuizId(quizId);
    }

    public List<QuizAttempt> getStudentAttempts(Long studentId) {
        return attemptRepository.findByStudent(userAccountRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found")));
    }

    public Optional<QuizAttempt> getAttemptById(Long attemptId) {
        return attemptRepository.findById(attemptId);
    }

    @Transactional(readOnly = true)
    public List<QuizQuestion> getQuestionsByQuiz(Long quizId) {
        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId);
        // Eager fetch should load options, but ensure they're initialized
        for (QuizQuestion question : questions) {
            question.getOptions().size(); // Trigger lazy load if needed
        }
        return questions;
    }

    @Transactional
    public QuizQuestion addQuestionToQuiz(Long quizId, QuizQuestion question, List<QuizOption> options) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        question.setQuiz(quiz);
        if (question.getOrderIndex() == null) {
            // Set order index to last if not provided
            List<QuizQuestion> existingQuestions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId);
            question.setOrderIndex(existingQuestions.size());
        }
        
        QuizQuestion savedQuestion = questionRepository.save(question);
        
        // Save options if provided
        if (options != null && !options.isEmpty()) {
            for (int i = 0; i < options.size(); i++) {
                QuizOption option = options.get(i);
                option.setQuestion(savedQuestion);
                if (option.getOrderIndex() == null) {
                    option.setOrderIndex(i);
                }
                optionRepository.save(option);
            }
            
            // Set correct answer based on correct option
            QuizOption correctOption = options.stream()
                    .filter(QuizOption::getIsCorrect)
                    .findFirst()
                    .orElse(null);
            if (correctOption != null && correctOption.getId() != null) {
                savedQuestion.setCorrectAnswer(correctOption.getId().toString());
            }
        }
        
        return questionRepository.save(savedQuestion);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    public Optional<QuizQuestion> getQuestionById(Long questionId) {
        return questionRepository.findById(questionId);
    }
}

