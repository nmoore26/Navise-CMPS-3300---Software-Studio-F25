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
        math = new Course(101, "Calculus", "Dr. Smith", 3, "MATH101", "MWF", "9:00", "Gibson", 101, null);
        cs = new Course(102, "CS Intro", "Dr. Lee", 3, "CS101", "TTH", "10:30", "Stanley", 202, null);
        eng = new Course(201, "English", "Dr. Brown", 3, "ENG201", "MWF", "11:00", "Jones", 105, null);
    }
    
    @Test
    public void testPathCourseManagement() {
        Path path = new Path("Science");
        path.add_req(math);
        path.add_req(cs);
        
        assertEquals(2, path.req_count());
        assertTrue(path.has_req(cs));
        
        assertTrue(path.rm_req(math));
        assertEquals(1, path.req_count());
        assertFalse(path.has_req(math));
    }
    
    @Test
    public void testPathCreditHourCalculation() {
        Path path = new Path("Test Path");
        path.add_req(math);
        path.add_req(cs);
        path.add_req(eng);
        
        assertEquals(9, path.total_hours());
    }
    
    @Test
    public void testEmptyPathBehavior() {
        Path path = new Path("Empty Path");
        assertTrue(path.is_empty());
        assertEquals(0, path.total_hours());
    }
    
    @Test
    public void testPathRequirementValidation() {
        Path path = new Path("Validation Test");
        path.add_req(math);
        
        assertTrue(path.has_req(math));
        assertFalse(path.has_req(cs));
    }
    
    @Test
    public void testPathDataIntegrity() {
        Path path = new Path("Data Integrity");
        List<Course> originalCourses = Arrays.asList(math, cs);
        path.set_requirements(originalCourses);
        
        assertEquals(2, path.req_count());
    }
}