package com.example.navisewebsite.domain;

/**
 * Represents a user in the system with database-backed credentials.
 * All non-admin users are Students.
 */
public class User {
    private int userId;
    private String email;
    private String password; // Should be hashed with BCrypt
    private String userType; // "student" or "admin"
    
    // Constructor
    public User(int userId, String email, String password, String userType) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.userType = userType;
    }
    
    // Getters
    public int getUserId() {
        return userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public boolean isStudent() {
        return "student".equalsIgnoreCase(userType);
    }
    
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(userType);
    }
    
    // Setters
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
}
