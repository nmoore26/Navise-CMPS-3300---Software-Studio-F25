package com.example.navisewebsite.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test configuration that provides shared in-memory SQLite databases for testing.
 * Maintains persistent connections to in-memory databases to ensure schema and data persist.
 */
public class TestDatabaseConfig {
    
    // Shared in-memory SQLite database URIs with proper parameters
    private static final String TEST_USERS_DB = "jdbc:sqlite:file:testusers?mode=memory&cache=shared";
    private static final String TEST_STUDENT_INFO_DB = "jdbc:sqlite:file:teststudentinfo?mode=memory&cache=shared";
    private static final String TEST_COURSES_DB = "jdbc:sqlite:file:testcourses?mode=memory&cache=shared";
    
    // Keep persistent connections to the in-memory databases
    // This ensures the databases stay alive and schema persists
    private static Connection persistentUsersConn;
    private static Connection persistentStudentInfoConn;
    private static Connection persistentCoursesConn;
    
    private static boolean initialized = false;

    /**
     * Initialize all in-memory test databases with complete schema.
     * Maintains persistent connections to keep in-memory databases alive.
     * Call this in @BeforeAll or @BeforeEach.
     */
    public static synchronized void initializeTestDatabases() {
        if (initialized) {
            clearAllData(); // Clear data if already initialized
            return;
        }

        try {
            // Create and keep persistent connections to each in-memory database
            // These connections keep the databases alive and accessible
            persistentUsersConn = DriverManager.getConnection(TEST_USERS_DB);
            persistentUsersConn.setAutoCommit(true);
            initializeUsersTable(persistentUsersConn);

            persistentStudentInfoConn = DriverManager.getConnection(TEST_STUDENT_INFO_DB);
            persistentStudentInfoConn.setAutoCommit(true);
            initializeStudentInfoTable(persistentStudentInfoConn);

            persistentCoursesConn = DriverManager.getConnection(TEST_COURSES_DB);
            persistentCoursesConn.setAutoCommit(true);
            initializeCoursesSchema(persistentCoursesConn);

            // Enable test mode in DatabaseUtil with the shared database URIs
            DatabaseUtil.setTestMode(true, TEST_USERS_DB, TEST_STUDENT_INFO_DB, TEST_COURSES_DB);

            initialized = true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize test databases", e);
        }
    }

    /**
     * Clear all data from tables (for test isolation between tests).
     */
    public static synchronized void clearAllData() {
        try {
            if (persistentUsersConn != null && !persistentUsersConn.isClosed()) {
                try (Statement stmt = persistentUsersConn.createStatement()) {
                    stmt.execute("DELETE FROM users");
                }
            }
            
            if (persistentStudentInfoConn != null && !persistentStudentInfoConn.isClosed()) {
                try (Statement stmt = persistentStudentInfoConn.createStatement()) {
                    stmt.execute("DELETE FROM student_info");
                }
            }
            
            if (persistentCoursesConn != null && !persistentCoursesConn.isClosed()) {
                try (Statement stmt = persistentCoursesConn.createStatement()) {
                    stmt.execute("DELETE FROM program_courses");
                    stmt.execute("DELETE FROM ntc_requirements");
                    stmt.execute("DELETE FROM programs");
                    stmt.execute("DELETE FROM courses");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close all test database connections and reset.
     * Call this in @AfterAll.
     */
    public static synchronized void closeTestDatabases() {
        try {
            if (persistentUsersConn != null && !persistentUsersConn.isClosed()) {
                persistentUsersConn.close();
            }
            if (persistentStudentInfoConn != null && !persistentStudentInfoConn.isClosed()) {
                persistentStudentInfoConn.close();
            }
            if (persistentCoursesConn != null && !persistentCoursesConn.isClosed()) {
                persistentCoursesConn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        persistentUsersConn = null;
        persistentStudentInfoConn = null;
        persistentCoursesConn = null;
        initialized = false;
        
        // Disable test mode in DatabaseUtil
        DatabaseUtil.disableTestMode();
    }

    private static void initializeUsersTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "first_name TEXT, " +
                    "last_name TEXT, " +
                    "user_type TEXT)");
        }
    }

    private static void initializeStudentInfoTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
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

    private static void initializeCoursesSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
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

