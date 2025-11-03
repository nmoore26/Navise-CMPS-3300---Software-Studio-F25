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
import java.util.List;

class MajorMinorTest {
    
    private Course math;
    private Course cs;
    private Course eng;
    private Student student;
    private Admin admin;
    
@BeforeEach
    public void setUp() {
        math = new Course(101, "Calculus", "Dr. Smith", 3, "MATH101", "MWF", "9:00", "Gibson", 101, null);
        cs = new Course(102, "CS Intro", "Dr. Lee", 3, "CS101", "TTH", "10:30", "Stanley", 202, null);
        eng = new Course(201, "English", "Dr. Brown", 3, "ENG201", "MWF", "11:00", "Jones", 105, null);
        student = new Student("student@tulane.edu", "pass");
        admin = new Admin("admin@admin.com", "admin");
    }
    
    @Test
    public void testMajorGraduationEligibility() {
        Major csMajor = new Major("Computer Science");
        csMajor.add_req(math);
        csMajor.add_req(cs);
        
        // Student hasn't taken required courses
        List<Course> noCourses = Arrays.asList();
        assertFalse(csMajor.can_graduate(noCourses));
        
        // Student has taken only one required course
        List<Course> oneCourse = Arrays.asList(math);
        assertFalse(csMajor.can_graduate(oneCourse));
        
        // Student has taken all required courses
        List<Course> allRequiredCourses = Arrays.asList(math, cs);
        assertTrue(csMajor.can_graduate(allRequiredCourses));
        
        // Student has taken required courses plus extras
        List<Course> extraCourses = Arrays.asList(math, cs, eng);
        assertTrue(csMajor.can_graduate(extraCourses));
    }
    
    @Test
    public void testMinorCreditLimitEnforcement() {
        Minor mathMinor = new Minor("Mathematics", 15);
        List<Course> withinLimit = Arrays.asList(math, cs, eng); // 9 hours
        
        Course advanced = new Course(401, "Advanced", "Dr. King", 4, "MATH401", "MWF", "9:00", "Gibson", 401, null);
        Course research = new Course(402, "Research", "Dr. Queen", 4, "MATH402", "TTH", "10:30", "Stanley", 402, null);
        List<Course> overLimit = Arrays.asList(math, cs, eng, advanced, research); // 17 hours
        
        assertTrue(mathMinor.within_limit(withinLimit));
        assertFalse(mathMinor.within_limit(overLimit));
    }
    
    @Test
    public void testStudentProgressTrackingWithMajor() {
        Major engineering = new Major("Engineering");
        engineering.add_req(math);
        engineering.add_req(cs);
        
        student.set_major(engineering);
        
        // No courses completed
        assertFalse(student.on_track());
        
        // Some required courses completed
        student.add_past(math);
        assertFalse(student.on_track());
        
        // All required courses completed
        student.add_past(cs);
        assertTrue(student.on_track());
    }
    
    @Test
    public void testStudentMinorCompliance() {
        Minor artMinor = new Minor("Art", 6);
        student.set_minor(artMinor);
        
        student.add_past(math); // 3 hours
        assertTrue(student.minor_ok());
        
        student.add_past(cs); // 6 hours total - at limit
        assertTrue(student.minor_ok());
        
        student.add_past(eng); // 9 hours total - over limit
        assertFalse(student.minor_ok());
    }
    
    @Test
    public void testAdminCurriculumManagement() {
        Major physics = new Major("Physics");
        Course quantum = new Course(501, "Quantum", "Dr. Atom", 4, "PHYS501", "MWF", "1:00", "Science", 501, null);
        
        assertTrue(admin.add_major_req(physics, quantum));
        assertTrue(admin.set_major_hours(physics, 36));
        assertTrue(physics.has_req(quantum));
        assertEquals(36, physics.get_min_hours());
    }
}