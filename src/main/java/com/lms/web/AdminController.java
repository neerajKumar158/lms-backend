package com.lms.web;

import com.lms.domain.UserAccount;
import com.lms.repository.UserAccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Admin Controller
 * Handles admin operations for managing teachers
 */
@RestController
@RequestMapping("/api/lms/admin")
public class AdminController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Check if current user is admin
     */
    private boolean isAdmin(User principal) {
        if (principal == null) {
            return false;
        }
        var user = userAccountRepository.findByEmail(principal.getUsername())
                .orElse(null);
        return user != null && user.getUserType() == UserAccount.UserType.ADMIN;
    }

    /**
     * Get all pending teachers (waiting for approval)
     */
    @GetMapping("/teachers/pending")
    public ResponseEntity<?> getPendingTeachers(@AuthenticationPrincipal User principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        List<Map<String, Object>> pendingTeachers = userAccountRepository.findAll().stream()
                .filter(u -> u.getUserType() == UserAccount.UserType.TEACHER)
                .filter(u -> u.getTeacherApproved() == null || !u.getTeacherApproved())
                .map(u -> {
                    Map<String, Object> teacherMap = new HashMap<>();
                    teacherMap.put("id", u.getId());
                    teacherMap.put("email", u.getEmail());
                    teacherMap.put("name", u.getName() != null ? u.getName() : "");
                    teacherMap.put("phone", u.getPhone() != null ? u.getPhone() : "");
                    teacherMap.put("qualifications", u.getQualifications() != null ? u.getQualifications() : "");
                    teacherMap.put("emailVerified", u.getEmailVerified());
                    teacherMap.put("profileCompleted", u.getProfileCompleted());
                    teacherMap.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
                    return teacherMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("pendingTeachers", pendingTeachers));
    }

    /**
     * Get all approved teachers
     */
    @GetMapping("/teachers/approved")
    public ResponseEntity<?> getApprovedTeachers(@AuthenticationPrincipal User principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        List<Map<String, Object>> approvedTeachers = userAccountRepository.findAll().stream()
                .filter(u -> u.getUserType() == UserAccount.UserType.TEACHER)
                .filter(u -> u.getTeacherApproved() != null && u.getTeacherApproved())
                .map(u -> {
                    Map<String, Object> teacherMap = new HashMap<>();
                    teacherMap.put("id", u.getId());
                    teacherMap.put("email", u.getEmail());
                    teacherMap.put("name", u.getName() != null ? u.getName() : "");
                    teacherMap.put("phone", u.getPhone() != null ? u.getPhone() : "");
                    teacherMap.put("qualifications", u.getQualifications() != null ? u.getQualifications() : "");
                    teacherMap.put("emailVerified", u.getEmailVerified());
                    teacherMap.put("profileCompleted", u.getProfileCompleted());
                    teacherMap.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
                    return teacherMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("approvedTeachers", approvedTeachers));
    }

    /**
     * Approve a teacher account
     */
    @PostMapping("/teachers/{teacherId}/approve")
    public ResponseEntity<Map<String, Object>> approveTeacher(
            @AuthenticationPrincipal User principal,
            @PathVariable Long teacherId) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        UserAccount teacher = userAccountRepository.findById(teacherId)
                .orElse(null);

        if (teacher == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Teacher not found"));
        }

        if (teacher.getUserType() != UserAccount.UserType.TEACHER) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is not a teacher"));
        }

        teacher.setTeacherApproved(true);
        userAccountRepository.save(teacher);

        return ResponseEntity.ok(Map.of("message", "Teacher approved successfully", "teacherId", teacherId));
    }

    /**
     * Disapprove/Reject a teacher account
     */
    @PostMapping("/teachers/{teacherId}/disapprove")
    public ResponseEntity<Map<String, Object>> disapproveTeacher(
            @AuthenticationPrincipal User principal,
            @PathVariable Long teacherId) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        UserAccount teacher = userAccountRepository.findById(teacherId)
                .orElse(null);

        if (teacher == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Teacher not found"));
        }

        if (teacher.getUserType() != UserAccount.UserType.TEACHER) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is not a teacher"));
        }

        teacher.setTeacherApproved(false);
        userAccountRepository.save(teacher);

        return ResponseEntity.ok(Map.of("message", "Teacher disapproved successfully", "teacherId", teacherId));
    }

    /**
     * Create a new teacher account directly (admin only)
     */
    @PostMapping("/teachers/create")
    public ResponseEntity<Map<String, Object>> createTeacher(
            @AuthenticationPrincipal User principal,
            @RequestBody CreateTeacherRequest request) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        if (userAccountRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        UserAccount teacher = new UserAccount();
        teacher.setEmail(request.email());
        teacher.setPasswordHash(passwordEncoder.encode(request.password()));
        teacher.setName(request.name());
        teacher.setPhone(request.phone());
        teacher.setUserType(UserAccount.UserType.TEACHER);
        teacher.setTeacherApproved(true); // Admin-created teachers are automatically approved
        teacher.setEmailVerified(true); // Admin-created teachers are automatically verified
        teacher.setProfileCompleted(request.qualifications() != null && !request.qualifications().isEmpty());
        teacher.setQualifications(request.qualifications());
        teacher.setBio(request.bio());

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_TEACHER");
        roles.add("ROLE_USER");
        teacher.setRoles(roles);

        UserAccount savedTeacher = userAccountRepository.save(teacher);

        return ResponseEntity.ok(Map.of(
                "message", "Teacher created successfully",
                "teacherId", savedTeacher.getId(),
                "email", savedTeacher.getEmail()
        ));
    }

    record CreateTeacherRequest(
            String email,
            String password,
            String name,
            String phone,
            String qualifications,
            String bio
    ) {}
}

