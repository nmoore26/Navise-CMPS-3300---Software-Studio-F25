package com.example.navisewebsite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/home"; // Redirect to the landing page
    }

    @GetMapping("/home")
    public String landingPage() {
        return "home"; // This will look for home.html in src/main/resources/templates/
    }
}
