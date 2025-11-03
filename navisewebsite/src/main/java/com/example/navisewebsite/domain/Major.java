package com.example.navisewebsite.domain;

import java.util.List;

public class Major extends Path {
    private int min_hours;
    
    public Major() {
        super();
        this.min_hours = 30;
    }
    
    public Major(String path_name) {
        super(path_name);
        this.min_hours = 30;
    }
    
    public Major(String path_name, int min_hours) {
        super(path_name);
        this.min_hours = min_hours;
    }
    
    public Major(String path_name, List<Course> requirements, int min_hours) {
        super(path_name, requirements);
        this.min_hours = min_hours;
    }
    
    public int get_min_hours() { return min_hours; }
    public void set_min_hours(int min_hours) { this.min_hours = min_hours; }
    
    // FIXED BUSINESS METHODS
    public boolean meets_reqs(List<Course> completed) {
        if (completed == null || get_requirements().isEmpty()) return false;
        
        // Check if ALL required courses are completed
        for (Course required : get_requirements()) {
            if (!completed.contains(required)) {
                return false;
            }
        }
        return true;
    }
    
    public int hours_needed(List<Course> completed) {
        if (completed == null) return min_hours;
        
        // Count hours from required courses that are NOT completed
        int needed = 0;
        for (Course required : get_requirements()) {
            if (!completed.contains(required)) {
                needed += required.get_credit_hours();
            }
        }
        return needed;
    }
    
    public boolean can_graduate(List<Course> completed) {
        return meets_reqs(completed);
    }
}