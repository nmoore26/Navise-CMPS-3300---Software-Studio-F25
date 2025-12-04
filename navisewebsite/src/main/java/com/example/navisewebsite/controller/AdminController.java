package com.example.navisewebsite.controller;

import com.example.navisewebsite.domain.Course;
import com.example.navisewebsite.repository.ProgramRepository;
import com.example.navisewebsite.repository.StudentInfoRepository;
import com.example.navisewebsite.service.AdminCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.util.List;

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

        ProgramRepository programRepo = new ProgramRepository();
        int programId = programRepo.addProgram(programName, programType);

        if (programId != -1) {
            model.addAttribute("message", "Program added successfully!");
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
}


