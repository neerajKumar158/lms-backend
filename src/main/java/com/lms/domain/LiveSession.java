package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Handles live streaming sessions for courses. This entity manages session
 * scheduling, meeting links, participant management, session status tracking,
 * and recording URLs for live interactive learning sessions.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LiveSession {
    /**
     * Unique identifier for the live session
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The course this session belongs to (optional, can be standalone)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    @JsonIgnoreProperties({"lectures", "enrollments", "liveSessions", "assignments", "quizzes", "instructor"})
    private Course course;

    /**
     * The instructor conducting the session
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "instructor_id", nullable = false)
    @JsonIgnoreProperties({"password", "enrollments", "courses", "assignments", "quizAttempts"})
    private UserAccount instructor;

    /**
     * Title of the live session
     */
    @Column(nullable = false)
    private String title;

    /**
     * Description of the live session content
     */
    @Column(length = 2000)
    private String description;

    /**
     * Scheduled date and time for the session
     */
    @Column(nullable = false)
    private LocalDateTime scheduledDateTime;

    /**
     * Duration of the session in minutes
     */
    @Column
    private Integer durationMinutes;

    /**
     * URL to join the meeting (Jitsi Meet room URL or other platform link)
     */
    @Column
    private String meetingLink;

    /**
     * Unique identifier for the meeting
     */
    @Column
    private String meetingId;

    /**
     * Current status of the session: SCHEDULED, ONGOING, COMPLETED, or CANCELLED
     */
    @Column
    private String status;

    /**
     * Maximum number of participants allowed in the session
     */
    @Column
    private Integer maxParticipants;

    /**
     * Timestamp when the session was started
     */
    @Column
    private LocalDateTime startedAt;

    /**
     * Timestamp when the session ended
     */
    @Column
    private LocalDateTime endedAt;

    /**
     * URL to the recording of the session (if available)
     */
    @Column
    private String recordingUrl;

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

