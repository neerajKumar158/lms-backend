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
 * UserAccount Entity - Phase 1.1
 * Extended to support:
 * - User Type: STUDENT, TEACHER, ORGANIZATION
 * - Profile information (bio, avatar, qualifications)
 * - Verification status
 * - Subscription/plan information
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "user_accounts")
public class UserAccount {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore // Never expose password hash
    private String passwordHash;

    @Column
    private String name;

    @Column
    private String phone;

    // Phase 1.1: User Type
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.STUDENT;

    // Phase 1.1: Profile Information
    @Column(length = 2000)
    private String bio;

    @Column
    private String avatarUrl;

    @Column(length = 1000)
    private String qualifications; // For teachers

    @Column
    private String address;

    @Column
    private String city;

    @Column
    private String country;

    // Phase 1.1: Verification Status
    @Column(nullable = false)
    private Boolean emailVerified = false;

    @Column
    @JsonIgnore // Don't expose verification token
    private String emailVerificationToken;

    @Column
    @JsonIgnore // Don't expose token expiry
    private LocalDateTime emailVerificationTokenExpiry;

    @Column
    private Boolean phoneVerified = false;

    @Column
    private Boolean profileCompleted = false;

    // Teacher approval status (only for teachers, null for other user types)
    @Column
    private Boolean teacherApproved = null; // null = not a teacher, false = pending approval, true = approved

    // Phase 1.1: Subscription/Plan Information
    @Column
    @Enumerated(EnumType.STRING)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    @Column
    private LocalDateTime subscriptionStartDate;

    @Column
    private LocalDateTime subscriptionEndDate;

    @Column
    private Boolean subscriptionActive = false;

    // Organization relationship
    @ManyToOne
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({"teachers", "courses", "students"}) // Prevent circular reference
    private Organization organization;

    // Roles for Spring Security
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @JsonIgnore // Don't expose roles in JSON
    private Set<String> roles;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

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
