package com.example.navisewebsite.service;

import com.example.navisewebsite.domain.ScheduleDomain.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service for merging courses into existing schedules.
 * Handles constraints like credit limits and semester bounds.
 * 
 * Follows Single Responsibility Principle - focuses only on schedule merging.
 */
public class ScheduleMergeService {
    
    private static final int DEFAULT_TARGET_CREDITS = 18;
    private static final int MAX_CREDITS_PER_SEMESTER = 21;
    private static final int MIN_CREDITS_PER_SEMESTER = 12;
    private static final int MAX_SEMESTERS = 8;
    
    private final ScheduleProjectionService projectionService;
    
    public ScheduleMergeService(ScheduleProjectionService projectionService) {
        this.projectionService = projectionService;
    }
    
    /**
     * Merge missing courses into an existing schedule with constraints:
     * - Target 18 credits per semester
     * - Never exceed 21 credits
     * - Maintain 12+ credit minimum
     * - Stay within 8 semesters
     * 
     * @param original existing schedule
     * @param pathwayId pathway to complete
     * @param userId user identifier
     * @return merge result with updated schedule and tracking of added courses
     */
    public MergeResult mergeWithExistingSchedule(SchedulePlan original, 
                                                 String pathwayId, 
                                                 String userId) {
        return mergeWithExistingSchedule(original, pathwayId, userId, DEFAULT_TARGET_CREDITS);
    }
    
    /**
     * Merge with custom target credits per semester.
     */
    public MergeResult mergeWithExistingSchedule(SchedulePlan original,
                                                 String pathwayId,
                                                 String userId,
                                                 int targetCreditsPerSemester) {
        // Defensive copy
        SchedulePlan merged = copySchedulePlan(original);
        
        List<ScheduleCourse> missing = projectionService.missingCoursesForPathway(pathwayId, userId);
        missing.sort(Comparator.comparingInt((ScheduleCourse c) -> c.credits).reversed());
        
        List<AddedCourseRecord> added = new ArrayList<>();
        List<ScheduleCourse> remaining = new ArrayList<>(missing);
        
        // Phase 1: Bring existing semesters to minimum credits
        for (SemesterPlan sem : merged.semesters) {
            fillSemesterToMinimum(sem, remaining, MIN_CREDITS_PER_SEMESTER, 
                                 MAX_CREDITS_PER_SEMESTER, added);
        }
        
        // Phase 2: Fill toward target credits
        for (SemesterPlan sem : merged.semesters) {
            fillSemesterToTarget(sem, remaining, targetCreditsPerSemester, 
                                MAX_CREDITS_PER_SEMESTER, added);
        }
        
        // Phase 3: Create new semesters if needed
        int nextSemIndex = merged.semesters.size() + 1;
        while (!remaining.isEmpty() && merged.semesters.size() < MAX_SEMESTERS) {
            SemesterPlan newSem = new SemesterPlan("Semester " + nextSemIndex++);
            fillSemesterToTarget(newSem, remaining, targetCreditsPerSemester, 
                                MAX_CREDITS_PER_SEMESTER, added);
            
            // Try to reach minimum if below
            if (newSem.totalCredits() < MIN_CREDITS_PER_SEMESTER && !remaining.isEmpty()) {
                fillSemesterToMinimum(newSem, remaining, MIN_CREDITS_PER_SEMESTER, 
                                     MAX_CREDITS_PER_SEMESTER, added);
            }
            
            merged.semesters.add(newSem);
        }
        
        // Phase 4: Mark any remaining courses as unscheduled
        for (ScheduleCourse c : remaining) {
            added.add(new AddedCourseRecord(c, "UNSCHEDULED"));
        }
        
        return new MergeResult(merged, added);
    }
    
    private SchedulePlan copySchedulePlan(SchedulePlan original) {
        SchedulePlan copy = new SchedulePlan();
        for (SemesterPlan s : original.semesters) {
            SemesterPlan semCopy = new SemesterPlan(s.semesterLabel);
            semCopy.courses.addAll(s.courses);
            copy.semesters.add(semCopy);
        }
        return copy;
    }
    
    /**
     * Fill semester to minimum target, respecting maximum limit.
     */
    private void fillSemesterToMinimum(SemesterPlan sem,
                                      List<ScheduleCourse> remaining,
                                      int minTarget,
                                      int maxPerSemester,
                                      List<AddedCourseRecord> added) {
        while (sem.totalCredits() < minTarget && !remaining.isEmpty()) {
            boolean placed = false;
            
            for (int i = 0; i < remaining.size(); i++) {
                ScheduleCourse course = remaining.get(i);
                if (sem.totalCredits() + course.credits <= maxPerSemester) {
                    sem.courses.add(course);
                    added.add(new AddedCourseRecord(course, sem.semesterLabel));
                    remaining.remove(i);
                    placed = true;
                    break;
                }
            }
            
            if (!placed) break; // Can't fit any more courses
        }
    }
    
    /**
     * Fill semester toward target credits, respecting maximum limit.
     */
    private void fillSemesterToTarget(SemesterPlan sem,
                                     List<ScheduleCourse> remaining,
                                     int target,
                                     int maxPerSemester,
                                     List<AddedCourseRecord> added) {
        while (sem.totalCredits() < target && !remaining.isEmpty()) {
            boolean placed = false;
            
            for (int i = 0; i < remaining.size(); i++) {
                ScheduleCourse course = remaining.get(i);
                int effectiveMax = Math.min(maxPerSemester, target);
                
                if (sem.totalCredits() + course.credits <= effectiveMax) {
                    sem.courses.add(course);
                    added.add(new AddedCourseRecord(course, sem.semesterLabel));
                    remaining.remove(i);
                    placed = true;
                    break;
                }
            }
            
            if (!placed) break; // Can't fit any more courses
        }
    }
}
