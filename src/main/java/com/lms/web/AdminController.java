package com.lms.web;

import com.lms.domain.*;
import com.lms.repository.*;
import com.lms.service.CourseService;
import com.lms.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Admin Controller
 * Handles admin operations for managing all system resources
 */
@RestController
@RequestMapping("/api/lms/admin")
public class AdminController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationService organizationService;

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

    // ==================== COURSE MANAGEMENT ====================

    /**
     * Get all courses (admin only)
     */
    @GetMapping("/courses")
    public ResponseEntity<?> getAllCourses(@AuthenticationPrincipal User principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            List<Course> courses = courseRepository.findAll();
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update any course (admin only)
     */
    @PutMapping("/courses/{courseId}")
    public ResponseEntity<?> updateCourse(
            @AuthenticationPrincipal User principal,
            @PathVariable Long courseId,
            @RequestBody Course courseData) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // Update course fields
            if (courseData.getTitle() != null) course.setTitle(courseData.getTitle());
            if (courseData.getDescription() != null) course.setDescription(courseData.getDescription());
            if (courseData.getPrice() != null) course.setPrice(courseData.getPrice());
            if (courseData.getStatus() != null) course.setStatus(courseData.getStatus());
            if (courseData.getLevel() != null) course.setLevel(courseData.getLevel());
            if (courseData.getThumbnailUrl() != null) course.setThumbnailUrl(courseData.getThumbnailUrl());
            if (courseData.getFeatured() != null) course.setFeatured(courseData.getFeatured());

            Course updated = courseRepository.save(course);
            return ResponseEntity.ok(Map.of("message", "Course updated successfully", "course", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete any course (admin only)
     */
    @DeleteMapping("/courses/{courseId}")
    @Transactional
    public ResponseEntity<?> deleteCourse(
            @AuthenticationPrincipal User principal,
            @PathVariable Long courseId) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            courseRepository.delete(course);
            return ResponseEntity.ok(Map.of("message", "Course deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Publish/Unpublish any course (admin only)
     */
    @PostMapping("/courses/{courseId}/publish")
    public ResponseEntity<?> publishCourse(
            @AuthenticationPrincipal User principal,
            @PathVariable Long courseId) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            course.setStatus(Course.CourseStatus.PUBLISHED);
            Course updated = courseRepository.save(course);
            return ResponseEntity.ok(Map.of("message", "Course published successfully", "course", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/courses/{courseId}/unpublish")
    public ResponseEntity<?> unpublishCourse(
            @AuthenticationPrincipal User principal,
            @PathVariable Long courseId) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            course.setStatus(Course.CourseStatus.DRAFT);
            Course updated = courseRepository.save(course);
            return ResponseEntity.ok(Map.of("message", "Course unpublished successfully", "course", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== STUDENT MANAGEMENT ====================

    /**
     * Get all users (admin only)
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal User principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        List<Map<String, Object>> users = userAccountRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", u.getId());
                    userMap.put("email", u.getEmail());
                    userMap.put("name", u.getName() != null ? u.getName() : "");
                    userMap.put("phone", u.getPhone() != null ? u.getPhone() : "");
                    userMap.put("userType", u.getUserType() != null ? u.getUserType().toString() : "null");
                    userMap.put("emailVerified", u.getEmailVerified());
                    userMap.put("profileCompleted", u.getProfileCompleted());
                    userMap.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
                    // Get organization info
                    if (u.getOrganization() != null) {
                        userMap.put("organizationId", u.getOrganization().getId());
                        userMap.put("organizationName", u.getOrganization().getName());
                    } else {
                        userMap.put("organizationId", null);
                        userMap.put("organizationName", null);
                    }
                    return userMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("users", users));
    }

    /**
     * Get all students (admin only)
     */
    @GetMapping("/students")
    public ResponseEntity<?> getAllStudents(@AuthenticationPrincipal User principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        List<Map<String, Object>> students = userAccountRepository.findAll().stream()
                .filter(u -> u.getUserType() == UserAccount.UserType.STUDENT)
                .map(u -> {
                    Map<String, Object> studentMap = new HashMap<>();
                    studentMap.put("id", u.getId());
                    studentMap.put("email", u.getEmail());
                    studentMap.put("name", u.getName() != null ? u.getName() : "");
                    studentMap.put("phone", u.getPhone() != null ? u.getPhone() : "");
                    studentMap.put("emailVerified", u.getEmailVerified());
                    studentMap.put("profileCompleted", u.getProfileCompleted());
                    studentMap.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
                    // Get enrollment count
                    long enrollmentCount = enrollmentRepository.findByStudent(u).size();
                    studentMap.put("enrollmentCount", enrollmentCount);
                    return studentMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("students", students));
    }

    /**
     * Get student details with enrollments (admin only)
     */
    @GetMapping("/students/{studentId}")
    public ResponseEntity<?> getStudentDetails(
            @AuthenticationPrincipal User principal,
            @PathVariable Long studentId) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        UserAccount student = userAccountRepository.findById(studentId)
                .orElse(null);

        if (student == null || student.getUserType() != UserAccount.UserType.STUDENT) {
            return ResponseEntity.badRequest().body(Map.of("error", "Student not found"));
        }

        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent(student);

        Map<String, Object> studentDetails = new HashMap<>();
        studentDetails.put("id", student.getId());
        studentDetails.put("email", student.getEmail());
        studentDetails.put("name", student.getName());
        studentDetails.put("phone", student.getPhone());
        studentDetails.put("bio", student.getBio());
        studentDetails.put("emailVerified", student.getEmailVerified());
        studentDetails.put("profileCompleted", student.getProfileCompleted());
        studentDetails.put("createdAt", student.getCreatedAt());
        studentDetails.put("enrollments", enrollments.stream().map(e -> {
            Map<String, Object> enrollmentMap = new HashMap<>();
            enrollmentMap.put("courseId", e.getCourse().getId());
            enrollmentMap.put("courseTitle", e.getCourse().getTitle());
            enrollmentMap.put("enrolledAt", e.getEnrolledAt());
            enrollmentMap.put("status", e.getStatus() != null ? e.getStatus() : "ACTIVE");
            enrollmentMap.put("progressPercentage", e.getProgressPercentage() != null ? e.getProgressPercentage() : 0);
            return enrollmentMap;
        }).collect(Collectors.toList()));

        return ResponseEntity.ok(studentDetails);
    }

    /**
     * Update student (admin only)
     */
    @PutMapping("/students/{studentId}")
    public ResponseEntity<?> updateStudent(
            @AuthenticationPrincipal User principal,
            @PathVariable Long studentId,
            @RequestBody UpdateUserRequest request) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        UserAccount student = userAccountRepository.findById(studentId)
                .orElse(null);

        if (student == null || student.getUserType() != UserAccount.UserType.STUDENT) {
            return ResponseEntity.badRequest().body(Map.of("error", "Student not found"));
        }

        if (request.name() != null) student.setName(request.name());
        if (request.phone() != null) student.setPhone(request.phone());
        if (request.bio() != null) student.setBio(request.bio());

        UserAccount updated = userAccountRepository.save(student);
        return ResponseEntity.ok(Map.of("message", "Student updated successfully", "student", updated));
    }

    /**
     * Delete student (admin only)
     */
    @DeleteMapping("/students/{studentId}")
    @Transactional
    public ResponseEntity<?> deleteStudent(
            @AuthenticationPrincipal User principal,
            @PathVariable Long studentId) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        UserAccount student = userAccountRepository.findById(studentId)
                .orElse(null);

        if (student == null || student.getUserType() != UserAccount.UserType.STUDENT) {
            return ResponseEntity.badRequest().body(Map.of("error", "Student not found"));
        }

        userAccountRepository.delete(student);
        return ResponseEntity.ok(Map.of("message", "Student deleted successfully"));
    }

    // ==================== TEACHER MANAGEMENT (Enhanced) ====================

    /**
     * Get all teachers (admin only)
     */
    @GetMapping("/teachers")
    public ResponseEntity<?> getAllTeachers(@AuthenticationPrincipal User principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        List<Map<String, Object>> teachers = userAccountRepository.findAll().stream()
                .filter(u -> u.getUserType() == UserAccount.UserType.TEACHER)
                .map(u -> {
                    Map<String, Object> teacherMap = new HashMap<>();
                    teacherMap.put("id", u.getId());
                    teacherMap.put("email", u.getEmail());
                    teacherMap.put("name", u.getName() != null ? u.getName() : "");
                    teacherMap.put("phone", u.getPhone() != null ? u.getPhone() : "");
                    teacherMap.put("qualifications", u.getQualifications() != null ? u.getQualifications() : "");
                    teacherMap.put("teacherApproved", u.getTeacherApproved());
                    teacherMap.put("emailVerified", u.getEmailVerified());
                    teacherMap.put("profileCompleted", u.getProfileCompleted());
                    teacherMap.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
                    // Get course count
                    long courseCount = courseRepository.findByInstructor(u).size();
                    teacherMap.put("courseCount", courseCount);
                    return teacherMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("teachers", teachers));
    }

    /**
     * Get teacher details with courses (admin only)
     */
    @GetMapping("/teachers/{teacherId}")
    public ResponseEntity<?> getTeacherDetails(
            @AuthenticationPrincipal User principal,
            @PathVariable Long teacherId) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        UserAccount teacher = userAccountRepository.findById(teacherId)
                .orElse(null);

        if (teacher == null || teacher.getUserType() != UserAccount.UserType.TEACHER) {
            return ResponseEntity.badRequest().body(Map.of("error", "Teacher not found"));
        }

        List<Course> courses = courseRepository.findByInstructor(teacher);

        Map<String, Object> teacherDetails = new HashMap<>();
        teacherDetails.put("id", teacher.getId());
        teacherDetails.put("email", teacher.getEmail());
        teacherDetails.put("name", teacher.getName());
        teacherDetails.put("phone", teacher.getPhone());
        teacherDetails.put("qualifications", teacher.getQualifications());
        teacherDetails.put("bio", teacher.getBio());
        teacherDetails.put("teacherApproved", teacher.getTeacherApproved());
        teacherDetails.put("emailVerified", teacher.getEmailVerified());
        teacherDetails.put("createdAt", teacher.getCreatedAt());
        teacherDetails.put("courses", courses);

        return ResponseEntity.ok(teacherDetails);
    }

    /**
     * Update teacher (admin only)
     */
    @PutMapping("/teachers/{teacherId}")
    public ResponseEntity<?> updateTeacher(
            @AuthenticationPrincipal User principal,
            @PathVariable Long teacherId,
            @RequestBody UpdateUserRequest request) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        UserAccount teacher = userAccountRepository.findById(teacherId)
                .orElse(null);

        if (teacher == null || teacher.getUserType() != UserAccount.UserType.TEACHER) {
            return ResponseEntity.badRequest().body(Map.of("error", "Teacher not found"));
        }

        if (request.name() != null) teacher.setName(request.name());
        if (request.phone() != null) teacher.setPhone(request.phone());
        if (request.qualifications() != null) teacher.setQualifications(request.qualifications());
        if (request.bio() != null) teacher.setBio(request.bio());

        UserAccount updated = userAccountRepository.save(teacher);
        return ResponseEntity.ok(Map.of("message", "Teacher updated successfully", "teacher", updated));
    }

    /**
     * Delete teacher (admin only)
     */
    @DeleteMapping("/teachers/{teacherId}")
    @Transactional
    public ResponseEntity<?> deleteTeacher(
            @AuthenticationPrincipal User principal,
            @PathVariable Long teacherId) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        UserAccount teacher = userAccountRepository.findById(teacherId)
                .orElse(null);

        if (teacher == null || teacher.getUserType() != UserAccount.UserType.TEACHER) {
            return ResponseEntity.badRequest().body(Map.of("error", "Teacher not found"));
        }

        userAccountRepository.delete(teacher);
        return ResponseEntity.ok(Map.of("message", "Teacher deleted successfully"));
    }

    // ==================== ORGANIZATION MANAGEMENT ====================

    /**
     * Get all organizations (admin only)
     */
    @GetMapping("/organizations")
    public ResponseEntity<?> getAllOrganizations(@AuthenticationPrincipal User principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            List<Organization> organizations = organizationRepository.findAll();
            List<Map<String, Object>> orgList = organizations.stream().map(org -> {
                Map<String, Object> orgMap = new HashMap<>();
                orgMap.put("id", org.getId());
                orgMap.put("name", org.getName());
                orgMap.put("description", org.getDescription());
                orgMap.put("website", org.getWebsite());
                orgMap.put("logoUrl", org.getLogoUrl());
                orgMap.put("isActive", org.getIsActive());
                orgMap.put("createdAt", org.getCreatedAt());
                // Get admin info
                if (org.getAdmin() != null) {
                    Map<String, Object> adminMap = new HashMap<>();
                    adminMap.put("id", org.getAdmin().getId());
                    adminMap.put("email", org.getAdmin().getEmail());
                    adminMap.put("name", org.getAdmin().getName());
                    orgMap.put("admin", adminMap);
                } else {
                    orgMap.put("admin", null);
                }
                // Get counts
                List<UserAccount> teachers = userAccountRepository.findByOrganizationId(org.getId());
                List<Course> courses = courseRepository.findByOrganizationId(org.getId());
                orgMap.put("teacherCount", teachers.size());
                orgMap.put("courseCount", courses.size());
                return orgMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("organizations", orgList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get organization details (admin only)
     */
    @GetMapping("/organizations/{orgId}")
    public ResponseEntity<?> getOrganizationDetails(
            @AuthenticationPrincipal User principal,
            @PathVariable Long orgId) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElse(null);

        if (org == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Organization not found"));
        }

        List<UserAccount> teachers = organizationService.getOrganizationTeachers(orgId);
        List<Course> courses = organizationService.getOrganizationCourses(orgId);

        Map<String, Object> orgDetails = new HashMap<>();
        orgDetails.put("id", org.getId());
        orgDetails.put("name", org.getName());
        orgDetails.put("description", org.getDescription());
        orgDetails.put("website", org.getWebsite());
        orgDetails.put("logoUrl", org.getLogoUrl());
        orgDetails.put("isActive", org.getIsActive());
        orgDetails.put("createdAt", org.getCreatedAt());
        orgDetails.put("teachers", teachers);
        orgDetails.put("courses", courses);

        return ResponseEntity.ok(orgDetails);
    }

    /**
     * Update organization (admin only)
     */
    @PutMapping("/organizations/{orgId}")
    public ResponseEntity<?> updateOrganization(
            @AuthenticationPrincipal User principal,
            @PathVariable Long orgId,
            @RequestBody UpdateOrganizationRequest request) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            Organization org = new Organization();
            org.setName(request.name());
            org.setDescription(request.description());
            org.setWebsite(request.website());
            org.setLogoUrl(request.logoUrl());
            org.setIsActive(request.isActive() != null ? request.isActive() : true);

            Organization updated = organizationService.updateOrganization(orgId, org);
            return ResponseEntity.ok(Map.of("message", "Organization updated successfully", "organization", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete organization (admin only)
     */
    @DeleteMapping("/organizations/{orgId}")
    @Transactional
    public ResponseEntity<?> deleteOrganization(
            @AuthenticationPrincipal User principal,
            @PathVariable Long orgId) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            Organization org = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));

            organizationRepository.delete(org);
            return ResponseEntity.ok(Map.of("message", "Organization deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create organization for existing user (admin only)
     * Useful for mapping existing users to organizations
     */
    @PostMapping("/organizations/create-for-user/{userId}")
    @Transactional
    public ResponseEntity<?> createOrganizationForUser(
            @AuthenticationPrincipal User principal,
            @PathVariable Long userId,
            @RequestBody MapOrganizationRequest request) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            Organization org = organizationService.createOrganizationForExistingUser(
                    userId,
                    request.name(),
                    request.updateUserType() != null ? request.updateUserType() : true
            );
            return ResponseEntity.ok(Map.of(
                    "message", "Organization created and mapped to user successfully",
                    "organizationId", org.getId(),
                    "organizationName", org.getName(),
                    "userId", userId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Associate existing organization with existing user (admin only)
     * Useful for mapping existing organizations to existing users
     */
    @PostMapping("/organizations/{orgId}/map-to-user/{userId}")
    @Transactional
    public ResponseEntity<?> mapOrganizationToUser(
            @AuthenticationPrincipal User principal,
            @PathVariable Long orgId,
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, Boolean> request) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }

        try {
            boolean updateUserType = request != null && request.containsKey("updateUserType") 
                    ? request.get("updateUserType") 
                    : true;
            
            organizationService.associateOrganizationWithUser(orgId, userId, updateUserType);
            return ResponseEntity.ok(Map.of(
                    "message", "Organization mapped to user successfully",
                    "organizationId", orgId,
                    "userId", userId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== REQUEST/RESPONSE RECORDS ====================

    record CreateTeacherRequest(
            String email,
            String password,
            String name,
            String phone,
            String qualifications,
            String bio
    ) {}

    record UpdateUserRequest(
            String name,
            String phone,
            String qualifications,
            String bio
    ) {}

    record UpdateOrganizationRequest(
            String name,
            String description,
            String website,
            String logoUrl,
            Boolean isActive
    ) {}

    record MapOrganizationRequest(
            String name,
            Boolean updateUserType
    ) {}
}

