package com.example.navisewebsite.controller;

import com.example.navisewebsite.domain.Account;
import com.example.navisewebsite.domain.AccountFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @PostMapping("/do-login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model) {

        try {
            // Get the account for the given email
            Account acct = AccountFactory.forEmail(email);

            // Determine the login flow ("admin" or "student")
            String view = acct.loginFlow(password);

            // Add email to model for greeting or display purposes
            model.addAttribute("email", acct.getEmail());

            if (view.equals("admin")) {
                // Redirect to /admin so the AdminController sets up the model correctly
                return "redirect:/admin";
            } else if (view.equals("student")) {
                // Redirect to student page (assuming you have a GET mapping for it)
                return "redirect:/student";
            } else {
                // If the view returned is unexpected, fallback to login page
                model.addAttribute("error", "Unexpected account type.");
                return "index";
            }

        } catch (Exception e) {
            // Show error on the login page if something goes wrong
            model.addAttribute("error", e.getMessage());
            return "index";
        }
    }
}
