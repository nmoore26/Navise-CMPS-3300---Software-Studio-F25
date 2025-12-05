
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

    // Fetch all programs (name and type)
    public static class ProgramInfo {
        public final String name;
        public final String type;
        public ProgramInfo(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    public java.util.List<ProgramInfo> getAllPrograms() {
        java.util.List<ProgramInfo> programs = new java.util.ArrayList<>();
        try (Connection conn = DatabaseUtil.connectCourses();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT program_name, program_type FROM programs ORDER BY program_type, program_name")) {
            while (rs.next()) {
                programs.add(new ProgramInfo(rs.getString("program_name"), rs.getString("program_type")));
            }
        } catch (SQLException e) {
            System.out.println("ERROR ProgramRepository: SQL Exception when fetching all programs: " + e.getMessage());
            e.printStackTrace();
        }
        return programs;
    }
    public int addProgram(String programName, String programType) {
        int programId = -1;

        try (Connection conn = DatabaseUtil.connectCourses()) {
            System.out.println("DEBUG ProgramRepository: Connected to database for adding program");
            
            // First, check if the program already exists
            String querySql = "SELECT program_id FROM programs WHERE program_name = ? AND program_type = ?";
            try (PreparedStatement queryStmt = conn.prepareStatement(querySql)) {
                queryStmt.setString(1, programName);
                queryStmt.setString(2, programType);
                ResultSet rs = queryStmt.executeQuery();
                if (rs.next()) {
                    // Program already exists, return its ID
                    programId = rs.getInt("program_id");
                    System.out.println("DEBUG ProgramRepository: Program '" + programName + "' already exists with ID: " + programId);
                    return programId;
                }
            }

            System.out.println("DEBUG ProgramRepository: Program '" + programName + "' does not exist, creating new one");
            
            // Program doesn't exist, insert it
            String insertSql = "INSERT INTO programs(program_name, program_type) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, programName);
                pstmt.setString(2, programType);
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("DEBUG ProgramRepository: INSERT executed, rows affected: " + rowsAffected);
            }
            
            // Get the last inserted program_id using SQLite's last_insert_rowid()
            String lastIdSql = "SELECT last_insert_rowid() as program_id";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(lastIdSql)) {
                if (rs.next()) {
                    programId = rs.getInt("program_id");
                    System.out.println("DEBUG ProgramRepository: New program ID: " + programId);
                }
            }
            
            // COMMIT THE TRANSACTION
            conn.commit();
            System.out.println("DEBUG ProgramRepository: Transaction committed successfully");

        } catch (SQLException e) {
            System.out.println("ERROR ProgramRepository: SQL Exception when adding program: " + e.getMessage());
            e.printStackTrace();
        }

        return programId;
    }

    // Link a course to a program only if not already linked
    public void addCourseToProgram(int programId, String courseID) {
        try (Connection conn = DatabaseUtil.connectCourses()) {
            // Check if the course is already linked to this program
            String checkSql = "SELECT COUNT(*) FROM program_courses WHERE program_id = ? AND course_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, programId);
                checkStmt.setString(2, courseID);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Course is already linked to this program, skip
                    System.out.println("DEBUG ProgramRepository: Course '" + courseID + "' already linked to program " + programId);
                    return;
                }
            }

            // Course is not linked, add it
            String insertSql = "INSERT INTO program_courses(program_id, course_id) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, programId);
                pstmt.setString(2, courseID);
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("DEBUG ProgramRepository: Linked course '" + courseID + "' to program " + programId + ", rows affected: " + rowsAffected);
            }
            
            // COMMIT THE TRANSACTION
            conn.commit();
            System.out.println("DEBUG ProgramRepository: Course link transaction committed");

        } catch (SQLException e) {
            System.out.println("ERROR ProgramRepository: SQL Exception when linking course: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Remove a program by name
    public void removeProgram(String programName) {
        try (Connection conn = DatabaseUtil.connectCourses()) {
            // First, delete all course associations for this program
            String deleteCoursesSql = "DELETE FROM program_courses WHERE program_id = (SELECT program_id FROM programs WHERE program_name = ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteCoursesSql)) {
                pstmt.setString(1, programName);
                int coursesDeleted = pstmt.executeUpdate();
                System.out.println("DEBUG ProgramRepository: Deleted " + coursesDeleted + " course associations");
            }

            // Then delete the program
            String deleteProgramSql = "DELETE FROM programs WHERE program_name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteProgramSql)) {
                pstmt.setString(1, programName);
                int programDeleted = pstmt.executeUpdate();
                System.out.println("DEBUG ProgramRepository: Deleted " + programDeleted + " program(s)");
            }
            
            // COMMIT THE TRANSACTION
            conn.commit();
            System.out.println("DEBUG ProgramRepository: Remove program transaction committed");

        } catch (SQLException e) {
            System.out.println("ERROR ProgramRepository: SQL Exception when removing program: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

