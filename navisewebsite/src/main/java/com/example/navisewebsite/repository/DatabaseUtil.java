package com.example.navisewebsite.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/*
The purpose of the DatabaseUtil class is to provide utility methods for connecting to the SQLite database
and initializing the database schema by creating necessary tables if they do not already exist.
*/
public class DatabaseUtil {

    private static final String DB_URL = "jdbc:sqlite:courses.db";

    // Connect to the SQLite DB
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Initialize database tables
    public static void initializeDatabase() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            // ---- Courses table ----
            String createCourses = """
                CREATE TABLE IF NOT EXISTS courses (
                    course_id TEXT PRIMARY KEY,
                    course_name TEXT,
                    course_code TEXT,
                    credit_hours INTEGER,
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
            """;

            // ---- Programs table ----
            String createPrograms = """
                CREATE TABLE IF NOT EXISTS programs (
                    program_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    program_name TEXT,
                    program_type TEXT
                );
            """;

            // ---- Program-Course linking table ----
            String createProgramCourses = """
                CREATE TABLE IF NOT EXISTS program_courses (
                    program_id INTEGER,
                    course_id TEXT,
                    FOREIGN KEY(program_id) REFERENCES programs(program_id),
                    FOREIGN KEY(course_id) REFERENCES courses(course_id)
                );
            """;

            // ---- NTC Requirements ----
            String createNTC = """
                CREATE TABLE IF NOT EXISTS ntc_requirements (
                    ntc_requirement TEXT,
                    num_classes INTEGER
                );
            """;

            stmt.execute(createCourses);
            stmt.execute(createPrograms);
            stmt.execute(createProgramCourses);
            stmt.execute(createNTC);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
