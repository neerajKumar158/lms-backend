package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ForumThread Entity
 * Discussion threads within a course
 */
@Entity
@Table(name = "forum_threads")
public class ForumThread {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"instructor", "enrollments", "lectures", "assignments", "quizzes"})
    private Course course;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "roles", "emailVerificationToken"})
    private UserAccount author;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String content;

    @Column(nullable = false)
    private Boolean isPinned = false;

    @Column(nullable = false)
    private Boolean isLocked = false;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column
    private LocalDateTime lastActivityAt = LocalDateTime.now();

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

