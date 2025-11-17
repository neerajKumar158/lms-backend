package com.lms.repository;

import com.lms.domain.CourseAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourseAnnouncementRepository extends JpaRepository<CourseAnnouncement, Long> {
    List<CourseAnnouncement> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    
    @Query("SELECT a FROM CourseAnnouncement a WHERE a.course.id = :courseId AND (a.expiresAt IS NULL OR a.expiresAt > :now) ORDER BY a.createdAt DESC")
    List<CourseAnnouncement> findByCourseIdAndExpiresAtAfterOrExpiresAtIsNullOrderByCreatedAtDesc(@Param("courseId") Long courseId, @Param("now") LocalDateTime now);
}

