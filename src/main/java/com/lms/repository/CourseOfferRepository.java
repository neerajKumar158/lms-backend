package com.lms.repository;

import com.lms.domain.Course;
import com.lms.domain.CourseOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourseOfferRepository extends JpaRepository<CourseOffer, Long> {
    List<CourseOffer> findByActiveTrue();
    
    @Query("SELECT o FROM CourseOffer o WHERE o.active = true " +
           "AND o.validFrom <= :now AND o.validTo >= :now " +
           "AND (o.applicableCourses IS EMPTY OR :course MEMBER OF o.applicableCourses)")
    List<CourseOffer> findActiveOffersForCourse(@Param("course") Course course, @Param("now") LocalDateTime now);
    
    List<CourseOffer> findByActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            LocalDateTime now1, LocalDateTime now2);
}

