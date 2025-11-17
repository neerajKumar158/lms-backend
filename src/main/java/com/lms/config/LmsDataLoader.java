package com.lms.config;

import com.lms.domain.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Loader - Creates sample featured courses and demo users
 * Runs on application startup
 */
@Component
public class LmsDataLoader implements CommandLineRunner {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseCategoryRepository categoryRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private StudyMaterialRepository studyMaterialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        System.out.println("Initializing default users and sample data...");

        try {
            // Create default teacher user
            UserAccount teacher = createDefaultTeacher();
            System.out.println("Default teacher created: " + teacher.getEmail() + " / Password: teacher123");
            
            // Create default student user
            UserAccount student = createDefaultStudent();
            System.out.println("Default student created: " + student.getEmail() + " / Password: student123");
            
            // Check if featured courses already exist
            long featuredCount = courseRepository.findFeaturedPublishedCourses().size();
            if (featuredCount >= 10) {
                System.out.println("Featured courses already exist (" + featuredCount + "). Skipping course creation.");
                return;
            }

            System.out.println("Creating sample courses... (Current featured courses: " + featuredCount + ")");
            
            // Create categories
            CourseCategory programming = createCategory("Programming", "Learn programming languages and frameworks");
            CourseCategory business = createCategory("Business", "Business and entrepreneurship courses");
            CourseCategory design = createCategory("Design", "UI/UX and graphic design courses");
            CourseCategory dataScience = createCategory("Data Science", "Data analysis and machine learning");
            CourseCategory marketing = createCategory("Marketing", "Digital marketing and SEO");
            System.out.println("Categories created");

            // Create featured courses
            int created = 0;
            created += createFeaturedCourse("Introduction to Java Programming", 
                "Learn Java from scratch. Perfect for beginners who want to start their programming journey.",
                BigDecimal.ZERO, Course.CourseLevel.BEGINNER, programming, teacher, true) != null ? 1 : 0;

            created += createFeaturedCourse("Web Development with Spring Boot", 
                "Master Spring Boot framework to build robust web applications. Learn REST APIs, security, and database integration.",
                new BigDecimal("999"), Course.CourseLevel.INTERMEDIATE, programming, teacher, true) != null ? 1 : 0;

            created += createFeaturedCourse("Python for Data Science", 
                "Learn Python programming for data analysis, visualization, and machine learning. Includes hands-on projects.",
                new BigDecimal("1299"), Course.CourseLevel.BEGINNER, dataScience, teacher, true) != null ? 1 : 0;

            created += createFeaturedCourse("UI/UX Design Fundamentals", 
                "Learn the principles of user interface and user experience design. Create beautiful and functional designs.",
                new BigDecimal("799"), Course.CourseLevel.BEGINNER, design, teacher, true) != null ? 1 : 0;

            created += createFeaturedCourse("Digital Marketing Mastery", 
                "Comprehensive guide to digital marketing including SEO, social media marketing, email marketing, and analytics.",
                new BigDecimal("1499"), Course.CourseLevel.INTERMEDIATE, marketing, teacher, true) != null ? 1 : 0;

            created += createFeaturedCourse("JavaScript Essentials", 
                "Master JavaScript fundamentals, ES6+, DOM manipulation, and modern JavaScript frameworks.",
                BigDecimal.ZERO, Course.CourseLevel.BEGINNER, programming, teacher, true) != null ? 1 : 0;

            created += createFeaturedCourse("Business Strategy & Planning", 
                "Learn how to develop effective business strategies, create business plans, and analyze market opportunities.",
                new BigDecimal("1999"), Course.CourseLevel.ADVANCED, business, teacher, true) != null ? 1 : 0;

            created += createFeaturedCourse("React.js Complete Guide", 
                "Build modern web applications with React. Learn hooks, context, routing, and state management.",
                new BigDecimal("1199"), Course.CourseLevel.INTERMEDIATE, programming, teacher, true) != null ? 1 : 0;

            created += createFeaturedCourse("Machine Learning Basics", 
                "Introduction to machine learning concepts, algorithms, and practical applications using Python.",
                new BigDecimal("1799"), Course.CourseLevel.INTERMEDIATE, dataScience, teacher, true) != null ? 1 : 0;

            created += createFeaturedCourse("Graphic Design for Beginners", 
                "Learn graphic design principles, tools, and techniques. Create stunning visuals for web and print.",
                new BigDecimal("899"), Course.CourseLevel.BEGINNER, design, teacher, true) != null ? 1 : 0;

            System.out.println("Sample data loaded successfully! Created " + created + " featured courses.");
        } catch (Exception e) {
            System.err.println("Error loading sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private UserAccount createDefaultTeacher() {
        if (userAccountRepository.findByEmail("teacher@lms.com").isPresent()) {
            return userAccountRepository.findByEmail("teacher@lms.com").get();
        }

        UserAccount teacher = new UserAccount();
        teacher.setEmail("teacher@lms.com");
        teacher.setPasswordHash(passwordEncoder.encode("teacher123"));
        teacher.setName("Default Teacher");
        teacher.setPhone("+1234567890");
        teacher.setUserType(UserAccount.UserType.TEACHER);
        teacher.setTeacherApproved(true); // Default teacher is pre-approved
        teacher.setEmailVerified(true);
        teacher.setProfileCompleted(true);
        teacher.setBio("Experienced teacher with expertise in multiple subjects.");
        teacher.setQualifications("M.Sc. in Computer Science, 10+ years of teaching experience");
        
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_TEACHER");
        roles.add("ROLE_USER");
        teacher.setRoles(roles);
        
        return userAccountRepository.save(teacher);
    }

    private UserAccount createDefaultStudent() {
        if (userAccountRepository.findByEmail("student@lms.com").isPresent()) {
            return userAccountRepository.findByEmail("student@lms.com").get();
        }

        UserAccount student = new UserAccount();
        student.setEmail("student@lms.com");
        student.setPasswordHash(passwordEncoder.encode("student123"));
        student.setName("Default Student");
        student.setPhone("+1234567891");
        student.setUserType(UserAccount.UserType.STUDENT);
        student.setEmailVerified(true);
        student.setProfileCompleted(true);
        student.setBio("Enthusiastic learner eager to expand knowledge.");
        
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_STUDENT");
        roles.add("ROLE_USER");
        student.setRoles(roles);
        
        return userAccountRepository.save(student);
    }

    private CourseCategory createCategory(String name, String description) {
        CourseCategory category = new CourseCategory();
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    private Course createFeaturedCourse(String title, String description, BigDecimal price, 
                                       Course.CourseLevel level, CourseCategory category,
                                       UserAccount instructor, boolean featured) {
        Course course = new Course();
        course.setTitle(title);
        course.setDescription(description);
        course.setPrice(price);
        course.setLevel(level);
        course.setCategory(category);
        course.setInstructor(instructor);
        course.setStatus(Course.CourseStatus.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
        course.setDurationHours(calculateDurationHours(level));
        course.setLanguage("English");
        course.setFeatured(true); // Mark as featured
        
        Course savedCourse = courseRepository.save(course);
        
        // Add sample lectures and materials for all featured courses (free or paid)
        addSampleContent(savedCourse);
        
        return savedCourse;
    }

    private void addSampleContent(Course course) {
        try {
            // Add sample lecture 1
            Lecture lecture1 = new Lecture();
            lecture1.setCourse(course);
            lecture1.setTitle("Introduction to " + course.getTitle());
            lecture1.setDescription("Get started with this course. Learn the basics and understand the fundamentals.");
            lecture1.setSequenceOrder(1);
            lecture1.setDurationMinutes(30);
            lecture1.setIsFree(true);
            lecture1 = lectureRepository.save(lecture1);

            // Add sample material for lecture 1 (without actual file - just for demo)
            StudyMaterial material1 = new StudyMaterial();
            material1.setLecture(lecture1);
            material1.setTitle("Course Overview PDF");
            material1.setDescription("Download the course overview and syllabus. (Sample material - file not available)");
            material1.setMaterialType("PDF");
            material1.setFileUrl(null); // No actual file - just for demo
            material1.setIsFree(true);
            studyMaterialRepository.save(material1);

            // Add sample lecture 2
            Lecture lecture2 = new Lecture();
            lecture2.setCourse(course);
            lecture2.setTitle("Getting Started");
            lecture2.setDescription("Learn how to set up your development environment and tools.");
            lecture2.setSequenceOrder(2);
            lecture2.setDurationMinutes(45);
            lecture2.setIsFree(true);
            lectureRepository.save(lecture2);
        } catch (Exception e) {
            System.err.println("Error adding sample content for course " + course.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Integer calculateDurationHours(Course.CourseLevel level) {
        return switch (level) {
            case BEGINNER -> 10;
            case INTERMEDIATE -> 20;
            case ADVANCED -> 30;
            default -> 15;
        };
    }
}

