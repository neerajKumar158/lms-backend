package com.lms.repository;

import com.lms.domain.ForumThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {
    List<ForumThread> findByCourseIdOrderByIsPinnedDescLastActivityAtDesc(Long courseId);
    List<ForumThread> findByCourseIdAndIsPinnedTrueOrderByLastActivityAtDesc(Long courseId);
    List<ForumThread> findByCourseIdAndIsPinnedFalseOrderByLastActivityAtDesc(Long courseId);
}

