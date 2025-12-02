package com.lms.repository;

import com.lms.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Handles data access operations for Quiz entities. This repository provides
 * methods for querying quizzes by course for quiz management and assessment.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    /**
     * Finds all quizzes for a specific course.
     *
     * @param courseId the course ID
     * @return the list of quizzes for the course
     */
    List<Quiz> findByCourseId(Long courseId);
}



