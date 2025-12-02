package com.lms.repository;

import com.lms.domain.Course;
import com.lms.domain.CourseWishlist;
import com.lms.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseWishlistRepository extends JpaRepository<CourseWishlist, Long> {
    List<CourseWishlist> findByUser(UserAccount user);
    Optional<CourseWishlist> findByUserAndCourse(UserAccount user, Course course);
    boolean existsByUserAndCourse(UserAccount user, Course course);
    long countByCourse(Course course);
}



