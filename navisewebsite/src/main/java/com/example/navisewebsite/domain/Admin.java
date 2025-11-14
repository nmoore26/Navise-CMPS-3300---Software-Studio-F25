package com.example.navisewebsite.domain;

public class Admin extends Account {
    
    public Admin(String email, String storedPassword) { 
        super(email, storedPassword); 
    }
    
    @Override 
    protected void authorize() {
        if (!email.endsWith("@admin.com")) {
            throw new SecurityException("Not an admin account");
        }
    }
    
    @Override 
    protected String postLogin() { 
        return "admin"; 
    }
    
    // Major management methods - updated to use refactored Path methods
    public boolean addMajorRequirement(Major major, Course course) {
        if (major != null && course != null) {
            major.addRequirement(course);
            return true;
        }
        return false;
    }
    
    public boolean removeMajorRequirement(Major major, Course course) {
        if (major != null && course != null) {
            return major.removeRequirement(course);
        }
        return false;
    }
    
    public boolean setMajorMinimumHours(Major major, int hours) {
        if (major != null && hours > 0) {
            major.setMinHours(hours);
            return true;
        }
        return false;
    }
    
    // Minor management methods - updated to use refactored Path methods
    public boolean addMinorRequirement(Minor minor, Course course) {
        if (minor != null && course != null) {
            minor.addRequirement(course);
            return true;
        }
        return false;
    }
    
    public boolean removeMinorRequirement(Minor minor, Course course) {
        if (minor != null && course != null) {
            return minor.removeRequirement(course);
        }
        return false;
    }
    
    public boolean setMinorMaximumHours(Minor minor, int hours) {
        if (minor != null && hours > 0) {
            minor.setMaxHours(hours);
            return true;
        }
        return false;
    }
    
    // Path management methods (generic)
    public boolean updatePathName(Path path, String newName) {
        if (path != null && newName != null && !newName.trim().isEmpty()) {
            path.setPathName(newName);
            return true;
        }
        return false;
    }
    
    // Student monitoring methods - updated to use refactored Student methods
    public String checkStudentStatus(Student student) {
        if (student == null) {
            return "No student provided";
        }
        return student.getStatus();
    }
    
    public boolean canStudentGraduate(Student student) {
        return student != null && student.canGraduate();
    }
    
    public boolean isStudentOnTrack(Student student) {
        return student != null && student.isOnTrack();
    }
    
    public boolean isStudentMinorCompliant(Student student) {
        return student != null && student.isMinorWithinLimit();
    }
    
    // Validation helper methods
    public String validateMajorCompletion(Student student) {
        if (student == null || student.getMajor() == null) {
            return "Student or major not assigned";
        }
        
        Major major = student.getMajor();
        StringBuilder report = new StringBuilder();
        
        report.append("Major Validation for: ").append(student.getEmail()).append("\n");
        report.append("Major: ").append(major.getPathName()).append("\n");
        report.append("Meets Requirements: ")
              .append(major.meetsRequirements(student.getPastCourses()) ? "Yes" : "No")
              .append("\n");
        report.append("Hours Completed: ").append(major.getCompletedHours(student.getPastCourses())).append("\n");
        report.append("Hours Needed: ").append(major.getHoursNeeded(student.getPastCourses())).append("\n");
        report.append("Minimum Hours Required: ").append(major.getMinHours()).append("\n");
        report.append("Can Graduate: ").append(major.canGraduate(student.getPastCourses()) ? "Yes" : "No").append("\n");
        
        return report.toString();
    }
    
    public String validateMinorCompletion(Student student) {
        if (student == null || student.getMinor() == null) {
            return "Student or minor not assigned";
        }
        
        Minor minor = student.getMinor();
        StringBuilder report = new StringBuilder();
        
        report.append("Minor Validation for: ").append(student.getEmail()).append("\n");
        report.append("Minor: ").append(minor.getPathName()).append("\n");
        report.append("Meets Requirements: ")
              .append(minor.meetsRequirements(student.getPastCourses()) ? "Yes" : "No")
              .append("\n");
        report.append("Hours Completed: ").append(minor.getCompletedHours(student.getPastCourses())).append("\n");
        report.append("Hours Remaining: ").append(minor.getHoursRemaining(student.getPastCourses())).append("\n");
        report.append("Maximum Hours Allowed: ").append(minor.getMaxHours()).append("\n");
        report.append("Within Limit: ").append(minor.isWithinLimit(student.getPastCourses()) ? "Yes" : "No").append("\n");
        report.append("Minor Complete: ").append(minor.isComplete(student.getPastCourses()) ? "Yes" : "No").append("\n");
        
        return report.toString();
    }
}