package com.example.navisewebsite.repository;

import com.example.navisewebsite.domain.User;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Optional;

/**
 * Repository for managing User entities (login credentials) with the database.
 * Handles CRUD operations for user authentication.
 */
@Repository
public class UserRepository {
    
    /**
     * Find a user by email
     * @param email The user's email
     * @return Optional containing the User if found
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT user_id, email, password, user_type FROM users WHERE email = ?";
        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    /**
     * Find a user by ID
     * @param userId The user's ID
     * @return Optional containing the User if found
     */
    public Optional<User> findById(int userId) {
        String sql = "SELECT user_id, email, password, user_type FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    /**
     * Add a new student user to the database
     * @param email The student's email
     * @param hashedPassword The hashed password (use BCryptPasswordEncoder)
     * @return true if successful, false otherwise
     */
    public int addStudent(String email, String hashedPassword) {
        return addUser(email, hashedPassword, "student");
    }
    
    /**
     * Add a new admin user to the database
     * @param email The admin's email
     * @param hashedPassword The hashed password
     * @return true if successful, false otherwise
     */
    public int addAdmin(String email, String hashedPassword) {
        return addUser(email, hashedPassword, "admin");
    }
    
    /**
     * Add a new user to the database
     * @param email The user's email
     * @param hashedPassword The hashed password
     * @param userType "student" or "admin"
     * @return The generated user_id if successful, -1 if failed
     */
    private int addUser(String email, String hashedPassword, String userType) {
        String sql = "INSERT INTO users (email, password, user_type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, userType);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return -1;
            }
            
            // Get the generated user_id
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Update a user's password
     * @param userId The user's ID
     * @param hashedPassword The new hashed password
     * @return true if successful, false otherwise
     */
    public boolean updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete a user by ID
     * @param userId The user's ID
     * @return true if successful, false otherwise
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Helper method to map a ResultSet row to a User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("user_type")
        );
    }
}
