package com.example.navisewebsite.domain;

import java.util.ArrayList;
import java.util.List;

public class Student extends Account {
    private int userId; // Database user ID
    private String firstName; // Student's first name
    private String lastName; // Student's last name
    private List<Course> currentCourses;
    private List<Course> pastCourses;
    private Major major;
    private Minor minor;
    
    // Constructor with database user ID and name (preferred for DB-backed students)
    public Student(int userId, String firstName, String lastName, String email, String storedPassword) { 
        super(email, storedPassword);
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.currentCourses = new ArrayList<>();
        this.pastCourses = new ArrayList<>();
    }
    
    // Constructor with database user ID (backward compatibility)
    public Student(int userId, String email, String storedPassword) { 
        super(email, storedPassword);
        this.userId = userId;
        this.firstName = "";
        this.lastName = "";
        this.currentCourses = new ArrayList<>();
        this.pastCourses = new ArrayList<>();
    }
    
    // Legacy constructor for backward compatibility
    public Student(String email, String storedPassword) { 
        super(email, storedPassword);
        this.userId = -1; // No database ID
        this.firstName = "";
        this.lastName = "";
        this.currentCourses = new ArrayList<>();
        this.pastCourses = new ArrayList<>();
    }
    
    @Override 
    protected void authorize() { 
        /* students: no special gate here */ 
    }
    
    @Override 
    protected String postLogin() { 
        return "student"; 
    }
    
    // Getters - following Java conventions
    public int getUserId() {
        return userId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getFullName() {
        return (firstName + " " + lastName).trim();
    }
    
    public List<Course> getCurrentCourses() { 
        return new ArrayList<>(currentCourses); 
    }
    
    public List<Course> getPastCourses() { 
        return new ArrayList<>(pastCourses); 
    }
    
    public Major getMajor() { 
        return major; 
    }
    
    public Minor getMinor() { 
        return minor; 
    }
    
    // Setters - following Java conventions
    public void setFirstName(String firstName) {
        this.firstName = firstName != null ? firstName : "";
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName : "";
    }
    
    public void setMajor(Major major) { 
        this.major = major; 
    }
    
    public void setMinor(Minor minor) { 
        this.minor = minor; 
    }
    
    // Course management methods
    public void addCurrentCourse(Course course) {
        if (course != null) {
            currentCourses.add(course);
        }
    }
    
    public void addPastCourse(Course course) {
        if (course != null) {
            pastCourses.add(course);
        }
    }
    
    public boolean removeCurrentCourse(Course course) {
        return currentCourses.remove(course);
    }
    
    public boolean removePastCourse(Course course) {
        return pastCourses.remove(course);
    }
    
    // Progress tracking methods - updated to use refactored Path methods
    public boolean isOnTrack() {
        return major != null && major.meetsRequirements(pastCourses);
    }
    
    public boolean isMinorWithinLimit() {
        return minor == null || minor.isWithinLimit(pastCourses);
    }
    
    public boolean canGraduate() {
        if (major == null) {
            return false;
        }
        
        boolean majorComplete = major.canGraduate(pastCourses);
        boolean minorComplete = (minor == null) || minor.isComplete(pastCourses);
        
        return majorComplete && minorComplete;
    }
    
    // Status reporting
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Student: ").append(getEmail()).append("\n");
        
        if (major != null) {
            sb.append("Major: ").append(major.getPathName()).append("\n");
            sb.append("On Track: ").append(isOnTrack() ? "Yes" : "No").append("\n");
            sb.append("Hours Needed: ").append(major.getHoursNeeded(pastCourses)).append("\n");
            sb.append("Hours Completed: ").append(major.getCompletedHours(pastCourses)).append("\n");
            sb.append("Can Graduate: ").append(canGraduate() ? "Yes" : "No").append("\n");
        } else {
            sb.append("Major: Not assigned\n");
        }
        
        if (minor != null) {
            sb.append("Minor: ").append(minor.getPathName()).append("\n");
            sb.append("Within Limit: ").append(isMinorWithinLimit() ? "Yes" : "No").append("\n");
            sb.append("Hours Remaining: ").append(minor.getHoursRemaining(pastCourses)).append("\n");
            sb.append("Minor Complete: ").append(minor.isComplete(pastCourses) ? "Yes" : "No").append("\n");
        } else {
            sb.append("Minor: Not assigned\n");
        }
        
        return sb.toString();
    }
    
    public int getTotalCreditsCompleted() {
        return pastCourses.stream()
                .mapToInt(Course::get_credit_hours)
                .sum();
    }
    
    public int getTotalCreditsInProgress() {
        return currentCourses.stream()
                .mapToInt(Course::get_credit_hours)
                .sum();
    }
}