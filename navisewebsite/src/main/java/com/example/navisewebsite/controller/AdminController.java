package com.example.navisewebsite.controller;

import com.example.navisewebsite.domain.Course;
import com.example.navisewebsite.service.AdminCourseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for handling admin actions like adding courses
 */
@Controller
public class AdminController {

    private final AdminCourseService courseService;

    public AdminController(AdminCourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Handles adding a course to a program (major or minor)
     *
     * @param course       The course to add
     * @param programName  Name of the program (sheetName)
     * @param programType  Either "Major" or "Minor"
     * @param model        Spring Model for passing data to the view
     * @return admin.html page
     */
    @PostMapping("/admin/add-course")
    public String addCourse(
            @ModelAttribute Course course,
            @RequestParam String programName,
            @RequestParam String programType,
            Model model) {

        // Validate programType
        if (!programType.equalsIgnoreCase("Major") && !programType.equalsIgnoreCase("Minor")) {
            model.addAttribute("error", "Program type must be either 'Major' or 'Minor'");
            return "admin";
        }

        // Call AdminCourseService to add course and link it
        courseService.add_course(course, programName, programType);

        model.addAttribute("message", "Course added successfully!");
        model.addAttribute("course", new Course()); // reset form object

        return "admin";
    }
}


