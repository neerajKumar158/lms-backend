package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Handles file or resource management for lectures. This entity manages study materials
 * (videos, PDFs, audio, documents, links) associated with lectures, including file metadata
 * and free preview content designation.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
public class StudyMaterial {
    /**
     * Unique identifier for the study material
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The lecture this study material belongs to
     */
    @ManyToOne
    @JoinColumn(name = "lecture_id", nullable = false)
    @JsonIgnoreProperties({"materials", "course"}) // Prevent circular reference
    private Lecture lecture;

    /**
     * Type of material: VIDEO, PDF, AUDIO, LINK, DOCUMENT
     */
    @Column(nullable = false)
    private String materialType;

    /**
     * Title of the study material
     */
    @Column(nullable = false)
    private String title;

    /**
     * Description of the study material content
     */
    @Column(length = 2000)
    private String description;

    /**
     * URL to the file or external link
     */
    @Column(length = 2000)
    private String fileUrl;

    /**
     * Original filename of the uploaded file
     */
    @Column
    private String fileName;

    /**
     * File size in bytes
     */
    @Column
    private Long fileSize;

    /**
     * Whether this material is available as a free preview
     */
    @Column
    private Boolean isFree = false;

    /**
     * Timestamp when the material was uploaded
     */
    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    /**
     * Gets the unique identifier of the study material
     *
     * @return the study material ID
     */
    public Long getId() { return id; }

    /**
     * Sets the unique identifier of the study material
     *
     * @param id the study material ID to set
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Gets the lecture this study material belongs to
     *
     * @return the lecture entity
     */
    public Lecture getLecture() { return lecture; }

    /**
     * Sets the lecture this study material belongs to
     *
     * @param lecture the lecture entity to set
     */
    public void setLecture(Lecture lecture) { this.lecture = lecture; }

    /**
     * Gets the type of material (VIDEO, PDF, AUDIO, LINK, DOCUMENT)
     *
     * @return the material type
     */
    public String getMaterialType() { return materialType; }

    /**
     * Sets the type of material
     *
     * @param materialType the material type to set
     */
    public void setMaterialType(String materialType) { this.materialType = materialType; }

    /**
     * Gets the title of the study material
     *
     * @return the title
     */
    public String getTitle() { return title; }

    /**
     * Sets the title of the study material
     *
     * @param title the title to set
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Gets the description of the study material
     *
     * @return the description
     */
    public String getDescription() { return description; }

    /**
     * Sets the description of the study material
     *
     * @param description the description to set
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Gets the URL to the file or external link
     *
     * @return the file URL
     */
    public String getFileUrl() { return fileUrl; }

    /**
     * Sets the URL to the file or external link
     *
     * @param fileUrl the file URL to set
     */
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    /**
     * Gets the original filename of the uploaded file
     *
     * @return the filename
     */
    public String getFileName() { return fileName; }

    /**
     * Sets the original filename of the uploaded file
     *
     * @param fileName the filename to set
     */
    public void setFileName(String fileName) { this.fileName = fileName; }

    /**
     * Gets the file size in bytes
     *
     * @return the file size
     */
    public Long getFileSize() { return fileSize; }

    /**
     * Sets the file size in bytes
     *
     * @param fileSize the file size to set
     */
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    /**
     * Checks if this material is available as a free preview
     *
     * @return true if the material is free, false otherwise
     */
    public Boolean getIsFree() { return isFree; }

    /**
     * Sets whether this material is available as a free preview
     *
     * @param isFree true to make it free, false otherwise
     */
    public void setIsFree(Boolean isFree) { this.isFree = isFree; }

    /**
     * Gets the timestamp when the material was uploaded
     *
     * @return the upload timestamp
     */
    public LocalDateTime getUploadedAt() { return uploadedAt; }

    /**
     * Sets the timestamp when the material was uploaded
     *
     * @param uploadedAt the upload timestamp to set
     */
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}

