package com.example.navisewebsite.domain;

import java.util.List;

public class Minor extends Path {
    private static final int DEFAULT_MAX_HOURS = 18;
    private int maxHours;
    
    public Minor() {
        super();
        this.maxHours = DEFAULT_MAX_HOURS;
    }
    
    public Minor(String pathName) {
        super(pathName);
        this.maxHours = DEFAULT_MAX_HOURS;
    }
    
    public Minor(String pathName, int maxHours) {
        super(pathName);
        this.maxHours = maxHours;
    }
    
    public Minor(String pathName, List<Course> requirements, int maxHours) {
        super(pathName, requirements);
        this.maxHours = maxHours;
    }
    
    public int getMaxHours() { 
        return maxHours; 
    }
    
    public void setMaxHours(int maxHours) { 
        this.maxHours = maxHours; 
    }
    
    // Business logic methods - now using parent class helpers
    public boolean isWithinLimit(List<Course> completed) {
        int completedHours = getCompletedHours(completed);
        return completedHours <= maxHours;
    }
    
    public int getHoursRemaining(List<Course> completed) {
        int completedHours = getCompletedHours(completed);
        return Math.max(0, maxHours - completedHours);
    }
    
    public int getCompletedHours(List<Course> completed) {
        List<Course> completedReqs = getCompletedRequirements(completed);
        return calculateTotalHours(completedReqs);
    }
    
    public boolean isComplete(List<Course> completed) {
        if (completed == null || completed.isEmpty()) {
            return false;
        }
        
        // Minor is complete if all requirements met and within hour limit
        List<Course> uncompleted = getUncompletedRequirements(completed);
        return uncompleted.isEmpty() && isWithinLimit(completed);
    }
    
    public boolean meetsRequirements(List<Course> completed) {
        if (completed == null || getRequirements().isEmpty()) {
            return false;
        }
        List<Course> uncompleted = getUncompletedRequirements(completed);
        return uncompleted.isEmpty();
    }
}