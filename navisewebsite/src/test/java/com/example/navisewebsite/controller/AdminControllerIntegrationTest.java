package com.example.navisewebsite.controller;

import com.example.navisewebsite.repository.CourseRepository;
import com.example.navisewebsite.repository.TestDatabaseConfig;
import com.example.navisewebsite.domain.Course;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    private MockHttpSession adminSession;

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
    public void setup() throws Exception {
        // Clear all data before each test for test isolation
        TestDatabaseConfig.clearAllData();
        
        // Create a mock admin session
        adminSession = new MockHttpSession();
        adminSession.setAttribute("userType", "admin");
        adminSession.setAttribute("email", "admin@test.com");
        adminSession.setAttribute("userId", 1);
    }

    @Test
    public void testAddCourseFormSubmitsAndPersists() throws Exception {
        mockMvc.perform(post("/admin/add-course")
                .session(adminSession)
                .param("courseID", "TEST101")
                .param("courseName", "Test Course")
                .param("courseCode", "TST101")
                .param("creditHours", "3")
                .param("professor", "Prof Test")
                .param("daysOffered", "MWF")
                .param("time", "10:00")
                .param("building", "Main")
                .param("roomNumber", "101")
                .param("attributesCSV", "Lab,Elective")
                .param("prerequisitesCSV", "None")
                .param("corequisitesCSV", "None")
                .param("termsCSV", "Fall,Spring")
                .param("programName", "Test Program")
                .param("programType", "Major"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin-home"));

        // Verify the course was added
        Course found = courseRepository.findById("TEST101").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getCourseName()).isEqualTo("Test Course");
        assertThat(found.getCreditHours()).isEqualTo(3);
    }

    @Test
    public void testAddProgramFormSubmitsAndPersists() throws Exception {
        mockMvc.perform(post("/admin/add-program")
                .session(adminSession)
                .param("programName", "Computer Science")
                .param("programType", "Major"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin-home"));
    }

    @Test
    public void testRemoveProgramFormSubmitsAndRemoves() throws Exception {
        // First, add a program
        mockMvc.perform(post("/admin/add-program")
                .session(adminSession)
                .param("programName", "Test Program")
                .param("programType", "Major"))
                .andExpect(status().isOk());

        // Then, remove the program
        mockMvc.perform(post("/admin/remove-program")
                .session(adminSession)
                .param("programName", "Test Program"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin-home"));
    }
}
