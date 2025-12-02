package com.lms.service;

import com.lms.domain.Conversation;
import com.lms.domain.Course;
import com.lms.domain.Message;
import com.lms.domain.UserAccount;
import com.lms.repository.ConversationRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.MessageRepository;
import com.lms.repository.UserAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing messaging and conversations.
 * Handles message creation, conversation management, and real-time notifications.
 */
@Slf4j
@Service
public class MessagingService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Get or create a conversation between two users
     */
    @Transactional
    public Conversation getOrCreateConversation(Long userId1, Long userId2, Long courseId) {
        try {
            log.info("Creating/getting conversation between user {} and user {}, courseId: {}", userId1, userId2, courseId);
            
            UserAccount user1 = userAccountRepository.findById(userId1)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId1));
            UserAccount user2 = userAccountRepository.findById(userId2)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId2));
            
            log.debug("Found users: {} and {}", user1.getEmail(), user2.getEmail());

            // Check if conversation already exists
            Optional<Conversation> existing = conversationRepository.findConversationBetweenUsers(user1, user2);
            if (existing.isPresent()) {
                log.info("Found existing conversation: {}", existing.get().getId());
                Conversation existingConv = existing.get();
                // If courseId is provided and different, update it
                if (courseId != null && (existingConv.getCourse() == null || !existingConv.getCourse().getId().equals(courseId))) {
                    try {
                        Optional<Course> course = courseRepository.findById(courseId);
                        if (course.isPresent()) {
                            existingConv.setCourse(course.get());
                            existingConv = conversationRepository.save(existingConv);
                            log.info("Updated course for existing conversation");
                        }
                    } catch (Exception e) {
                        log.warn("Error updating course for existing conversation: {}", e.getMessage(), e);
                    }
                }
                return existingConv;
            }

            // Create new conversation
            log.info("Creating new conversation");
            Conversation conversation = new Conversation();
            conversation.setParticipant1(user1);
            conversation.setParticipant2(user2);
            conversation.setCreatedAt(LocalDateTime.now());
            
            if (courseId != null) {
                try {
                    log.debug("Loading course with ID: {}", courseId);
                    Optional<Course> courseOpt = courseRepository.findById(courseId);
                    if (courseOpt.isPresent()) {
                        Course course = courseOpt.get();
                        conversation.setCourse(course);
                        log.debug("Course loaded: {}", course.getTitle());
                    } else {
                        log.warn("Course with ID {} not found, creating conversation without course link", courseId);
                    }
                } catch (Exception e) {
                    log.error("Error loading course {} for conversation: {}", courseId, e.getMessage(), e);
                    // Continue without course - it's optional
                }
            }
            
            log.debug("Saving conversation...");
            Conversation saved = conversationRepository.saveAndFlush(conversation);
            log.info("Conversation saved successfully with ID: {}", saved.getId());
            
            // Explicitly load course if present to avoid lazy loading issues
            if (saved.getCourse() != null && saved.getCourse().getId() != null) {
                try {
                    // Touch the course to ensure it's loaded within transaction
                    String courseTitle = saved.getCourse().getTitle();
                    log.debug("Course loaded in transaction: {}", courseTitle);
                } catch (Exception e) {
                    log.warn("Could not access course title: {}", e.getMessage());
                }
            }
            
            return saved;
        } catch (Exception e) {
            log.error("Error in getOrCreateConversation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create conversation: " + e.getMessage(), e);
        }
    }

    /**
     * Send a message in a conversation
     */
    @Transactional
    public Message sendMessage(Long conversationId, Long senderId, String content, String attachmentUrl, String attachmentName) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        UserAccount sender = userAccountRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify sender is a participant
        if (!conversation.getParticipant1().getId().equals(senderId) &&
            !conversation.getParticipant2().getId().equals(senderId)) {
            throw new RuntimeException("User is not a participant in this conversation");
        }

        // Create message
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        // Content can be empty if there's an attachment
        message.setContent(content != null ? content : "");
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);
        if (attachmentUrl != null && !attachmentUrl.isEmpty()) {
            message.setAttachmentUrl(attachmentUrl);
            message.setAttachmentName(attachmentName);
        }

        log.debug("Saving message: conversationId={}, senderId={}, content length={}, hasAttachment={}", 
                conversationId, senderId, content != null ? content.length() : 0, attachmentUrl != null);
        
        Message savedMessage = messageRepository.saveAndFlush(message);
        
        log.info("Message saved with ID: {}", savedMessage.getId());

        // Update conversation's last message time
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Send real-time notification to the other participant
        Long recipientId = conversation.getParticipant1().getId().equals(senderId) ?
                conversation.getParticipant2().getId() : conversation.getParticipant1().getId();
        
        messagingTemplate.convertAndSend("/queue/messages/" + recipientId, savedMessage);
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, savedMessage);

        log.info("Message sent from user {} to conversation {}", senderId, conversationId);
        return savedMessage;
    }

    /**
     * Get all messages in a conversation
     */
    public List<Message> getConversationMessages(Long conversationId) {
        log.debug("Loading messages for conversation: {}", conversationId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        List<Message> messages = messageRepository.findByConversationOrderBySentAtAsc(conversation);
        log.info("Loaded {} messages for conversation {}", messages.size(), conversationId);
        return messages;
    }

    /**
     * Get all conversations for a user
     */
    public List<Conversation> getUserConversations(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return conversationRepository.findConversationsByUser(user);
    }

    /**
     * Mark messages as read in a conversation
     */
    @Transactional
    public void markMessagesAsRead(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        messageRepository.markMessagesAsRead(conversation, user);
        log.info("Messages marked as read for conversation {} by user {}", conversationId, userId);
    }

    /**
     * Get unread message count for a user
     */
    public Long getUnreadCount(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Conversation> conversations = conversationRepository.findConversationsByUser(user);
        long totalUnread = 0;
        
        for (Conversation conv : conversations) {
            totalUnread += messageRepository.countUnreadMessages(conv, user);
        }
        
        return totalUnread;
    }

    /**
     * Search messages in a conversation
     */
    public List<Message> searchMessages(Long conversationId, String keyword) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return messageRepository.searchMessages(conversation, keyword);
    }
}

