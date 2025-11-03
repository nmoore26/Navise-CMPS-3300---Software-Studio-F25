package com.example.navisewebsite.controller;
import com.example.navisewebsite.domain.Course;  // Course class in your domain folder
import com.example.navisewebsite.service.AdminCourseService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;



@Controller
public class AdminController{
    private final AdminCourseService courseService;
    public AdminController(AdminCourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping("/admin/add-course")
    public String addCourse(@ModelAttribute Course course, Model model) {
        courseService.add_course(course);

        model.addAttribute("message", "Course added successfully!");
        model.addAttribute("course", new Course()); //bind empty Course object to the form
        
        return "admin";
    }
}

