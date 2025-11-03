package com.example.navisewebsite.domain;

import java.util.List;

public class Minor extends Path {
    private int max_hours;
    
    public Minor() {
        super();
        this.max_hours = 18; // default
    }
    
    public Minor(String path_name) {
        super(path_name);
        this.max_hours = 18;
    }
    
    public Minor(String path_name, int max_hours) {
        super(path_name);
        this.max_hours = max_hours; // Use the parameter
    }
    
    public Minor(String path_name, List<Course> requirements, int max_hours) {
        super(path_name, requirements);
        this.max_hours = max_hours;
    }
    
    public int get_max_hours() { return max_hours; }
    public void set_max_hours(int max_hours) { this.max_hours = max_hours; }
    
    // Business methods
    public boolean within_limit(List<Course> completed) {
        if (completed == null) return true;
        int completed_hours = completed.stream()
                .mapToInt(Course::get_credit_hours)
                .sum();
        return completed_hours <= max_hours;
    }
    
    public int hours_remaining(List<Course> completed) {
        if (completed == null) return max_hours;
        int completed_hours = completed.stream()
                .mapToInt(Course::get_credit_hours)
                .sum();
        return Math.max(0, max_hours - completed_hours);
    }
    
    public boolean is_complete(List<Course> completed) {
        if (completed == null) return false;
        return !completed.isEmpty() && within_limit(completed);
    }
}
