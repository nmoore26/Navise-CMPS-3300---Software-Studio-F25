package com.example.navisewebsite;

import org.junit.jupiter.api.Test;

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
    }
    
    @Test
    public void testMajorGraduationEligibility() {
        Major csMajor = new Major("Computer Science");
        csMajor.getRequirements().add(math);
        csMajor.getRequirements().add(cs);
        
        // Student hasn't taken required courses
        List<Course> noCourses = Arrays.asList();
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
        List<Course> overLimit = Arrays.asList(math, cs, eng, advanced, research); // 17 hours
        
        assertTrue(mathMinor.isWithinLimit(withinLimit));
        assertFalse(mathMinor.isWithinLimit(overLimit));
    }
    
    @Test
    public void testStudentProgressTrackingWithMajor() {
        Major engineering = new Major("Engineering");
        engineering.getRequirements().add(math);
        engineering.getRequirements().add(cs);
        
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
        Minor artMinor = new Minor("Art", 6);
        student.setMinor(artMinor);
        
        student.addPastCourse(math); // 3 hours
       System.out.println("DEBUG: pastCourses.size() = " + student.getPastCourses().size());
System.out.println("DEBUG: student.getTotalCreditsCompleted() = " + student.getTotalCreditsCompleted());
System.out.println("DEBUG: minor = " + (student.getMinor() == null ? "null" : student.getMinor().getPathName()));
System.out.println("DEBUG: minor.getMaxHours() = " + (student.getMinor() == null ? "null" : student.getMinor().getMaxHours()));
System.out.println("DEBUG: minor.getCompletedHours(past) = " + (student.getMinor() == null ? "null" : student.getMinor().getCompletedHours(student.getPastCourses()))); assertTrue(student.isOnTrack());
        
        student.addPastCourse(cs); // 6 hours total - at limit
        assertTrue(student.isOnTrack());
        
        student.addPastCourse(eng); // 9 hours total - over limit
        assertFalse(student.isOnTrack());
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
        
        physics.getRequirements().add(quantum);
        physics.setMinHours(36);

        assertTrue(physics.getRequirements().contains(quantum));
        assertEquals(36, physics.getMinHours());
    }
}
