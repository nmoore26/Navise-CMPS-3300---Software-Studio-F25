package com.example.navisewebsite.domain;    

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProjectedSchedule
 * 
 * Projects missing courses for a pathway and merges them into a student's schedule.
 * Uses repository pattern for testability and DB independence.
 */
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

    // Estimate semesters needed using greedy packing
    public int estimateSemestersNeeded(String pathwayId, String userId, int creditsPerSemester) {
        if (creditsPerSemester <= 0) throw new IllegalArgumentException("creditsPerSemester must be positive");
        List<Course> missing = missingCoursesForPathway(pathwayId, userId);
        if (missing.isEmpty()) return 0;

        List<Course> sorted = new ArrayList<>(missing);
        sorted.sort(Comparator.comparingInt((Course c) -> c.credits).reversed());

        boolean[] used = new boolean[sorted.size()];
        int remaining = sorted.size();
        int semesters = 0;

        while (remaining > 0) {
            semesters++;
            int capacity = creditsPerSemester;
            for (int i = 0; i < sorted.size(); i++) {
                if (used[i]) continue;
                Course c = sorted.get(i);
                if (c.credits <= capacity) {
                    used[i] = true;
                    capacity -= c.credits;
                    remaining--;
                }
            }
        }
        return semesters;
    }

    // Project missing courses into semester buckets
    public SchedulePlan projectMissingCourses(String pathwayId, String userId, int creditsPerSemester) {
        List<Course> missing = missingCoursesForPathway(pathwayId, userId);
        SchedulePlan plan = new SchedulePlan();
        if (missing.isEmpty()) return plan;

        List<Course> sorted = new ArrayList<>(missing);
        sorted.sort(Comparator.comparingInt((Course c) -> c.credits).reversed());

        boolean[] used = new boolean[sorted.size()];
        int remaining = sorted.size();
        int semIndex = 1;

        while (remaining > 0) {
            SemesterPlan sem = new SemesterPlan("Semester " + semIndex++);
            int capacity = creditsPerSemester;
            for (int i = 0; i < sorted.size(); i++) {
                if (used[i]) continue;
                Course c = sorted.get(i);
                if (c.credits <= capacity) {
                    sem.courses.add(c);
                    used[i] = true;
                    capacity -= c.credits;
                    remaining--;
                }
            }
            plan.semesters.add(sem);
        }
        return plan;
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
        SchedulePlan merged = new SchedulePlan();
        for (SemesterPlan s : original.semesters) {
            SemesterPlan copy = new SemesterPlan(s.semesterLabel);
            copy.courses.addAll(s.courses);
            merged.semesters.add(copy);
        }

        List<Course> missing = missingCoursesForPathway(pathwayId, userId);
        missing.sort(Comparator.comparingInt((Course c) -> c.credits).reversed());

        List<AddedCourseRecord> added = new ArrayList<>();
        List<Course> remaining = new ArrayList<>(missing);

        // First pass: bring existing semesters to minCreditsPerSemester
        for (SemesterPlan sem : merged.semesters) {
            while (sem.totalCredits() < minCreditsPerSemester && !remaining.isEmpty()) {
                boolean placed = false;
                for (int i = 0; i < remaining.size(); i++) {
                    Course c = remaining.get(i);
                    if (sem.totalCredits() + c.credits <= maxCreditsPerSemester) {
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

        // Second pass: fill toward targetCreditsPerSemester
        for (SemesterPlan sem : merged.semesters) {
            while (sem.totalCredits() < targetCreditsPerSemester && !remaining.isEmpty()) {
                boolean placed = false;
                for (int i = 0; i < remaining.size(); i++) {
                    Course c = remaining.get(i);
                    if (sem.totalCredits() + c.credits <= Math.min(maxCreditsPerSemester, targetCreditsPerSemester)) {
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

        // Third pass: create new semesters up to maxSemesters
        int nextSemIndex = merged.semesters.size() + 1;
        while (!remaining.isEmpty() && merged.semesters.size() < maxSemesters) {
            SemesterPlan newSem = new SemesterPlan("Semester " + nextSemIndex++);
            while (newSem.totalCredits() < targetCreditsPerSemester && !remaining.isEmpty()) {
                boolean placed = false;
                for (int i = 0; i < remaining.size(); i++) {
                    Course c = remaining.get(i);
                    if (newSem.totalCredits() + c.credits <= maxCreditsPerSemester) {
                        newSem.courses.add(c);
                        added.add(new AddedCourseRecord(c, newSem.semesterLabel));
                        remaining.remove(i);
                        placed = true;
                        break;
                    }
                }
                if (!placed) break;
            }
            // Try to reach minCreditsPerSemester if below
            if (newSem.totalCredits() < minCreditsPerSemester && !remaining.isEmpty()) {
                for (int i = 0; i < remaining.size(); ) {
                    Course c = remaining.get(i);
                    if (newSem.totalCredits() + c.credits <= maxCreditsPerSemester) {
                        newSem.courses.add(c);
                        added.add(new AddedCourseRecord(c, newSem.semesterLabel));
                        remaining.remove(i);
                    } else {
                        i++;
                    }
                    if (newSem.totalCredits() >= minCreditsPerSemester) break;
                }
            }
            merged.semesters.add(newSem);
        }

        // Mark remaining courses as UNSCHEDULED
        for (Course c : remaining) {
            added.add(new AddedCourseRecord(c, "UNSCHEDULED"));
        }

        return new MergeResult(merged, added);
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
    public static CourseRepository sqliteCourseRepository(String sqliteFile) {
        return new CourseRepository() {
            private Connection connect() throws SQLException {
                return DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
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
                            out.add(new Course(
                                    safeInt(rs, "id"),
                                    safeString(rs, "code"),
                                    safeInt(rs, "credits"),
                                    safeString(rs, "title"),
                                    safeString(rs, "meeting_time")));
                        }
                    }
                } catch (SQLException e) {
                    return tryAlternateCourseQuery(sqliteFile, pathwayId);
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
                            return Optional.of(new Course(
                                    safeInt(rs, "id"),
                                    safeString(rs, "code"),
                                    safeInt(rs, "credits"),
                                    safeString(rs, "title"),
                                    safeString(rs, "meeting_time")));
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
                            return Optional.of(new Course(
                                    safeInt(rs, "id"),
                                    safeString(rs, "code"),
                                    safeInt(rs, "credits"),
                                    safeString(rs, "title"),
                                    safeString(rs, "meeting_time")));
                        }
                    }
                } catch (SQLException e) {
                    // ignore
                }
                return Optional.empty();
            }

            private List<Course> tryAlternateCourseQuery(String sqliteFile, String pathwayId) {
                List<Course> out = new ArrayList<>();
                String altSql = "SELECT c.id, c.course_code AS code, c.credit_hours AS credits, c.name AS title, '' AS meeting_time " +
                        "FROM pathway_courses pc JOIN courses c ON pc.course_id = c.id WHERE pc.pathway_id = ?";
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
                     PreparedStatement ps = conn.prepareStatement(altSql)) {
                    ps.setString(1, pathwayId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            out.add(new Course(
                                    safeInt(rs, "id"),
                                    safeString(rs, "code"),
                                    safeInt(rs, "credits"),
                                    safeString(rs, "title"),
                                    ""));
                        }
                    }
                } catch (SQLException ex) {
                    // final fallback: empty
                }
                return out;
            }

            private int safeInt(ResultSet rs, String col) {
                try { return rs.getInt(col); } catch (SQLException e) { return 0; }
            }

            private String safeString(ResultSet rs, String col) {
                try { return rs.getString(col); } catch (SQLException e) { return ""; }
            }
        };
    }

    // SQLite UserRepository
    public static UserRepository sqliteUserRepository(String sqliteFile) {
        return new UserRepository() {
            private Connection connect() throws SQLException {
                return DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
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
                    out = tryAlternateUserQuery(sqliteFile, userId);
                }
                return out;
            }

            private List<Integer> tryAlternateUserQuery(String sqliteFile, String userId) {
                List<Integer> out = new ArrayList<>();
                String alt = "SELECT courseid AS course_id FROM user_courses WHERE userid = ?";
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
                     PreparedStatement ps = conn.prepareStatement(alt)) {
                    ps.setString(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) out.add(safeInt(rs, "course_id"));
                    }
                } catch (SQLException ex) {
                    // final fallback: empty
                }
                return out;
            }

            private int safeInt(ResultSet rs, String col) {
                try { return rs.getInt(col); } catch (SQLException e) { return 0; }
            }
        };
    }

    // Load student schedule from database grouped by semester_label
    public static SchedulePlan loadStudentScheduleFromDb(String sqliteFile, String userId) {
        SchedulePlan plan = new SchedulePlan();
        String sql = "SELECT uc.semester_label, c.id, c.code, c.credits, c.title, c.meeting_time " +
                "FROM user_courses uc JOIN courses c ON uc.course_id = c.id WHERE uc.user_id = ? " +
                "ORDER BY uc.semester_label, c.code";
        Map<String, SemesterPlan> map = new LinkedHashMap<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
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
            return tryAlternateLoad(sqliteFile, userId);
        }
        plan.semesters.addAll(map.values());
        return plan;
    }

    private static SchedulePlan tryAlternateLoad(String sqliteFile, String userId) {
        SchedulePlan plan = new SchedulePlan();
        String alt = "SELECT uc.semester AS semester_label, c.id, c.course_code AS code, c.credit_hours AS credits, c.name AS title " +
                "FROM user_courses uc JOIN courses c ON uc.course_id = c.id WHERE uc.user_id = ? ORDER BY uc.semester, c.course_code";
        Map<String, SemesterPlan> map = new LinkedHashMap<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
             PreparedStatement ps = conn.prepareStatement(alt)) {
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
                            "");
                    sp.courses.add(c);
                }
            }
        } catch (SQLException ex) {
            // final fallback: empty
        }
        plan.semesters.addAll(map.values());
        return plan;
    }

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
}