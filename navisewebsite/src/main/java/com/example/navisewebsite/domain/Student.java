package com.example.navisewebsite.domain;

import java.util.ArrayList;
import java.util.List;

public class Student extends Account {
    private List<Course> current_courses;
    private List<Course> past_courses;
    private Major major;
    private Minor minor;
    
    public Student(String email, String storedPassword) { 
        super(email, storedPassword);
        this.current_courses = new ArrayList<>();
        this.past_courses = new ArrayList<>();
    }
    
    @Override 
    protected void authorize() { /* students: no special gate here */ }
    
    @Override 
    protected String postLogin() { return "student"; }
    
    // Getters
    public List<Course> get_current_courses() { return new ArrayList<>(current_courses); }
    public List<Course> get_past_courses() { return new ArrayList<>(past_courses); }
    public Major get_major() { return major; }
    public Minor get_minor() { return minor; }
    
    // Setters
    public void set_major(Major major) { this.major = major; }
    public void set_minor(Minor minor) { this.minor = minor; }
    
    // Business methods
    public void add_current(Course c) {
        if (c != null) current_courses.add(c);
    }
    
    public void add_past(Course c) {
        if (c != null) past_courses.add(c);
    }
    
    public boolean on_track() {
        return major != null && major.meets_reqs(past_courses);
    }
    
    public boolean minor_ok() {
        return minor == null || minor.within_limit(past_courses);
    }
    
    public String status() {
        StringBuilder sb = new StringBuilder();
        sb.append("Student: ").append(getEmail()).append("\n");
        
        if (major != null) {
            sb.append("Major: ").append(major.get_path_name()).append("\n");
            sb.append("On Track: ").append(on_track() ? "Yes" : "No").append("\n");
            sb.append("Hours Needed: ").append(major.hours_needed(past_courses)).append("\n");
        }
        
        if (minor != null) {
            sb.append("Minor: ").append(minor.get_path_name()).append("\n");
            sb.append("Within Limit: ").append(minor_ok() ? "Yes" : "No").append("\n");
            sb.append("Hours Remaining: ").append(minor.hours_remaining(past_courses)).append("\n");
        }
        
        return sb.toString();
    }
}