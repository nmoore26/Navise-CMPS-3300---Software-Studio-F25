package com.example.navisewebsite.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test-specific database utility that uses in-memory SQLite databases.
 * Each test gets fresh in-memory databases to ensure isolation.
 */
public class TestDatabaseUtil {
    // In-memory SQLite database URLs
    private static final String USERS_DB = "jdbc:sqlite::memory:";
    private static final String STUDENT_INFO_DB = "jdbc:sqlite::memory:";
    private static final String COURSES_DB = "jdbc:sqlite::memory:";

    // Keep connections alive for the duration of tests
    private static Connection usersConnection;
    private static Connection studentInfoConnection;
    private static Connection coursesConnection;

    /**
     * Initialize in-memory test databases with all required tables.
     * Must be called once per test class or before each test.
     */
    public static void initializeTestDatabases() {
        try {
            // Create connections to in-memory databases
            usersConnection = DriverManager.getConnection(USERS_DB);
            studentInfoConnection = DriverManager.getConnection(STUDENT_INFO_DB);
            coursesConnection = DriverManager.getConnection(COURSES_DB);

            // Initialize schema
            initializeUsersSchema();
            initializeStudentInfoSchema();
            initializeCoursesSchema();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize test databases", e);
        }
    }

    /**
     * Close all test database connections.
     * Call this in tearDown or afterAll methods.
     */
    public static void closeTestDatabases() {
        try {
            if (usersConnection != null && !usersConnection.isClosed()) {
                usersConnection.close();
            }
            if (studentInfoConnection != null && !studentInfoConnection.isClosed()) {
                studentInfoConnection.close();
            }
            if (coursesConnection != null && !coursesConnection.isClosed()) {
                coursesConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get connection to in-memory users database.
     */
    public static Connection getTestUsersConnection() throws SQLException {
        if (usersConnection == null || usersConnection.isClosed()) {
            usersConnection = DriverManager.getConnection(USERS_DB);
        }
        return usersConnection;
    }

    /**
     * Get connection to in-memory student_info database.
     */
    public static Connection getTestStudentInfoConnection() throws SQLException {
        if (studentInfoConnection == null || studentInfoConnection.isClosed()) {
            studentInfoConnection = DriverManager.getConnection(STUDENT_INFO_DB);
        }
        return studentInfoConnection;
    }

    /**
     * Get connection to in-memory courses database.
     */
    public static Connection getTestCoursesConnection() throws SQLException {
        if (coursesConnection == null || coursesConnection.isClosed()) {
            coursesConnection = DriverManager.getConnection(COURSES_DB);
        }
        return coursesConnection;
    }

    private static void initializeUsersSchema() throws SQLException {
        try (Statement stmt = usersConnection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "first_name TEXT, " +
                    "last_name TEXT, " +
                    "user_type TEXT)");
        }
    }

    private static void initializeStudentInfoSchema() throws SQLException {
        try (Statement stmt = studentInfoConnection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS student_info (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "first_name TEXT, " +
                    "last_name TEXT, " +
                    "major TEXT, " +
                    "minor TEXT, " +
                    "school_year TEXT, " +
                    "past_courses TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id))");
        }
    }

    private static void initializeCoursesSchema() throws SQLException {
        try (Statement stmt = coursesConnection.createStatement()) {
            // Create courses table
            stmt.execute("CREATE TABLE IF NOT EXISTS courses (" +
                    "course_id TEXT PRIMARY KEY, " +
                    "course_name TEXT NOT NULL, " +
                    "course_code TEXT NOT NULL, " +
                    "credit_hours INTEGER NOT NULL, " +
                    "professor TEXT, " +
                    "days TEXT, " +
                    "time TEXT, " +
                    "building TEXT, " +
                    "room TEXT, " +
                    "attributes TEXT, " +
                    "prerequisites TEXT, " +
                    "corequisites TEXT, " +
                    "terms TEXT)");

            // Create programs table
            stmt.execute("CREATE TABLE IF NOT EXISTS programs (" +
                    "program_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "program_name TEXT NOT NULL, " +
                    "program_type TEXT NOT NULL)");

            // Create program_courses junction table
            stmt.execute("CREATE TABLE IF NOT EXISTS program_courses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "program_id INTEGER NOT NULL, " +
                    "course_id TEXT NOT NULL, " +
                    "FOREIGN KEY(program_id) REFERENCES programs(program_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(course_id) REFERENCES courses(course_id) ON DELETE CASCADE)");

            // Create ntc_requirements table
            stmt.execute("CREATE TABLE IF NOT EXISTS ntc_requirements (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "requirement_name TEXT, " +
                    "description TEXT)");
        }
    }
}
