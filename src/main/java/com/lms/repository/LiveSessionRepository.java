package com.lms.repository;

import com.lms.domain.LiveSession;
import com.lms.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {
    List<LiveSession> findByInstructor(UserAccount instructor);
    List<LiveSession> findByCourseId(Long courseId);
    List<LiveSession> findByStatus(String status);
    List<LiveSession> findByScheduledDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<LiveSession> findByStatusAndScheduledDateTimeAfter(String status, LocalDateTime dateTime);
}

