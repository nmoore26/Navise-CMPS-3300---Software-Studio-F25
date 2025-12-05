package com.example.navisewebsite.service;

import com.example.navisewebsite.domain.ScheduleDomain.*;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service for loading student schedules from database.
 * Handles database-specific logic for retrieving existing schedules.
 * 
 * Follows Single Responsibility Principle - focuses only on loading schedules.
 */
public class ScheduleLoaderService {
    
    // SQLite connection details
    private static final String COURSES_DB_URL = "jdbc:sqlite:courses.db";
    private static final String STUDENT_INFO_DB_URL = "jdbc:sqlite:student_info.db";
    
    /**
     * Load a student's schedule from database, grouped by semester.
     * 
     * @param userId the user identifier
     * @return schedule plan with courses organized by semester
     */
    public SchedulePlan loadStudentSchedule(String userId) {
        SchedulePlan plan = new SchedulePlan();
        String sql = "SELECT uc.semester_label, c.id, c.code, c.credits, c.title, c.meeting_time " +
                "FROM user_courses uc JOIN courses c ON uc.course_id = c.id " +
                "WHERE uc.user_id = ? ORDER BY uc.semester_label, c.code";
        
        Map<String, SemesterPlan> semesterMap = new LinkedHashMap<>();
        
        try (Connection conn = connectSQLite();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String semLabel = safeString(rs, "semester_label");
                    if (semLabel.isEmpty()) {
                        semLabel = "Semester 1";
                    }
                    
                    SemesterPlan sem = semesterMap.computeIfAbsent(semLabel, SemesterPlan::new);
                    
                    ScheduleCourse course = new ScheduleCourse(
                            safeInt(rs, "id"),
                            safeString(rs, "code"),
                            safeInt(rs, "credits"),
                            safeString(rs, "title"),
                            safeString(rs, "meeting_time")
                    );
                    
                    sem.courses.add(course);
                }
            }
        } catch (SQLException e) {
            return tryAlternateLoad(userId);
        }
        
        plan.semesters.addAll(semesterMap.values());
        return plan;
    }
    
    private Connection connectSQLite() throws SQLException {
        return DriverManager.getConnection(COURSES_DB_URL);
    }
    
    /**
     * Fallback query for alternate schema.
     */
    private SchedulePlan tryAlternateLoad(String userId) {
        SchedulePlan plan = new SchedulePlan();
        String alt = "SELECT uc.semester AS semester_label, c.id, c.course_code AS code, " +
                "c.credit_hours AS credits, c.name AS title " +
                "FROM user_courses uc JOIN courses c ON uc.course_id = c.id " +
                "WHERE uc.user_id = ? ORDER BY uc.semester, c.course_code";
        
        Map<String, SemesterPlan> semesterMap = new LinkedHashMap<>();
        
        try (Connection conn = connectSQLite();
             PreparedStatement ps = conn.prepareStatement(alt)) {
            ps.setString(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String semLabel = safeString(rs, "semester_label");
                    if (semLabel.isEmpty()) {
                        semLabel = "Semester 1";
                    }
                    
                    SemesterPlan sem = semesterMap.computeIfAbsent(semLabel, SemesterPlan::new);
                    
                    ScheduleCourse course = new ScheduleCourse(
                            safeInt(rs, "id"),
                            safeString(rs, "code"),
                            safeInt(rs, "credits"),
                            safeString(rs, "title"),
                            ""
                    );
                    
                    sem.courses.add(course);
                }
            }
        } catch (SQLException ex) {
            // Final fallback: empty schedule
        }
        
        plan.semesters.addAll(semesterMap.values());
        return plan;
    }
    
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
