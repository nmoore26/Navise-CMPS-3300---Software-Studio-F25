package com.example.navisewebsite;

import com.example.navisewebsite.domain.Course;
import com.example.navisewebsite.service.AdminCourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NaviseWebsiteApplicationTests {

    private AdminCourseService adminCourseService;

    @BeforeEach
    void setup() {
		try {
        	adminCourseService = new AdminCourseService();
		} catch (Exception e) {
			fail("Failed to initialize AdminCourseService: " + e.getMessage());}
    }

    // 1. Test adding a course creates the sheet if missing
    @Test
    void testAddCourseCreatesSheet() {
        Course course = new Course(
    	"C101", "Intro to CS", "CS101", 3, "Prof A",
    	"MWF", "09:00", "Main", "101",
    	Arrays.asList("NTC1"),
    	Arrays.asList("None"),        // prerequisites as List<String>
    	Arrays.asList("None"),        // corequisites as List<String>
    	Arrays.asList("Fall")         // term_offered as List<String>
);
        adminCourseService.add_course(course, "FallCourses");
        Optional<Course> retrieved = adminCourseService.findById("C101", "FallCourses");
        assertTrue(retrieved.isPresent(), "Course should be added and retrievable");
    }

    /// 2. Test removing a course by ID
    @Test
    void testRemoveCourse() {
        Course course = new Course(
                "C103", "Algorithms", "CS103", 4, "Prof C",
                "MWF", "11:00", "Main", "103",
                Arrays.asList("NTC3"),
                Arrays.asList("CS102"),
                Arrays.asList("None"),
                Arrays.asList("Fall")
        );
        adminCourseService.add_course(course, "FallCourses");
        adminCourseService.remove_course("C103", "FallCourses");
        Optional<Course> retrieved = adminCourseService.findById("C103", "FallCourses");
        assertFalse(retrieved.isPresent(), "Course should be removed");
    }

    // 3. Test removing a course from a sheet that does not exist
    @Test
    void testRemoveCourseFromNonexistentSheet() {
        assertDoesNotThrow(() -> adminCourseService.remove_course("C999", "NonExistent"));
        Optional<Course> retrieved = adminCourseService.findById("C999", "NonExistent");
        assertFalse(retrieved.isPresent(), "No course should exist");
    }

    // 4. Test adding multiple courses to the same sheet
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
        adminCourseService.add_course(c1, "MathSheet");
        adminCourseService.add_course(c2, "MathSheet");

        assertTrue(adminCourseService.findById("C201", "MathSheet").isPresent());
        assertTrue(adminCourseService.findById("C202", "MathSheet").isPresent());
    }

    // 5. Adding a course with multiple attributes
    @Test
    void testCourseAttributes() {
        Course course = new Course(
                "C401", "Chemistry", "CHEM101", 3, "Prof Chem",
                "TR", "14:00", "Lab", "401",
                Arrays.asList("LabRequired", "SafetyTraining"),
                Arrays.asList("None"),
                Arrays.asList("None"),
                Arrays.asList("Spring")
        );
        adminCourseService.add_course(course, "ChemSheet");

        Optional<Course> retrieved = adminCourseService.findById("C401", "ChemSheet");
        assertEquals(2, retrieved.get().get_attribute().size());
    }

    // 6. Courses added to different sheets do not mix
    @Test
    void testCoursesSeparateSheets() {
        Course course1 = new Course(
                "C501", "History", "HIST101", 3, "Prof H",
                "MWF", "08:00", "Building1", "501",
                Arrays.asList("NTC1"),
                Arrays.asList("None"),
                Arrays.asList("None"),
                Arrays.asList("Fall")
        );
        Course course2 = new Course(
                "C502", "History2", "HIST102", 3, "Prof I",
                "TR", "09:00", "Building2", "502",
                Arrays.asList("NTC2"),
                Arrays.asList("None"),
                Arrays.asList("None"),
                Arrays.asList("Spring")
        );

        adminCourseService.add_course(course1, "FallSheet");
        adminCourseService.add_course(course2, "SpringSheet");

        assertTrue(adminCourseService.findById("C501", "FallSheet").isPresent());
        assertFalse(adminCourseService.findById("C501", "SpringSheet").isPresent());
    }


    // 7. Removing a non-existent course does not affect others
    @Test
    void testRemoveNonExistentCourse() {
        Course course = new Course(
                "C601", "Biology", "BIO101", 4, "Prof Bio",
                "MWF", "10:00", "BioBuilding", "601",
                Arrays.asList("Lab"),
                Arrays.asList("None"),
                Arrays.asList("None"),
                Arrays.asList("Fall")
        );
        adminCourseService.add_course(course, "BioSheet");
        adminCourseService.remove_course("UNKNOWN", "BioSheet");

        Optional<Course> retrieved = adminCourseService.findById("C601", "BioSheet");
        assertTrue(retrieved.isPresent(), "Existing course should remain");
      	assertFalse(adminCourseService.findById("C501","SpringSheet").isPresent());
	}



    // 8. Removing all courses from a sheet
    @Test
    void testRemoveAllCourses() {
        Course c1 = new Course(
                "C701", "Art1", "ART101", 3, "Prof A",
                "MWF", "08:00", "ArtBuilding", "701",
                Arrays.asList("NTC1"),
                Arrays.asList("None"),
                Arrays.asList("None"),
                Arrays.asList("Fall")
        );
        Course c2 = new Course(
                "C702", "Art2", "ART102", 3, "Prof B",
                "TR", "09:00", "ArtBuilding", "702",
                Arrays.asList("NTC2"),
                Arrays.asList("None"),
                Arrays.asList("None"),
                Arrays.asList("Fall")
        );

        adminCourseService.add_course(c1, "ArtSheet");
        adminCourseService.add_course(c2, "ArtSheet");

        adminCourseService.remove_course("C701", "ArtSheet");
        adminCourseService.remove_course("C702", "ArtSheet");

        assertFalse(adminCourseService.findById("C701", "ArtSheet").isPresent());
        assertFalse(adminCourseService.findById("C702", "ArtSheet").isPresent());
    }
	// 9. Test getting a course by ID
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
		adminCourseService.add_course(course, "SpringCourses");
		Optional<Course> retrieved = adminCourseService.findById("C102", "SpringCourses");
		assertTrue(retrieved.isPresent(), "Course should be found by ID");
	}

	// 10. Test getCourseByID returns empty if course not found
	@Test
	void testGetCourseByIDNotFound() {
		Optional<Course> result = adminCourseService.findById("UNKNOWN", "FallCourses");
		assertFalse(result.isPresent(), "No course should be found for unknown ID");
	}



}
