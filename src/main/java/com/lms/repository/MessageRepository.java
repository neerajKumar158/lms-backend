package com.lms.repository;

import com.lms.domain.Message;
import com.lms.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Message entities.
 * Provides data access methods for managing messages in conversations.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Find all messages in a conversation, ordered by sent time
     */
    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);
    
    /**
     * Count unread messages in a conversation for a specific user (messages not sent by them)
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation = :conversation " +
           "AND m.sender != :user AND m.isRead = false")
    Long countUnreadMessages(@Param("conversation") Conversation conversation, 
                             @Param("user") com.lms.domain.UserAccount user);
    
    /**
     * Mark messages as read in a conversation for a specific user
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP " +
           "WHERE m.conversation = :conversation AND m.sender != :user AND m.isRead = false")
    void markMessagesAsRead(@Param("conversation") Conversation conversation,
                           @Param("user") com.lms.domain.UserAccount user);
    
    /**
     * Search messages by content
     */
    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation " +
           "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY m.sentAt DESC")
    List<Message> searchMessages(@Param("conversation") Conversation conversation,
                                @Param("keyword") String keyword);
}

