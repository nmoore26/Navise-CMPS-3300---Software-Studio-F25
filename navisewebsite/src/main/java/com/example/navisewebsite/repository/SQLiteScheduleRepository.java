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
            String sql = "SELECT c.id, c.code, c.credits, c.title, c.meeting_time " +
                    "FROM pathway_courses pc JOIN courses c ON pc.course_id = c.id " +
                    "WHERE pc.pathway_id = ?";
            
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
            String sql = "SELECT id, code, credits, title, meeting_time FROM courses WHERE id = ?";
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
            String sql = "SELECT id, code, credits, title, meeting_time FROM courses WHERE code = ?";
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
                    "c.name AS title, '' AS meeting_time " +
                    "FROM pathway_courses pc JOIN courses c ON pc.course_id = c.id " +
                    "WHERE pc.pathway_id = ?";
            
            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(altSql)) {
                ps.setString(1, pathwayId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        courses.add(buildCourseFromResultSet(rs, 
                            "id", "code", "credits", "title", null));
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
            String sql = "SELECT course_id FROM user_courses WHERE user_id = ?";
            List<Integer> courseIds = new ArrayList<>();
            
            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        courseIds.add(safeInt(rs, "course_id"));
                    }
                }
            } catch (SQLException e) {
                return tryAlternateUserQuery(userId);
            }
            return courseIds;
        }
        
        private List<Integer> tryAlternateUserQuery(String userId) {
            List<Integer> courseIds = new ArrayList<>();
            String alt = "SELECT courseid AS course_id FROM user_courses WHERE userid = ?";
            
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