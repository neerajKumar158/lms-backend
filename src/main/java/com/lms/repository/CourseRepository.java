package com.lms.repository;

import com.lms.domain.Course;
import com.lms.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Handles data access operations for Course entities. This repository provides
 * methods for querying courses by status, instructor, category, and other criteria,
 * including search and filtering capabilities for published courses.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    /**
     * Finds all courses with the specified status
     *
     * @param status the course status to search for
     * @return list of courses with the specified status
     */
    List<Course> findByStatus(Course.CourseStatus status);

    /**
     * Finds all courses created by the specified instructor
     *
     * @param instructor the instructor user account
     * @return list of courses created by the instructor
     */
    List<Course> findByInstructor(UserAccount instructor);

    /**
     * Finds all courses belonging to the specified organization
     *
     * @param organizationId the organization ID
     * @return list of courses belonging to the organization
     */
    List<Course> findByOrganizationId(Long organizationId);

    /**
     * Finds all courses in the specified category
     *
     * @param categoryId the category ID
     * @return list of courses in the category
     */
    List<Course> findByCategoryId(Long categoryId);

    /**
     * Finds all courses with the specified price
     *
     * @param price the course price
     * @return list of courses with the specified price
     */
    List<Course> findByPrice(BigDecimal price);
    
    /**
     * Searches for published courses matching the keyword in title or description
     *
     * @param keyword the search keyword
     * @return list of published courses matching the keyword
     */
    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND (c.title LIKE %:keyword% OR c.description LIKE %:keyword%)")
    List<Course> searchPublishedCourses(@Param("keyword") String keyword);
    
    /**
     * Finds all free published courses (price = 0)
     *
     * @return list of free published courses
     */
    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND c.price = 0")
    List<Course> findFreePublishedCourses();
    
    /**
     * Finds all featured published courses, ordered by publication date descending
     *
     * @return list of featured published courses
     */
    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND c.featured = true ORDER BY c.publishedAt DESC")
    List<Course> findFeaturedPublishedCourses();
}

