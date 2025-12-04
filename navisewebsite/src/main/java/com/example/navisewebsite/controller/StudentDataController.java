package com.example.navisewebsite.controller;

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
    
    private static final String DB_PATH = "student_info.db";
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
        
        Integer userId = (Integer) session.getAttribute("userId");
        String email = (String) session.getAttribute("email");
        model.addAttribute("studentEmail", email);
        
        if (userId == null) {
            model.addAttribute("major", "Information not provided");
            model.addAttribute("minor", "Information not provided");
            model.addAttribute("courseHistory", "Information not provided");
            return "student-my-courses";
        }
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            String sql = "SELECT major, minor, past_courses FROM student_info WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
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
        
        Integer userId = (Integer) session.getAttribute("userId");
        String email = (String) session.getAttribute("email");
        model.addAttribute("studentEmail", email);
        
        if (userId == null) {
            return "student-degree-progress";
        }
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            // Get student's major, minor, and past courses
            String sql = "SELECT major, minor, past_courses FROM student_info WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String major = rs.getString("major");
                String minor = rs.getString("minor");
                String pastCoursesStr = rs.getString("past_courses");
                
                List<String> completedCourses = parseCourseList(pastCoursesStr);
                
                // Add the raw past courses list to the model for display
                model.addAttribute("pastCourses", pastCoursesStr != null && !pastCoursesStr.isEmpty() ? pastCoursesStr : "No courses recorded");
                
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
            List<String> majors = getAvailablePrograms("Major");
            List<String> minors = getAvailablePrograms("Minor");
            
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
        Integer userId = (Integer) session.getAttribute("userId");
        model.addAttribute("studentEmail", email);
        
        // Validate userId
        if (userId == null) {
            model.addAttribute("error", "User session invalid. Please log in again.");
            return "student-projected-schedule";
        }
        
        // Reload dropdown options (with correct capitalization)
        try {
            List<String> majors = getAvailablePrograms("Major");
            List<String> minors = getAvailablePrograms("Minor");
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
            // Get ALL required courses for the selected programs (not filtering by completed)
            List<Map<String, Object>> allRequiredCourses = new ArrayList<>();
            
            if (major != null && !major.isEmpty()) {
                List<Map<String, Object>> majorCourses = getAllCoursesForProgram(major);
                allRequiredCourses.addAll(majorCourses);
            }
            
            if (minor != null && !minor.isEmpty()) {
                List<Map<String, Object>> minorCourses = getAllCoursesForProgram(minor);
                allRequiredCourses.addAll(minorCourses);
            }
            
            // Remove duplicates by course_id
            Map<String, Map<String, Object>> uniqueCourses = new LinkedHashMap<>();
            for (Map<String, Object> course : allRequiredCourses) {
                String courseId = (String) course.get("course_id");
                if (!uniqueCourses.containsKey(courseId)) {
                    uniqueCourses.put(courseId, course);
                }
            }
            
            // Organize into exactly 8 semesters
            List<Map<String, Object>> semesters = organizeCoursesBySemester(
                new ArrayList<>(uniqueCourses.values()), 15);
            
            int totalCourseCount = uniqueCourses.size();
            int totalCredits = calculateTotalCredits(uniqueCourses.values());
            
            model.addAttribute("semesters", semesters);
            model.addAttribute("selectedMajor", major);
            model.addAttribute("selectedMinor", minor);
            model.addAttribute("totalCourses", totalCourseCount);
            model.addAttribute("totalCredits", totalCredits);
            
        } catch (Exception e) {
            model.addAttribute("error", "Unable to project schedule: " + e.getMessage());
            e.printStackTrace();
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
            // Get requirements for the program by joining programs -> program_courses -> courses
            String sql = "SELECT c.* FROM courses c " +
                    "JOIN program_courses pc ON c.course_id = pc.course_id " +
                    "JOIN programs p ON pc.program_id = p.program_id " +
                    "WHERE p.program_name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, program);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Map<String, String> courseData = new HashMap<>();
                // course_id is the actual course code like "MATH 1210"
                String courseId = rs.getString("course_id");
                courseData.put("code", courseId != null ? courseId : "N/A");
                courseData.put("name", rs.getString("course_name") != null ? rs.getString("course_name") : "N/A");
                courseData.put("credits", String.valueOf(rs.getInt("credit_hours")));
                courseData.put("professor", rs.getString("professor") != null ? rs.getString("professor") : "TBA");
                courseData.put("days", rs.getString("days") != null ? rs.getString("days") : "TBA");
                courseData.put("time", rs.getString("time") != null ? rs.getString("time") : "TBA");
                courseData.put("building", rs.getString("building") != null ? rs.getString("building") : "TBA");
                courseData.put("room", rs.getString("room") != null ? rs.getString("room") : "TBA");
                
                // Check if this course_id is in the student's completed courses
                if (courseId != null && completedCourses.contains(courseId)) {
                    completed.add(courseData);
                } else {
                    remaining.add(courseData);
                }
            }
        } catch (SQLException e) {
            // Handle error - log it for debugging
            System.err.println("Error getting requirements and progress: " + e.getMessage());
            e.printStackTrace();
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
    
    private List<String> getStudentCompletedCourses(Integer userId) throws SQLException {
        List<String> completedCourses = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            String sql = "SELECT past_courses FROM student_info WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String pastCoursesStr = rs.getString("past_courses");
                completedCourses = parseCourseList(pastCoursesStr);
            }
        }
        return completedCourses;
    }
    
    private List<Map<String, Object>> getAllCoursesForProgram(String programName) throws SQLException {
        List<Map<String, Object>> courses = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + COURSES_DB_PATH)) {
            // Get ALL required courses for the program
            String sql = "SELECT c.course_id, c.course_name, c.credit_hours, c.professor, " +
                    "c.days, c.time, c.building, c.room " +
                    "FROM courses c " +
                    "JOIN program_courses pc ON c.course_id = pc.course_id " +
                    "JOIN programs p ON pc.program_id = p.program_id " +
                    "WHERE p.program_name = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, programName);
            ResultSet rs = ps.executeQuery();
            
            int totalCoursesFound = 0;
            
            while (rs.next()) {
                totalCoursesFound++;
                Map<String, Object> courseData = new HashMap<>();
                courseData.put("course_id", rs.getString("course_id"));
                courseData.put("course_name", rs.getString("course_name"));
                courseData.put("credit_hours", rs.getInt("credit_hours"));
                courseData.put("professor", rs.getString("professor"));
                courseData.put("days", rs.getString("days"));
                courseData.put("time", rs.getString("time"));
                courseData.put("building", rs.getString("building"));
                courseData.put("room", rs.getString("room"));
                courses.add(courseData);
            }
            
            System.out.println("DEBUG: Program '" + programName + "' - Found " + totalCoursesFound + " total required courses");
        }
        
        return courses;
    }
    
    private List<Map<String, Object>> getCoursesForProgram(String programName, List<String> completedCourses) throws SQLException {
        List<Map<String, Object>> courses = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + COURSES_DB_PATH)) {
            // Get all required courses for the program that haven't been completed
            String sql = "SELECT c.course_id, c.course_name, c.credit_hours, c.professor, " +
                    "c.days, c.time, c.building, c.room " +
                    "FROM courses c " +
                    "JOIN program_courses pc ON c.course_id = pc.course_id " +
                    "JOIN programs p ON pc.program_id = p.program_id " +
                    "WHERE p.program_name = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, programName);
            ResultSet rs = ps.executeQuery();
            
            int totalCoursesFound = 0;
            int coursesAdded = 0;
            
            while (rs.next()) {
                totalCoursesFound++;
                String courseId = rs.getString("course_id");
                
                // Only include if not already completed
                if (!completedCourses.contains(courseId)) {
                    coursesAdded++;
                    Map<String, Object> courseData = new HashMap<>();
                    courseData.put("course_id", courseId);
                    courseData.put("course_name", rs.getString("course_name"));
                    courseData.put("credit_hours", rs.getInt("credit_hours"));
                    courseData.put("professor", rs.getString("professor"));
                    courseData.put("days", rs.getString("days"));
                    courseData.put("time", rs.getString("time"));
                    courseData.put("building", rs.getString("building"));
                    courseData.put("room", rs.getString("room"));
                    courses.add(courseData);
                }
            }
            
            System.out.println("DEBUG: Program '" + programName + "' - Found " + totalCoursesFound + 
                             " total courses, " + coursesAdded + " not yet completed");
            System.out.println("DEBUG: Completed courses: " + completedCourses);
        }
        
        return courses;
    }
    
    private List<Map<String, Object>> organizeCoursesBySemester(List<Map<String, Object>> courses, int maxCreditsPerSemester) {
        List<Map<String, Object>> semesters = new ArrayList<>();
        
        if (courses.isEmpty()) {
            return semesters;
        }
        
        // Always create exactly 8 semesters (4 years)
        int targetSemesters = 8;
        int totalCourses = courses.size();
        
        System.out.println("DEBUG: Distributing " + totalCourses + " courses across " + targetSemesters + " semesters");
        
        // Calculate how many courses per semester (distribute evenly)
        int baseCoursesPerSemester = totalCourses / targetSemesters;
        int remainingCourses = totalCourses % targetSemesters; // Extra courses to distribute
        
        System.out.println("DEBUG: Base courses per semester: " + baseCoursesPerSemester + 
                         ", Extra courses to distribute: " + remainingCourses);
        
        int courseIndex = 0;
        
        // Create all 8 semesters
        for (int semesterNum = 1; semesterNum <= targetSemesters; semesterNum++) {
            List<Map<String, Object>> currentSemesterCourses = new ArrayList<>();
            int currentCredits = 0;
            
            // Determine how many courses for this semester
            // First 'remainingCourses' semesters get one extra course
            int coursesForThisSemester = baseCoursesPerSemester;
            if (semesterNum <= remainingCourses) {
                coursesForThisSemester++;
            }
            
            // Add courses to this semester
            for (int i = 0; i < coursesForThisSemester && courseIndex < totalCourses; i++) {
                Map<String, Object> course = courses.get(courseIndex);
                int credits = (Integer) course.get("credit_hours");
                
                currentSemesterCourses.add(course);
                currentCredits += credits;
                courseIndex++;
            }
            
            // Create semester (even if empty for later semesters)
            Map<String, Object> semester = new HashMap<>();
            semester.put("semesterName", "Semester " + semesterNum);
            semester.put("courses", currentSemesterCourses);
            semester.put("totalCredits", currentCredits);
            semesters.add(semester);
            
            System.out.println("DEBUG: Semester " + semesterNum + " - " + 
                             currentSemesterCourses.size() + " courses, " + 
                             currentCredits + " credits");
        }
        
        System.out.println("DEBUG: Final check - Total courses distributed: " + courseIndex + " out of " + totalCourses);
        
        return semesters;
    }
    
    private int calculateTotalCredits(Collection<Map<String, Object>> courses) {
        int total = 0;
        for (Map<String, Object> course : courses) {
            total += (Integer) course.get("credit_hours");
        }
        return total;
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
