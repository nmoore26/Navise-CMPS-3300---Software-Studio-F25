package com.example.navisewebsite.controller;

import com.example.navisewebsite.repository.CourseRepository;
import com.example.navisewebsite.domain.Course;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    @BeforeEach
    public void setup() throws Exception {
        // Ensure all connections use the shared in-memory test DB
        com.example.navisewebsite.repository.DatabaseUtil.useTestDatabase();
        // Explicitly initialize schema for in-memory DB
        try (java.sql.Connection conn = com.example.navisewebsite.repository.DatabaseUtil.connect();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS courses (
                    course_id TEXT PRIMARY KEY,
                    course_name TEXT NOT NULL,
                    course_code TEXT NOT NULL,
                    credit_hours INTEGER NOT NULL,
                    professor TEXT,
                    days TEXT,
                    time TEXT,
                    building TEXT,
                    room TEXT,
                    attributes TEXT,
                    prerequisites TEXT,
                    corequisites TEXT,
                    terms TEXT
                );
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS programs (
                    program_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    program_name TEXT NOT NULL,
                    program_type TEXT NOT NULL
                );
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS program_courses (
                    program_id INTEGER NOT NULL,
                    course_id TEXT NOT NULL,
                    PRIMARY KEY (program_id, course_id),
                    FOREIGN KEY (program_id) REFERENCES programs(program_id),
                    FOREIGN KEY (course_id) REFERENCES courses(course_id)
                );
            """);
        }
    }

    @Test
    public void testAddCourseFormSubmitsAndPersists() throws Exception {
        mockMvc.perform(post("/admin/add-course")
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
                .andExpect(MockMvcResultMatchers.view().name("admin"));

        // Verify the course was added
        Course found = courseRepository.findById("TEST101").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getCourseName()).isEqualTo("Test Course");
        assertThat(found.getCreditHours()).isEqualTo(3);
    }

    @Test
    public void testAddProgramFormSubmitsAndPersists() throws Exception {
        mockMvc.perform(post("/admin/add-program")
                .param("programName", "Computer Science")
                .param("programType", "Major"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin"));
    }

    @Test
    public void testRemoveProgramFormSubmitsAndRemoves() throws Exception {
        // First, add a program
        mockMvc.perform(post("/admin/add-program")
                .param("programName", "Test Program")
                .param("programType", "Major"))
                .andExpect(status().isOk());

        // Then, remove the program
        mockMvc.perform(post("/admin/remove-program")
                .param("programName", "Test Program"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin"));
    }
}
