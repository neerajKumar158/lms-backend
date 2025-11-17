package com.lms.repository;

import com.lms.domain.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    @Query("SELECT l FROM Lecture l WHERE l.course.id = :courseId ORDER BY l.sequenceOrder ASC")
    List<Lecture> findByCourseIdOrderBySequenceOrderAsc(@Param("courseId") Long courseId);
}

