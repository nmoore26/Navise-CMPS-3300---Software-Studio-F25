package com.example.navisewebsite.repository;

import com.example.navisewebsite.domain.ScheduleDomain.ScheduleCourse;
import java.util.List;
import java.util.Optional;

/**
 * Repository interfaces for schedule-related data access.
 * Follows Interface Segregation Principle by separating course and user concerns.
 * 
 * Implementations should handle database-specific logic and provide
 * defensive fallbacks for schema variations.
 */
public class ScheduleRepository {
    
    /**
     * Repository for accessing course data.
     */
    public interface CourseRepository {
        /**
         * Get all courses required for a pathway (major or minor).
         * @param pathwayId the pathway identifier
         * @return list of courses
         */
        List<ScheduleCourse> coursesForPathway(String pathwayId);
        
        /**
         * Find a course by its unique identifier.
         * @param id the course id
         * @return optional containing the course if found
         */
        Optional<ScheduleCourse> courseById(int id);
        
        /**
         * Find a course by its code (e.g., "CMPS-101").
         * @param code the course code
         * @return optional containing the course if found
         */
        Optional<ScheduleCourse> courseByCode(String code);
    }
    
    /**
     * Repository for accessing user course completion data.
     */
    public interface UserRepository {
        /**
         * Get all course IDs that a user has completed.
         * @param userId the user identifier
         * @return list of completed course IDs
         */
        List<Integer> completedCourseIdsForUser(String userId);
    }
}
