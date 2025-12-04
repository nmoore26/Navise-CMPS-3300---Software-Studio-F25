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

    // PostgreSQL does not support in-memory test DBs; use a separate schema or database for tests if needed.
}
