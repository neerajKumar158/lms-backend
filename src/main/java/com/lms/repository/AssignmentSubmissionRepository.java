package com.lms.repository;

import com.lms.domain.AssignmentSubmission;
import com.lms.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);
    List<AssignmentSubmission> findByStudent(UserAccount student);
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
}



