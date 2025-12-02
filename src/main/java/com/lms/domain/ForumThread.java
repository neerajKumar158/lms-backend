package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles discussion threads within courses. This entity manages thread
 * creation, pinning, locking, view tracking, and post organization for
 * course-related discussions and community engagement.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
@Table(name = "forum_threads")
public class ForumThread {
    /**
     * Unique identifier for the thread
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The course this thread belongs to
     */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"instructor", "enrollments", "lectures", "assignments", "quizzes"})
    private Course course;

    /**
     * The user who created the thread
     */
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "roles", "emailVerificationToken"})
    private UserAccount author;

    /**
     * Title of the thread
     */
    @Column(nullable = false)
    private String title;

    /**
     * Content/body of the thread
     */
    @Column(length = 5000)
    private String content;

    /**
     * Whether the thread is pinned to the top
     */
    @Column(nullable = false)
    private Boolean isPinned = false;

    /**
     * Whether the thread is locked (no new posts allowed)
     */
    @Column(nullable = false)
    private Boolean isLocked = false;

    /**
     * Number of times the thread has been viewed
     */
    @Column(nullable = false)
    private Integer viewCount = 0;

    /**
     * Timestamp when the thread was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when the thread was last updated
     */
    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Timestamp of the last activity in the thread
     */
    @Column
    private LocalDateTime lastActivityAt = LocalDateTime.now();

    /**
     * List of posts in this thread (ordered by creation date)
     */
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @JsonIgnore
    private List<ForumPost> posts = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public UserAccount getAuthor() { return author; }
    public void setAuthor(UserAccount author) { this.author = author; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getIsPinned() { return isPinned; }
    public void setIsPinned(Boolean isPinned) { this.isPinned = isPinned; }

    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public List<ForumPost> getPosts() { return posts; }
    public void setPosts(List<ForumPost> posts) { this.posts = posts; }

    public Integer getPostCount() {
        return posts != null ? posts.size() : 0;
    }
}

