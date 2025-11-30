package com.example.navisewebsite;

import org.junit.jupiter.api.Test;
import com.example.navisewebsite.domain.Course;
import com.example.navisewebsite.domain.Path;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;

public class PathTest {
    
    private Course math;
    private Course cs;
    private Course eng;
    
    @BeforeEach
    public void setUp() {
        math = new Course(
                "101", "Calculus", "MATH101", 3, "Dr. Smith",
                "MWF", "9:00", "Gibson", "101", null, null, null, null
        );

        cs = new Course(
                "102", "CS Intro", "CS101", 3, "Dr. Lee",
                "TTH", "10:30", "Stanley", "202", null, null, null, null
        );

        eng = new Course(
                "201", "English", "ENG201", 3, "Dr. Brown",
                "MWF", "11:00", "Jones", "105", null, null, null, null
        );
    }
    
    @Test
    public void testPathCourseManagement() {
        Path path = new Path("Science");
        path.addRequirement(math);
        path.addRequirement(cs);
        
        assertEquals(2, path.getRequirementCount());
        assertTrue(path.hasRequirement(cs));
        
        assertTrue(path.removeRequirement(math));
        assertEquals(1, path.getRequirementCount());
        assertFalse(path.hasRequirement(math));
    }
    
    @Test
    public void testPathCreditHourCalculation() {
        Path path = new Path("Test Path");
        path.addRequirement(math);
        path.addRequirement(cs);
        path.addRequirement(eng);
        
        assertEquals(9, path.getTotalRequiredHours());
    }
    
    @Test
    public void testEmptyPathBehavior() {
        Path path = new Path("Empty Path");
        assertTrue(path.isEmpty());
        assertEquals(0, path.getTotalRequiredHours());
    }
    
    @Test
    public void testPathRequirementValidation() {
        Path path = new Path("Validation Test");
        path.addRequirement(math);
        
        assertTrue(path.hasRequirement(math));
        assertFalse(path.hasRequirement(cs));
    }
    
    @Test
    public void testPathDataIntegrity() {
        Path path = new Path("Data Integrity");
        List<Course> originalCourses = Arrays.asList(math, cs);
        path.setRequirements(originalCourses);
        
        assertEquals(2, path.getRequirementCount());
    }
}
