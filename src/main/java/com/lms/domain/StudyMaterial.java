package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class StudyMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lecture_id", nullable = false)
    @JsonIgnoreProperties({"materials", "course"}) // Prevent circular reference
    private Lecture lecture;

    @Column(nullable = false)
    private String materialType; // VIDEO, PDF, AUDIO, LINK, DOCUMENT

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String fileUrl; // URL to file or external link

    @Column
    private String fileName; // Original filename

    @Column
    private Long fileSize; // File size in bytes

    @Column
    private Boolean isFree = false; // Free preview material

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Lecture getLecture() { return lecture; }
    public void setLecture(Lecture lecture) { this.lecture = lecture; }
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public Boolean getIsFree() { return isFree; }
    public void setIsFree(Boolean isFree) { this.isFree = isFree; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}

