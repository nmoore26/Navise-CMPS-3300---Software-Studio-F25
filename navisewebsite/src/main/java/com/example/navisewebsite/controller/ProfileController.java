package com.example.navisewebsite.controller;

import com.example.navisewebsite.repository.StudentInfoRepository;
import com.example.navisewebsite.repository.StudentInfoRepository.StudentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

/**
 * Controller for student profile management.
 */
@Controller
public class ProfileController {

    @Autowired
    private StudentInfoRepository studentInfoRepository;

    /**
     * Display the student profile page
     */
    @GetMapping("/student/profile")
    public String viewProfile(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        String email = (String) session.getAttribute("email");

        if (userId == null) {
            return "redirect:/";
        }

        // Fetch student info from database
        Optional<StudentInfo> studentInfoOpt = studentInfoRepository.findByUserId(userId);
        
        if (studentInfoOpt.isPresent()) {
            StudentInfo info = studentInfoOpt.get();
            model.addAttribute("firstName", info.firstName);
            model.addAttribute("lastName", info.lastName);
            model.addAttribute("email", email);
            model.addAttribute("major", info.major);
            model.addAttribute("minor", info.minor);
            model.addAttribute("schoolYear", info.schoolYear);
            model.addAttribute("pastCourses", info.pastCourses);
        } else {
            // If student info doesn't exist, show default values
            model.addAttribute("firstName", session.getAttribute("firstName"));
            model.addAttribute("lastName", session.getAttribute("lastName"));
            model.addAttribute("email", email);
            model.addAttribute("major", "");
            model.addAttribute("minor", "");
            model.addAttribute("schoolYear", "");
            model.addAttribute("pastCourses", "");
        }

        return "student-profile";
    }

    /**
     * Display the edit profile form
     */
    @GetMapping("/student/profile/edit")
    public String editProfile(HttpSession session, Model model) {
        return viewProfileForm(session, model, "student-profile-edit");
    }

    /**
     * Helper method to populate profile form
     */
    private String viewProfileForm(HttpSession session, Model model, String template) {
        Integer userId = (Integer) session.getAttribute("userId");
        String email = (String) session.getAttribute("email");

        if (userId == null) {
            return "redirect:/";
        }

        // Fetch student info from database
        Optional<StudentInfo> studentInfoOpt = studentInfoRepository.findByUserId(userId);
        
        if (studentInfoOpt.isPresent()) {
            StudentInfo info = studentInfoOpt.get();
            model.addAttribute("firstName", info.firstName);
            model.addAttribute("lastName", info.lastName);
            model.addAttribute("email", email);
            model.addAttribute("major", info.major);
            model.addAttribute("minor", info.minor);
            model.addAttribute("schoolYear", info.schoolYear);
            model.addAttribute("pastCourses", info.pastCourses);
        } else {
            // If student info doesn't exist, show default values
            model.addAttribute("firstName", session.getAttribute("firstName"));
            model.addAttribute("lastName", session.getAttribute("lastName"));
            model.addAttribute("email", email);
            model.addAttribute("major", "");
            model.addAttribute("minor", "");
            model.addAttribute("schoolYear", "");
            model.addAttribute("pastCourses", "");
        }

        return template;
    }

    /**
     * Update the student profile
     */
    @PostMapping("/student/profile/update")
    public String updateProfile(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam(required = false) String major,
                               @RequestParam(required = false) String minor,
                               @RequestParam(required = false) String schoolYear,
                               @RequestParam(required = false) String pastCourses,
                               HttpSession session,
                               Model model) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        // Validate input
        if (firstName == null || firstName.trim().isEmpty()) {
            model.addAttribute("error", "First name is required.");
            return viewProfile(session, model);
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            model.addAttribute("error", "Last name is required.");
            return viewProfile(session, model);
        }

        // Prepare values
        String majorVal = (major == null) ? "" : major.trim();
        String minorVal = (minor == null) ? "" : minor.trim();
        String schoolYearVal = (schoolYear == null) ? "" : schoolYear.trim();
        String pastCoursesVal = (pastCourses == null) ? "" : pastCourses.trim();

        // Check if student info record exists
        Optional<StudentInfo> existingInfo = studentInfoRepository.findByUserId(userId);
        
        int result;
        if (existingInfo.isPresent()) {
            // Update existing record
            result = studentInfoRepository.updateStudentInfo(userId, firstName, lastName, majorVal, minorVal, schoolYearVal, pastCoursesVal);
        } else {
            // Insert new record if it doesn't exist
            result = studentInfoRepository.insertStudentInfo(userId, firstName, lastName, majorVal, minorVal, schoolYearVal, pastCoursesVal);
        }

        if (result > 0) {
            // Update session attributes
            session.setAttribute("firstName", firstName);
            session.setAttribute("lastName", lastName);
            session.setAttribute("major", majorVal);
            session.setAttribute("minor", minorVal);
            session.setAttribute("schoolYear", schoolYearVal);
            
            model.addAttribute("success", "Profile updated successfully!");
        } else {
            model.addAttribute("error", "Failed to update profile. Please try again.");
        }

        return viewProfile(session, model);
    }
}
