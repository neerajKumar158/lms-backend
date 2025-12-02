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
import java.util.Optional;

@RestController
@RequestMapping("/api/lms/organization")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping
    public ResponseEntity<?> getMyOrganization(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<Organization> org = organizationService.getOrganizationByAdminId(user.getId());
            if (org.isPresent()) {
                return ResponseEntity.ok(org.get());
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Organization not found",
                        "message", "Please create an organization first. User ID: " + user.getId(),
                        "userType", user.getUserType() != null ? user.getUserType().toString() : "null"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
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
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Organization org = organizationService.getOrganizationByAdminId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Organization not found for user ID: " + user.getId()));
            
            List<UserAccount> teachers = organizationService.getOrganizationTeachers(org.getId());
            return ResponseEntity.ok(teachers != null ? teachers : List.of());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
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
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Organization org = organizationService.getOrganizationByAdminId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Organization not found for user ID: " + user.getId()));
            
            List<Course> courses = organizationService.getOrganizationCourses(org.getId());
            return ResponseEntity.ok(courses != null ? courses : List.of());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/students")
    public ResponseEntity<?> getStudents(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required. Please login first."));
        }
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Organization org = organizationService.getOrganizationByAdminId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Organization not found for user ID: " + user.getId()));
            
            List<CourseEnrollment> enrollments = organizationService.getOrganizationEnrollments(org.getId());
            // Get unique students by their ID
            Map<Long, Map<String, Object>> uniqueStudents = new java.util.HashMap<>();
            if (enrollments != null) {
                enrollments.forEach(enrollment -> {
                    Long studentId = enrollment.getStudent().getId();
                    if (!uniqueStudents.containsKey(studentId)) {
                        Map<String, Object> studentMap = new java.util.HashMap<>();
                        studentMap.put("id", enrollment.getStudent().getId());
                        studentMap.put("name", enrollment.getStudent().getName() != null ? enrollment.getStudent().getName() : "");
                        studentMap.put("email", enrollment.getStudent().getEmail());
                        studentMap.put("course", enrollment.getCourse().getTitle());
                        uniqueStudents.put(studentId, studentMap);
                    }
                });
            }
            return ResponseEntity.ok(new java.util.ArrayList<>(uniqueStudents.values()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
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
                    .orElseThrow(() -> new RuntimeException("Organization not found for user ID: " + user.getId()));
            
            List<UserAccount> teachers = organizationService.getOrganizationTeachers(org.getId());
            List<Course> courses = organizationService.getOrganizationCourses(org.getId());
            long studentsCount = organizationService.getOrganizationStudentsCount(org.getId());
            List<CourseEnrollment> enrollments = organizationService.getOrganizationEnrollments(org.getId());
            
            return ResponseEntity.ok(Map.of(
                    "teachersCount", teachers != null ? teachers.size() : 0,
                    "coursesCount", courses != null ? courses.size() : 0,
                    "studentsCount", studentsCount,
                    "enrollmentsCount", enrollments != null ? enrollments.size() : 0
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Creates an organization for the current user (if they don't have one).
     * This is a convenience endpoint that creates a default organization.
     */
    @PostMapping("/create-for-me")
    public ResponseEntity<Map<String, Object>> createOrganizationForMe(
            @AuthenticationPrincipal User principal,
            @RequestBody(required = false) Map<String, String> request) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check if user already has an organization
            Optional<Organization> existingOrg = organizationService.getOrganizationByAdminId(user.getId());
            if (existingOrg.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "message", "Organization already exists",
                        "organizationId", existingOrg.get().getId(),
                        "organizationName", existingOrg.get().getName()
                ));
            }
            
            String orgName = request != null && request.containsKey("name") 
                    ? request.get("name") 
                    : "Organization for " + user.getEmail();
            
            Organization org = organizationService.createOrganizationForExistingUser(
                    user.getId(), 
                    orgName, 
                    true // Update user type to ORGANIZATION
            );
            
            return ResponseEntity.ok(Map.of(
                    "message", "Organization created successfully",
                    "organizationId", org.getId(),
                    "organizationName", org.getName()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/fix-courses")
    public ResponseEntity<Map<String, Object>> fixCoursesOrganization(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Organization org = organizationService.getOrganizationByAdminId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            int fixedCount = organizationService.fixCoursesOrganization(org.getId());
            return ResponseEntity.ok(Map.of(
                    "message", "Courses fixed successfully",
                    "fixedCount", fixedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> getDebugInfo(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        try {
            var user = userAccountRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Organization org = organizationService.getOrganizationByAdminId(user.getId())
                    .orElse(null);
            
            Map<String, Object> debug = new java.util.HashMap<>();
            debug.put("userId", user.getId());
            debug.put("userEmail", user.getEmail());
            debug.put("userType", user.getUserType() != null ? user.getUserType().toString() : "null");
            debug.put("userOrganization", user.getOrganization() != null ? user.getOrganization().getId() : "null");
            
            if (org != null) {
                debug.put("organizationId", org.getId());
                debug.put("organizationName", org.getName());
                
                // Check all users with this organization
                List<UserAccount> allOrgUsers = userAccountRepository.findByOrganizationId(org.getId());
                debug.put("allOrgUsersCount", allOrgUsers.size());
                debug.put("allOrgUsers", allOrgUsers.stream().map(u -> Map.of(
                        "id", u.getId(),
                        "email", u.getEmail(),
                        "userType", u.getUserType() != null ? u.getUserType().toString() : "null"
                )).toList());
                
                // Check teachers
                List<UserAccount> teachers = organizationService.getOrganizationTeachers(org.getId());
                debug.put("teachersCount", teachers.size());
                
                // Check courses
                List<Course> courses = organizationService.getOrganizationCourses(org.getId());
                debug.put("coursesCount", courses.size());
                debug.put("courses", courses.stream().map(c -> Map.of(
                        "id", c.getId(),
                        "title", c.getTitle() != null ? c.getTitle() : "null",
                        "organization", c.getOrganization() != null ? c.getOrganization().getId() : "null",
                        "instructor", c.getInstructor() != null ? c.getInstructor().getId() : "null",
                        "instructorOrg", c.getInstructor() != null && c.getInstructor().getOrganization() != null 
                                ? c.getInstructor().getOrganization().getId() : "null"
                )).toList());
                
                // Check enrollments
                List<CourseEnrollment> enrollments = organizationService.getOrganizationEnrollments(org.getId());
                debug.put("enrollmentsCount", enrollments.size());
            } else {
                debug.put("organization", "not found");
            }
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage(), "stackTrace", e.getStackTrace()));
        }
    }

    record CreateOrganizationRequest(String name, String description, String website, String logoUrl) {}
    record AddTeacherRequest(String email) {}
}

