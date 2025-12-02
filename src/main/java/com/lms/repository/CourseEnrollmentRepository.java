package com.lms.repository;

import com.lms.domain.CourseEnrollment;
import com.lms.domain.UserAccount;
import com.lms.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Handles data access operations for CourseEnrollment entities. This repository provides
 * methods for querying enrollments by student, course, and checking enrollment existence
 * for enrollment management and validation.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    /**
     * Finds all enrollments for a specific student.
     *
     * @param student the student user account
     * @return the list of enrollments for the student
     */
    List<CourseEnrollment> findByStudent(UserAccount student);

    /**
     * Finds all enrollments for a specific course.
     *
     * @param course the course entity
     * @return the list of enrollments for the course
     */
    List<CourseEnrollment> findByCourse(Course course);

    /**
     * Finds a specific enrollment by student and course.
     *
     * @param student the student user account
     * @param course the course entity
     * @return the Optional containing the enrollment if found, empty otherwise
     */
    Optional<CourseEnrollment> findByStudentAndCourse(UserAccount student, Course course);

    /**
     * Checks if an enrollment exists for a specific student and course.
     *
     * @param student the student user account
     * @param course the course entity
     * @return true if enrollment exists, false otherwise
     */
    boolean existsByStudentAndCourse(UserAccount student, Course course);
}

