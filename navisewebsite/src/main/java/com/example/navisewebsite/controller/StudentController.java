package com.example.navisewebsite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentController {

    @GetMapping("/student")
    public String studentPage(Model model) {
        // Add any attributes needed for the student page
        // For example: model.addAttribute("studentName", "John Doe");
        return "student"; // Thymeleaf template named student.html
    }
}

