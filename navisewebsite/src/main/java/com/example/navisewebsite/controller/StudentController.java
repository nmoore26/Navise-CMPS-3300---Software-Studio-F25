package com.example.navisewebsite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student")
public class StudentController {

    @GetMapping("")
    public String studentHome(Model model) {
        model.addAttribute("email", "student@example.edu");
        return "student";
    }

    @GetMapping("/schedules")
    public String viewSchedules(Model model) {
        model.addAttribute("email", "student@example.edu");
        // later: add schedules to model
        return "view-schedules";
    }

    @PostMapping("/schedules/generate")
    public String generateSchedule(Model model) {
        model.addAttribute("email", "student@example.edu");
        // later: call ProjectedSchedule here
        return "schedule-generated";
    }
}