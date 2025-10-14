package com.example.navisewebsite.controller;

import com.example.navisewebsite.domain.Account;
import com.example.navisewebsite.domain.AccountFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @PostMapping({"/do-login"})
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model) {
        try {
            Account acct = AccountFactory.forEmail(email);
            String view = acct.loginFlow(password);
            model.addAttribute("email", acct.getEmail());
            return view; // "admin" or "student"
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "index";
        }
    }
}