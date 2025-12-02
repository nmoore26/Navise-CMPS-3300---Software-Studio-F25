package com.example.navisewebsite.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
/*
The purpose of the DatabaseUtil class is to manage database connections and initialize
the SQLite database schema for the application. It provides methods to connect to either
the production or test database and to create necessary tables if they do not exist.
*/

public class DatabaseUtil {

    // --- Production DB ---
    // Use absolute path to ensure the database file is created in the navisewebsite directory
    private static final String PROD_DB_URL = "jdbc:sqlite:courses.db";

    // --- Test DB (shared in-memory) ---
    // Use a file: URI with mode=memory and cache=shared so multiple connections
    // within the same process see the same in-memory database.
    private static final String TEST_DB_URL = "jdbc:sqlite:file:memdb?mode=memory&cache=shared";
    private static boolean useTestDB = false;
    private static boolean testDatabaseInitialized = false;
    // Keep a single persistent "keeper" connection open for the in-memory
    // test DB so it persists for the duration of the test run.
    private static Connection testKeeper = null;

    // --- Switching flag for tests ---
    public static void useTestDatabase() {
        useTestDB = true;
        // Ensure the shared test connection is opened and kept alive
        try {
            if (testKeeper == null || testKeeper.isClosed()) {
                testKeeper = DriverManager.getConnection(TEST_DB_URL);
                // Initialize the test database only once per JVM
                if (!testDatabaseInitialized) {
                    initializeDatabaseTests();
                    testDatabaseInitialized = true;
                }
                // Register shutdown hook to close the keeper when JVM exits
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    closeTestDatabase();
                }));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the persistent test keeper connection if open. Safe to call multiple times.
     */
    public static void closeTestDatabase() {
        try {
            if (testKeeper != null && !testKeeper.isClosed()) {
                testKeeper.close();
                testKeeper = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear the users table for test isolation between test methods.
     */
    public static void clearUsersTable() {
        if (useTestDB) {
            try (Connection conn = connect();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM users");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // --- Universal connect method ---
    public static Connection connect() throws SQLException {
        if (useTestDB) {
            // Return a fresh connection to the shared in-memory database.
            return DriverManager.getConnection(TEST_DB_URL);
        }
        return DriverManager.getConnection(PROD_DB_URL);
    }

    // =====================================================================
    // PRODUCTION DATABASE INITIALIZER
    // =====================================================================
    public static void initializeDatabase() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    user_type TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """;

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

            String createPrograms = """
                CREATE TABLE IF NOT EXISTS programs (
                    program_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    program_name TEXT,
                    program_type TEXT
                );
            """;

            String createProgramCourses = """
                CREATE TABLE IF NOT EXISTS program_courses (
                    program_id INTEGER,
                    course_id TEXT,
                    FOREIGN KEY(program_id) REFERENCES programs(program_id),
                    FOREIGN KEY(course_id) REFERENCES courses(course_id)
                );
            """;

            String createNTC = """
                CREATE TABLE IF NOT EXISTS ntc_requirements (
                    ntc_requirement TEXT,
                    num_classes INTEGER
                );
            """;

            stmt.execute(createUsers);
            stmt.execute(createCourses);
            stmt.execute(createPrograms);
            stmt.execute(createProgramCourses);
            stmt.execute(createNTC);

            System.out.println("Production database initialized.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    // TEST DATABASE INITIALIZER — YOU REQUESTED THIS STAY SEPARATE
    // =====================================================================
    public static void initializeDatabaseTests() {
        // Use the shared test connection and do NOT close it here so the
        // in-memory database remains available across the test lifecycle.
        try {
            Connection conn_t = connect();
            try (Statement stmt = conn_t.createStatement()) {

            String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    user_type TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """;

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

            String createPrograms = """
                CREATE TABLE IF NOT EXISTS programs (
                    program_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    program_name TEXT,
                    program_type TEXT
                );
            """;

            String createProgramCourses = """
                CREATE TABLE IF NOT EXISTS program_courses (
                    program_id INTEGER,
                    course_id TEXT,
                    FOREIGN KEY(program_id) REFERENCES programs(program_id),
                    FOREIGN KEY(course_id) REFERENCES courses(course_id)
                );
            """;

            String createNTC = """
                CREATE TABLE IF NOT EXISTS ntc_requirements (
                    ntc_requirement TEXT,
                    num_classes INTEGER
                );
            """;

                stmt.execute(createUsers);
                stmt.execute(createCourses);
                stmt.execute(createPrograms);
                stmt.execute(createProgramCourses);
                stmt.execute(createNTC);

                System.out.println("In-memory TEST database initialized.");
            } catch (SQLException e) {
                if (!e.getMessage().contains("already exists")) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
