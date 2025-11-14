package com.example.navisewebsite.domain;

import java.util.List;

public class Major extends Path {
    private static final int DEFAULT_MIN_HOURS = 30;
    private int minHours;
    
    public Major() {
        super();
        this.minHours = DEFAULT_MIN_HOURS;
    }
    
    public Major(String pathName) {
        super(pathName);
        this.minHours = DEFAULT_MIN_HOURS;
    }
    
    public Major(String pathName, int minHours) {
        super(pathName);
        this.minHours = minHours;
    }
    
    public Major(String pathName, List<Course> requirements, int minHours) {
        super(pathName, requirements);
        this.minHours = minHours;
    }
    
    public int getMinHours() { 
        return minHours; 
    }
    
    public void setMinHours(int minHours) { 
        this.minHours = minHours; 
    }
    
    // Business logic methods - now using parent class helpers
    public boolean meetsRequirements(List<Course> completed) {
        if (completed == null || getRequirements().isEmpty()) {
            return false;
        }
        
        // All required courses must be completed
        List<Course> uncompleted = getUncompletedRequirements(completed);
        return uncompleted.isEmpty();
    }
    
    public int getHoursNeeded(List<Course> completed) {
        List<Course> uncompleted = getUncompletedRequirements(completed);
        return calculateTotalHours(uncompleted);
    }
    
    public int getCompletedHours(List<Course> completed) {
        List<Course> completedReqs = getCompletedRequirements(completed);
        return calculateTotalHours(completedReqs);
    }
    
    public boolean canGraduate(List<Course> completed) {
        return meetsRequirements(completed) && 
               getCompletedHours(completed) >= minHours;
    }
    
    public boolean meetsMinimumHours(List<Course> completed) {
        return getCompletedHours(completed) >= minHours;
    }
}