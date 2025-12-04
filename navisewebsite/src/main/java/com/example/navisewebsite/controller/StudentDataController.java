package com.example.navisewebsite.controller;

import com.example.navisewebsite.domain.ScheduleDomain.*;
import com.example.navisewebsite.repository.SQLiteScheduleRepository.*;
import com.example.navisewebsite.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.*;
import java.util.*;

/**
 * Controller for student data pages: My Courses, Degree Progress, Projected Schedule.
 */
@Controller
public class StudentDataController {
    
    private static final String DB_PATH = "studentinfo.db";
    private static final String COURSES_DB_PATH = "courses.db";
    
    // Helper to check authentication
    private boolean isAuthenticated(HttpSession session) {
        Object userType = session.getAttribute("userType");
        return userType != null && "student".equals(userType);
    }
    
    /**
     * My Courses page - displays major, minor, and course history.
     */
    @GetMapping("/student/my-courses")
    public String myCourses(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/";
        }
        
        String email = (String) session.getAttribute("email");
        model.addAttribute("studentEmail", email);
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            String sql = "SELECT major, minor, past_courses FROM students WHERE email = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String major = rs.getString("major");
                String minor = rs.getString("minor");
                String pastCourses = rs.getString("past_courses");
                
                model.addAttribute("major", major != null && !major.isEmpty() 
                    ? major : "Information not provided");
                model.addAttribute("minor", minor != null && !minor.isEmpty() 
                    ? minor : "Information not provided");
                model.addAttribute("courseHistory", pastCourses != null && !pastCourses.isEmpty() 
                    ? pastCourses : "Information not provided");
            } else {
                model.addAttribute("major", "Information not provided");
                model.addAttribute("minor", "Information not provided");
                model.addAttribute("courseHistory", "Information not provided");
            }
        } catch (SQLException e) {
            model.addAttribute("error", "Unable to load student data: " + e.getMessage());
        }
        
        return "student-my-courses";
    }
    
    /**
     * Degree Progress page - compares requirements vs completed courses.
     */
    @GetMapping("/student/degree-progress")
    public String degreeProgress(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/";
        }
        
        String email = (String) session.getAttribute("email");
        model.addAttribute("studentEmail", email);
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            // Get student's major, minor, and past courses
            String sql = "SELECT major, minor, past_courses FROM students WHERE email = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String major = rs.getString("major");
                String minor = rs.getString("minor");
                String pastCoursesStr = rs.getString("past_courses");
                
                List<String> completedCourses = parseCourseList(pastCoursesStr);
                
                // Get major requirements and progress
                if (major != null && !major.isEmpty()) {
                    Map<String, Object> majorData = getRequirementsAndProgress(major, completedCourses);
                    model.addAttribute("major", major);
                    model.addAttribute("majorCompleted", majorData.get("completed"));
                    model.addAttribute("majorRemaining", majorData.get("remaining"));
                } else {
                    model.addAttribute("major", null);
                }
                
                // Get minor requirements and progress
                if (minor != null && !minor.isEmpty()) {
                    Map<String, Object> minorData = getRequirementsAndProgress(minor, completedCourses);
                    model.addAttribute("minor", minor);
                    model.addAttribute("minorCompleted", minorData.get("completed"));
                    model.addAttribute("minorRemaining", minorData.get("remaining"));
                } else {
                    model.addAttribute("minor", null);
                }
            }
        } catch (SQLException e) {
            model.addAttribute("error", "Unable to load degree progress: " + e.getMessage());
        }
        
        return "student-degree-progress";
    }
    
    /**
     * Projected Schedule page - form to select major/minor and view projection.
     */
    @GetMapping("/student/projected-schedule")
    public String projectedScheduleForm(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/";
        }
        
        String email = (String) session.getAttribute("email");
        model.addAttribute("studentEmail", email);
        
        // Get list of majors and minors for dropdowns
        try {
            List<String> majors = getAvailablePrograms("major");
            List<String> minors = getAvailablePrograms("minor");
            
            model.addAttribute("majors", majors);
            model.addAttribute("minors", minors);
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load program options: " + e.getMessage());
        }
        
        return "student-projected-schedule";
    }
    
    /**
     * Handle projected schedule form submission.
     */
    @PostMapping("/student/projected-schedule")
    public String projectSchedule(@RequestParam(required = false) String major,
                                  @RequestParam(required = false) String minor,
                                  HttpSession session,
                                  Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/";
        }
        
        String email = (String) session.getAttribute("email");
        model.addAttribute("studentEmail", email);
        
        // Reload dropdown options
        try {
            List<String> majors = getAvailablePrograms("major");
            List<String> minors = getAvailablePrograms("minor");
            model.addAttribute("majors", majors);
            model.addAttribute("minors", minors);
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load program options");
        }
        
        // Validate input
        if ((major == null || major.isEmpty()) && (minor == null || minor.isEmpty())) {
            model.addAttribute("error", "Please select at least one program (major or minor)");
            return "student-projected-schedule";
        }
        
        try {
            // Get student's completed courses
            // List<String> completedCourses = getCompletedCourses(email);
            
            // Project schedule using services
            SQLiteCourseRepository courseRepo = new SQLiteCourseRepository(COURSES_DB_PATH);
            SQLiteUserRepository userRepo = new SQLiteUserRepository(DB_PATH);
            ScheduleProjectionService projectionService = new ScheduleProjectionService(courseRepo, userRepo);
            
            SchedulePlan schedule;
            if (major != null && !major.isEmpty() && (minor == null || minor.isEmpty())) {
                // Major only
                schedule = projectionService.projectMissingCourses(major, email, 15);
            } else if ((major == null || major.isEmpty()) && minor != null && !minor.isEmpty()) {
                // Minor only
                schedule = projectionService.projectMissingCourses(minor, email, 15);
            } else {
                // Both major and minor
                schedule = projectionService.projectForPrograms(major, minor, email, 15);
            }
            
            model.addAttribute("schedule", schedule);
            model.addAttribute("selectedMajor", major);
            model.addAttribute("selectedMinor", minor);
            
        } catch (Exception e) {
            model.addAttribute("error", "Unable to project schedule: " + e.getMessage());
        }
        
        return "student-projected-schedule";
    }
    
    // Helper methods
    
    private List<String> parseCourseList(String courseStr) {
        if (courseStr == null || courseStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(courseStr.split(",\\s*"));
    }
    
    private Map<String, Object> getRequirementsAndProgress(String program, List<String> completedCourses) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> completed = new ArrayList<>();
        List<Map<String, String>> remaining = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + COURSES_DB_PATH)) {
            // Get requirements for the program
            String sql = "SELECT c.* FROM courses c " +
                    "JOIN program_courses pc ON c.courseID = pc.course_id " +
                    "WHERE pc.program_name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, program);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Map<String, String> courseData = new HashMap<>();
                courseData.put("code", rs.getString("course_code"));
                courseData.put("name", rs.getString("course_name"));
                courseData.put("credits", String.valueOf(rs.getInt("credit_hours")));
                courseData.put("professor", rs.getString("professor_name"));
                courseData.put("days", rs.getString("days_offered"));
                courseData.put("time", rs.getString("time"));
                courseData.put("building", rs.getString("building"));
                courseData.put("room", rs.getString("room_number"));
                
                String courseCode = rs.getString("course_code");
                if (completedCourses.contains(courseCode)) {
                    completed.add(courseData);
                } else {
                    remaining.add(courseData);
                }
            }
        } catch (SQLException e) {
            // Handle error
        }
        
        result.put("completed", completed);
        result.put("remaining", remaining);
        return result;
    }
    
    private List<String> getAvailablePrograms(String type) throws SQLException {
        List<String> programs = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + COURSES_DB_PATH)) {
            String sql = "SELECT DISTINCT program_name FROM programs WHERE program_type = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                programs.add(rs.getString("program_name"));
            }
        }
        return programs;
    }
    
    /* private List<String> getCompletedCourses(String email) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            String sql = "SELECT past_courses FROM students WHERE email = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String pastCoursesStr = rs.getString("past_courses");
                return parseCourseList(pastCoursesStr);
            }
        }
        return new ArrayList<>();
    } */
}
