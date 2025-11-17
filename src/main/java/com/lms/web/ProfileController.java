package com.lms.web;

import com.lms.domain.UserAccount;
import com.lms.repository.UserAccountRepository;
import com.lms.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Profile Controller - Enhanced Profile Management
 * Profile completion and management with all features
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    record ProfileDto(
        Long id, String email, String name, String phone, String userType,
        String bio, String avatarUrl, String qualifications,
        String address, String city, String country,
        Boolean emailVerified, Boolean phoneVerified, Boolean profileCompleted,
        String subscriptionPlan, LocalDateTime subscriptionStartDate,
        LocalDateTime subscriptionEndDate, Boolean subscriptionActive,
        LocalDateTime createdAt
    ) {}
    
    record UpdateProfileRequest(
        String name, String phone, String bio, String qualifications,
        String address, String city, String country, String avatarUrl
    ) {}

    private final UserAccountRepository users;
    private final JwtService jwtService;
    private final com.lms.service.FileUploadService fileUploadService;

    public ProfileController(
            UserAccountRepository users,
            JwtService jwtService,
            com.lms.service.FileUploadService fileUploadService) {
        this.users = users;
        this.jwtService = jwtService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User principal) {
        try {
            UserAccount ua = users.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            ProfileDto profile = new ProfileDto(
                ua.getId(),
                ua.getEmail(),
                ua.getName(),
                ua.getPhone(),
                ua.getUserType() != null ? ua.getUserType().name() : null,
                ua.getBio(),
                ua.getAvatarUrl(),
                ua.getQualifications(),
                ua.getAddress(),
                ua.getCity(),
                ua.getCountry(),
                ua.getEmailVerified(),
                ua.getPhoneVerified(),
                ua.getProfileCompleted(),
                ua.getSubscriptionPlan() != null ? ua.getSubscriptionPlan().name() : null,
                ua.getSubscriptionStartDate(),
                ua.getSubscriptionEndDate(),
                ua.getSubscriptionActive(),
                ua.getCreatedAt()
            );
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load profile"));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User principal,
            @RequestBody UpdateProfileRequest req) {
        try {
            UserAccount ua = users.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (req.name() != null) ua.setName(req.name());
            if (req.phone() != null) ua.setPhone(req.phone());
            if (req.bio() != null) ua.setBio(req.bio());
            if (req.qualifications() != null) ua.setQualifications(req.qualifications());
            if (req.address() != null) ua.setAddress(req.address());
            if (req.city() != null) ua.setCity(req.city());
            if (req.country() != null) ua.setCountry(req.country());
            if (req.avatarUrl() != null) ua.setAvatarUrl(req.avatarUrl());
            
            ua.setUpdatedAt(LocalDateTime.now());
            
            // Mark profile as completed if essential fields are filled
            if (ua.getName() != null && !ua.getName().isEmpty() && 
                ua.getPhone() != null && !ua.getPhone().isEmpty()) {
                ua.setProfileCompleted(true);
            }
            
            users.save(ua);
            return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to update profile"));
        }
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(
            @AuthenticationPrincipal User principal,
            @RequestParam("file") MultipartFile file) {
        try {
            UserAccount ua = users.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }
            
            // Upload avatar to avatars subdirectory
            String avatarUrl = fileUploadService.uploadFile(file, "avatars");
            ua.setAvatarUrl(avatarUrl);
            ua.setUpdatedAt(LocalDateTime.now());
            users.save(ua);
            
            return ResponseEntity.ok(Map.of("success", true, "avatarUrl", avatarUrl, "message", "Avatar uploaded successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to upload avatar"));
        }
    }

    @GetMapping("/completion-status")
    public ResponseEntity<?> getCompletionStatus(@AuthenticationPrincipal User principal) {
        try {
            UserAccount ua = users.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> status = new HashMap<>();
            status.put("emailVerified", ua.getEmailVerified() != null ? ua.getEmailVerified() : false);
            status.put("phoneVerified", ua.getPhoneVerified() != null ? ua.getPhoneVerified() : false);
            status.put("profileCompleted", ua.getProfileCompleted() != null ? ua.getProfileCompleted() : false);
            status.put("hasName", ua.getName() != null && !ua.getName().isEmpty());
            status.put("hasPhone", ua.getPhone() != null && !ua.getPhone().isEmpty());
            status.put("hasBio", ua.getBio() != null && !ua.getBio().isEmpty());
            status.put("hasAvatar", ua.getAvatarUrl() != null && !ua.getAvatarUrl().isEmpty());
            status.put("hasQualifications", ua.getQualifications() != null && !ua.getQualifications().isEmpty());
            
            // Calculate completion percentage
            int completed = 0;
            int total = 7;
            if (status.get("emailVerified").equals(true)) completed++;
            if (status.get("phoneVerified").equals(true)) completed++;
            if ((Boolean) status.get("hasName")) completed++;
            if ((Boolean) status.get("hasPhone")) completed++;
            if ((Boolean) status.get("hasBio")) completed++;
            if ((Boolean) status.get("hasAvatar")) completed++;
            if ((Boolean) status.get("hasQualifications")) completed++;
            
            status.put("completionPercentage", (completed * 100) / total);
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load completion status"));
        }
    }

    @GetMapping("/verification-status")
    public ResponseEntity<?> getVerificationStatus(@AuthenticationPrincipal User principal) {
        try {
            UserAccount ua = users.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> verification = new HashMap<>();
            verification.put("emailVerified", ua.getEmailVerified() != null ? ua.getEmailVerified() : false);
            verification.put("phoneVerified", ua.getPhoneVerified() != null ? ua.getPhoneVerified() : false);
            verification.put("profileCompleted", ua.getProfileCompleted() != null ? ua.getProfileCompleted() : false);
            verification.put("userType", ua.getUserType() != null ? ua.getUserType().name() : "STUDENT");
            
            return ResponseEntity.ok(verification);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load verification status"));
        }
    }

    @GetMapping("/subscription")
    public ResponseEntity<?> getSubscriptionInfo(@AuthenticationPrincipal User principal) {
        try {
            UserAccount ua = users.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> subscription = new HashMap<>();
            subscription.put("plan", ua.getSubscriptionPlan() != null ? ua.getSubscriptionPlan().name() : "FREE");
            subscription.put("active", ua.getSubscriptionActive() != null ? ua.getSubscriptionActive() : false);
            subscription.put("startDate", ua.getSubscriptionStartDate());
            subscription.put("endDate", ua.getSubscriptionEndDate());
            
            // Check if subscription is expired
            if (ua.getSubscriptionEndDate() != null && ua.getSubscriptionEndDate().isBefore(LocalDateTime.now())) {
                subscription.put("expired", true);
                ua.setSubscriptionActive(false);
                users.save(ua);
            } else {
                subscription.put("expired", false);
            }
            
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to load subscription info"));
        }
    }
}

