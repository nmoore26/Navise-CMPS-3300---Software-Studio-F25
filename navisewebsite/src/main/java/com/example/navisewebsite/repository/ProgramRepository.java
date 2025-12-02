package com.example.navisewebsite.repository;

import java.sql.*;
/*
The purpose of the ProgramRepository class is to manage the storage and retrieval of program (major/minor) data
in the SQLite database. It provides methods to add new programs and link courses to programs.
*/

import org.springframework.stereotype.Repository;

@Repository
public class ProgramRepository {

    // Add a new program (Major/Minor) to programs table only if it doesn't exist
    public int addProgram(String programName, String programType) {
        int programId = -1;

        try (Connection conn = DatabaseUtil.connect()) {
            // First, check if the program already exists
            String querySql = "SELECT program_id FROM programs WHERE program_name = ? AND program_type = ?";
            try (PreparedStatement queryStmt = conn.prepareStatement(querySql)) {
                queryStmt.setString(1, programName);
                queryStmt.setString(2, programType);
                ResultSet rs = queryStmt.executeQuery();
                if (rs.next()) {
                    // Program already exists, return its ID
                    programId = rs.getInt("program_id");
                    return programId;
                }
            }

            // Program doesn't exist, insert it
            String insertSql = "INSERT INTO programs(program_name, program_type) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, programName);
                pstmt.setString(2, programType);
                pstmt.executeUpdate();

                // Get the generated program_id
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    programId = generatedKeys.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return programId;
    }

    // Link a course to a program only if not already linked
    public void addCourseToProgram(int programId, String courseID) {
        try (Connection conn = DatabaseUtil.connect()) {
            // Check if the course is already linked to this program
            String checkSql = "SELECT COUNT(*) FROM program_courses WHERE program_id = ? AND course_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, programId);
                checkStmt.setString(2, courseID);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Course is already linked to this program, skip
                    return;
                }
            }

            // Course is not linked, add it
            String insertSql = "INSERT INTO program_courses(program_id, course_id) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, programId);
                pstmt.setString(2, courseID);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove a program by name
    public void removeProgram(String programName) {
        try (Connection conn = DatabaseUtil.connect()) {
            // First, delete all course associations for this program
            String deleteCoursesSql = "DELETE FROM program_courses WHERE program_id = (SELECT program_id FROM programs WHERE program_name = ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteCoursesSql)) {
                pstmt.setString(1, programName);
                pstmt.executeUpdate();
            }

            // Then delete the program
            String deleteProgramSql = "DELETE FROM programs WHERE program_name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteProgramSql)) {
                pstmt.setString(1, programName);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

