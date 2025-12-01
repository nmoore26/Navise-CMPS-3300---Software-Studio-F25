package com.example.navisewebsite.repository;

import com.example.navisewebsite.domain.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
/*
The purpose of the CourseRepository class is to provide methods for performing CRUD operations
on the 'courses' table in the SQLite database. It allows adding new courses, retrieving courses
by ID, and fetching all courses.
 */
@Repository
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
            System.out.println("Inserting course: " + course.get_courseID());


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
        String sql = "SELECT * FROM courses WHERE course_id = ?";
        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToCourse(rs));
            }
            System.out.println("Looking up course: " + courseID);


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
    // Helper to safely split nullable CSV columns into lists
    String attrs = rs.getString("attributes");
    List<String> attributeList = (attrs == null || attrs.isBlank()) ? new ArrayList<>() : Arrays.asList(attrs.split(","));

    String prereq = rs.getString("prerequisites");
    List<String> prereqList = (prereq == null || prereq.isBlank()) ? new ArrayList<>() : Arrays.asList(prereq.split(","));

    String coreq = rs.getString("corequisites");
    List<String> coreqList = (coreq == null || coreq.isBlank()) ? new ArrayList<>() : Arrays.asList(coreq.split(","));

    String terms = rs.getString("terms");
    List<String> termsList = (terms == null || terms.isBlank()) ? new ArrayList<>() : Arrays.asList(terms.split(","));

    return new Course(
        rs.getString("course_id"),
        rs.getString("course_name"),
        rs.getString("course_code"),
        rs.getInt("credit_hours"),
        rs.getString("professor"),
        rs.getString("days"),
        rs.getString("time"),
        rs.getString("building"),
        rs.getString("room"),
        attributeList,
        prereqList,
        coreqList,
        termsList
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
