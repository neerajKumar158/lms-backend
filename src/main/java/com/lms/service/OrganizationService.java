package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    public Optional<Organization> getOrganizationByAdminId(Long adminId) {
        return organizationRepository.findByAdminId(adminId);
    }

    public Optional<Organization> getOrganizationById(Long organizationId) {
        return organizationRepository.findById(organizationId);
    }

    @Transactional
    public Organization createOrganization(Long adminId, Organization organization) {
        UserAccount admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        if (admin.getUserType() != UserAccount.UserType.ORGANIZATION) {
            throw new RuntimeException("User must be of type ORGANIZATION");
        }
        
        organization.setAdmin(admin);
        organization.setCreatedAt(LocalDateTime.now());
        organization.setIsActive(true);
        
        return organizationRepository.save(organization);
    }

    /**
     * Creates an organization for an existing user, optionally updating their user type.
     * Useful for mapping existing users to organizations.
     *
     * @param userId the user ID to create organization for
     * @param organizationName the name of the organization
     * @param updateUserType whether to update user type to ORGANIZATION if not already
     * @return the created organization
     */
    @Transactional
    public Organization createOrganizationForExistingUser(Long userId, String organizationName, boolean updateUserType) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Check if user already has an organization
        Optional<Organization> existingOrg = organizationRepository.findByAdminId(userId);
        if (existingOrg.isPresent()) {
            throw new RuntimeException("User already has an organization: " + existingOrg.get().getName());
        }
        
        // Update user type if needed
        if (updateUserType && user.getUserType() != UserAccount.UserType.ORGANIZATION) {
            user.setUserType(UserAccount.UserType.ORGANIZATION);
            userAccountRepository.save(user);
        }
        
        Organization organization = new Organization();
        organization.setName(organizationName);
        organization.setDescription("Organization for " + user.getEmail());
        organization.setAdmin(user);
        organization.setCreatedAt(LocalDateTime.now());
        organization.setIsActive(true);
        
        return organizationRepository.save(organization);
    }

    /**
     * Associates an existing organization with an existing user as admin.
     * Useful for mapping existing organizations to existing users.
     *
     * @param organizationId the organization ID
     * @param userId the user ID to set as admin
     * @param updateUserType whether to update user type to ORGANIZATION if not already
     */
    @Transactional
    public void associateOrganizationWithUser(Long organizationId, Long userId, boolean updateUserType) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));
        
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Check if organization already has an admin
        if (organization.getAdmin() != null && !organization.getAdmin().getId().equals(userId)) {
            throw new RuntimeException("Organization already has an admin: " + organization.getAdmin().getEmail());
        }
        
        // Update user type if needed
        if (updateUserType && user.getUserType() != UserAccount.UserType.ORGANIZATION) {
            user.setUserType(UserAccount.UserType.ORGANIZATION);
            userAccountRepository.save(user);
        }
        
        organization.setAdmin(user);
        organizationRepository.save(organization);
    }

    @Transactional
    public Organization updateOrganization(Long organizationId, Organization updatedOrganization) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        organization.setName(updatedOrganization.getName());
        organization.setDescription(updatedOrganization.getDescription());
        organization.setWebsite(updatedOrganization.getWebsite());
        organization.setLogoUrl(updatedOrganization.getLogoUrl());
        
        return organizationRepository.save(organization);
    }

    @Transactional
    public void addTeacherToOrganization(Long organizationId, Long teacherId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        UserAccount teacher = userAccountRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        if (teacher.getUserType() != UserAccount.UserType.TEACHER) {
            throw new RuntimeException("User must be of type TEACHER");
        }
        
        teacher.setOrganization(organization);
        userAccountRepository.save(teacher);
    }

    @Transactional
    public void removeTeacherFromOrganization(Long organizationId, Long teacherId) {
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        UserAccount teacher = userAccountRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        if (teacher.getOrganization() != null && teacher.getOrganization().getId().equals(organizationId)) {
            teacher.setOrganization(null);
            userAccountRepository.save(teacher);
        }
    }

    public List<UserAccount> getOrganizationTeachers(Long organizationId) {
        return userAccountRepository.findByOrganizationId(organizationId).stream()
                .filter(user -> user.getUserType() == UserAccount.UserType.TEACHER)
                .toList();
    }

    public List<Course> getOrganizationCourses(Long organizationId) {
        return courseRepository.findByOrganizationId(organizationId);
    }

    public List<CourseEnrollment> getOrganizationEnrollments(Long organizationId) {
        // Get all enrollments for courses belonging to this organization
        List<Course> courses = courseRepository.findByOrganizationId(organizationId);
        return courses.stream()
                .flatMap(course -> enrollmentRepository.findByCourse(course).stream())
                .toList();
    }

    public long getOrganizationStudentsCount(Long organizationId) {
        List<CourseEnrollment> enrollments = getOrganizationEnrollments(organizationId);
        return enrollments.stream()
                .map(CourseEnrollment::getStudent)
                .distinct()
                .count();
    }

    /**
     * Fixes courses that don't have organization set but their instructor belongs to the organization.
     * This is useful for courses created before teachers were added to the organization.
     *
     * @param organizationId the organization ID
     * @return number of courses fixed
     */
    @Transactional
    public int fixCoursesOrganization(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        List<UserAccount> teachers = getOrganizationTeachers(organizationId);
        int fixedCount = 0;
        
        for (UserAccount teacher : teachers) {
            List<Course> teacherCourses = courseRepository.findByInstructor(teacher);
            for (Course course : teacherCourses) {
                if (course.getOrganization() == null) {
                    course.setOrganization(organization);
                    courseRepository.save(course);
                    fixedCount++;
                }
            }
        }
        
        return fixedCount;
    }
}



