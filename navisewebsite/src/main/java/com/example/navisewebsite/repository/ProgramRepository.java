package com.example.navisewebsite.repository;

import java.sql.*;
/*
The purpose of the ProgramRepository class is to manage the storage and retrieval of program (major/minor) data
in the SQLite database. It provides methods to add new programs and link courses to programs.
*/


public class ProgramRepository {

    // Add a new program (Major/Minor) to programs table
    public int addProgram(String programName, String programType) {
        int programId = -1;

        String insertSql = "INSERT OR IGNORE INTO programs(program_name, program_type) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            pstmt.setString(1, programName);
            pstmt.setString(2, programType);
            pstmt.executeUpdate();

            // Retrieve the program_id
            String querySql = "SELECT program_id FROM programs WHERE program_name = ?";
            try (PreparedStatement queryStmt = conn.prepareStatement(querySql)) {
                queryStmt.setString(1, programName);
                ResultSet rs = queryStmt.executeQuery();
                if (rs.next()) {
                    programId = rs.getInt("program_id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return programId;
    }

    // Link a course to a program
    public void addCourseToProgram(int programId, String courseID) {
        String sql = "INSERT INTO program_courses(program_id, course_id) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, programId);
            pstmt.setString(2, courseID);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

