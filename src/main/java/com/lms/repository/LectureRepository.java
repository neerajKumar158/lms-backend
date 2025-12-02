package com.lms.repository;

import com.lms.domain.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Handles data access operations for Lecture entities. This repository provides
 * methods for querying lectures by course with proper sequence ordering for
 * course content organization.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    /**
     * Finds all lectures for a course, ordered by sequence order ascending.
     *
     * @param courseId the course ID
     * @return the list of lectures ordered by sequence
     */
    @Query("SELECT l FROM Lecture l WHERE l.course.id = :courseId ORDER BY l.sequenceOrder ASC")
    List<Lecture> findByCourseIdOrderBySequenceOrderAsc(@Param("courseId") Long courseId);
}

