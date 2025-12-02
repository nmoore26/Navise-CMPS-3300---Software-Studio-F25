package com.example.navisewebsite;    

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import com.example.navisewebsite.domain.ProjectedSchedule;

public class ProjectedScheduleTests {

    // In-memory CourseRepository
    private static class InMemoryCourseRepo implements ProjectedSchedule.CourseRepository {
        private final Map<String, List<ProjectedSchedule.Course>> pathways = new HashMap<>();
        private final Map<Integer, ProjectedSchedule.Course> byId = new HashMap<>();
        private final Map<String, ProjectedSchedule.Course> byCode = new HashMap<>();

        void addPathway(String id, ProjectedSchedule.Course... courses) {
            pathways.put(id, Arrays.asList(courses));
            for (ProjectedSchedule.Course c : courses) {
                byId.put(c.id, c);
                byCode.put(c.code, c);
            }
        }

        @Override
        public List<ProjectedSchedule.Course> coursesForPathway(String pathwayId) {
            return pathways.getOrDefault(pathwayId, Collections.emptyList());
        }

        @Override
        public Optional<ProjectedSchedule.Course> courseById(int id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<ProjectedSchedule.Course> courseByCode(String code) {
            return Optional.ofNullable(byCode.get(code));
        }
    }

    // In-memory UserRepository
    private static class InMemoryUserRepo implements ProjectedSchedule.UserRepository {
        private final Map<String, List<Integer>> userCourses = new HashMap<>();

        void setCompleted(String userId, Integer... ids) {
            userCourses.put(userId, Arrays.asList(ids));
        }

        @Override
        public List<Integer> completedCourseIdsForUser(String userId) {
            return userCourses.getOrDefault(userId, Collections.emptyList());
        }
    }

    // Build sample original schedule
    private ProjectedSchedule.SchedulePlan buildOriginalSchedule() {
        ProjectedSchedule.SchedulePlan original = new ProjectedSchedule.SchedulePlan();
        ProjectedSchedule.SemesterPlan s1 = new ProjectedSchedule.SemesterPlan("Semester 1");
        s1.courses.add(new ProjectedSchedule.Course(101, "MATH1210", 4, "Calculus I", "MWF 9:00-9:50"));
        s1.courses.add(new ProjectedSchedule.Course(127, "CMPS1270", 3, "Intro CS", "TTh 10:00-11:15"));
        s1.courses.add(new ProjectedSchedule.Course(201, "PS1010", 3, "Public Service", "T 14:00-16:00"));
        ProjectedSchedule.SemesterPlan s2 = new ProjectedSchedule.SemesterPlan("Semester 2");
        s2.courses.add(new ProjectedSchedule.Course(301, "WRIT1000", 3, "Writing Tier 1", "MW 11:00-12:15"));
        s2.courses.add(new ProjectedSchedule.Course(309, "MATH3090", 3, "Advanced Math", "TTh 13:00-14:15"));
        s2.courses.add(new ProjectedSchedule.Course(401, "LANG1010", 3, "Foreign Language", "F 10:00-12:00"));
        original.semesters.add(s1);
        original.semesters.add(s2);
        return original;
    }

    @Test
    public void testMissingCoursesForPathway_allMissing() {
        InMemoryCourseRepo cr = new InMemoryCourseRepo();
        cr.addPathway("CS", new ProjectedSchedule.Course(1, "CS101", 3, "Intro CS", ""),
                new ProjectedSchedule.Course(2, "CS102", 3, "Data Structures", ""));
        InMemoryUserRepo ur = new InMemoryUserRepo();
        ur.setCompleted("u1");

        ProjectedSchedule ps = new ProjectedSchedule(cr, ur);
        List<ProjectedSchedule.Course> missing = ps.missingCoursesForPathway("CS", "u1");
        assertEquals(2, missing.size());
    }

