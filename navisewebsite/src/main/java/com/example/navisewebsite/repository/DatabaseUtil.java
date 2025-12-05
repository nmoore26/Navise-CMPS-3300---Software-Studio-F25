

package com.example.navisewebsite.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    // Get the directory where the JAR is running from and ensure we're in the navisewebsite directory
    // If running from root, look for navisewebsite subdirectory; otherwise use current dir
    private static final String DB_DIR = getDbDirectory();
    
    private static String getDbDirectory() {
        // Check for Render's persistent disk mount point first
        java.io.File renderData = new java.io.File("/data");
        if (renderData.exists() && renderData.isDirectory()) {
            return "/data";
        }
        
        // Otherwise use the existing logic for local development
        String userDir = System.getProperty("user.dir");
        java.io.File naviseDir = new java.io.File(userDir, "navisewebsite");
        if (naviseDir.exists() && naviseDir.isDirectory()) {
            return naviseDir.getAbsolutePath();
        }
        return userDir;
    }
    
    private static final String USERS_DB = "jdbc:sqlite:" + DB_DIR + "/users.db";
    private static final String STUDENT_INFO_DB = "jdbc:sqlite:" + DB_DIR + "/student_info.db";
    private static final String COURSES_DB = "jdbc:sqlite:" + DB_DIR + "/courses.db";
    
    // Test mode - uses in-memory database URIs
    private static boolean testMode = false;
    private static String testUsersUri;
    private static String testStudentInfoUri;
    private static String testCoursesUri;

    /**
     * Enable test mode to use in-memory SQLite databases.
     * Call this in test setup with shared in-memory database URIs.
     */
    public static void setTestMode(boolean enabled, 
                                   String usersUri,
                                   String studentInfoUri,
                                   String coursesUri) {
        testMode = enabled;
        testUsersUri = usersUri;
        testStudentInfoUri = studentInfoUri;
        testCoursesUri = coursesUri;
    }

    /**
     * Disable test mode and revert to file-based databases.
     */
    public static void disableTestMode() {
        testMode = false;
        testUsersUri = null;
        testStudentInfoUri = null;
        testCoursesUri = null;
    }

    public static Connection connectUsers() throws SQLException {
        if (testMode && testUsersUri != null) {
            return DriverManager.getConnection(testUsersUri);
        }
        return DriverManager.getConnection(USERS_DB);
    }

    public static Connection connectStudentInfo() throws SQLException {
        if (testMode && testStudentInfoUri != null) {
            return DriverManager.getConnection(testStudentInfoUri);
        }
        return DriverManager.getConnection(STUDENT_INFO_DB);
    }

    public static Connection connectCourses() throws SQLException {
        if (testMode && testCoursesUri != null) {
            return DriverManager.getConnection(testCoursesUri);
        }
        return DriverManager.getConnection(COURSES_DB);
    }

    public static void initializeDatabases() {
        if (testMode) {
            // In test mode, assume databases are already initialized
            return;
        }
        initializeUsers();
        initializeStudentInfo();
        initializeCourses();
    }

    private static void initializeUsers() {
        try (Connection conn = connectUsers(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (user_id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE, password TEXT, first_name TEXT, last_name TEXT, user_type TEXT)");
            System.out.println("✓ users table initialized successfully");
        } catch (SQLException e) {
            System.err.println("✗ Error initializing users table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeStudentInfo() {
        try (Connection conn = connectStudentInfo(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS student_info (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, first_name TEXT, last_name TEXT, major TEXT, minor TEXT, school_year TEXT, past_courses TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(user_id) REFERENCES users(user_id))");
            System.out.println("✓ student_info table initialized successfully");
        } catch (SQLException e) {
            System.err.println("✗ Error initializing student_info table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeCourses() {
        try (Connection conn = connectCourses(); Statement stmt = conn.createStatement()) {
            // Create courses table
            stmt.execute("CREATE TABLE IF NOT EXISTS courses (course_id TEXT PRIMARY KEY, course_name TEXT, course_code TEXT, credit_hours INTEGER, professor TEXT, days TEXT, time TEXT, building TEXT, room TEXT, attributes TEXT, prerequisites TEXT, corequisites TEXT, terms TEXT)");
            
            // Create programs table
            stmt.execute("CREATE TABLE IF NOT EXISTS programs (program_id INTEGER PRIMARY KEY AUTOINCREMENT, program_name TEXT, program_type TEXT)");
            
            // Create program_courses junction table
            stmt.execute("CREATE TABLE IF NOT EXISTS program_courses (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER NOT NULL, course_id TEXT NOT NULL, UNIQUE(program_id, course_id), FOREIGN KEY(program_id) REFERENCES programs(program_id) ON DELETE CASCADE, FOREIGN KEY(course_id) REFERENCES courses(course_id) ON DELETE CASCADE)");
            
            // Create ntc_requirements table
            stmt.execute("CREATE TABLE IF NOT EXISTS ntc_requirements (id INTEGER PRIMARY KEY AUTOINCREMENT, requirement_name TEXT, description TEXT)");
            
            System.out.println("✓ courses, programs, program_courses, and ntc_requirements tables initialized successfully");
            // NOTE: DO NOT populate program_courses here - CourseDataInitializer handles this
            // after loading all courses from the Excel file
        } catch (SQLException e) {
            System.err.println("✗ Error initializing courses schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
