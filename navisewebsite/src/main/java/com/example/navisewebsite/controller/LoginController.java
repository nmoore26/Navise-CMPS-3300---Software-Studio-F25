package com.example.navisewebsite.controller;

import com.example.navisewebsite.domain.Account;
import com.example.navisewebsite.domain.AccountFactory;
import com.example.navisewebsite.domain.User;
import com.example.navisewebsite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import java.net.URLEncoder;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/index")
    public String showLoginForm() {
        return "index";
    }

    @PostMapping("/do-login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        try {
            // Check if admin (hardcoded authentication for admins)
            if (email.endsWith("@admin.com") && password.equals("admin")) {
                // Store admin info in session
                session.setAttribute("userId", -1); // Admin marker
                session.setAttribute("email", email);
                session.setAttribute("userType", "admin");
                
                // Use legacy Account system for admins
                Account acct = AccountFactory.forEmail(email);
                model.addAttribute("email", acct.getEmail());
                return "redirect:/admin-home";
            }
            
            // Check database for student users
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Verify password (for now plain text, TODO: implement BCrypt hashing)
                if (user.getPassword().equals(password) && user.isStudent()) {
                    // Store student info in session
                    session.setAttribute("userId", user.getUserId());
                    session.setAttribute("email", email);
                    session.setAttribute("userType", "student");
                    
                    model.addAttribute("email", email);
                    model.addAttribute("userId", user.getUserId());
                    
                    return "redirect:/student-home";
                } else {
                    String errorMsg = "Invalid password.";
                    try {
                        errorMsg = java.net.URLEncoder.encode(errorMsg, "UTF-8");
                    } catch (java.io.UnsupportedEncodingException ignored) {}
                    return "redirect:/index?error=" + errorMsg;
                }
            } else {
                String errorMsg = "User not found. Please register or check your email.";
                try {
                    errorMsg = java.net.URLEncoder.encode(errorMsg, "UTF-8");
                } catch (java.io.UnsupportedEncodingException ignored) {}
                return "redirect:/index?error=" + errorMsg;
            }

        } catch (Exception e) {
            // Show error on the login page if something goes wrong
            String errorMsg = e.getMessage();
            if (errorMsg == null) errorMsg = "An error occurred during login.";
            try {
                errorMsg = java.net.URLEncoder.encode(errorMsg, "UTF-8");
            } catch (java.io.UnsupportedEncodingException ignored) {}
            return "redirect:/index?error=" + errorMsg;
        }
    }
}
