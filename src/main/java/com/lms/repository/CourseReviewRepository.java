package com.lms.repository;

import com.lms.domain.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    List<CourseReview> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    List<CourseReview> findByCourseIdAndIsVisibleTrueOrderByCreatedAtDesc(Long courseId);
    Optional<CourseReview> findByCourseIdAndStudentId(Long courseId, Long studentId);
    List<CourseReview> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    
    @Query("SELECT AVG(r.rating) FROM CourseReview r WHERE r.course.id = :courseId AND r.isVisible = true")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(r) FROM CourseReview r WHERE r.course.id = :courseId AND r.isVisible = true")
    Long countByCourseId(@Param("courseId") Long courseId);
}



