package com.example.navisewebsite.repository;

import com.example.navisewebsite.domain.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
/*
The purpose of the CourseRepository class is to provide methods for performing CRUD operations
on the 'courses' table in the SQLite database. It allows adding new courses, retrieving courses
by ID, and fetching all courses.
 */

public class CourseRepository {

    public void addCourse(Course course) {
        String sql = """
            INSERT INTO courses(course_id, course_name, course_code, credit_hours, professor, days, 
                time, building, room, attributes, prerequisites, corequisites, 
                terms) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?);
            """;
           
        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.get_courseID());
            pstmt.setString(2, course.get_course_name());
            pstmt.setString(3, course.get_course_code());
            pstmt.setInt(4, course.get_credit_hours());
            pstmt.setString(5, course.get_professor_name());
            pstmt.setString(6, course.get_days_offered());
            pstmt.setString(7, course.get_time());
            pstmt.setString(8, course.get_building());
            pstmt.setString(9, course.get_room_number());
            pstmt.setString(10, String.join(",", course.get_attribute()));
            pstmt.setString(11, String.join(",", course.get_prerequisites()));
            pstmt.setString(12, String.join(",", course.get_corequisites()));
            pstmt.setString(13, String.join(",", course.get_term_offered()));

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Remove a course by its ID
    public void removeCourse(Course course) {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection conn = DatabaseUtil.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, course.get_courseID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public Optional<Course> findById(String courseID) {
        String sql = "SELECT * FROM courses WHERE \"CourseID\" = ?";
        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToCourse(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Course> findAll() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses";
        try (Connection conn = DatabaseUtil.connect();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        return new Course(
                rs.getString("CourseID"),
                rs.getString("CourseName"),
                rs.getString("CRN: Course Code"),
                rs.getInt("CreditHrs"),
                rs.getString("Professor"),
                rs.getString("DaysOffered"),
                rs.getString("Time"),
                rs.getString("Building"),
                rs.getString("Room Number"),
                Arrays.asList(rs.getString("Attribute/NTC Requirement").split(",")),
                Arrays.asList(rs.getString("Prerequisite").split(",")),
                Arrays.asList(rs.getString("Corequisite").split(",")),
                Arrays.asList(rs.getString("Terms Offered").split(","))
        );
    }
    public void addNTCRequirement(String requirement, int num) {
        String sql = "INSERT INTO ntc_requirements (ntc_requirement, num_classes) VALUES (?, ?)";

        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, requirement);
            pstmt.setInt(2, num);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
