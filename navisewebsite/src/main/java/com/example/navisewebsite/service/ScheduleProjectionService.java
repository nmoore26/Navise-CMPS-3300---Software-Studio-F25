package com.example.navisewebsite.service;

import com.example.navisewebsite.domain.ScheduleDomain.*;
import com.example.navisewebsite.repository.ScheduleRepositoryInterfaces.*;
// REMOVE: import com.example.navisewebsite.repository.ScheduleRepository.*;
// ADD: import com.example.navisewebsite.repository.ScheduleRepositoryInterfaces.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for projecting course schedules.
 * Handles core logic for determining remaining courses and planning semesters.
 * 
 * Follows Single Responsibility Principle - focuses only on schedule projection.
 * Follows Open/Closed Principle - extensible through strategy pattern for packing algorithms.
 */
public class ScheduleProjectionService {
    
    private final ScheduleCourseRepository courseRepo;
    private final ScheduleUserRepository userRepo;
    
    // FIXED CONSTRUCTOR: Use the new interface types
    public ScheduleProjectionService(ScheduleCourseRepository courseRepo, ScheduleUserRepository userRepo) {
        this.courseRepo = courseRepo;
        this.userRepo = userRepo;
    }
    
    /**
     * Get courses needed to complete a pathway.
     * @param pathwayId the pathway (major/minor) identifier
     * @param userId the user identifier
     * @return list of courses not yet completed
     */
    public List<ScheduleCourse> missingCoursesForPathway(String pathwayId, String userId) {
        List<ScheduleCourse> required = courseRepo.coursesForPathway(pathwayId);
        Set<Integer> completedIds = new HashSet<>(userRepo.completedCourseIdsForUser(userId));
        
        return required.stream()
                .filter(c -> !completedIds.contains(c.id))
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate total credit hours remaining for a pathway.
     */
    public int remainingCredits(String pathwayId, String userId) {
        return missingCoursesForPathway(pathwayId, userId).stream()
                .mapToInt(c -> c.credits)
                .sum();
    }
    
    /**
     * Check if user is within 9 credits of pathway completion.
     */
    public boolean isNearCompletion(String pathwayId, String userId) {
        return remainingCredits(pathwayId, userId) <= 9;
    }
    
    /**
     * Estimate number of semesters needed using greedy packing.
     */
    public int estimateSemestersNeeded(String pathwayId, String userId, int creditsPerSemester) {
        if (creditsPerSemester <= 0) {
            throw new IllegalArgumentException("creditsPerSemester must be positive");
        }
        
        List<ScheduleCourse> missing = missingCoursesForPathway(pathwayId, userId);
        if (missing.isEmpty()) {
            return 0;
        }
        
        List<List<ScheduleCourse>> buckets = packCoursesGreedy(missing, creditsPerSemester);
        return buckets.size();
    }
    
    /**
     * Project remaining courses into semester buckets.
     */
    public SchedulePlan projectMissingCourses(String pathwayId, String userId, int creditsPerSemester) {
        List<ScheduleCourse> missing = missingCoursesForPathway(pathwayId, userId);
        SchedulePlan plan = new SchedulePlan();
        
        if (missing.isEmpty()) {
            return plan;
        }
        
        List<List<ScheduleCourse>> buckets = packCoursesGreedy(missing, creditsPerSemester);
        int semesterNum = 1;
        
        for (List<ScheduleCourse> bucket : buckets) {
            SemesterPlan semester = new SemesterPlan("Semester " + semesterNum++);
            semester.courses.addAll(bucket);
            plan.semesters.add(semester);
        }
        
        return plan;
    }
    
    /**
     * Project schedule for combined major and minor programs.
     * Deduplicates courses that satisfy both requirements.
     */
    public SchedulePlan projectForPrograms(String majorId, String minorId, 
                                          String userId, int creditsPerSemester) {
        if (creditsPerSemester <= 0) {
            throw new IllegalArgumentException("creditsPerSemester must be positive");
        }
        
        List<ScheduleCourse> majorMissing = missingCoursesForPathway(majorId, userId);
        List<ScheduleCourse> minorMissing = (minorId == null || minorId.isEmpty())
                ? Collections.emptyList()
                : missingCoursesForPathway(minorId, userId);
        
        // Deduplicate by course id (preserve order: major first, then minor)
        Map<Integer, ScheduleCourse> byId = new LinkedHashMap<>();
        for (ScheduleCourse c : majorMissing) {
            byId.put(c.id, c);
        }
        for (ScheduleCourse c : minorMissing) {
            byId.putIfAbsent(c.id, c);
        }
        
        List<ScheduleCourse> combined = new ArrayList<>(byId.values());
        SchedulePlan plan = new SchedulePlan();
        
        if (combined.isEmpty()) {
            return plan;
        }
        
        List<List<ScheduleCourse>> buckets = packCoursesGreedy(combined, creditsPerSemester);
        int semesterNum = 1;
        
        for (List<ScheduleCourse> bucket : buckets) {
            SemesterPlan semester = new SemesterPlan("Semester " + semesterNum++);
            semester.courses.addAll(bucket);
            plan.semesters.add(semester);
        }
        
        return plan;
    }
    
    /**
     * Greedy bin-packing algorithm for courses.
     * Sorts by credits (descending) and fills semesters sequentially.
     * 
     * @param courses list of courses to pack
     * @param creditsPerSemester capacity per semester
     * @return list of buckets (each bucket = one semester)
     */
    List<List<ScheduleCourse>> packCoursesGreedy(List<ScheduleCourse> courses, int creditsPerSemester) {
        List<ScheduleCourse> sorted = new ArrayList<>(courses);
        sorted.sort(Comparator.comparingInt((ScheduleCourse c) -> c.credits).reversed());
        
        boolean[] used = new boolean[sorted.size()];
        int remaining = sorted.size();
        List<List<ScheduleCourse>> buckets = new ArrayList<>();
        
        while (remaining > 0) {
            int capacity = creditsPerSemester;
            List<ScheduleCourse> bucket = new ArrayList<>();
            
            for (int i = 0; i < sorted.size(); i++) {
                if (used[i]) continue;
                
                ScheduleCourse course = sorted.get(i);
                if (course.credits <= capacity) {
                    used[i] = true;
                    bucket.add(course);
                    capacity -= course.credits;
                    remaining--;
                }
            }
            
            buckets.add(bucket);
        }
        
        return buckets;
    }
}