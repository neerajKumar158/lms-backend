package com.lms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles course categorization and organization. This entity manages
 * course categories with names, descriptions, icons, and category-based
 * course grouping for improved course discovery and navigation.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Setter
@Getter
@Entity
public class CourseCategory {
    /**
     * Unique identifier for the category
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the category (unique)
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Description of the category
     */
    @Column(length = 1000)
    private String description;

    /**
     * URL to the category icon image
     */
    @Column
    private String iconUrl;

    /**
     * List of courses in this category
     */
    @OneToMany(mappedBy = "category")
    private List<Course> courses = new ArrayList<>();

}

