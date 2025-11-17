package com.lms.web;

import com.lms.domain.UserAccount;
import com.lms.repository.UserAccountRepository;
import com.lms.security.JwtService;
import com.lms.service.EmailVerificationService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Authentication Controller - Phase 2.2
 * Multi-step registration with role selection and email verification
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    record Step1RegisterRequest(@Email String email, @NotBlank String password, String userType) {}
    record Step2ProfileRequest(String name, String phone, String bio, String qualifications) {}
    record LoginRequest(@Email String email, @NotBlank String password) {}
    record VerifyEmailRequest(String token) {}
    record ResendVerificationRequest(@Email String email) {}

    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;

    @Value("${app.teacher.registration-enabled:true}")
    private boolean teacherRegistrationEnabled;

    @Value("${app.teacher.login-enabled:true}")
    private boolean teacherLoginEnabled;

    public AuthController(UserAccountRepository users, PasswordEncoder encoder, 
                         AuthenticationManager authManager, JwtService jwtService,
                         EmailVerificationService emailVerificationService) {
        this.users = users;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * Step 1: Initial Registration
     * User provides email, password, and selects user type
     */
    @PostMapping("/register/step1")
    public Map<String, Object> registerStep1(@RequestBody Step1RegisterRequest req) {
        if (users.findByEmail(req.email()).isPresent()) {
            return Map.of("error", "EMAIL_EXISTS", "message", "Email already registered");
        }

        UserAccount u = new UserAccount();
        u.setEmail(req.email());
        u.setPasswordHash(encoder.encode(req.password()));
        
        // Set user type
        try {
            UserAccount.UserType userType = UserAccount.UserType.valueOf(
                req.userType() != null ? req.userType().toUpperCase() : "STUDENT"
            );
            u.setUserType(userType);
        } catch (IllegalArgumentException e) {
            return Map.of("error", "INVALID_USER_TYPE", "message", "Invalid user type. Use: STUDENT, TEACHER, or ORGANIZATION");
        }
        
        // Check teacher-specific restrictions
        if (u.getUserType() == UserAccount.UserType.TEACHER) {
            // Check if teacher registration is enabled via configuration
            // If disabled, block registration immediately with clear error message
            if (!teacherRegistrationEnabled) {
                return Map.of("error", "TEACHER_REGISTRATION_DISABLED", "message", "Teacher registration is currently disabled. Please contact an administrator.");
            }
            
            // If registration is enabled (teacherRegistrationEnabled == true), allow registration
            // Set approval status to true (auto-approved when registration is enabled)
            u.setTeacherApproved(true);
        }
        
        // Set roles based on user type
        String role = switch (u.getUserType()) {
            case TEACHER -> "ROLE_TEACHER";
            case ORGANIZATION -> "ROLE_ORGANIZATION";
            case ADMIN -> "ROLE_ADMIN";
            default -> "ROLE_STUDENT";
        };
        // Use HashSet instead of Set.of() to allow Hibernate to modify the collection
        Set<String> roles = new HashSet<>();
        roles.add(role);
        roles.add("ROLE_USER");
        u.setRoles(roles);
        
        // Save user first
        UserAccount savedUser = users.save(u);
        
        // Send verification email (pass saved user to avoid merge issues)
        emailVerificationService.sendVerificationEmail(savedUser);
        
        String token = jwtService.generateToken(u.getEmail());
        String message = "Registration successful. Please verify your email.";
        if (u.getUserType() == UserAccount.UserType.TEACHER) {
            message = "Registration successful. Your teacher account has been created. Please verify your email.";
        }
        return Map.of(
            "token", token,
            "userType", u.getUserType().name(),
            "emailVerified", u.getEmailVerified(),
            "message", message,
            "nextStep", "verify_email"
        );
    }

    /**
     * Step 2: Profile Completion
     * User completes their profile information
     */
    @PostMapping("/register/step2")
    public Map<String, Object> completeProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Step2ProfileRequest req) {
        
        String email = extractEmailFromToken(authHeader);
        if (email == null) {
            return Map.of("error", "UNAUTHORIZED", "message", "Please login first");
        }

        UserAccount user = users.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update profile
        if (req.name() != null) user.setName(req.name());
        if (req.phone() != null) user.setPhone(req.phone());
        if (req.bio() != null) user.setBio(req.bio());
        if (req.qualifications() != null && user.getUserType() == UserAccount.UserType.TEACHER) {
            user.setQualifications(req.qualifications());
        }

        user.setProfileCompleted(true);
        users.save(user);

        return Map.of(
            "success", true,
            "message", "Profile completed successfully",
            "profileCompleted", true
        );
    }

    /**
     * Verify Email
     */
    @GetMapping("/verify-email")
    public Map<String, Object> verifyEmail(@RequestParam String token) {
        boolean verified = emailVerificationService.verifyEmail(token);
        if (verified) {
            return Map.of("success", true, "message", "Email verified successfully");
        } else {
            return Map.of("error", "INVALID_TOKEN", "message", "Invalid or expired verification token");
        }
    }

    /**
     * Resend Verification Email
     */
    @PostMapping("/resend-verification")
    public Map<String, Object> resendVerification(@RequestBody ResendVerificationRequest req) {
        boolean sent = emailVerificationService.resendVerificationEmail(req.email());
        if (sent) {
            return Map.of("success", true, "message", "Verification email sent");
        } else {
            return Map.of("error", "NOT_FOUND", "message", "User not found or already verified");
        }
    }

    /**
     * Login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "INVALID_CREDENTIALS", "message", "Invalid email or password. Please try again."));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "AUTHENTICATION_FAILED", "message", "Authentication failed. Please check your credentials and try again."));
        }
        
        UserAccount user = users.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check teacher-specific restrictions
        if (user.getUserType() == UserAccount.UserType.TEACHER) {
            // Check if teacher login is enabled via configuration
            // If disabled, block login immediately with clear error message
            if (!teacherLoginEnabled) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "TEACHER_LOGIN_DISABLED", "message", "Teacher login is currently disabled. Please contact an administrator."));
            }
            
            // If login is enabled (teacherLoginEnabled == true), allow login
            // Approval check is optional - if you want to keep it, uncomment below:
            // Boolean approved = user.getTeacherApproved();
            // if (approved == null || !approved) {
            //     return ResponseEntity.status(HttpStatus.FORBIDDEN)
            //             .body(Map.of("error", "TEACHER_NOT_APPROVED", "message", "Your teacher account is pending admin approval. Please contact an administrator."));
            // }
        }
        
        String token = jwtService.generateToken(req.email());
        
        return ResponseEntity.ok(Map.of(
            "token", token,
            "userType", user.getUserType().name(),
            "emailVerified", user.getEmailVerified(),
            "profileCompleted", user.getProfileCompleted()
        ));
    }

    /**
     * Check Registration Status
     */
    @GetMapping("/registration-status")
    public Map<String, Object> getRegistrationStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String email = extractEmailFromToken(authHeader);
        if (email == null) {
            return Map.of("error", "UNAUTHORIZED");
        }

        UserAccount user = users.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return Map.of(
            "emailVerified", user.getEmailVerified(),
            "profileCompleted", user.getProfileCompleted(),
            "userType", user.getUserType().name(),
            "nextStep", determineNextStep(user)
        );
    }

    private String extractEmailFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.substring(7);
            return jwtService.extractSubject(token);
        } catch (Exception e) {
            return null;
        }
    }

    private String determineNextStep(UserAccount user) {
        if (!user.getEmailVerified()) {
            return "verify_email";
        }
        if (!user.getProfileCompleted()) {
            return "complete_profile";
        }
        return "dashboard";
    }
}
