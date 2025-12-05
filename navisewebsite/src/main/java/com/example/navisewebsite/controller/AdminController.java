package com.example.navisewebsite.controller;

import com.example.navisewebsite.domain.Course;
import com.example.navisewebsite.repository.ProgramRepository;
import com.example.navisewebsite.repository.StudentInfoRepository;
import com.example.navisewebsite.repository.DatabaseUtil;
import com.example.navisewebsite.service.AdminCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.sql.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling admin actions like adding courses
 * Its purpose is to provide endpoints for admin functionalities,
 * specifically adding courses to major or minor programs.
 */
@Controller
public class AdminController {

    private final AdminCourseService courseService;
    
    @Autowired
    private StudentInfoRepository studentInfoRepository;

    public AdminController(AdminCourseService courseService) {
        this.courseService = courseService;
    }
    // Display the admin page
    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("course", new Course());
        return "admin";
    }

    /**
     * Serve the admin home/dashboard page.
     * Requires active session (admin must be logged in).
     * 
     * @param session the HttpSession containing admin data
     * @param model the Model to pass data to the template
     * @return the admin-home template or redirect to login if not authenticated
     */
    @GetMapping("/admin-home")
    public String adminHome(HttpSession session, Model model) {
        // Check if user is logged in and is an admin
        Object userType = session.getAttribute("userType");
        Object email = session.getAttribute("email");
        
        if (userType == null || !"admin".equals(userType)) {
            // Redirect to login if not authenticated as an admin
            return "redirect:/";
        }
        
        // Pass admin email to the template
        model.addAttribute("email", email);
        
        // Load all students from student_info table
        List<StudentInfoRepository.StudentInfo> students = studentInfoRepository.findAllStudents();
        model.addAttribute("students", students);
        
        return "admin-home";
    }

    /**
     * Handles adding a course to a program (major or minor)
     *
     * @param course       The course to add
     * @param programName  Name of the program (sheetName)
     * @param programType  Either "Major" or "Minor"
     * @param session      HttpSession to retrieve user context
     * @param model        Spring Model for passing data to the view
     * @return redirect to admin-home page
     */
    @PostMapping("/admin/add-course")
    public String addCourse(
            @ModelAttribute("course") Course course,
            @RequestParam(required = false) String attributesCSV,
            @RequestParam(required = false) String prerequisitesCSV,
            @RequestParam(required = false) String corequisitesCSV,
            @RequestParam(required = false) String termsCSV,
            @RequestParam String programName,
            @RequestParam String programType,
            HttpSession session,
            Model model) {

        // Validate program type
        if (!programType.equalsIgnoreCase("Major") && !programType.equalsIgnoreCase("Minor")) {
            model.addAttribute("error", "Program type must be either 'Major' or 'Minor'");
            return adminHome(session, model);
        }

        // Convert CSV strings to lists
        course.setAttributeFromCSV(attributesCSV);
        course.setPrerequisitesFromCSV(prerequisitesCSV);
        course.setCorequisitesFromCSV(corequisitesCSV);
        course.setTermOfferedFromCSV(termsCSV);

        // Call service to add course & link to program
        courseService.add_course(course, programName, programType);

        model.addAttribute("message", "Course added successfully!");

        return adminHome(session, model);
    }

    @PostMapping("/admin/remove-course")
    public String removeCourse(@ModelAttribute("course") Course course, HttpSession session, Model model) {
        if (course.get_courseID() == null || course.get_courseID().isEmpty()) {
            model.addAttribute("error", "Course ID is required to remove a course.");
            return adminHome(session, model);
        }

        courseService.remove_course(course);

        model.addAttribute("message", "Course removed successfully!");

        return adminHome(session, model);
    }
    // Add a program (Major/Minor)
    @PostMapping("/admin/add-program")
    public String addProgram(@RequestParam String programName,
                             @RequestParam String programType,
                             HttpSession session,
                             Model model) {

        if (!programType.equalsIgnoreCase("Major") && !programType.equalsIgnoreCase("Minor")) {
            model.addAttribute("error", "Program type must be 'Major' or 'Minor'");
            return adminHome(session, model);
        }

        System.out.println("DEBUG: Adding program '" + programName + "' of type '" + programType + "'");
        
        ProgramRepository programRepo = new ProgramRepository();
        int programId = programRepo.addProgram(programName, programType);

        System.out.println("DEBUG: Program saved with ID: " + programId);
        
        if (programId != -1) {
            model.addAttribute("message", "Program added successfully! (ID: " + programId + ")");
        } else {
            model.addAttribute("error", "Failed to add program. It may already exist.");
        }

        return adminHome(session, model);
    }

    @PostMapping("/admin/remove-program")
    public String removeProgram(@RequestParam String programName,
                                HttpSession session,
                                Model model) {
        if (programName == null || programName.isEmpty()) {
            model.addAttribute("error", "Program name is required to remove a program.");
            return adminHome(session, model);
        }

        ProgramRepository programRepo = new ProgramRepository();
        programRepo.removeProgram(programName);

        model.addAttribute("message", "Program removed successfully!");

        return adminHome(session, model);
    }

    /**
     * Update a student's major/minor information
     */
    @PostMapping("/admin/update-student-info")
    public String updateStudentInfo(@RequestParam Integer userId,
                                     @RequestParam String major,
                                     @RequestParam String minor,
                                     HttpSession session,
                                     Model model) {
        Object userType = session.getAttribute("userType");
        if (userType == null || !"admin".equals(userType)) {
            return "redirect:/";
        }

        try (Connection conn = DatabaseUtil.connectStudentInfo(); 
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE student_info SET major = ?, minor = ? WHERE user_id = ?")) {
            ps.setString(1, major);
            ps.setString(2, minor);
            ps.setInt(3, userId);
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                model.addAttribute("message", "Student info updated successfully!");
            } else {
                model.addAttribute("error", "Student not found or could not be updated.");
            }
        } catch (SQLException e) {
            model.addAttribute("error", "Database error: " + e.getMessage());
        }

        // Reload student list
        List<StudentInfoRepository.StudentInfo> students = studentInfoRepository.findAllStudents();
        model.addAttribute("students", students);
        model.addAttribute("email", session.getAttribute("email"));
        return "admin-home";
    }

    /**
     * Get database statistics for admin monitoring
     */
    @GetMapping("/admin/database-stats")
    public String getDatabaseStats(HttpSession session, Model model) {
        Object userType = session.getAttribute("userType");
        if (userType == null || !"admin".equals(userType)) {
            return "redirect:/";
        }

        Map<String, Integer> stats = new HashMap<>();
        
        try {
            // Count users
            try (Connection conn = DatabaseUtil.connectUsers(); 
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users")) {
                if (rs.next()) stats.put("users", rs.getInt("count"));
            }

            // Count students
            try (Connection conn = DatabaseUtil.connectStudentInfo(); 
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM student_info")) {
                if (rs.next()) stats.put("students", rs.getInt("count"));
            }

            // Count courses
            try (Connection conn = DatabaseUtil.connectCourses(); 
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM courses")) {
                if (rs.next()) stats.put("courses", rs.getInt("count"));
            }

            // Count programs
            try (Connection conn = DatabaseUtil.connectCourses(); 
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM programs")) {
                if (rs.next()) stats.put("programs", rs.getInt("count"));
            }

            // Count program_courses links
            try (Connection conn = DatabaseUtil.connectCourses(); 
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM program_courses")) {
                if (rs.next()) stats.put("program_courses", rs.getInt("count"));
            }

            // Count NTC requirements
            try (Connection conn = DatabaseUtil.connectCourses(); 
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM ntc_requirements")) {
                if (rs.next()) stats.put("ntc_requirements", rs.getInt("count"));
            }

        } catch (SQLException e) {
            model.addAttribute("error", "Error fetching statistics: " + e.getMessage());
        }

        model.addAttribute("stats", stats);
        model.addAttribute("email", session.getAttribute("email"));
        return "admin-database-stats";
    }
}


