package com.lms.repository;

import com.lms.domain.Conversation;
import com.lms.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Conversation entities.
 * Provides data access methods for managing conversations between users.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    /**
     * Find a conversation between two specific users
     */
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.participant1 = :user1 AND c.participant2 = :user2) OR " +
           "(c.participant1 = :user2 AND c.participant2 = :user1)")
    Optional<Conversation> findConversationBetweenUsers(
        @Param("user1") UserAccount user1,
        @Param("user2") UserAccount user2
    );
    
    /**
     * Find all conversations for a user (where they are participant1 or participant2)
     */
    @Query("SELECT c FROM Conversation c WHERE c.participant1 = :user OR c.participant2 = :user " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC")
    List<Conversation> findConversationsByUser(@Param("user") UserAccount user);
    
    /**
     * Find conversations for a user related to a specific course
     */
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.participant1 = :user OR c.participant2 = :user) AND c.course = :course")
    List<Conversation> findConversationsByUserAndCourse(
        @Param("user") UserAccount user,
        @Param("course") com.lms.domain.Course course
    );
}

