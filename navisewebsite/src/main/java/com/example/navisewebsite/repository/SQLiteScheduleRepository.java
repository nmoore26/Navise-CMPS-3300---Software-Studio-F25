package com.example.navisewebsite.repository;

import com.example.navisewebsite.domain.ScheduleDomain.ScheduleCourse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementations of schedule repositories.
 * Includes defensive fallback queries for schema variations.
 * 
 * Follows Dependency Inversion Principle by implementing repository interfaces.
 */
public class SQLiteScheduleRepository {
    
    /**
     * SQLite implementation of ScheduleCourseRepository.
     */
    public static class SQLiteCourseRepository implements ScheduleRepositoryInterfaces.ScheduleCourseRepository {
        private final String sqliteFile;
        
        public SQLiteCourseRepository(String sqliteFile) {
            this.sqliteFile = sqliteFile;
        }
        
        private Connection connect() throws SQLException {
            return DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
        }
        
        @Override
        public List<ScheduleCourse> coursesForPathway(String pathwayId) {
            String sql = "SELECT c.courseID as id, c.course_code as code, c.credit_hours as credits, c.course_name as title, c.time as meeting_time " +
                    "FROM program_courses pc JOIN courses c ON pc.course_id = c.courseID " +
                    "WHERE pc.program_name = ?";
            
            List<ScheduleCourse> courses = new ArrayList<>();
            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pathwayId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        courses.add(buildCourseFromResultSet(rs, 
                            "id", "code", "credits", "title", "meeting_time"));
                    }
                }
            } catch (SQLException e) {
                return tryAlternateCourseQuery(pathwayId);
            }
            return courses;
        }
        
        @Override
        public Optional<ScheduleCourse> courseById(int id) {
            String sql = "SELECT courseID as id, course_code as code, credit_hours as credits, course_name as title, time as meeting_time FROM courses WHERE courseID = ?";
            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(buildCourseFromResultSet(rs, 
                            "id", "code", "credits", "title", "meeting_time"));
                    }
                }
            } catch (SQLException e) {
                // Ignore and return empty
            }
            return Optional.empty();
        }
        
        @Override
        public Optional<ScheduleCourse> courseByCode(String code) {
            String sql = "SELECT courseID as id, course_code as code, credit_hours as credits, course_name as title, time as meeting_time FROM courses WHERE course_code = ?";
            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(buildCourseFromResultSet(rs, 
                            "id", "code", "credits", "title", "meeting_time"));
                    }
                }
            } catch (SQLException e) {
                // Ignore and return empty
            }
            return Optional.empty();
        }
        
        private List<ScheduleCourse> tryAlternateCourseQuery(String pathwayId) {
            List<ScheduleCourse> courses = new ArrayList<>();
            String altSql = "SELECT c.id, c.course_code AS code, c.credit_hours AS credits, " +
                    "c.course_name AS title, c.time AS meeting_time " +
                    "FROM program_courses pc JOIN courses c ON pc.course_id = c.id " +
                    "WHERE pc.program_name = ?";
            
            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(altSql)) {
                ps.setString(1, pathwayId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        courses.add(buildCourseFromResultSet(rs, 
                            "id", "code", "credits", "title", "meeting_time"));
                    }
                }
            } catch (SQLException ex) {
                // Final fallback: empty list
            }
            return courses;
        }
        
        private ScheduleCourse buildCourseFromResultSet(ResultSet rs, 
                String idCol, String codeCol, String creditsCol, 
                String titleCol, String meetingCol) throws SQLException {
            int id = safeInt(rs, idCol);
            String code = safeString(rs, codeCol);
            int credits = safeInt(rs, creditsCol);
            String title = titleCol == null ? "" : safeString(rs, titleCol);
            String meeting = meetingCol == null ? "" : safeString(rs, meetingCol);
            return new ScheduleCourse(id, code, credits, title, meeting);
        }
    }
    
    /**
     * SQLite implementation of UserRepository.
     */
    public static class SQLiteUserRepository implements ScheduleRepositoryInterfaces.ScheduleUserRepository {
        private final String sqliteFile;
        
        public SQLiteUserRepository(String sqliteFile) {
            this.sqliteFile = sqliteFile;
        }
        
        private Connection connect() throws SQLException {
            return DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
        }
        
        @Override
        public List<Integer> completedCourseIdsForUser(String userId) {
            // First try to get completed courses from student_info.past_courses field
            String sql = "SELECT past_courses FROM student_info WHERE user_id = ?";
            List<Integer> courseIds = new ArrayList<>();
            
            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                // userId could be email or numeric ID, try numeric first
                try {
                    ps.setInt(1, Integer.parseInt(userId));
                } catch (NumberFormatException e) {
                    // If not numeric, try as string (though our schema uses INTEGER for user_id)
                    return courseIds; // Return empty if can't parse
                }
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String pastCourses = safeString(rs, "past_courses");
                        if (pastCourses != null && !pastCourses.trim().isEmpty()) {
                            // Parse comma-separated course codes and look up IDs
                            String[] courseCodes = pastCourses.split(",\\s*");
                            courseIds = lookupCourseIdsByCodes(courseCodes);
                        }
                    }
                }
            } catch (SQLException e) {
                return tryAlternateUserQuery(userId);
            }
            return courseIds;
        }
        
        /**
         * Helper method to convert course codes to course IDs
         */
        private List<Integer> lookupCourseIdsByCodes(String[] courseCodes) {
            List<Integer> ids = new ArrayList<>();
            // Need to connect to courses.db to look up course IDs
            String courseDbPath = "courses.db";
            String sql = "SELECT courseID FROM courses WHERE course_code = ?";
            
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + courseDbPath)) {
                for (String code : courseCodes) {
                    if (code.trim().isEmpty()) continue;
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, code.trim());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                ids.add(rs.getInt("courseID"));
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                // Return what we have
            }
            return ids;
        }
        
        private List<Integer> tryAlternateUserQuery(String userId) {
            List<Integer> courseIds = new ArrayList<>();
            // Fallback: try old schema with user_courses table
            String alt = "SELECT course_id FROM user_courses WHERE user_id = ?";
            
            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(alt)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        courseIds.add(safeInt(rs, "course_id"));
                    }
                }
            } catch (SQLException ex) {
                // Final fallback: empty list
            }
            return courseIds;
        }
    }
    
    // Utility methods
    private static int safeInt(ResultSet rs, String col) {
        try {
            return rs.getInt(col);
        } catch (SQLException e) {
            return 0;
        }
    }
    
    private static String safeString(ResultSet rs, String col) {
        try {
            return rs.getString(col);
        } catch (SQLException e) {
            return "";
        }
    }
}