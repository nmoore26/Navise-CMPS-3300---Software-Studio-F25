package com.example.navisewebsite.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Path {
    private String pathName;
    private List<Course> requirements;
    
    public Path() {
        this.requirements = new ArrayList<>();
    }
    
    public Path(String pathName) {
        this();
        this.pathName = pathName;
    }
    
    public Path(String pathName, List<Course> requirements) {
        this.pathName = pathName;
        this.requirements = (requirements != null) ? new ArrayList<>(requirements) : new ArrayList<>();
    }
    
    // Getters and Setters with Java conventions
    public String getPathName() { 
        return pathName; 
    }
    
    public List<Course> getRequirements() { 
        return Collections.unmodifiableList(requirements); 
    }
    
    public void setPathName(String pathName) { 
        this.pathName = pathName; 
    }
    
    public void setRequirements(List<Course> requirements) { 
        this.requirements = new ArrayList<>(requirements); 
    }
    
    // Course management methods
    public void addRequirement(Course course) {
        if (course != null) {
            requirements.add(course);
        }
    }
    
    public boolean removeRequirement(Course course) {
        return requirements.remove(course);
    }
    
    public boolean hasRequirement(Course course) {
        return requirements.contains(course);
    }
    
    // Hour calculation methods - extracted for reuse
    public int getTotalRequiredHours() {
        return calculateTotalHours(requirements);
    }
    
    public int getRequirementCount() {
        return requirements.size();
    }
    
    public boolean isEmpty() {
        return requirements.isEmpty();
    }
    
    // Protected helper methods for subclasses
    protected List<Course> getUncompletedRequirements(List<Course> completed) {
        if (completed == null) {
            return new ArrayList<>(requirements);
        }
        return requirements.stream()
                .filter(req -> !completed.contains(req))
                .collect(Collectors.toList());
    }
    
    protected List<Course> getCompletedRequirements(List<Course> completed) {
        if (completed == null) {
            return new ArrayList<>();
        }
        return requirements.stream()
                .filter(completed::contains)
                .collect(Collectors.toList());
    }
    
    protected int calculateTotalHours(List<Course> courses) {
        if (courses == null) {
            return 0;
        }
        return courses.stream()
                .mapToInt(Course::get_credit_hours)
                .sum();
    }
}