    @Test
    public void testMissingCoursesForPathway_someCompleted() {
        InMemoryCourseRepo cr = new InMemoryCourseRepo();
        cr.addPathway("MATH", new ProjectedSchedule.Course(10, "MATH101", 4, "Calc I", ""),
                new ProjectedSchedule.Course(11, "MATH201", 4, "Calc II", ""));
        InMemoryUserRepo ur = new InMemoryUserRepo();
        ur.setCompleted("u2", 10);

        ProjectedSchedule ps = new ProjectedSchedule(cr, ur);
        List<ProjectedSchedule.Course> missing = ps.missingCoursesForPathway("MATH", "u2");
        assertEquals(1, missing.size());
        assertEquals("MATH201", missing.get(0).code);
    }

    @Test
    public void testRemainingCredits() {
        InMemoryCourseRepo cr = new InMemoryCourseRepo();
        cr.addPathway("BIO",
                new ProjectedSchedule.Course(20, "BIO101", 4, "Intro Bio", ""),
                new ProjectedSchedule.Course(21, "BIO201", 4, "Genetics", ""),
                new ProjectedSchedule.Course(22, "BIO301", 3, "Ecology", ""));
        InMemoryUserRepo ur = new InMemoryUserRepo();
        ur.setCompleted("u3", 20);

        ProjectedSchedule ps = new ProjectedSchedule(cr, ur);
        int remaining = ps.remainingCredits("BIO", "u3");
        assertEquals(7, remaining);
    }

    @Test
    public void testIsNearCompletion_withinNineCredits() {
        InMemoryCourseRepo cr = new InMemoryCourseRepo();
        cr.addPathway("BIO",
                new ProjectedSchedule.Course(20, "BIO101", 4, "Intro Bio", ""),
                new ProjectedSchedule.Course(21, "BIO201", 4, "Genetics", ""),
                new ProjectedSchedule.Course(22, "BIO301", 3, "Ecology", ""));
        InMemoryUserRepo ur = new InMemoryUserRepo();
        ur.setCompleted("u3", 20);

        ProjectedSchedule ps = new ProjectedSchedule(cr, ur);
        assertTrue(ps.isNearCompletion("BIO", "u3"));
    }

    @Test
    public void testIsNearCompletion_moreThanNineCredits() {
        InMemoryCourseRepo cr = new InMemoryCourseRepo();
        cr.addPathway("CHEM",
                new ProjectedSchedule.Course(30, "CHEM101", 4, "General Chem", ""),
                new ProjectedSchedule.Course(31, "CHEM201", 4, "Organic Chem", ""),
                new ProjectedSchedule.Course(32, "CHEM301", 4, "Physical Chem", ""));
        InMemoryUserRepo ur = new InMemoryUserRepo();
        ur.setCompleted("u4");

        ProjectedSchedule ps = new ProjectedSchedule(cr, ur);
        assertFalse(ps.isNearCompletion("CHEM", "u4"));
    }

    @Test
    public void testEstimateSemestersNeeded() {
        InMemoryCourseRepo cr = new InMemoryCourseRepo();
        cr.addPathway("ENG",
                new ProjectedSchedule.Course(30, "ENG101", 4, "Eng I", ""),
                new ProjectedSchedule.Course(31, "ENG201", 4, "Eng II", ""),
                new ProjectedSchedule.Course(32, "ENG301", 3, "Eng III", ""),
                new ProjectedSchedule.Course(33, "ENG401", 3, "Eng IV", ""));
        InMemoryUserRepo ur = new InMemoryUserRepo();
        ur.setCompleted("u5");

        ProjectedSchedule ps = new ProjectedSchedule(cr, ur);
        int semesters = ps.estimateSemestersNeeded("ENG", "u5", 7);
        assertEquals(2, semesters);
    }

