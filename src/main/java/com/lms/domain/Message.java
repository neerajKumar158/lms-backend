package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a message in a conversation between users.
 * This entity stores message content, sender, timestamp, and read status.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
@Table(name = "messages")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The conversation this message belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonIgnoreProperties({"messages", "participant1", "participant2"})
    private Conversation conversation;
    
    /**
     * The user who sent this message
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "enrollments", "courses"})
    private UserAccount sender;
    
    /**
     * The content of the message
     */
    @Column(nullable = false, length = 5000)
    private String content;
    
    /**
     * Timestamp when the message was sent
     */
    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();
    
    /**
     * Whether the message has been read by the recipient
     */
    @Column(nullable = false)
    private Boolean isRead = false;
    
    /**
     * Timestamp when the message was read
     */
    @Column
    private LocalDateTime readAt;
    
    /**
     * Optional: File attachment URL (for file sharing)
     */
    @Column
    private String attachmentUrl;
    
    /**
     * Optional: File attachment name
     */
    @Column
    private String attachmentName;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }
    
    public UserAccount getSender() { return sender; }
    public void setSender(UserAccount sender) { this.sender = sender; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    
    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
}

