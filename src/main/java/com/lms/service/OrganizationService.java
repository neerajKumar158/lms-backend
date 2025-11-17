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
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        UserAccount teacher = userAccountRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        if (teacher.getOrganization() != null && teacher.getOrganization().getId().equals(organizationId)) {
            teacher.setOrganization(null);
            userAccountRepository.save(teacher);
        }
    }

    public List<UserAccount> getOrganizationTeachers(Long organizationId) {
        return userAccountRepository.findByOrganizationId(organizationId);
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
}



