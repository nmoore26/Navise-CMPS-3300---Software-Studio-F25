package com.example.navisewebsite;

import org.junit.jupiter.api.Test;

import com.example.navisewebsite.domain.Admin;
import com.example.navisewebsite.domain.Course;
import com.example.navisewebsite.domain.Major;
import com.example.navisewebsite.domain.Minor;
import com.example.navisewebsite.domain.Student;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class MajorMinorTest {
    
    private Course math;
    private Course cs;
    private Course eng;
    private Student student;
    private Admin admin;
    
    @BeforeEach
    public void setUp() {

        math = new Course(
                "101",
                "Calculus",
                "MATH101",
                3,
                "Dr. Smith",
                "MWF",
                "9:00",
                "Gibson",
                "101",
                null,
                null,
                null,
                null
        );

        cs = new Course(
                "102",
                "CS Intro",
                "CS101",
                3,
                "Dr. Lee",
                "TTH",
                "10:30",
                "Stanley",
                "202",
                null,
                null,
                null,
                null
        );

        eng = new Course(
                "201", 
                "English",
                "ENG201",
                3,
                "Dr. Brown",
                "MWF",
                "11:00",
                "Jones",
                "105",
                null,
                null,
                null,
                null
        );

        student = new Student("student@tulane.edu", "pass");
        admin   = new Admin("admin@admin.com", "admin");
    }
    
    @Test
    public void testMajorGraduationEligibility() {
        // Create a Major with low minimum hours for testing
        Major csMajor = new Major("Computer Science", 6); // 2 courses x 3 = 6 hours
        csMajor.addRequirement(math);
        csMajor.addRequirement(cs);
        
        // Student hasn't taken required courses
        List<Course> noCourses = Collections.emptyList();
        assertFalse(csMajor.canGraduate(noCourses));
        
        // Student has taken only one required course
        List<Course> oneCourse = Arrays.asList(math);
        assertFalse(csMajor.canGraduate(oneCourse));
        
        // Student has taken all required courses
        List<Course> allRequiredCourses = Arrays.asList(math, cs);
        assertTrue(csMajor.canGraduate(allRequiredCourses));
        
        // Student has taken required courses plus extras
        List<Course> extraCourses = Arrays.asList(math, cs, eng);
        assertTrue(csMajor.canGraduate(extraCourses));
    }
    
    @Test
    public void testMinorCreditLimitEnforcement() {
        Minor mathMinor = new Minor("Mathematics", 15);
        mathMinor.addRequirement(math);
        mathMinor.addRequirement(cs);
        mathMinor.addRequirement(eng);
        
        List<Course> withinLimit = Arrays.asList(math, cs, eng); // 9 hours
        
        Course advanced = new Course(
            "401",
            "Advanced",
            "MATH401",
            4,
            "Dr. King",
            "MWF",
            "9:00",
            "Gibson",
            "401",
            null,
            null,
            null,
            null
        );
        Course research = new Course(
            "402",
            "Research",
            "MATH402",
            4,
            "Dr. Queen",
            "TTH",
            "10:30",
            "Stanley",
            "402",
            null,
            null,
            null,
            null
        );
        
        // Add requirements for these courses
        mathMinor.addRequirement(advanced);
        mathMinor.addRequirement(research);
        
        List<Course> overLimit = Arrays.asList(math, cs, eng, advanced, research); // 17 hours
        
        assertTrue(mathMinor.isWithinLimit(withinLimit));
        assertFalse(mathMinor.isWithinLimit(overLimit));
    }
    
    @Test
    public void testStudentProgressTrackingWithMajor() {
        Major engineering = new Major("Engineering");
        engineering.addRequirement(math);
        engineering.addRequirement(cs);
        
        student.setMajor(engineering);
        
        // No courses completed
        assertFalse(student.isOnTrack());
        
        // Some required courses completed
        student.addPastCourse(math);
        assertFalse(student.isOnTrack());
        
        // All required courses completed
        student.addPastCourse(cs);
        assertTrue(student.isOnTrack());
    }
    
    @Test
    public void testStudentMinorCompliance() {
        Minor artMinor = new Minor("Art", 9); // Set max at 9 hours
        // Only require 2 courses to complete the minor
        artMinor.addRequirement(math);  // 3 hours
        artMinor.addRequirement(cs);    // 3 hours
        
        student.setMinor(artMinor);
        
        student.addPastCourse(math); // 3 hours
        assertTrue(student.isMinorWithinLimit());
        
        student.addPastCourse(cs); // 6 hours total (both required) - within 9
        assertTrue(student.isMinorWithinLimit());
        
        // Minor is complete with 6 hours (both requirements met, within limit)
        assertTrue(student.isMinorWithinLimit());
        
        // Test with a minor that has stricter limits
        Minor restrictiveMinor = new Minor("Restricted Art", 5); // Only 5 hour max
        restrictiveMinor.addRequirement(math); // 3 hours
        restrictiveMinor.addRequirement(cs);   // 3 hours (total would be 6, exceeds 5)
        
        Student student2 = new Student("student2@tulane.edu", "pass");
        student2.setMinor(restrictiveMinor);
        student2.addPastCourse(math); // 3 hours - within limit
        assertTrue(student2.isMinorWithinLimit());
        
        student2.addPastCourse(cs); // Would be 6 hours total - exceeds 5 hour limit
        assertFalse(student2.isMinorWithinLimit());
    }
    
    @Test
    public void testAdminCurriculumManagement() {
        Major physics = new Major("Physics");
        Course quantum = new Course(
            "501",
            "Quantum",
            "PHYS501",
            4,
            "Dr. Atom",
            "MWF",
            "1:00",
            "Science",
            "501",
            null,
            null,
            null,
            null
        );
        
        physics.addRequirement(quantum);
        physics.setMinHours(36);
        
        assertTrue(physics.hasRequirement(quantum));
        assertEquals(36, physics.getMinHours());
    }
}
