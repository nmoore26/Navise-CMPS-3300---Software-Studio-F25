package com.example.navisewebsite;

import com.example.navisewebsite.domain.Course;
import com.example.navisewebsite.domain.ICourse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class CourseAndICourseTest {

    private Course math;
    private Course cs;
    private Course eng;

    @BeforeEach
    public void setUp() {
        math = new Course(101, "Calculus", "Dr. Smith", 3, "MATH101", "MWF", "09:00", "Gibson", 101, null);
        cs   = new Course(102, "CS Intro", "Dr. Lee", 3, "CS101", "TTH", "10:30", "Stanley", 202, null);
        eng  = new Course(201, "English", "Dr. Brown", 3, "ENG201", "MWF", "11:00", "Jones", 105, Arrays.asList("Language"));
    }

    @Test
    public void testCourseGetters() {
        assertEquals(101, math.get_courseID());
        assertEquals("Calculus", math.get_course_name());
        assertEquals("Dr. Smith", math.get_professor_name());
        assertEquals(3, math.get_credit_hours());
        assertEquals("MATH101", math.get_course_code());
        assertEquals("MWF", math.get_days_offered());
        assertEquals("09:00", math.get_time());
        assertEquals("Gibson", math.get_building());
        assertEquals(101, math.get_room_number());
        assertTrue(math.get_attribute().isEmpty());

        assertEquals(Collections.singletonList("Language"), eng.get_attribute());
    }

    @Test
    public void testCourseAttributeDefensiveCopyAndImmutability() {
        List<String> attrs = new ArrayList<>(Arrays.asList("Lab", "Project"));
        Course withAttrs = new Course(301, "Physics", "Dr. Newton", 4, "PHYS301", "MWF", "14:00", "Hall", 12, attrs);

        //Mutate original list; Course should be unaffected
        attrs.add("Extra");
        assertEquals(Arrays.asList("Lab", "Project"), withAttrs.get_attribute());

        //Returned list should be unmodifiable
        List<String> returned = withAttrs.get_attribute();
        assertThrows(UnsupportedOperationException.class, () -> returned.add("Oops"));
    }

    @Test
    public void testICourseBasicOperations() throws IOException {
        ICourse repo = new InMemoryCourseRepo();

        //Initially empty
        assertTrue(repo.findAll().isEmpty());

        //Simulate "loading from CSV" by filename hint
        repo.add_course("seed_math.csv");//adds a Math course with id 1
        repo.add_course("seed_cs.csv");//adds a CS course with id 2
        assertEquals(2, repo.findAll().size());

        //Add a real course via the test double's helper
        ((InMemoryCourseRepo) repo).put(math);
        ((InMemoryCourseRepo) repo).put(cs);
        ((InMemoryCourseRepo) repo).put(eng);

        //Verify findById and findAll
        assertTrue(repo.findById(101).isPresent());
        assertEquals("Calculus", repo.findById(101).get().get_course_name());
        assertFalse(repo.findById(9999).isPresent());

        //Remove by id
        repo.remove_course(102); //remove "CS Intro"
        assertFalse(repo.findById(102).isPresent());

        //sanity on remaining count (2 seeded + 2 remaining from custom puts = 4)
        assertEquals(4, repo.findAll().size());
    }

    
    //Simple in-memory test double for ICourse.
    private static class InMemoryCourseRepo implements ICourse {
        private final Map<Integer, Course> store = new HashMap<>();
        private final AtomicInteger syntheticId = new AtomicInteger(0);

        @Override
        public void add_course(String file_name) throws IOException {
            //In a real impl, read CSV. For tests, add a synthetic course based on filename hint.
            int id = syntheticId.incrementAndGet();
            String name = file_name.toLowerCase().contains("cs") ? "CS Seed" : "Math Seed";
            String code = file_name.toLowerCase().contains("cs") ? "CS-SEED" : "MATH-SEED";

            Course c = new Course(
                    id,
                    name,
                    "Test Prof",
                    3,
                    code,
                    "MWF",
                    "08:00",
                    "Test Hall",
                    1,
                    Collections.singletonList("Seeded")
            );
            store.put(c.get_courseID(), c);
        }

        @Override
        public void remove_course(int courseID) {
            store.remove(courseID);
        }

        @Override
        public Optional<Course> findById(int id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Collection<Course> findAll() {
            return Collections.unmodifiableCollection(store.values());
        }

        //Helper for tests to insert specific courses
        void put(Course c) {
            store.put(c.get_courseID(), c);
        }
    }
}