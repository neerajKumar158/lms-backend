package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ForumPost Entity
 * Individual posts/replies within a forum thread
 */
@Entity
@Table(name = "forum_posts")
public class ForumPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "thread_id", nullable = false)
    @JsonIgnore
    private ForumThread thread;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "roles", "emailVerificationToken"})
    private UserAccount author;

    @Column(length = 10000, nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "parent_post_id")
    @JsonIgnoreProperties({"thread", "parentPost", "replies"})
    private ForumPost parentPost; // For nested replies

    @OneToMany(mappedBy = "parentPost", cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    @JsonIgnore
    private java.util.List<ForumPost> replies = new java.util.ArrayList<>();

    @Column(nullable = false)
    private Boolean isEdited = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ForumThread getThread() { return thread; }
    public void setThread(ForumThread thread) { this.thread = thread; }

    public UserAccount getAuthor() { return author; }
    public void setAuthor(UserAccount author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public ForumPost getParentPost() { return parentPost; }
    public void setParentPost(ForumPost parentPost) { this.parentPost = parentPost; }

    public java.util.List<ForumPost> getReplies() { return replies; }
    public void setReplies(java.util.List<ForumPost> replies) { this.replies = replies; }

    public Boolean getIsEdited() { return isEdited; }
    public void setIsEdited(Boolean isEdited) { this.isEdited = isEdited; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

