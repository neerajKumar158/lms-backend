package com.lms.repository;

import com.lms.domain.QuizAttempt;
import com.lms.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByQuizId(Long quizId);
    List<QuizAttempt> findByStudent(UserAccount student);
    Optional<QuizAttempt> findByQuizIdAndStudentId(Long quizId, Long studentId);
    long countByQuizIdAndStudentId(Long quizId, Long studentId);
}



