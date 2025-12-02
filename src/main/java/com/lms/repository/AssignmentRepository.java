package com.lms.repository;

import com.lms.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Handles data access operations for Assignment entities. This repository provides
 * methods for querying assignments by course for assignment management and retrieval.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    /**
     * Finds all assignments for a specific course.
     *
     * @param courseId the course ID
     * @return the list of assignments for the course
     */
    List<Assignment> findByCourseId(Long courseId);
}



