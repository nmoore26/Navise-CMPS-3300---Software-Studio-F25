package com.example.navisewebsite.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain classes for schedule projection.
 * Extracted from ProjectedSchedule to follow Single Responsibility Principle.
 * 
 * Contains:
 * - ScheduleCourse: Simplified course representation for schedules
 * - SemesterPlan: Container for courses in a single semester
 * - SchedulePlan: Full multi-semester schedule
 * - AddedCourseRecord: Tracks where courses were added during merge operations
 */
public class ScheduleDomain {
    
    /**
     * Simplified course representation for schedule projection.
     * Contains only the fields needed for scheduling logic.
     */
    public static class ScheduleCourse {
        public final int id;
        public final String code;
        public final int credits;
        public final String title;
        public final String meetingTime;

        public ScheduleCourse(int id, String code, int credits, String title, String meetingTime) {
            this.id = id;
            this.code = code == null ? "" : code;
            this.credits = credits;
            this.title = title == null ? "" : title;
            this.meetingTime = meetingTime == null ? "" : meetingTime;
        }

        public String shortInfo() {
            return code + " (" + credits + "cr)";
        }

        @Override
        public String toString() {
            String mt = meetingTime.isEmpty() ? "" : ", " + meetingTime;
            String t = title.isEmpty() ? "" : " - " + title;
            return String.format("%s [%d] %s%s%s", code, id, credits + "cr", t, mt);
        }
    }

    /**
     * Container for courses planned in a single semester.
     */
    public static class SemesterPlan {
        public final String semesterLabel;
        public final List<ScheduleCourse> courses = new ArrayList<>();

        public SemesterPlan(String semesterLabel) {
            this.semesterLabel = semesterLabel;
        }

        public int totalCredits() {
            return courses.stream().mapToInt(c -> c.credits).sum();
        }
        
        public int courseCount() {
            return courses.size();
        }
    }

    /**
     * Full schedule spanning multiple semesters.
     */
    public static class SchedulePlan {
        public final List<SemesterPlan> semesters = new ArrayList<>();

        public int totalCourses() {
            return semesters.stream().mapToInt(s -> s.courses.size()).sum();
        }

        public int totalCredits() {
            return semesters.stream().mapToInt(SemesterPlan::totalCredits).sum();
        }
        
        public int semesterCount() {
            return semesters.size();
        }
    }

    /**
     * Record of a course addition during schedule merge operations.
     * Tracks which course was added to which semester.
     */
    public static class AddedCourseRecord {
        public final ScheduleCourse course;
        public final String semesterLabel;

        public AddedCourseRecord(ScheduleCourse course, String semesterLabel) {
            this.course = course;
            this.semesterLabel = semesterLabel;
        }
    }
    
    /**
     * Result of merging courses into an existing schedule.
     */
    public static class MergeResult {
        public final SchedulePlan mergedSchedule;
        public final List<AddedCourseRecord> addedCourses;

        public MergeResult(SchedulePlan mergedSchedule, List<AddedCourseRecord> addedCourses) {
            this.mergedSchedule = mergedSchedule;
            this.addedCourses = addedCourses;
        }
    }
}