    @Test
    public void testProjectMissingCourses() {
        InMemoryCourseRepo cr = new InMemoryCourseRepo();
        cr.addPathway("ENG",
                new ProjectedSchedule.Course(30, "ENG101", 4, "Eng I", ""),
                new ProjectedSchedule.Course(31, "ENG201", 4, "Eng II", ""),
                new ProjectedSchedule.Course(32, "ENG301", 3, "Eng III", ""),
                new ProjectedSchedule.Course(33, "ENG401", 3, "Eng IV", ""));
        InMemoryUserRepo ur = new InMemoryUserRepo();
        ur.setCompleted("u6");

        ProjectedSchedule ps = new ProjectedSchedule(cr, ur);
        ProjectedSchedule.SchedulePlan plan = ps.projectMissingCourses("ENG", "u6", 7);
        assertEquals(2, plan.semesters.size());
        assertEquals(4, plan.totalCourses());
    }

    @Test
    public void testMergeWithExistingSchedule_respectsCreditLimits() {
        InMemoryCourseRepo cr = new InMemoryCourseRepo();
        cr.addPathway("AFRS",
                new ProjectedSchedule.Course(40, "AFRS1000", 3, "Intro Africana Studies", ""),
                new ProjectedSchedule.Course(41, "AFRS2000", 3, "Race & Inclusion", ""),
                new ProjectedSchedule.Course(42, "AFRS3000", 3, "Topics", ""),
                new ProjectedSchedule.Course(43, "AFRS4000", 3, "Senior Seminar", ""));
        InMemoryUserRepo ur = new InMemoryUserRepo();
        ur.setCompleted("s1", 40);

        ProjectedSchedule ps = new ProjectedSchedule(cr, ur);
        ProjectedSchedule.SchedulePlan original = buildOriginalSchedule();

        ProjectedSchedule.MergeResult result = ps.mergeWithExistingSchedule(original, "AFRS", "s1", 18);

        // No semester should exceed 21 credits
        for (ProjectedSchedule.SemesterPlan sem : result.mergedSchedule.semesters) {
            assertTrue(sem.totalCredits() <= 21, "Semester exceeds 21 credits: " + sem.semesterLabel);
        }

        // Added courses should be tracked
        List<String> addedCodes = new ArrayList<>();
        for (ProjectedSchedule.AddedCourseRecord a : result.addedCourses) {
            addedCodes.add(a.course.code);
        }
        assertTrue(addedCodes.contains("AFRS2000"));
    }

    @Test
    public void testMergeWithExistingSchedule_respectsEightSemesterCap() {
        InMemoryCourseRepo cr = new InMemoryCourseRepo();
        // Create many courses to exceed 8 semesters
        List<ProjectedSchedule.Course> many = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            many.add(new ProjectedSchedule.Course(500 + i, "C" + (500 + i), 3, "Course " + i, ""));
        }
        cr.addPathway("MANY", many.toArray(new ProjectedSchedule.Course[0]));
        InMemoryUserRepo ur = new InMemoryUserRepo();
        ur.setCompleted("uMany");

        ProjectedSchedule ps = new ProjectedSchedule(cr, ur);
        ProjectedSchedule.SchedulePlan original = new ProjectedSchedule.SchedulePlan();
        ProjectedSchedule.SemesterPlan s1 = new ProjectedSchedule.SemesterPlan("Semester 1");
        s1.courses.add(new ProjectedSchedule.Course(101, "X1", 12, "Full", ""));
        ProjectedSchedule.SemesterPlan s2 = new ProjectedSchedule.SemesterPlan("Semester 2");
        s2.courses.add(new ProjectedSchedule.Course(102, "X2", 12, "Full", ""));
        original.semesters.add(s1);
        original.semesters.add(s2);

        ProjectedSchedule.MergeResult result = ps.mergeWithExistingSchedule(original, "MANY", "uMany", 18);

        // Schedule should not exceed 8 semesters
        assertTrue(result.mergedSchedule.semesters.size() <= 8);

