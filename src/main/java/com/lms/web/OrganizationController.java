package com.lms.web;

import com.lms.domain.*;
import com.lms.repository.UserAccountRepository;
import com.lms.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lms/organization")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping
    public ResponseEntity<Organization> getMyOrganization(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(null);
        }
        var user = userAccountRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return organizationService.getOrganizationByAdminId(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrganization(
            @AuthenticationPrincipal User principal,
            @RequestBody CreateOrganizationRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Organization organization = new Organization();
            organization.setName(request.name());
            organization.setDescription(request.description());
            organization.setWebsite(request.website());
            organization.setLogoUrl(request.logoUrl());

            Organization created = organizationService.createOrganization(user.getId(), organization);
            return ResponseEntity.ok(Map.of("id", created.getId(), "message", "Organization created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateOrganization(
            @AuthenticationPrincipal User principal,
            @RequestBody CreateOrganizationRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Organization org = organizationService.getOrganizationByAdminId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            Organization updated = new Organization();
            updated.setName(request.name());
            updated.setDescription(request.description());
            updated.setWebsite(request.website());
            updated.setLogoUrl(request.logoUrl());

            organizationService.updateOrganization(org.getId(), updated);
            return ResponseEntity.ok(Map.of("message", "Organization updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/teachers")
    public ResponseEntity<?> getTeachers(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
        }
        var user = userAccountRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Organization org = organizationService.getOrganizationByAdminId(user.getId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        return ResponseEntity.ok(organizationService.getOrganizationTeachers(org.getId()));
    }

    @PostMapping("/teachers")
    public ResponseEntity<Map<String, Object>> addTeacher(
            @AuthenticationPrincipal User principal,
            @RequestBody AddTeacherRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Organization org = organizationService.getOrganizationByAdminId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            UserAccount teacher = userAccountRepository.findByEmail(request.email())
                    .orElseThrow(() -> new RuntimeException("Teacher not found with email: " + request.email()));
            
            organizationService.addTeacherToOrganization(org.getId(), teacher.getId());
            return ResponseEntity.ok(Map.of("message", "Teacher added successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/teachers/{teacherId}")
    public ResponseEntity<Map<String, Object>> removeTeacher(
            @AuthenticationPrincipal User principal,
            @PathVariable("teacherId") Long teacherId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Organization org = organizationService.getOrganizationByAdminId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            organizationService.removeTeacherFromOrganization(org.getId(), teacherId);
            return ResponseEntity.ok(Map.of("message", "Teacher removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/courses")
    public ResponseEntity<?> getCourses(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
        }
        var user = userAccountRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Organization org = organizationService.getOrganizationByAdminId(user.getId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        return ResponseEntity.ok(organizationService.getOrganizationCourses(org.getId()));
    }

    @GetMapping("/students")
    public ResponseEntity<?> getStudents(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
        }
        var user = userAccountRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Organization org = organizationService.getOrganizationByAdminId(user.getId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        List<CourseEnrollment> enrollments = organizationService.getOrganizationEnrollments(org.getId());
        List<Map<String, Object>> students = enrollments.stream()
                .map(enrollment -> {
                    Map<String, Object> studentMap = new java.util.HashMap<>();
                    studentMap.put("id", enrollment.getStudent().getId());
                    studentMap.put("name", enrollment.getStudent().getName() != null ? enrollment.getStudent().getName() : "");
                    studentMap.put("email", enrollment.getStudent().getEmail());
                    studentMap.put("course", enrollment.getCourse().getTitle());
                    return studentMap;
                })
                .distinct()
                .toList();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(@AuthenticationPrincipal User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
            }
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Organization org = organizationService.getOrganizationByAdminId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            List<UserAccount> teachers = organizationService.getOrganizationTeachers(org.getId());
            List<Course> courses = organizationService.getOrganizationCourses(org.getId());
            long studentsCount = organizationService.getOrganizationStudentsCount(org.getId());
            List<CourseEnrollment> enrollments = organizationService.getOrganizationEnrollments(org.getId());
            
            return ResponseEntity.ok(Map.of(
                    "teachersCount", teachers.size(),
                    "coursesCount", courses.size(),
                    "studentsCount", studentsCount,
                    "enrollmentsCount", enrollments.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    record CreateOrganizationRequest(String name, String description, String website, String logoUrl) {}
    record AddTeacherRequest(String email) {}
}

