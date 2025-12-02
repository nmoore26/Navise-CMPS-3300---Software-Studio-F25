package com.example.navisewebsite.domain;

import java.util.ArrayList;
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
        return requirements;
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
                .mapToInt(this::getCourseCredits)
                .sum();
    }
    
    // Helper to safely get credits from a Course object regardless of method name
    private int getCourseCredits(Course course) {
        if (course == null) return 0;
        
        try {
            // Try get_credit_hours() first
            return course.get_credit_hours();
        } catch (Throwable t1) {
            try {
                // Try getCredits() method
                java.lang.reflect.Method m = course.getClass().getMethod("getCredits");
                Object result = m.invoke(course);
                if (result instanceof Number) {
                    return ((Number) result).intValue();
                }
            } catch (Throwable t2) {
                try {
                    // Try direct field access for "credits"
                    java.lang.reflect.Field f = course.getClass().getDeclaredField("credits");
                    f.setAccessible(true);
                    Object val = f.get(course);
                    if (val instanceof Number) {
                        return ((Number) val).intValue();
                    }
                } catch (Throwable t3) {
                    try {
                        // Try direct field access for "credit_hours"
                        java.lang.reflect.Field f = course.getClass().getDeclaredField("credit_hours");
                        f.setAccessible(true);
                        Object val = f.get(course);
                        if (val instanceof Number) {
                            return ((Number) val).intValue();
                        }
                    } catch (Throwable t4) {
                        // All attempts failed
                    }
                }
            }
        }
        return 0;
    }
}
