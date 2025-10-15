package com.example.navisewebsite.domain;

public class Admin extends Account {
    public Admin(String email, String storedPassword) { super(email, storedPassword); }
    @Override protected void authorize() {
        if (!email.endsWith("@admin.com")) throw new SecurityException("Not an admin account");
    }
    @Override protected String postLogin() { return "admin"; }
}