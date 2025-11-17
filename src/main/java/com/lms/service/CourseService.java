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

    @Transactional(readOnly = true)
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
            System.err.println("Error loading all published courses: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
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
            System.err.println("Error loading free courses: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
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
            System.err.println("Error loading featured courses: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
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
            System.err.println("Error searching courses: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
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
            System.err.println("Error loading course " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<Course> getCoursesByInstructor(Long instructorId) {
        return userAccountRepository.findById(instructorId)
                .map(courseRepository::findByInstructor)
                .orElse(List.of());
    }

    public List<Course> getCoursesByCategory(Long categoryId) {
        return courseRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public Course createCourse(Course course, Long instructorId) {
        UserAccount instructor = userAccountRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        
        course.setInstructor(instructor);
        course.setStatus(Course.CourseStatus.DRAFT);
        course.setCreatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long courseId, Course updatedCourse) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.setTitle(updatedCourse.getTitle());
        course.setDescription(updatedCourse.getDescription());
        course.setPrice(updatedCourse.getPrice());
        course.setCategory(updatedCourse.getCategory());
        course.setLevel(updatedCourse.getLevel());
        course.setThumbnailUrl(updatedCourse.getThumbnailUrl());
        
        return courseRepository.save(course);
    }

    @Transactional
    public void publishCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.setStatus(Course.CourseStatus.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    @Transactional
    public void unpublishCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.setStatus(Course.CourseStatus.DRAFT);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Delete all related entities (cascade should handle most, but explicit deletion is safer)
        courseRepository.delete(course);
    }

    @Transactional
    public void deleteLecture(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));
        
        lectureRepository.delete(lecture);
    }

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

    @Transactional
    public StudyMaterial addMaterialToLecture(Long lectureId, StudyMaterial material) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));
        
        material.setLecture(lecture);
        material.setUploadedAt(LocalDateTime.now());
        return studyMaterialRepository.save(material);
    }

    public List<Lecture> getCourseLectures(Long courseId) {
        return lectureRepository.findByCourseIdOrderBySequenceOrderAsc(courseId);
    }

    public List<StudyMaterial> getLectureMaterials(Long lectureId) {
        return studyMaterialRepository.findByLectureId(lectureId);
    }

    public List<CourseCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public CourseCategory createCategory(CourseCategory category) {
        return categoryRepository.save(category);
    }
}

