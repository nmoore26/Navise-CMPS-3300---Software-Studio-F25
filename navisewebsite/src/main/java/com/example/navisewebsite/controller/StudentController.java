package com.example.navisewebsite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
public class StudentController {

    @GetMapping("/student")
    public String studentPage(Model model) {
        // Add any attributes needed for the student page
        // For example: model.addAttribute("studentName", "John Doe");
        return "student"; // Thymeleaf template named student.html
    }

    /**
     * Serve the student home/dashboard page.
     * Requires active session (student must be logged in).
     * 
     * @param session the HttpSession containing student data
     * @param model the Model to pass data to the template
     * @return the student-home template or redirect to login if not authenticated
     */
    @GetMapping("/student-home")
    public String studentHome(HttpSession session, Model model) {
        // Check if user is logged in and is a student
        Object userType = session.getAttribute("userType");
        Object email = session.getAttribute("email");
        
        if (userType == null || !"student".equals(userType)) {
            // Redirect to login if not authenticated as a student
            return "redirect:/";
        }
        
        // Pass student email to the template
        model.addAttribute("studentEmail", email);
        
        return "student-home";
    }

    /**
     * Student information page (for entering past courses, year in college, etc.)
     * 
     * @param session the HttpSession containing student data
     * @param model the Model to pass data to the template
     * @return the student info template or redirect to login if not authenticated
     */
    @GetMapping("/student/info")
    public String studentInfo(HttpSession session, Model model) {
        // Check if user is logged in and is a student
        Object userType = session.getAttribute("userType");
        
        if (userType == null || !"student".equals(userType)) {
            return "redirect:/";
        }
        
        return "student-info"; // TODO: Create student-info.html template
    }

    /**
     * Student profile page with editable fields
     * 
     * @param session the HttpSession containing student data
     * @param model the Model to pass data to the template
     * @return the student profile template or redirect to login if not authenticated
     */
    @GetMapping("/student/profile")
    public String studentProfile(HttpSession session, Model model) {
        // Check if user is logged in and is a student
        Object userType = session.getAttribute("userType");
        Object email = session.getAttribute("email");
        
        if (userType == null || !"student".equals(userType)) {
            return "redirect:/";
        }
        
        model.addAttribute("studentEmail", email);
        return "student-profile";
    }

    /**
     * Handle profile form submission
     */
    @PostMapping("/student/profile")
    public String updateProfile(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String schoolYear,
                                @RequestParam(required = false) String major,
                                @RequestParam(required = false) String minor,
                                @RequestParam(required = false) String pastCourses,
                                HttpSession session,
                                Model model) {
        // Check if user is logged in
        Object userType = session.getAttribute("userType");
        if (userType == null || !"student".equals(userType)) {
            return "redirect:/";
        }
        
        Object email = session.getAttribute("email");
        
        // Validate input
        if (firstName == null || firstName.trim().isEmpty()) {
            model.addAttribute("error", "First name is required.");
            model.addAttribute("studentEmail", email);
            return "student-profile";
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            model.addAttribute("error", "Last name is required.");
            model.addAttribute("studentEmail", email);
            return "student-profile";
        }
        
        // TODO: Save profile information to database
        model.addAttribute("message", "Profile updated successfully!");
        model.addAttribute("studentEmail", email);
        return "student-profile";
    }

    /**
     * My courses page (displays enrolled courses).
     * 
     * @param session the HttpSession containing student data
     * @param model the Model to pass data to the template
     * @return the courses template or redirect to login if not authenticated
     */
    @GetMapping("/student/courses")
    public String myCourses(HttpSession session, Model model) {
        // Check if user is logged in and is a student
        Object userType = session.getAttribute("userType");
        
        if (userType == null || !"student".equals(userType)) {
            return "redirect:/";
        }
        
        return "student-courses";
    }

    /**
     * Schedule builder page (for planning courses).
     * 
     * @param session the HttpSession containing student data
     * @param model the Model to pass data to the template
     * @return the schedule builder template or redirect to login if not authenticated
     */
    @GetMapping("/student/schedule")
    public String scheduleBuilder(HttpSession session, Model model) {
        // Check if user is logged in and is a student
        Object userType = session.getAttribute("userType");
        
        if (userType == null || !"student".equals(userType)) {
            return "redirect:/";
        }
        
        return "student-schedule"; // TODO: Create student-schedule.html template
    }
}

