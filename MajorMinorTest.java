package com.example.navisewebsite.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class MajorMinorTest {
    
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
        Major csMajor = new Major("Computer Science", 30);
        List<Course> completedCourses = Arrays.asList(math, cs, eng);
        
        assertFalse(csMajor.can_graduate(completedCourses));
        
        Course algo = new Course(301, "Algorithms", "Dr. White", 4, "CS301", "MWF", "1:00", "Gibson", 301, null);
        Course systems = new Course(302, "Systems", "Dr. Black", 4, "CS302", "TTH", "2:30", "Stanley", 305, null);
        Course theory = new Course(303, "Theory", "Dr. Green", 4, "CS303", "MWF", "3:00", "Jones", 201, null);
        
        List<Course> sufficientCourses = Arrays.asList(math, cs, eng, algo, systems, theory);
        assertTrue(csMajor.can_graduate(sufficientCourses));
    }
    
    @Test
    public void testMinorCreditLimitEnforcement() {
        Minor mathMinor = new Minor("Mathematics", 15);
        List<Course> withinLimit = Arrays.asList(math, cs, eng);
        
        Course advanced = new Course(401, "Advanced", "Dr. King", 4, "MATH401", "MWF", "9:00", "Gibson", 401, null);
        Course research = new Course(402, "Research", "Dr. Queen", 4, "MATH402", "TTH", "10:30", "Stanley", 402, null);
        List<Course> overLimit = Arrays.asList(math, cs, eng, advanced, research);
        
        assertTrue(mathMinor.within_limit(withinLimit));
        assertFalse(mathMinor.within_limit(overLimit));
    }
    
    @Test
    public void testStudentProgressTrackingWithMajor() {
        Major engineering = new Major("Engineering", 12);
        student.set_major(engineering);
        
        student.add_past(math);
        student.add_past(cs);
        assertFalse(student.on_track());
        
        student.add_past(eng);
        assertTrue(student.on_track());
    }
    
    @Test
    public void testStudentMinorCompliance() {
        Minor artMinor = new Minor("Art", 6);
        student.set_minor(artMinor);
        
        student.add_past(math);
        assertTrue(student.minor_ok());
        
        student.add_past(cs);
        student.add_past(eng);
        assertFalse(student.minor_ok());
    }
    
    @Test
    public void testAdminCurriculumManagement() {
        Major physics = new Major("Physics");
        Course quantum = new Course(501, "Quantum", "Dr. Atom", 4, "PHYS501", "MWF", "1:00", "Science", 501, null);
        
        assertTrue(admin.add_major_req(physics, quantum));
        assertTrue(admin.set_major_hours(physics, 36));
        assertTrue(physics.has_req(quantum));
    }
}