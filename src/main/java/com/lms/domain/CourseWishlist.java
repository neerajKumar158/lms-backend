package com.lms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Handles course wishlist and favorites. This entity manages user wishlist
 * entries, ensuring one entry per user per course, and tracks when courses
 * were added for personalized course recommendations and saved items.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Setter
@Getter
@Entity
@Table(name = "course_wishlist", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "course_id"})
})
public class CourseWishlist {
    /**
     * Unique identifier for the wishlist entry
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who added the course to wishlist
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    /**
     * The course added to the wishlist
     */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * Timestamp when the course was added to the wishlist
     */
    @Column(nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();
}


