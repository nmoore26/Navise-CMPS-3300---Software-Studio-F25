package com.example.navisewebsite;

import com.example.navisewebsite.domain.Course;
import com.example.navisewebsite.domain.ICourse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CourseAndICourseTest {

    private Course math;
    private Course cs;
    private Course eng;

    @BeforeEach
    public void setUp() {
        math = new Course(
                "101",
                "Calculus",
                "MATH101",
                3,
                "Dr. Smith",
                "MWF",
                "09:00",
                "Gibson",
                "101",
                Arrays.asList("STEM", "Writing Intensive"),
                Arrays.asList("MATH099"),
                Collections.emptyList(),
                Arrays.asList("Fall", "Spring")
        );

        cs = new Course(
                "102",
                "CS Intro",
                "CS101",
                3,
                "Dr. Lee",
                "TTH",
                "10:30",
                "Stanley",
                "202",
                Collections.singletonList("STEM"),
                Collections.emptyList(),
                Collections.emptyList(),
                Arrays.asList("Fall")
        );

        eng = new Course(
                "201",
                "English",
                "ENG201",
                3,
                "Dr. Brown",
                "MWF",
                "11:00",
                "Jones",
                "105",
                Collections.emptyList(),
                Arrays.asList("ENG101"),
                Arrays.asList("ENG102"),
                Arrays.asList("Spring")
        );
    }

    @Test
    public void testCourseGettersReflectNewFields() {
        assertEquals("101", math.get_courseID());
        assertEquals("Calculus", math.get_course_name());
        assertEquals("Dr. Smith", math.get_professor_name());
        assertEquals(3, math.get_credit_hours());
        assertEquals("MATH101", math.get_course_code());
        assertEquals("MWF", math.get_days_offered());
        assertEquals("09:00", math.get_time());
        assertEquals("Gibson", math.get_building());
        assertEquals("101", math.get_room_number());

        assertEquals(Arrays.asList("STEM", "Writing Intensive"), math.get_attribute());
        assertEquals(Collections.singletonList("MATH099"), math.get_prerequisites());
        assertEquals(Collections.emptyList(), math.get_corequisites());
        assertEquals(Arrays.asList("Fall", "Spring"), math.get_term_offered());
    }

    @Test
    public void testListFieldsAreDefensivelyCopiedAndUnmodifiable() {
        List<String> attrs = new ArrayList<>(Arrays.asList("Lab", "Project"));
        List<String> pres  = new ArrayList<>(Collections.singletonList("PHYS101"));
        List<String> cores = new ArrayList<>(Collections.singletonList("MATH201"));
        List<String> terms = new ArrayList<>(Arrays.asList("Summer"));

        Course physics = new Course(
                "301",
                "Physics",
                "PHYS301",
                4,
                "Dr. Newton",
                "MWF",
                "14:00",
                "Hall",
                "012",
                attrs, pres, cores, terms
        );

        //cahnge original lists after construction
        attrs.add("Extra");
        pres.clear();
        cores.add("CHEM101");
        terms.add("Fall");

        assertEquals(Arrays.asList("Lab", "Project"), physics.get_attribute());
        assertEquals(Collections.singletonList("PHYS101"), physics.get_prerequisites());
        assertEquals(Collections.singletonList("MATH201"), physics.get_corequisites());
        assertEquals(Collections.singletonList("Summer"), physics.get_term_offered());

        //cant modify returned lists
        assertThrows(UnsupportedOperationException.class, () -> physics.get_attribute().add("X"));
        assertThrows(UnsupportedOperationException.class, () -> physics.get_prerequisites().add("X"));
        assertThrows(UnsupportedOperationException.class, () -> physics.get_corequisites().add("X"));
        assertThrows(UnsupportedOperationException.class, () -> physics.get_term_offered().add("X"));
    }

    @Test
    public void testICourse_SheetScopedCRUD_andFindAll() throws IOException {
        ICourse repo = new InMemoryCourseRepo();

        assertTrue(repo.findAll().isEmpty());

        //add to two different sheets
        repo.add_course(math, "SheetA");
        repo.add_course(cs,   "SheetA");
        repo.add_course(eng,  "SheetB");

        //findById is sheet-aware
        assertTrue(repo.findById("101", "SheetA").isPresent());
        assertFalse(repo.findById("101", "SheetB").isPresent()); // same id absent on SheetB
        assertEquals("Calculus", repo.findById("101", "SheetA").get().get_course_name());

        //remove only affects the specified sheet
        repo.remove_course("102", "SheetA"); // remove CS on SheetA
        assertFalse(repo.findById("102", "SheetA").isPresent());

        //still present elsewhere (ENG on SheetB)
        assertTrue(repo.findById("201", "SheetB").isPresent());

        //findAll returns all courses across sheets
        List<Course> all = repo.findAll();
        //remaining: math (SheetA) + eng (SheetB) = 2
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(c -> c.get_courseID().equals("101")));
        assertTrue(all.stream().anyMatch(c -> c.get_courseID().equals("201")));
    }

    private static class InMemoryCourseRepo implements ICourse {
        private final Map<String, Map<String, Course>> store = new HashMap<>();

        @Override
        public void add_course(Course course, String sheet_name) throws IOException {
            store.computeIfAbsent(sheet_name, k -> new LinkedHashMap<>())
                 .put(course.get_courseID(), course);
        }

        @Override
        public void remove_course(String courseID, String sheetName) {
            Map<String, Course> sheet = store.get(sheetName);
            if (sheet != null) {
                sheet.remove(courseID);
                if (sheet.isEmpty()) store.remove(sheetName);
            }
        }

        @Override
        public Optional<Course> findById(String id, String sheet_name) {
            Map<String, Course> sheet = store.get(sheet_name);
            return Optional.ofNullable(sheet == null ? null : sheet.get(id));
        }

        @Override
        public List<Course> findAll() {
            List<Course> all = new ArrayList<>();
            for (Map<String, Course> sheet : store.values()) {
                all.addAll(sheet.values());
            }
            return Collections.unmodifiableList(all);
        }
    }
}
