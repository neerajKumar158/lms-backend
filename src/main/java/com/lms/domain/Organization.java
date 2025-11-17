package com.lms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column
    private String website;

    @Column
    private String logoUrl;

    @OneToOne
    @JoinColumn(name = "admin_id")
    private UserAccount admin; // Organization admin

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<UserAccount> teachers = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<Course> courses = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private Boolean isActive = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public UserAccount getAdmin() { return admin; }
    public void setAdmin(UserAccount admin) { this.admin = admin; }
    public List<UserAccount> getTeachers() { return teachers; }
    public void setTeachers(List<UserAccount> teachers) { this.teachers = teachers; }
    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

