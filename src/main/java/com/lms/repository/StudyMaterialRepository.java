package com.lms.repository;

import com.lms.domain.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Handles data access operations for StudyMaterial entities. This repository provides
 * methods for querying study materials by lecture for course content management.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {
    /**
     * Finds all study materials for a specific lecture.
     *
     * @param lectureId the lecture ID
     * @return the list of study materials for the lecture
     */
    List<StudyMaterial> findByLectureId(Long lectureId);
}

