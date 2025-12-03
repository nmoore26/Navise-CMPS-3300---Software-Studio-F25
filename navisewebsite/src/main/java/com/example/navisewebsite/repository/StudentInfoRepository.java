package com.example.navisewebsite.repository;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class StudentInfoRepository {

    /**
     * Insert a student info record linked to a user_id.
     */
    public int insertStudentInfo(int userId, String firstName, String lastName, String major, String minor, String pastCourses) {
        String sql = "INSERT INTO student_info (user_id, first_name, last_name, major, minor, past_courses) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.connectStudentInfo(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, major);
            ps.setString(5, minor);
            ps.setString(6, pastCourses);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Find student info by user_id.
     */
    public Optional<StudentInfo> findByUserId(int userId) {
        String sql = "SELECT id, user_id, first_name, last_name, major, minor, past_courses FROM student_info WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.connectStudentInfo(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StudentInfo info = new StudentInfo(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("major"),
                            rs.getString("minor"),
                            rs.getString("past_courses")
                    );
                    return Optional.of(info);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /** Simple POJO for student_info rows. */
    public static class StudentInfo {
        public final int id;
        public final int userId;
        public final String firstName;
        public final String lastName;
        public final String major;
        public final String minor;
        public final String pastCourses;

        public StudentInfo(int id, int userId, String firstName, String lastName, String major, String minor, String pastCourses) {
            this.id = id;
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.major = major;
            this.minor = minor;
            this.pastCourses = pastCourses;
        }
    }
}
