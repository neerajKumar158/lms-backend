package com.lms.repository;

import com.lms.domain.CoursePayment;
import com.lms.domain.UserAccount;
import com.lms.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoursePaymentRepository extends JpaRepository<CoursePayment, Long> {
    List<CoursePayment> findByStudent(UserAccount student);
    List<CoursePayment> findByCourse(Course course);
    Optional<CoursePayment> findByRazorpayOrderId(String razorpayOrderId);
    Optional<CoursePayment> findByRazorpayPaymentId(String razorpayPaymentId);
    List<CoursePayment> findByStatus(String status);
}

