package com.example.navisewebsite.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:postgresql://tramway.proxy.rlwy.net:45308/railway";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "ECRzrnCljFHfGvFVvPZmJVlSuCfsCnLp";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static Connection connectUsers() throws SQLException {
        return connect();
    }

    public static Connection connectStudentInfo() throws SQLException {
        return connect();
    }

    public static void clearUsersTable() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
