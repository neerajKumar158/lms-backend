package com.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Handles user account management and authentication. This entity manages
 * user registration, profile information, user types (STUDENT, TEACHER,
 * ORGANIZATION, ADMIN), verification status, subscription plans, and
 * role-based access control for the LMS platform.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "user_accounts")
public class UserAccount {

    /**
     * Unique identifier for the user account
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email address of the user (unique, used for login)
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Hashed password for authentication (never exposed in JSON)
     */
    @Column(nullable = false)
    @JsonIgnore // Never expose password hash
    private String passwordHash;

    /**
     * Full name of the user
     */
    @Column
    private String name;

    /**
     * Phone number of the user
     */
    @Column
    private String phone;

    /**
     * Type of user: STUDENT, TEACHER, ORGANIZATION, or ADMIN
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.STUDENT;

    /**
     * Biography or description of the user
     */
    @Column(length = 2000)
    private String bio;

    /**
     * URL to the user's avatar/profile picture
     */
    @Column
    private String avatarUrl;

    /**
     * Qualifications or credentials (primarily for teachers)
     */
    @Column(length = 1000)
    private String qualifications;

    /**
     * Street address of the user
     */
    @Column
    private String address;

    /**
     * City where the user is located
     */
    @Column
    private String city;

    /**
     * Country where the user is located
     */
    @Column
    private String country;

    /**
     * Whether the user's email has been verified
     */
    @Column(nullable = false)
    private Boolean emailVerified = false;

    /**
     * Token used for email verification (not exposed in JSON)
     */
    @Column
    @JsonIgnore // Don't expose verification token
    private String emailVerificationToken;

    /**
     * Expiry date and time for the email verification token (not exposed in JSON)
     */
    @Column
    @JsonIgnore // Don't expose token expiry
    private LocalDateTime emailVerificationTokenExpiry;

    /**
     * Whether the user's phone number has been verified
     */
    @Column
    private Boolean phoneVerified = false;

    /**
     * Whether the user has completed their profile
     */
    @Column
    private Boolean profileCompleted = false;

    /**
     * Teacher approval status: null for non-teachers, false for pending approval, true for approved
     */
    @Column
    private Boolean teacherApproved = null;

    /**
     * Subscription plan: FREE, BASIC, PREMIUM, or ENTERPRISE
     */
    @Column
    @Enumerated(EnumType.STRING)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    /**
     * Start date of the subscription
     */
    @Column
    private LocalDateTime subscriptionStartDate;

    /**
     * End date of the subscription
     */
    @Column
    private LocalDateTime subscriptionEndDate;

    /**
     * Whether the subscription is currently active
     */
    @Column
    private Boolean subscriptionActive = false;

    /**
     * Organization the user belongs to (if applicable)
     */
    @ManyToOne
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({"teachers", "courses", "students"}) // Prevent circular reference
    private Organization organization;

    /**
     * Set of roles assigned to the user for Spring Security (not exposed in JSON)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @JsonIgnore // Don't expose roles in JSON
    private Set<String> roles;

    /**
     * Timestamp when the user account was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when the user account was last updated
     */
    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Enums
    public enum UserType {
        STUDENT, TEACHER, ORGANIZATION, ADMIN
    }

    public enum SubscriptionPlan {
        FREE,        // Free tier with limited features
        BASIC,       // Basic paid plan
        PREMIUM,     // Premium plan with all features
        ENTERPRISE   // Enterprise plan for organizations
    }

}
