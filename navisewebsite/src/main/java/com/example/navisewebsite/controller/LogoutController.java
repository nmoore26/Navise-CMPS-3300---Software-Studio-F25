package com.example.navisewebsite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpSession;

/**
 * Controller for handling user logout functionality.
 * Invalidates the user session and redirects to the home page.
 */
@Controller
public class LogoutController {

    /**
     * Handle logout request by invalidating session and redirecting to home page.
     * 
     * @param session the HttpSession to be invalidated
     * @return redirect to the public home page
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        // Invalidate the session to remove all user data
        if (session != null) {
            session.invalidate();
        }
        
        // Redirect to the public home page
        return "redirect:/home";
    }
}
