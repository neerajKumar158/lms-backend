package com.lms.repository;

import com.lms.domain.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {
    List<StudyMaterial> findByLectureId(Long lectureId);
}

