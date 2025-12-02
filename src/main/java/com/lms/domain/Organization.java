package com.lms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles educational institution or company management. This entity manages
 * organizations that can create and manage courses, teachers, and students
 * within the LMS platform, providing organizational structure and administration.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
public class Organization {
    /**
     * Unique identifier for the organization
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the organization
     */
    @Column(nullable = false)
    private String name;

    /**
     * Description of the organization
     */
    @Column(length = 2000)
    private String description;

    /**
     * Website URL of the organization
     */
    @Column
    private String website;

    /**
     * URL to the organization's logo image
     */
    @Column
    private String logoUrl;

    /**
     * The admin user account for this organization
     */
    @OneToOne
    @JoinColumn(name = "admin_id")
    private UserAccount admin;

    /**
     * List of teachers associated with this organization
     */
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<UserAccount> teachers = new ArrayList<>();

    /**
     * List of courses created by this organization
     */
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<Course> courses = new ArrayList<>();

    /**
     * Timestamp when the organization was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Whether the organization is currently active
     */
    @Column
    private Boolean isActive = true;

    /**
     * Gets the unique identifier of the organization
     *
     * @return the organization ID
     */
    public Long getId() { return id; }

    /**
     * Sets the unique identifier of the organization
     *
     * @param id the organization ID to set
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Gets the name of the organization
     *
     * @return the organization name
     */
    public String getName() { return name; }

    /**
     * Sets the name of the organization
     *
     * @param name the organization name to set
     */
    public void setName(String name) { this.name = name; }

    /**
     * Gets the description of the organization
     *
     * @return the description
     */
    public String getDescription() { return description; }

    /**
     * Sets the description of the organization
     *
     * @param description the description to set
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Gets the website URL of the organization
     *
     * @return the website URL
     */
    public String getWebsite() { return website; }

    /**
     * Sets the website URL of the organization
     *
     * @param website the website URL to set
     */
    public void setWebsite(String website) { this.website = website; }

    /**
     * Gets the logo URL of the organization
     *
     * @return the logo URL
     */
    public String getLogoUrl() { return logoUrl; }

    /**
     * Sets the logo URL of the organization
     *
     * @param logoUrl the logo URL to set
     */
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    /**
     * Gets the admin user account for this organization
     *
     * @return the admin user account
     */
    public UserAccount getAdmin() { return admin; }

    /**
     * Sets the admin user account for this organization
     *
     * @param admin the admin user account to set
     */
    public void setAdmin(UserAccount admin) { this.admin = admin; }

    /**
     * Gets the list of teachers associated with this organization
     *
     * @return the list of teachers
     */
    public List<UserAccount> getTeachers() { return teachers; }

    /**
     * Sets the list of teachers associated with this organization
     *
     * @param teachers the list of teachers to set
     */
    public void setTeachers(List<UserAccount> teachers) { this.teachers = teachers; }

    /**
     * Gets the list of courses created by this organization
     *
     * @return the list of courses
     */
    public List<Course> getCourses() { return courses; }

    /**
     * Sets the list of courses created by this organization
     *
     * @param courses the list of courses to set
     */
    public void setCourses(List<Course> courses) { this.courses = courses; }

    /**
     * Gets the timestamp when the organization was created
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Sets the timestamp when the organization was created
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Checks if the organization is currently active
     *
     * @return true if active, false otherwise
     */
    public Boolean getIsActive() { return isActive; }

    /**
     * Sets whether the organization is currently active
     *
     * @param isActive true to activate, false to deactivate
     */
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

