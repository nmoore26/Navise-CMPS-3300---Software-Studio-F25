package com.example.navisewebsite.repository;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class StudentInfoRepository {

    /**
     * Insert a student info record linked to a user_id.
     */
    public int insertStudentInfo(int userId, String firstName, String lastName, String major, String minor, String schoolYear, String pastCourses) {
        String sql = "INSERT INTO student_info (user_id, first_name, last_name, major, minor, school_year, past_courses) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.connectStudentInfo(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, major);
            ps.setString(5, minor);
            ps.setString(6, schoolYear);
            ps.setString(7, pastCourses);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Update student info record.
     */
    public int updateStudentInfo(int userId, String firstName, String lastName, String major, String minor, String schoolYear, String pastCourses) {
        String sql = "UPDATE student_info SET first_name = ?, last_name = ?, major = ?, minor = ?, school_year = ?, past_courses = ? WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.connectStudentInfo(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, major);
            ps.setString(4, minor);
            ps.setString(5, schoolYear);
            ps.setString(6, pastCourses);
            ps.setInt(7, userId);
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
        String sql = "SELECT id, user_id, first_name, last_name, major, minor, school_year, past_courses FROM student_info WHERE user_id = ?";
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
                            rs.getString("school_year"),
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

    /**
     * Get all student info records.
     */
    public List<StudentInfo> findAllStudents() {
        List<StudentInfo> students = new ArrayList<>();
        String sql = "SELECT id, user_id, first_name, last_name, major, minor, school_year, past_courses FROM student_info ORDER BY last_name, first_name";
        try (Connection conn = DatabaseUtil.connectStudentInfo(); 
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StudentInfo info = new StudentInfo(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("major"),
                        rs.getString("minor"),
                        rs.getString("school_year"),
                        rs.getString("past_courses")
                );
                students.add(info);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    /** Simple POJO for student_info rows. */
    public static class StudentInfo {
        public final int id;
        public final int userId;
        public final String firstName;
        public final String lastName;
        public final String major;
        public final String minor;
        public final String schoolYear;
        public final String pastCourses;

        public StudentInfo(int id, int userId, String firstName, String lastName, String major, String minor, String schoolYear, String pastCourses) {
            this.id = id;
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.major = major;
            this.minor = minor;
            this.schoolYear = schoolYear;
            this.pastCourses = pastCourses;
        }
    }
}
