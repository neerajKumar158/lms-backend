package com.lms.repository;

import com.lms.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByName(String name);
    List<Organization> findByIsActiveTrue();
    Optional<Organization> findByAdminId(Long adminId);
}