        // Check for UNSCHEDULED courses
        boolean hasUnscheduled = result.addedCourses.stream()
                .anyMatch(a -> "UNSCHEDULED".equals(a.semesterLabel));
        assertTrue(hasUnscheduled);

        // No semester should exceed 21 credits
        for (ProjectedSchedule.SemesterPlan sem : result.mergedSchedule.semesters) {
            assertTrue(sem.totalCredits() <= 21);
        }
    }

    @Test
    public void testMergeWithExistingSchedule_maintainsMinimumCredits() {
        InMemoryCourseRepo cr = new InMemoryCourseRepo();
        cr.addPathway("PHY",
                new ProjectedSchedule.Course(60, "PHY101", 4, "Physics I", ""),
                new ProjectedSchedule.Course(61, "PHY201", 4, "Physics II", ""),
                new ProjectedSchedule.Course(62, "PHY301", 3, "Modern Physics", ""));
        InMemoryUserRepo ur = new InMemoryUserRepo();
        ur.setCompleted("u7");

        ProjectedSchedule ps = new ProjectedSchedule(cr, ur);
        ProjectedSchedule.SchedulePlan original = new ProjectedSchedule.SchedulePlan();
        ProjectedSchedule.SemesterPlan s1 = new ProjectedSchedule.SemesterPlan("Semester 1");
        s1.courses.add(new ProjectedSchedule.Course(70, "GEN101", 10, "General Ed", ""));
        original.semesters.add(s1);

        ProjectedSchedule.MergeResult result = ps.mergeWithExistingSchedule(original, "PHY", "u7", 18);

        // Check that existing semester was brought up to at least 12 credits
        assertTrue(result.mergedSchedule.semesters.get(0).totalCredits() >= 12);
    }

    @Test
    public void testRenderScheduleComparison_outputFormat() {
        ProjectedSchedule.SchedulePlan original = buildOriginalSchedule();
        ProjectedSchedule.SchedulePlan merged = buildOriginalSchedule();
        merged.semesters.get(0).courses.add(
                new ProjectedSchedule.Course(50, "AFRS2000", 3, "Race & Inclusion", "T 15:00-17:00"));
        List<ProjectedSchedule.AddedCourseRecord> added = Collections.singletonList(
                new ProjectedSchedule.AddedCourseRecord(
                        new ProjectedSchedule.Course(50, "AFRS2000", 3, "Race & Inclusion", "T 15:00-17:00"),
                        "Semester 1")
        );

        String out = ProjectedSchedule.renderScheduleComparison(original, merged, added);
        assertTrue(out.contains("original schedule-"));
        assertTrue(out.contains("new schedule-"));
        assertTrue(out.contains("added-"));
        assertTrue(out.contains("AFRS2000"));
        assertTrue(out.contains("3cr"));
    }

    @Test
    public void testRenderScheduleComparison_includesCourseAttributes() {
        ProjectedSchedule.SchedulePlan original = new ProjectedSchedule.SchedulePlan();
        ProjectedSchedule.SchedulePlan merged = new ProjectedSchedule.SchedulePlan();
        ProjectedSchedule.SemesterPlan s1 = new ProjectedSchedule.SemesterPlan("Semester 1");
        s1.courses.add(new ProjectedSchedule.Course(100, "TEST101", 3, "Test Course", "MW 10:00-11:15"));
        merged.semesters.add(s1);

        List<ProjectedSchedule.AddedCourseRecord> added = Collections.singletonList(
                new ProjectedSchedule.AddedCourseRecord(
                        new ProjectedSchedule.Course(100, "TEST101", 3, "Test Course", "MW 10:00-11:15"),
                        "Semester 1")
        );

        String out = ProjectedSchedule.renderScheduleComparison(original, merged, added);
        assertTrue(out.contains("TEST101"));
        assertTrue(out.contains("3cr"));
        assertTrue(out.contains("MW 10:00-11:15"));
        assertTrue(out.contains("Test Course"));
    }
}
