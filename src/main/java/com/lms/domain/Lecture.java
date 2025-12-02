package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles individual lessons or lectures within a course. This entity manages
 * lecture content, sequencing, and associated study materials (videos, PDFs, etc.),
 * maintaining the course structure and organization.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
public class Lecture {
    /**
     * Unique identifier for the lecture
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The course this lecture belongs to
     */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"lectures", "enrollments", "liveSessions", "assignments", "quizzes"}) // Prevent circular reference
    private Course course;

    /**
     * Title of the lecture
     */
    @Column(nullable = false)
    private String title;

    /**
     * Detailed description of the lecture content
     */
    @Column(length = 2000)
    private String description;

    /**
     * Order of lecture in course (used for sequencing)
     */
    @Column
    private Integer sequenceOrder;

    /**
     * Duration of the lecture in minutes
     */
    @Column
    private Integer durationMinutes;

    /**
     * Whether this lecture is available as a free preview
     */
    @Column
    private Boolean isFree = false;

    /**
     * List of study materials (videos, PDFs, etc.) associated with this lecture
     */
    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"lecture"}) // Prevent circular reference
    private List<StudyMaterial> materials = new ArrayList<>();

    /**
     * Gets the unique identifier of the lecture
     *
     * @return the lecture ID
     */
    public Long getId() { return id; }

    /**
     * Sets the unique identifier of the lecture
     *
     * @param id the lecture ID to set
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Gets the course this lecture belongs to
     *
     * @return the course entity
     */
    public Course getCourse() { return course; }

    /**
     * Sets the course this lecture belongs to
     *
     * @param course the course entity to set
     */
    public void setCourse(Course course) { this.course = course; }

    /**
     * Gets the title of the lecture
     *
     * @return the lecture title
     */
    public String getTitle() { return title; }

    /**
     * Sets the title of the lecture
     *
     * @param title the lecture title to set
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Gets the description of the lecture
     *
     * @return the lecture description
     */
    public String getDescription() { return description; }

    /**
     * Sets the description of the lecture
     *
     * @param description the lecture description to set
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Gets the sequence order of the lecture within the course
     *
     * @return the sequence order
     */
    public Integer getSequenceOrder() { return sequenceOrder; }

    /**
     * Sets the sequence order of the lecture within the course
     *
     * @param sequenceOrder the sequence order to set
     */
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    /**
     * Gets the duration of the lecture in minutes
     *
     * @return the duration in minutes
     */
    public Integer getDurationMinutes() { return durationMinutes; }

    /**
     * Sets the duration of the lecture in minutes
     *
     * @param durationMinutes the duration in minutes to set
     */
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    /**
     * Checks if this lecture is available as a free preview
     *
     * @return true if the lecture is free, false otherwise
     */
    public Boolean getIsFree() { return isFree; }

    /**
     * Sets whether this lecture is available as a free preview
     *
     * @param isFree true to make it free, false otherwise
     */
    public void setIsFree(Boolean isFree) { this.isFree = isFree; }

    /**
     * Gets the list of study materials associated with this lecture
     *
     * @return the list of study materials
     */
    public List<StudyMaterial> getMaterials() { return materials; }

    /**
     * Sets the list of study materials associated with this lecture
     *
     * @param materials the list of study materials to set
     */
    public void setMaterials(List<StudyMaterial> materials) { this.materials = materials; }
}

