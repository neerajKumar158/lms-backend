package com.lms.repository;

import com.lms.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.List;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> findByEmailVerificationToken(String token);
    List<UserAccount> findByOrganizationId(Long organizationId);
}



