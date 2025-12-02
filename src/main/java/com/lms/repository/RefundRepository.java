package com.lms.repository;

import com.lms.domain.Course;
import com.lms.domain.CoursePayment;
import com.lms.domain.Refund;
import com.lms.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByStudent(UserAccount student);
    List<Refund> findByCourse(Course course);
    Optional<Refund> findByPayment(CoursePayment payment);
    List<Refund> findByStatus(Refund.RefundStatus status);
}




