package com.example.navisewebsite.controller;

import com.example.navisewebsite.repository.UserRepository;
import com.example.navisewebsite.repository.StudentInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

/**
 * Controller for student registration.
 * Allows new students to create an account with email and password.
 */
@Controller
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentInfoRepository studentInfoRepository;

    /**
     * Display the registration form
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register"; // Renders register.html
    }

    /**
     * Handle student registration
     */
    @PostMapping("/register")
    public String register(@RequestParam String firstName,
                          @RequestParam String lastName,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          @RequestParam(required = false) String major,
                          @RequestParam(required = false) String minor,
                          HttpSession session,
                          Model model) {

        // Validate input
        if (firstName == null || firstName.trim().isEmpty()) {
            model.addAttribute("error", "First name is required.");
            return "register";
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            model.addAttribute("error", "Last name is required.");
            return "register";
        }

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email is required.");
            return "register";
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Password is required.");
            return "register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "register";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "register";
        }

        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "This email is already registered. Please login or use a different email.");
            return "register";
        }

    // Register the new student
    // TODO: Hash the password with BCrypt before storing
    int userId = userRepository.addStudent(email, password, firstName, lastName);

        if (userId > 0) {
            // Persist student profile info
            String majorVal = (major == null) ? "" : major.trim();
            String minorVal = (minor == null) ? "" : minor.trim();
            studentInfoRepository.insertStudentInfo(userId, firstName, lastName, majorVal, minorVal, "");

            // Store student info in session
            session.setAttribute("userId", userId);
            session.setAttribute("email", email);
            session.setAttribute("userType", "student");
            session.setAttribute("firstName", firstName);
            session.setAttribute("lastName", lastName);
            session.setAttribute("major", majorVal);
            session.setAttribute("minor", minorVal);
            
            model.addAttribute("message", "Registration successful! Welcome to Navise.");
            return "redirect:/student-home";
        } else {
            model.addAttribute("error", "Registration failed. Please try again.");
            return "register";
        }
    }
}
