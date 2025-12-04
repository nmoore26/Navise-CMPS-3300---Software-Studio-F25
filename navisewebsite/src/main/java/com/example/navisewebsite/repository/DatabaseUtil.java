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

    // --- Production DBs ---
    // Use paths relative to application working dir (navisewebsite)
    // Separate DB files: courses.db for course/program data; users.db for user data
    // PostgreSQL connection details (update as needed)
    private static final String DB_URL = "jdbc:postgresql://tramway.proxy.rlwy.net:45308/railway";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "ECRzrnCljFHfGvFVvPZmJVlSuCfsCnLp";

    // Clear the users table for test isolation between test methods.
    public static void clearUsersTable() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Universal connect method ---
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    public static Connection connectUsers() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    public static Connection connectStudentInfo() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // =====================================================================
    // PRODUCTION DATABASE INITIALIZER
    // =====================================================================
    public static void initializeDatabase() {
        // Initialize all tables in PostgreSQL
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            String createCourses = """
                CREATE TABLE IF NOT EXISTS courses (
                    course_id VARCHAR PRIMARY KEY,
                    course_name VARCHAR,
                    course_code VARCHAR,
                    credit_hours INTEGER,
                    professor VARCHAR,
                    days VARCHAR,
                    time VARCHAR,
                    building VARCHAR,
                    room VARCHAR,
                    attributes VARCHAR,
                    prerequisites VARCHAR,
                    corequisites VARCHAR,
                    terms VARCHAR
                );
            """;
            String createPrograms = """
                CREATE TABLE IF NOT EXISTS programs (
                    program_id SERIAL PRIMARY KEY,
                    program_name VARCHAR,
                    program_type VARCHAR
                );
            """;
            String createProgramCourses = """
                CREATE TABLE IF NOT EXISTS program_courses (
                    program_id INTEGER,
                    course_id VARCHAR,
                    FOREIGN KEY(program_id) REFERENCES programs(program_id),
                    FOREIGN KEY(course_id) REFERENCES courses(course_id)
                );
            """;
            String createNTC = """
                CREATE TABLE IF NOT EXISTS ntc_requirements (
                    ntc_requirement VARCHAR,
                    num_classes INTEGER
                );
            """;
            String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id SERIAL PRIMARY KEY,
                    email VARCHAR NOT NULL UNIQUE,
                    password VARCHAR NOT NULL,
                    first_name VARCHAR,
                    last_name VARCHAR,
                    user_type VARCHAR NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """;
            String createStudentInfo = """
                CREATE TABLE IF NOT EXISTS student_info (
                    id SERIAL PRIMARY KEY,
                    user_id INTEGER NOT NULL,
                    first_name VARCHAR,
                    last_name VARCHAR,
                    major VARCHAR,
                    minor VARCHAR,
                    school_year VARCHAR,
                    past_courses VARCHAR,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(user_id) REFERENCES users(user_id)
                );
            """;
            stmt.execute(createCourses);
            stmt.execute(createPrograms);
            stmt.execute(createProgramCourses);
            stmt.execute(createNTC);
            stmt.execute(createUsers);
            stmt.execute(createStudentInfo);
            System.out.println("PostgreSQL database schema initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // PostgreSQL does not support in-memory test DBs; use a separate schema or database for tests if needed.
}
