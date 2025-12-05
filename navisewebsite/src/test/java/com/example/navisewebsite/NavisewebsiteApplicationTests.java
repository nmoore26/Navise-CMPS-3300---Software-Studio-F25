package com.example.navisewebsite;

import com.example.navisewebsite.domain.Course;
import com.example.navisewebsite.repository.TestDatabaseConfig;
import com.example.navisewebsite.service.AdminCourseService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class NavisewebsiteApplicationTests {

    private AdminCourseService adminCourseService;

    @BeforeAll
    public static void setUpAll() {
        // Initialize in-memory test databases once for all tests
        TestDatabaseConfig.initializeTestDatabases();
    }

    @AfterAll
    public static void tearDownAll() {
        // Close test databases after all tests
        TestDatabaseConfig.closeTestDatabases();
    }

    @BeforeEach
    void setup() {
        // Clear all data before each test for test isolation
        TestDatabaseConfig.clearAllData();
        adminCourseService = new AdminCourseService(
            new com.example.navisewebsite.repository.CourseRepository(),
            new com.example.navisewebsite.repository.ProgramRepository()
        );
    }


    // 1. Add a course to a Major program
    @Test
    void testAddCourseToMajorProgram() {
        Course course = new Course(
            "C101", "Intro to CS", "CS101", 3, "Prof A",
            "MWF", "09:00", "Main", "101",
            Arrays.asList("NTC1"),
            Arrays.asList("None"),
            Arrays.asList("None"),
            Arrays.asList("Fall")
        );
        adminCourseService.add_course(course, "Computer Science", "Major");
        Optional<Course> retrieved = adminCourseService.findById("C101");
        assertTrue(retrieved.isPresent(), "Course should be added and retrievable");
    }

    // 2. Add a course to a Minor program
    @Test
    void testAddCourseToMinorProgram() {
        Course course = new Course(
            "C102", "Intro to Math", "MATH101", 3, "Prof B",
            "TR", "10:00", "Science", "102",
            Arrays.asList("NTC2"),
            Arrays.asList("None"),
            Arrays.asList("None"),
            Arrays.asList("Spring")
        );
        adminCourseService.add_course(course, "Mathematics", "Minor");
        Optional<Course> retrieved = adminCourseService.findById("C102");
        assertTrue(retrieved.isPresent(), "Course should be added to minor program");
    }

    // 3. Remove a course
    @Test
    void testRemoveCourse() {
        Course course = new Course(
            "C103", "Algorithms", "CS103", 4, "Prof C",
            "MWF", "11:00", "Main", "103",
            Arrays.asList("NTC3"),
            Arrays.asList("C101"),
            Arrays.asList("None"),
            Arrays.asList("Fall")
        );
        adminCourseService.add_course(course, "Computer Science", "Major");
        adminCourseService.remove_course(course);
        Optional<Course> retrieved = adminCourseService.findById("C103");
        assertFalse(retrieved.isPresent(), "Course should be removed");
    }

    // 4. Remove a non-existent course
    @Test
    void testRemoveNonExistentCourse() {
        Course course = new Course(
            "C103", "Algorithms", "CS103", 4, "Prof C",
            "MWF", "11:00", "Main", "103",
            Arrays.asList("NTC3"),
            Arrays.asList("C101"),
            Arrays.asList("None"),
            Arrays.asList("Fall")
        );
        assertDoesNotThrow(() -> adminCourseService.remove_course(course), "Removing non-existent course should not throw");
        Optional<Course> retrieved = adminCourseService.findById("C999");
        assertFalse(retrieved.isPresent(), "No course should exist");
    }

    // 5. Add multiple courses
    @Test
    void testAddMultipleCourses() {
        Course c1 = new Course(
            "C201", "Math1", "MATH101", 3, "Prof X",
            "MWF", "08:00", "Science", "201",
            Arrays.asList("NTC1"),
            Arrays.asList("None"),
            Arrays.asList("None"),
            Arrays.asList("Fall")
        );
        Course c2 = new Course(
            "C202", "Math2", "MATH102", 3, "Prof Y",
            "TR", "09:00", "Science", "202",
            Arrays.asList("NTC2"),
            Arrays.asList("C201"),
            Arrays.asList("None"),
            Arrays.asList("Spring")
        );

        adminCourseService.add_course(c1, "Mathematics", "Major");
        adminCourseService.add_course(c2, "Mathematics", "Major");

        assertTrue(adminCourseService.findById("C201").isPresent());
        assertTrue(adminCourseService.findById("C202").isPresent());
    }

    // 6. Course with multiple attributes
    @Test
    void testCourseWithMultipleAttributes() {
        Course course = new Course(
            "C401", "Chemistry", "CHEM101", 3, "Prof Chem",
            "TR", "14:00", "Lab", "401",
            Arrays.asList("LabRequired", "SafetyTraining"),
            Arrays.asList("None"),
            Arrays.asList("None"),
            Arrays.asList("Spring")
        );
        adminCourseService.add_course(course, "Chemistry", "Major");

        Optional<Course> retrieved = adminCourseService.findById("C401");
        assertEquals(2, retrieved.get().get_attribute().size());
    }

    // 7. Remove all courses
    @Test
    void testRemoveAllCourses() {
        Course c1 = new Course(
            "C501", "Art1", "ART101", 3, "Prof A",
            "MWF", "08:00", "ArtBuilding", "501",
            Arrays.asList("NTC1"),
            Arrays.asList("None"),
            Arrays.asList("None"),
            Arrays.asList("Fall")
        );
        Course c2 = new Course(
            "C502", "Art2", "ART102", 3, "Prof B",
            "TR", "09:00", "ArtBuilding", "502",
            Arrays.asList("NTC2"),
            Arrays.asList("None"),
            Arrays.asList("None"),
            Arrays.asList("Fall")
        );

        adminCourseService.add_course(c1, "ArtProgram", "Major");
        adminCourseService.add_course(c2, "ArtProgram", "Major");

        adminCourseService.remove_course(c1);
        adminCourseService.remove_course(c2);

        assertFalse(adminCourseService.findById("C501").isPresent());
        assertFalse(adminCourseService.findById("C502").isPresent());
    }

    // 8. Retrieve a course by ID
    @Test
    void testGetCourseByID() {
        Course course = new Course(
            "C102", "Data Structures", "CS102", 4, "Prof B",
            "TR", "10:00", "Main", "102",
            Arrays.asList("NTC2"),
            Arrays.asList("C101"),
            Arrays.asList("None"),
            Arrays.asList("Spring")
        );
        adminCourseService.add_course(course, "CSProgram", "Major");
        Optional<Course> retrieved = adminCourseService.findById("C102");
        assertTrue(retrieved.isPresent(), "Course should be found by ID");
    }

    // 9. Retrieve non-existent course by ID
    @Test
    void testGetCourseByIDNotFound() {
        Optional<Course> retrieved = adminCourseService.findById("UNKNOWN");
        assertFalse(retrieved.isPresent(), "No course should be found for unknown ID");
    }
}
