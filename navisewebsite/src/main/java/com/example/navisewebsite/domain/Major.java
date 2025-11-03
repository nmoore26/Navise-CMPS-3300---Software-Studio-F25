package com.example.navisewebsite.domain;

import java.util.List;

public class Major extends Path {
    private int min_hours;
    
    public Major() {
        super();
        this.min_hours = 30; // default
    }
    
    public Major(String path_name) {
        super(path_name);
        this.min_hours = 30;
    }
    
    public Major(String path_name, int min_hours) {
        super(path_name);
        this.min_hours = min_hours; // Use the parameter, not hardcoded 30
    }
    
    public Major(String path_name, List<Course> requirements, int min_hours) {
        super(path_name, requirements);
        this.min_hours = min_hours;
    }
    
    public int get_min_hours() { return min_hours; }
    public void set_min_hours(int min_hours) { this.min_hours = min_hours; }
    
    // Business methods
    public boolean meets_reqs(List<Course> completed) {
        if (completed == null) return false;
        int completed_hours = completed.stream()
                .mapToInt(Course::get_credit_hours)
                .sum();
        return completed_hours >= min_hours;
    }
    
    public int hours_needed(List<Course> completed) {
        if (completed == null) return min_hours;
        int completed_hours = completed.stream()
                .mapToInt(Course::get_credit_hours)
                .sum();
        return Math.max(0, min_hours - completed_hours);
    }
    
    public boolean can_graduate(List<Course> completed) {
        return meets_reqs(completed);
    }
}