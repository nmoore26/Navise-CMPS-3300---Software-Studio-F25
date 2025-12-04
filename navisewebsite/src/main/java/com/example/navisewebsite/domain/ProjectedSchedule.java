package com.example.navisewebsite.domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//Utilities for projecting and merging course schedules for a pathway and user.

public class ProjectedSchedule {

    // Domain classes
    public static class Course {
        public final int id;
        public final String code;
        public final int credits;
        public final String title;
        public final String meetingTime;

        public Course(int id, String code, int credits, String title, String meetingTime) {
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

    // Semester container
    public static class SemesterPlan {
        public final String semesterLabel;
        public final List<Course> courses = new ArrayList<>();

        public SemesterPlan(String semesterLabel) {
            this.semesterLabel = semesterLabel;
        }

        public int totalCredits() {
            return courses.stream().mapToInt(c -> c.credits).sum();
        }
    }

    // Full schedule container
    public static class SchedulePlan {
        public final List<SemesterPlan> semesters = new ArrayList<>();

        public int totalCourses() {
            return semesters.stream().mapToInt(s -> s.courses.size()).sum();
        }

        public int totalCredits() {
            return semesters.stream().mapToInt(SemesterPlan::totalCredits).sum();
        }
    }

    // Record of added course with placement
    public static class AddedCourseRecord {
        public final Course course;
        public final String semesterLabel;

        public AddedCourseRecord(Course course, String semesterLabel) {
            this.course = course;
            this.semesterLabel = semesterLabel;
        }
    }

    // Repository interfaces
    public interface CourseRepository {
        List<Course> coursesForPathway(String pathwayId);
        Optional<Course> courseById(int id);
        Optional<Course> courseByCode(String code);
    }

    public interface UserRepository {
        List<Integer> completedCourseIdsForUser(String userId);
    }

    // Dependencies
    private final CourseRepository courseRepo;
    private final UserRepository userRepo;

    public ProjectedSchedule(CourseRepository courseRepo, UserRepository userRepo) {
        this.courseRepo = courseRepo;
        this.userRepo = userRepo;
    }

    // Return courses needed to complete pathway
    public List<Course> missingCoursesForPathway(String pathwayId, String userId) {
        List<Course> required = courseRepo.coursesForPathway(pathwayId);
        Set<Integer> completedIds = new HashSet<>(userRepo.completedCourseIdsForUser(userId));
        return required.stream()
                .filter(c -> !completedIds.contains(c.id))
                .collect(Collectors.toList());
    }

    // Sum credits of missing courses
    public int remainingCredits(String pathwayId, String userId) {
        return missingCoursesForPathway(pathwayId, userId).stream()
                .mapToInt(c -> c.credits)
                .sum();
    }

    // Check if user is within 9 credits of completion
    public boolean isNearCompletion(String pathwayId, String userId) {
        return remainingCredits(pathwayId, userId) <= 9;
    }

    // Estimate semesters needed using greedy packing (largest-first)
    public int estimateSemestersNeeded(String pathwayId, String userId, int creditsPerSemester) {
        if (creditsPerSemester <= 0) throw new IllegalArgumentException("creditsPerSemester must be positive");
        List<Course> missing = missingCoursesForPathway(pathwayId, userId);
        if (missing.isEmpty()) return 0;

        List<List<Course>> buckets = packCoursesGreedy(missing, creditsPerSemester);
        return buckets.size();
    }

    // Project missing courses into semester buckets
    public SchedulePlan projectMissingCourses(String pathwayId, String userId, int creditsPerSemester) {
        List<Course> missing = missingCoursesForPathway(pathwayId, userId);
        SchedulePlan plan = new SchedulePlan();
        if (missing.isEmpty()) return plan;

        List<List<Course>> buckets = packCoursesGreedy(missing, creditsPerSemester);
        int idx = 1;
        for (List<Course> bucket : buckets) {
            SemesterPlan sem = new SemesterPlan("Semester " + (idx++));
            sem.courses.addAll(bucket);
            plan.semesters.add(sem);
        }
        return plan;
    }

    /*
      Greedy bin-packing: sort descending by credits and fill each semester until capacity.
      Returns a list of buckets (each bucket is a list of courses for one semester).
      This preserves the original largest-first packing behavior.
     */
    private List<List<Course>> packCoursesGreedy(List<Course> courses, int creditsPerSemester) {
        List<Course> sorted = new ArrayList<>(courses);
        sorted.sort(Comparator.comparingInt((Course c) -> c.credits).reversed());

        boolean[] used = new boolean[sorted.size()];
        int remaining = sorted.size();
        List<List<Course>> buckets = new ArrayList<>();

        while (remaining > 0) {
            int capacity = creditsPerSemester;
            List<Course> bucket = new ArrayList<>();
            for (int i = 0; i < sorted.size(); i++) {
                if (used[i]) continue;
                Course c = sorted.get(i);
                if (c.credits <= capacity) {
                    used[i] = true;
                    bucket.add(c);
                    capacity -= c.credits;
                    remaining--;
                }
            }
            buckets.add(bucket);
        }
        return buckets;
    }

    // Merge missing courses into existing schedule with constraints
    // Target 18 credits/semester, never exceed 21, stay within 8 semesters, maintain 12+ credit minimum
    public MergeResult mergeWithExistingSchedule(SchedulePlan original,
                                                 String pathwayId,
                                                 String userId,
                                                 int targetCreditsPerSemester) {
        final int maxCreditsPerSemester = 21;
        final int minCreditsPerSemester = 12;
        final int maxSemesters = 8;

        // Defensive copy of original
        SchedulePlan merged = copySchedulePlan(original);

        List<Course> missing = missingCoursesForPathway(pathwayId, userId);
        missing.sort(Comparator.comparingInt((Course c) -> c.credits).reversed());

        List<AddedCourseRecord> added = new ArrayList<>();
        List<Course> remaining = new ArrayList<>(missing);

        // First pass: bring existing semesters to minCreditsPerSemester
        for (SemesterPlan sem : merged.semesters) {
            fillSemesterToMinimum(sem, remaining, minCreditsPerSemester, maxCreditsPerSemester, added);
        }

        // Second pass: fill toward targetCreditsPerSemester (but never exceed max)
        for (SemesterPlan sem : merged.semesters) {
            fillSemesterToTarget(sem, remaining, targetCreditsPerSemester, maxCreditsPerSemester, added);
        }

        // Third pass: create new semesters up to maxSemesters
        int nextSemIndex = merged.semesters.size() + 1;
        while (!remaining.isEmpty() && merged.semesters.size() < maxSemesters) {
            SemesterPlan newSem = new SemesterPlan("Semester " + nextSemIndex++);
            fillSemesterToTarget(newSem, remaining, targetCreditsPerSemester, maxCreditsPerSemester, added);

            // If still below minimum, try to reach minimum
            if (newSem.totalCredits() < minCreditsPerSemester && !remaining.isEmpty()) {
                fillSemesterToMinimum(newSem, remaining, minCreditsPerSemester, maxCreditsPerSemester, added);
            }

            merged.semesters.add(newSem);
        }

        // Mark remaining courses as UNSCHEDULED
        for (Course c : remaining) {
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

    /*
      Try to add courses from 'remaining' into 'sem' until sem.totalCredits() >= minTarget,
      but never exceed maxPerSemester. Records additions into 'added'.
     */
    private void fillSemesterToMinimum(SemesterPlan sem,
                                       List<Course> remaining,
                                       int minTarget,
                                       int maxPerSemester,
                                       List<AddedCourseRecord> added) {
        while (sem.totalCredits() < minTarget && !remaining.isEmpty()) {
            boolean placed = false;
            for (int i = 0; i < remaining.size(); i++) {
                Course c = remaining.get(i);
                if (sem.totalCredits() + c.credits <= maxPerSemester) {
                    sem.courses.add(c);
                    added.add(new AddedCourseRecord(c, sem.semesterLabel));
                    remaining.remove(i);
                    placed = true;
                    break;
                }
            }
            if (!placed) break;
        }
    }

    // Try to add courses from 'remaining' into 'sem' until sem.totalCredits() >= target,
     // but never exceed maxPerSemester. Records additions into 'added'.

    private void fillSemesterToTarget(SemesterPlan sem,
                                      List<Course> remaining,
                                      int target,
                                      int maxPerSemester,
                                      List<AddedCourseRecord> added) {
        while (sem.totalCredits() < target && !remaining.isEmpty()) {
            boolean placed = false;
            for (int i = 0; i < remaining.size(); i++) {
                Course c = remaining.get(i);
                if (sem.totalCredits() + c.credits <= Math.min(maxPerSemester, target)) {
                    sem.courses.add(c);
                    added.add(new AddedCourseRecord(c, sem.semesterLabel));
                    remaining.remove(i);
                    placed = true;
                    break;
                }
            }
            if (!placed) break;
        }
    }

    public static class MergeResult {
        public final SchedulePlan mergedSchedule;
        public final List<AddedCourseRecord> addedCourses;

        public MergeResult(SchedulePlan mergedSchedule, List<AddedCourseRecord> addedCourses) {
            this.mergedSchedule = mergedSchedule;
            this.addedCourses = addedCourses;
        }
    }

    // SQLite CourseRepository with defensive fallback queries
    // PostgreSQL CourseRepository implementation
    public static CourseRepository postgresCourseRepository() {
        return new CourseRepository() {
            private Connection connect() throws SQLException {
                return DriverManager.getConnection("jdbc:postgresql://tramway.proxy.rlwy.net:45308/railway", "postgres", "ECRzrnCljFHfGvFVvPZmJVlSuCfsCnLp");
            }

            @Override
            public List<Course> coursesForPathway(String pathwayId) {
        String sql = "SELECT c.id, c.code, c.credits, c.title, c.meeting_time " +
            "FROM pathway_courses pc JOIN courses c ON pc.course_id = c.id " +
            "WHERE pc.pathway_id = ?";
                List<Course> out = new ArrayList<>();
                try (Connection conn = connect();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, pathwayId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            out.add(buildCourseFromResultSet(rs, "id", "code", "credits", "title", "meeting_time"));
                        }
                    }
                } catch (SQLException e) {
                    // ignore
                }
                return out;
            }

            @Override
            public Optional<Course> courseById(int id) {
                String sql = "SELECT id, code, credits, title, meeting_time FROM courses WHERE id = ?";
                try (Connection conn = connect();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return Optional.of(buildCourseFromResultSet(rs, "id", "code", "credits", "title", "meeting_time"));
                        }
                    }
                } catch (SQLException e) {
                    // ignore
                }
                return Optional.empty();
            }

            @Override
            public Optional<Course> courseByCode(String code) {
                String sql = "SELECT id, code, credits, title, meeting_time FROM courses WHERE code = ?";
                try (Connection conn = connect();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, code);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return Optional.of(buildCourseFromResultSet(rs, "id", "code", "credits", "title", "meeting_time"));
                        }
                    }
                } catch (SQLException e) {
                    // ignore
                }
                return Optional.empty();
            }


            private Course buildCourseFromResultSet(ResultSet rs, String idCol, String codeCol, String creditsCol, String titleCol, String meetingCol) throws SQLException {
                int id = safeInt(rs, idCol);
                String code = safeString(rs, codeCol);
                int credits = safeInt(rs, creditsCol);
                String title = titleCol == null ? "" : safeString(rs, titleCol);
                String meeting = meetingCol == null ? "" : safeString(rs, meetingCol);
                return new Course(id, code, credits, title, meeting);
            }
        };
    }

    // SQLite UserRepository
    // PostgreSQL UserRepository implementation
    public static UserRepository postgresUserRepository() {
        return new UserRepository() {
            private Connection connect() throws SQLException {
                return DriverManager.getConnection("jdbc:postgresql://tramway.proxy.rlwy.net:45308/railway", "postgres", "ECRzrnCljFHfGvFVvPZmJVlSuCfsCnLp");
            }

            @Override
            public List<Integer> completedCourseIdsForUser(String userId) {
                String sql = "SELECT course_id FROM user_courses WHERE user_id = ?";
                List<Integer> out = new ArrayList<>();
                try (Connection conn = connect();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            out.add(safeInt(rs, "course_id"));
                        }
                    }
                } catch (SQLException e) {
                    // ignore
                }
                return out;
            }

        };
    }

    // Load student schedule from database grouped by semester_label
    public static SchedulePlan loadStudentScheduleFromDb(String userId) {
        SchedulePlan plan = new SchedulePlan();
        String sql = "SELECT uc.semester_label, c.id, c.code, c.credits, c.title, c.meeting_time " +
                "FROM user_courses uc JOIN courses c ON uc.course_id = c.id WHERE uc.user_id = ? " +
                "ORDER BY uc.semester_label, c.code";
        Map<String, SemesterPlan> map = new LinkedHashMap<>();
    try (Connection conn = DriverManager.getConnection("jdbc:postgresql://tramway.proxy.rlwy.net:45308/railway", "postgres", "ECRzrnCljFHfGvFVvPZmJVlSuCfsCnLp");
           PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sem = safeString(rs, "semester_label");
                    if (sem.isEmpty()) sem = "Semester 1";
                    SemesterPlan sp = map.computeIfAbsent(sem, SemesterPlan::new);
                    Course c = new Course(
                            safeInt(rs, "id"),
                            safeString(rs, "code"),
                            safeInt(rs, "credits"),
                            safeString(rs, "title"),
                            safeString(rs, "meeting_time"));
                    sp.courses.add(c);
                }
            }
        } catch (SQLException e) {
            // ignore
        }
        plan.semesters.addAll(map.values());
        return plan;
    }

    // Removed obsolete tryAlternateLoad fallback method and all sqliteFile references
        // Removed obsolete tryAlternateLoad fallback method and all sqliteFile references
    
    private static int safeInt(ResultSet rs, String col) {
        try { return rs.getInt(col); } catch (SQLException e) { return 0; }
    }

    private static String safeString(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (SQLException e) { return ""; }
    }

    // Render comparison of original vs new schedule with added courses
    public static String renderScheduleComparison(SchedulePlan original, SchedulePlan merged, List<AddedCourseRecord> added) {
        StringBuilder sb = new StringBuilder();
        sb.append("original schedule-\n");
        for (SemesterPlan s : original.semesters) {
            sb.append(s.semesterLabel).append("- ");
            sb.append(s.courses.stream().map(c -> c.code).collect(Collectors.joining(", ")));
            sb.append("\n");
        }
        sb.append("\nnew schedule-\n");
        for (SemesterPlan s : merged.semesters) {
            sb.append(s.semesterLabel).append("- ");
            sb.append(s.courses.stream().map(c -> c.code).collect(Collectors.joining(", ")));
            sb.append("\n");
        }
        sb.append("\nadded-\n");
        for (AddedCourseRecord a : added) {
            sb.append(a.semesterLabel).append("- ").append(a.course.code)
              .append(" (").append(a.course.credits).append("cr");
            if (!a.course.meetingTime.isEmpty()) sb.append(", ").append(a.course.meetingTime);
            if (!a.course.title.isEmpty()) sb.append(", ").append(a.course.title);
            sb.append(")\n");
        }
        return sb.toString();
    }

    // Return courses needed to complete a program (major or minor).
    public List<Course> missingCoursesForProgram(String programId, String userId) {
        return missingCoursesForPathway(programId, userId);
    }

    // Project a hypothetical schedule for a desired major and optional minor.
    // Combines missing courses for both programs, deduplicates by course id, and
    // uses existing greedy packing logic to create a SchedulePlan.
    public SchedulePlan projectForPrograms(String majorId, String minorId, String userId, int creditsPerSemester) {
        if (creditsPerSemester <= 0) throw new IllegalArgumentException("creditsPerSemester must be positive");

        List<Course> majorMissing = missingCoursesForProgram(majorId, userId);
        List<Course> minorMissing = (minorId == null || minorId.isEmpty())
                ? Collections.emptyList()
                : missingCoursesForProgram(minorId, userId);

        // Combine and deduplicate by course id (preserve insertion order: major first, then minor)
        Map<Integer, Course> byId = new LinkedHashMap<>();
        for (Course c : majorMissing) byId.put(c.id, c);
        for (Course c : minorMissing) byId.putIfAbsent(c.id, c);

        List<Course> combined = new ArrayList<>(byId.values());

        SchedulePlan plan = new SchedulePlan();
        if (combined.isEmpty()) return plan;

        // Reuse existing greedy packer to create semester buckets
        List<List<Course>> buckets = packCoursesGreedy(combined, creditsPerSemester);
        int idx = 1;
        for (List<Course> bucket : buckets) {
            SemesterPlan sem = new SemesterPlan("Semester " + (idx++));
            sem.courses.addAll(bucket);
            plan.semesters.add(sem);
        }
        return plan;
    }


}
