package com.lms.service;

import com.lms.domain.*;
import com.lms.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Handles course management and related operations. This service manages course
 * creation, updates, publishing workflows, lecture management, and study material
 * organization, providing comprehensive course administration capabilities.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Slf4j
@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseCategoryRepository categoryRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private StudyMaterialRepository studyMaterialRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    /**
     * Retrieves all published courses with initialized relationships.
     *
     * @return the list of all published courses
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "courses", key = "'all-published'")
    public List<Course> getAllPublishedCourses() {
        try {
            List<Course> courses = courseRepository.findByStatus(Course.CourseStatus.PUBLISHED);
            // Initialize only necessary relationships - don't touch collections
            for (Course course : courses) {
                if (course.getInstructor() != null) {
                    course.getInstructor().getName(); // Trigger lazy load
                    course.getInstructor().getEmail(); // Ensure loaded
                }
                if (course.getCategory() != null) {
                    course.getCategory().getName(); // Trigger lazy load
                }
                // DO NOT access collections (lectures, liveSessions, etc.) - they're @JsonIgnore
            }
            return courses;
        } catch (Exception e) {
            log.error("Error loading all published courses: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves all free published courses with initialized relationships.
     *
     * @return the list of free published courses
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "courses", key = "'free'")
    public List<Course> getFreeCourses() {
        try {
            List<Course> courses = courseRepository.findFreePublishedCourses();
            // Initialize only necessary relationships - don't touch collections
            for (Course course : courses) {
                if (course.getInstructor() != null) {
                    course.getInstructor().getName();
                    course.getInstructor().getEmail();
                }
                if (course.getCategory() != null) {
                    course.getCategory().getName();
                }
                // DO NOT access collections
            }
            return courses;
        } catch (Exception e) {
            log.error("Error loading free courses: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves featured published courses, with fallback to regular published courses.
     *
     * @return the list of featured published courses
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "popularCourses", key = "'featured'")
    public List<Course> getFeaturedCourses() {
        try {
            List<Course> courses = courseRepository.findFeaturedPublishedCourses();
            // Ensure we return at least some courses if featured is empty but we have published courses
            if (courses.isEmpty()) {
                courses = courseRepository.findByStatus(Course.CourseStatus.PUBLISHED).stream()
                        .limit(10)
                        .toList();
            }
            // Initialize only necessary relationships - don't touch collections
            for (Course course : courses) {
                if (course.getInstructor() != null) {
                    course.getInstructor().getName();
                    course.getInstructor().getEmail();
                }
                if (course.getCategory() != null) {
                    course.getCategory().getName();
                }
                // DO NOT access collections
            }
            return courses;
        } catch (Exception e) {
            log.error("Error loading featured courses: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Searches for published courses matching the keyword in title or description.
     *
     * @param keyword the search keyword
     * @return the list of published courses matching the keyword
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "searchResults", key = "#keyword.toLowerCase()")
    public List<Course> searchCourses(String keyword) {
        try {
            List<Course> courses = courseRepository.searchPublishedCourses(keyword);
            // Initialize only necessary relationships - don't touch collections
            for (Course course : courses) {
                if (course.getInstructor() != null) {
                    course.getInstructor().getName();
                    course.getInstructor().getEmail();
                }
                if (course.getCategory() != null) {
                    course.getCategory().getName();
                }
                // DO NOT access collections
            }
            return courses;
        } catch (Exception e) {
            log.error("Error searching courses with keyword '{}': {}", keyword, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves a course by its ID with initialized relationships.
     *
     * @param id the course ID
     * @return the Optional containing the course if found, empty otherwise
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "courseDetails", key = "#id")
    public Optional<Course> getCourseById(Long id) {
        try {
            Optional<Course> courseOpt = courseRepository.findById(id);
            if (courseOpt.isPresent()) {
                Course course = courseOpt.get();
                // Trigger lazy loading of relationships within transaction
                if (course.getInstructor() != null) {
                    course.getInstructor().getName(); // Trigger lazy load
                    course.getInstructor().getEmail(); // Ensure it's loaded
                }
                if (course.getCategory() != null) {
                    course.getCategory().getName(); // Trigger lazy load
                }
                // DO NOT initialize collections here - they're @JsonIgnore and loaded via separate endpoints
            }
            return courseOpt;
        } catch (Exception e) {
            log.error("Error loading course {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves all courses created by a specific instructor.
     *
     * @param instructorId the instructor user ID
     * @return the list of courses created by the instructor
     */
    public List<Course> getCoursesByInstructor(Long instructorId) {
        return userAccountRepository.findById(instructorId)
                .map(courseRepository::findByInstructor)
                .orElse(List.of());
    }

    /**
     * Retrieves all courses in a specific category.
     *
     * @param categoryId the category ID
     * @return the list of courses in the category
     */
    public List<Course> getCoursesByCategory(Long categoryId) {
        return courseRepository.findByCategoryId(categoryId);
    }

    /**
     * Creates a new course and associates it with an instructor in DRAFT status.
     *
     * @param course the course entity to create
     * @param instructorId the instructor user ID
     * @return the created course entity
     */
    @Transactional
    @CacheEvict(value = {"courses", "popularCourses", "searchResults", "courseDetails"}, allEntries = true)
    public Course createCourse(Course course, Long instructorId) {
        UserAccount instructor = userAccountRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        
        course.setInstructor(instructor);
        // If instructor belongs to an organization, associate course with that organization
        if (instructor.getOrganization() != null) {
            course.setOrganization(instructor.getOrganization());
        }
        course.setStatus(Course.CourseStatus.DRAFT);
        course.setCreatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    /**
     * Updates an existing course with new information.
     *
     * @param courseId the ID of the course to update
     * @param updatedCourse the course entity containing updated information
     * @return the updated course entity
     */
    @Transactional
    @CacheEvict(value = {"courses", "popularCourses", "searchResults", "courseDetails"}, allEntries = true)
    public Course updateCourse(Long courseId, Course updatedCourse) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.setTitle(updatedCourse.getTitle());
        course.setDescription(updatedCourse.getDescription());
        course.setPrice(updatedCourse.getPrice());
        course.setCategory(updatedCourse.getCategory());
        course.setLevel(updatedCourse.getLevel());
        course.setThumbnailUrl(updatedCourse.getThumbnailUrl());
        
        // Update organization from instructor if not set
        if (course.getOrganization() == null && course.getInstructor() != null 
                && course.getInstructor().getOrganization() != null) {
            course.setOrganization(course.getInstructor().getOrganization());
        }
        
        return courseRepository.save(course);
    }

    /**
     * Publishes a course, changing its status from DRAFT to PUBLISHED.
     *
     * @param courseId the ID of the course to publish
     */
    @Transactional
    public void publishCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.setStatus(Course.CourseStatus.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    /**
     * Unpublishes a course, changing its status from PUBLISHED to DRAFT.
     *
     * @param courseId the ID of the course to unpublish
     */
    @Transactional
    public void unpublishCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.setStatus(Course.CourseStatus.DRAFT);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    /**
     * Deletes a course and all its related entities.
     *
     * @param courseId the ID of the course to delete
     */
    @Transactional
    @CacheEvict(value = {"courses", "popularCourses", "searchResults", "courseDetails"}, allEntries = true)
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Delete all related entities (cascade should handle most, but explicit deletion is safer)
        courseRepository.delete(course);
    }

    /**
     * Deletes a lecture from a course.
     *
     * @param lectureId the ID of the lecture to delete
     */
    @Transactional
    public void deleteLecture(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));
        
        lectureRepository.delete(lecture);
    }

    /**
     * Adds a new lecture to a course with automatic sequence ordering.
     *
     * @param courseId the ID of the course
     * @param lecture the lecture entity to add
     * @return the created lecture entity
     */
    @Transactional
    public Lecture addLectureToCourse(Long courseId, Lecture lecture) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        lecture.setCourse(course);
        if (lecture.getSequenceOrder() == null) {
            List<Lecture> existingLectures = lectureRepository.findByCourseIdOrderBySequenceOrderAsc(courseId);
            lecture.setSequenceOrder(existingLectures.size() + 1);
        }
        return lectureRepository.save(lecture);
    }

    /**
     * Adds a study material to a lecture with automatic timestamp.
     *
     * @param lectureId the ID of the lecture
     * @param material the study material entity to add
     * @return the created study material entity
     */
    @Transactional
    public StudyMaterial addMaterialToLecture(Long lectureId, StudyMaterial material) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));
        
        material.setLecture(lecture);
        material.setUploadedAt(LocalDateTime.now());
        return studyMaterialRepository.save(material);
    }

    /**
     * Retrieves all lectures for a course, ordered by sequence.
     *
     * @param courseId the ID of the course
     * @return the list of lectures ordered by sequence
     */
    public List<Lecture> getCourseLectures(Long courseId) {
        return lectureRepository.findByCourseIdOrderBySequenceOrderAsc(courseId);
    }

    /**
     * Retrieves all study materials for a lecture.
     *
     * @param lectureId the ID of the lecture
     * @return the list of study materials
     */
    public List<StudyMaterial> getLectureMaterials(Long lectureId) {
        return studyMaterialRepository.findByLectureId(lectureId);
    }

    /**
     * Retrieves all course categories.
     *
     * @return the list of all course categories
     */
    public List<CourseCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Creates a new course category.
     *
     * @param category the category entity to create
     * @return the created category entity
     */
    public CourseCategory createCategory(CourseCategory category) {
        return categoryRepository.save(category);
    }
}

