package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a conversation between two users (e.g., student and teacher).
 * This entity manages the conversation metadata and relationship between participants.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
@Table(name = "conversations")
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * First participant (typically the student)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "participant1_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "enrollments", "courses"})
    private UserAccount participant1;
    
    /**
     * Second participant (typically the teacher)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "participant2_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "enrollments", "courses"})
    private UserAccount participant2;
    
    /**
     * Optional: Course this conversation is related to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;
    
    /**
     * Timestamp when the conversation was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Timestamp of the last message in this conversation
     */
    @Column
    private LocalDateTime lastMessageAt;
    
    /**
     * Messages in this conversation
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"conversation"})
    private List<Message> messages = new ArrayList<>();
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UserAccount getParticipant1() { return participant1; }
    public void setParticipant1(UserAccount participant1) { this.participant1 = participant1; }
    
    public UserAccount getParticipant2() { return participant2; }
    public void setParticipant2(UserAccount participant2) { this.participant2 = participant2; }
    
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
}

