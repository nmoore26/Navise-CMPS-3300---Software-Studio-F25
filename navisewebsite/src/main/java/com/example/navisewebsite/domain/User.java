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
    private String firstName;
    private String lastName;
    
    // Constructor
    public User(int userId, String email, String password, String userType) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.userType = userType;
    }
    
    public User(int userId, String email, String password, String userType, String firstName, String lastName) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
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
    
    // getters and setters for firstName and lastName
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
