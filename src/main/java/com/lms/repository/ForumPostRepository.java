package com.lms.repository;

import com.lms.domain.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByThreadIdOrderByCreatedAtAsc(Long threadId);
    List<ForumPost> findByThreadIdAndParentPostIsNullOrderByCreatedAtAsc(Long threadId);
    List<ForumPost> findByParentPostIdOrderByCreatedAtAsc(Long parentPostId);
}

