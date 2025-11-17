package com.lms.repository;

import com.lms.domain.CourseEnrollment;
import com.lms.domain.UserAccount;
import com.lms.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    List<CourseEnrollment> findByStudent(UserAccount student);
    List<CourseEnrollment> findByCourse(Course course);
    Optional<CourseEnrollment> findByStudentAndCourse(UserAccount student, Course course);
    boolean existsByStudentAndCourse(UserAccount student, Course course);
}

