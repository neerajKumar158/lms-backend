package com.lms.repository;

import com.lms.domain.Course;
import com.lms.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByStatus(Course.CourseStatus status);
    List<Course> findByInstructor(UserAccount instructor);
    List<Course> findByOrganizationId(Long organizationId);
    List<Course> findByCategoryId(Long categoryId);
    List<Course> findByPrice(BigDecimal price);
    
    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND (c.title LIKE %:keyword% OR c.description LIKE %:keyword%)")
    List<Course> searchPublishedCourses(@Param("keyword") String keyword);
    
    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND c.price = 0")
    List<Course> findFreePublishedCourses();
    
    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND c.featured = true ORDER BY c.publishedAt DESC")
    List<Course> findFeaturedPublishedCourses();
}

