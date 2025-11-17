package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"lectures", "enrollments", "liveSessions", "assignments", "quizzes"}) // Prevent circular reference
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column
    private Integer sequenceOrder; // Order of lecture in course

    @Column
    private Integer durationMinutes;

    @Column
    private Boolean isFree = false; // Free preview lecture

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"lecture"}) // Prevent circular reference
    private List<StudyMaterial> materials = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Boolean getIsFree() { return isFree; }
    public void setIsFree(Boolean isFree) { this.isFree = isFree; }
    public List<StudyMaterial> getMaterials() { return materials; }
    public void setMaterials(List<StudyMaterial> materials) { this.materials = materials; }
}

