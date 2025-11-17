package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LiveSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    @JsonIgnoreProperties({"lectures", "enrollments", "liveSessions", "assignments", "quizzes", "instructor"})
    private Course course; // Optional: can be standalone session

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "instructor_id", nullable = false)
    @JsonIgnoreProperties({"password", "enrollments", "courses", "assignments", "quizAttempts"})
    private UserAccount instructor;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime scheduledDateTime;

    @Column
    private Integer durationMinutes;

    @Column
    private String meetingLink; // Jitsi Meet room URL or other platform link

    @Column
    private String meetingId; // Unique meeting identifier

    @Column
    private String status; // SCHEDULED, ONGOING, COMPLETED, CANCELLED

    @Column
    private Integer maxParticipants;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt;

    @Column
    private String recordingUrl; // Optional: link to recording

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public UserAccount getInstructor() { return instructor; }
    public void setInstructor(UserAccount instructor) { this.instructor = instructor; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getScheduledDateTime() { return scheduledDateTime; }
    public void setScheduledDateTime(LocalDateTime scheduledDateTime) { this.scheduledDateTime = scheduledDateTime; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public String getRecordingUrl() { return recordingUrl; }
    public void setRecordingUrl(String recordingUrl) { this.recordingUrl = recordingUrl; }
}

