package com.lms.repository;

import com.lms.domain.CourseCertificate;
import com.lms.domain.Course;
import com.lms.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseCertificateRepository extends JpaRepository<CourseCertificate, Long> {
    List<CourseCertificate> findByStudent(UserAccount student);
    List<CourseCertificate> findByCourse(Course course);
    Optional<CourseCertificate> findByCourseAndStudent(Course course, UserAccount student);
    Optional<CourseCertificate> findByCertificateNumber(String certificateNumber);
}



