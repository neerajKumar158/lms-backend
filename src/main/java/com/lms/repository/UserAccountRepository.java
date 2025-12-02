package com.lms.repository;

import com.lms.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Handles data access operations for UserAccount entities. This repository provides
 * methods for querying users by email, verification tokens, and organization
 * membership for authentication and user management.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    /**
     * Finds a user account by email address.
     *
     * @param email the email address to search for
     * @return the Optional containing the user account if found, empty otherwise
     */
    Optional<UserAccount> findByEmail(String email);

    /**
     * Finds a user account by email verification token.
     *
     * @param token the verification token to search for
     * @return the Optional containing the user account if found, empty otherwise
     */
    Optional<UserAccount> findByEmailVerificationToken(String token);

    /**
     * Finds all user accounts belonging to a specific organization.
     *
     * @param organizationId the organization ID
     * @return the list of user accounts in the organization
     */
    List<UserAccount> findByOrganizationId(Long organizationId);
}



