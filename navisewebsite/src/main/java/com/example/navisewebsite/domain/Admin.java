package com.example.navisewebsite.domain;

public class Admin extends Account {
    public Admin(String email, String storedPassword) { super(email, storedPassword); }
    
    @Override 
    protected void authorize() {
        if (!email.endsWith("@admin.com")) throw new SecurityException("Not an admin account");
    }
    
    @Override 
    protected String postLogin() { return "admin"; }
    
    // Admin business methods for Path management
    public boolean add_major_req(Major m, Course c) {
        if (m != null && c != null) {
            m.add_req(c);
            return true;
        }
        return false;
    }
    
    public boolean set_major_hours(Major m, int hours) {
        if (m != null && hours > 0) {
            m.set_min_hours(hours);
            return true;
        }
        return false;
    }
    
    public boolean set_minor_hours(Minor m, int hours) {
        if (m != null && hours > 0) {
            m.set_max_hours(hours);
            return true;
        }
        return false;
    }
    
    public String check_student(Student s) {
        if (s == null) return "No student";
        return s.status();
    }
    
    public boolean can_graduate(Student s) {
        return s != null && s.on_track() && s.minor_ok();
    }
}