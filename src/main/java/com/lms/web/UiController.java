package com.lms.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/ui")
public class UiController {

    @GetMapping("")
    public String index(Model model) {
        return "lms/home";
    }
    
    @GetMapping("/")
    public String indexSlash(Model model) {
        return "lms/home";
    }

    @GetMapping("/lms")
    public String lmsHome(Model model) {
        return "lms/home";
    }

    @GetMapping("/lms/courses")
    public String lmsCourses(Model model) {
        return "lms/courses";
    }

    @GetMapping("/lms/courses/{id}")
    public String lmsCourseDetail(@PathVariable("id") Long id, Model model) {
        log.info("Loading course detail page for ID: {}", id);
        model.addAttribute("courseId", id);
        return "lms/course-detail";
    }

    @GetMapping("/lms/course/{id}")
    public String lmsCourseDetailAlt(@PathVariable("id") Long id, Model model) {
        model.addAttribute("courseId", id);
        return "lms/course-detail";
    }

    @GetMapping("/lms/student/dashboard")
    public String studentDashboard(Model model) {
        return "lms/student-dashboard";
    }

    @GetMapping("/lms/teacher/dashboard")
    public String teacherDashboard(Model model) {
        return "lms/teacher-dashboard";
    }

    @GetMapping("/lms/organization/dashboard")
    public String organizationDashboard(Model model) {
        return "lms/organization-dashboard";
    }

    @GetMapping("/lms/live/{sessionId}")
    public String liveSession(@PathVariable("sessionId") Long sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        return "lms/live-session";
    }

    @GetMapping("/auth")
    public String auth(Model model) {
        return "auth";
    }

    @GetMapping("/lms/profile/complete")
    public String profileCompletion(Model model) {
        return "lms/profile-completion";
    }

    @GetMapping("/lms/assignment/submit")
    public String assignmentSubmit(Model model) {
        return "lms/assignment-submit";
    }

    @GetMapping("/lms/quiz/take")
    public String quizTake(Model model) {
        return "lms/quiz-take";
    }

    @GetMapping("/lms/quiz/manage")
    public String quizManage(Model model) {
        return "lms/quiz-manage";
    }

    @GetMapping("/lms/course/manage")
    public String courseManage(Model model) {
        return "lms/course-manage";
    }

    @GetMapping("/lms/report-card/detail")
    public String reportCardDetail(
            @RequestParam(value = "courseId", required = false) Long courseId, 
            @RequestParam(value = "studentId", required = false) Long studentId, 
            Model model) {
        model.addAttribute("courseId", courseId);
        model.addAttribute("studentId", studentId);
        return "lms/report-card-detail";
    }

    @GetMapping("/lms/certificate/{certificateNumber}")
    public String viewCertificate(@PathVariable("certificateNumber") String certificateNumber, Model model) {
        model.addAttribute("certificateNumber", certificateNumber);
        return "lms/certificate-view";
    }

    @GetMapping("/lms/profile")
    public String profile(Model model) {
        return "lms/profile";
    }

    @GetMapping("/lms/assignment/manage")
    public String assignmentManage(@RequestParam(value = "courseId", required = false) Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "lms/assignment-manage";
    }

    @GetMapping("/lms/assignment/grade")
    public String assignmentGrade(@RequestParam(value = "assignmentId", required = false) Long assignmentId, Model model) {
        model.addAttribute("assignmentId", assignmentId);
        return "lms/assignment-grade";
    }

    @GetMapping("/lms/course/{courseId}/reviews")
    public String courseReviews(@PathVariable("courseId") Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "lms/course-reviews";
    }

    @GetMapping("/lms/course/{courseId}/announcements")
    public String courseAnnouncements(@PathVariable("courseId") Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "lms/course-announcements";
    }

    @GetMapping("/lms/course/{courseId}/forum")
    public String courseForum(@PathVariable("courseId") Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "lms/course-forum";
    }

    @GetMapping("/lms/notifications")
    public String notifications(Model model) {
        return "lms/notifications";
    }

    @GetMapping("/lms/live-sessions/manage")
    public String liveSessionsManage(@RequestParam(value = "courseId", required = false) Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "lms/live-sessions-manage";
    }

    @GetMapping("/lms/admin/analytics")
    public String adminAnalytics(Model model) {
        return "lms/admin-analytics";
    }

    @GetMapping("/lms/recommendations")
    public String recommendations(Model model) {
        return "lms/recommendations";
    }

    @GetMapping("/lms/wishlist")
    public String wishlist(Model model) {
        return "lms/wishlist";
    }

    @GetMapping("/lms/refunds")
    public String refunds(Model model) {
        return "lms/refunds";
    }

    @GetMapping("/lms/admin/coupons")
    public String adminCoupons(Model model) {
        return "lms/admin-coupons";
    }

    @GetMapping("/lms/admin/refunds")
    public String adminRefunds(Model model) {
        return "lms/admin-refunds";
    }

    @GetMapping("/lms/admin/manage")
    public String adminManage(Model model) {
        return "lms/admin-manage";
    }

    @GetMapping("/lms/messaging")
    public String messaging(Model model) {
        return "lms/messaging";
    }
}

