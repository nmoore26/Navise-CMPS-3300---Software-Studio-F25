package com.example.navisewebsite.domain;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Major specialization of Path with a minimum-hours requirement.
 */
public class Major extends Path {
    private static final int DEFAULT_MIN_HOURS = 0;
    private int minHours;

    public Major() {
        this(null, DEFAULT_MIN_HOURS);
    }

    public Major(String pathName) {
        this(pathName, DEFAULT_MIN_HOURS);
    }

    public Major(String pathName, int minHours) {
        super(pathName);
        this.minHours = minHours;
    }

    public Major(String pathName, List<Course> requirements, int minHours) {
        super(pathName, requirements);
        this.minHours = minHours;
    }

    public int getMinHours() {
        return minHours;
    }

    public void setMinHours(int minHours) {
        this.minHours = minHours;
    }

    //Return the actual mutable requirements list stored on Path.
    // If Path stored an immutable or fixed-size list, replace it with a mutable ArrayList copy
    // and set it back into the Path instance so callers can mutate it and changes persist.
    @Override
    public List<Course> getRequirements() {
        List<Course> viaReflection = ensureMutableRequirementsViaReflection();
        if (viaReflection != null) {
            return viaReflection;
        }

        List<Course> sup = super.getRequirements();
        return sup == null ? new ArrayList<>() : sup;
    }

    @SuppressWarnings("unchecked")
    private List<Course> ensureMutableRequirementsViaReflection() {
        try {
            Field f = Path.class.getDeclaredField("requirements");
            f.setAccessible(true);
            Object raw = f.get(this);

            if (raw == null) {
                List<Course> newList = new ArrayList<>();
                f.set(this, newList);
                return newList;
            }

            if (raw instanceof List) {
                List<Course> list = (List<Course>) raw;
                if (list instanceof ArrayList) {
                    return list;
                }
                List<Course> mutable = new ArrayList<>(list);
                f.set(this, mutable);
                return mutable;
            }
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
            // Reflection failed; fall back to superclass behavior in caller.
        }
        return null;
    }

    public boolean meetsRequirements(List<Course> completed) {
        List<Course> reqs = getRequirements();
        if (reqs == null || reqs.isEmpty()) return false;
        if (completed == null) return false;

        for (Course req : reqs) {
            boolean found = completed.stream().anyMatch(c -> courseMatches(req, c));
            if (!found) return false;
        }
        return true;
    }

    public int getHoursNeeded(List<Course> completed) {
        List<Course> uncompleted = getUncompletedRequirements(completed);
        return calculateTotalHours(uncompleted);
    }

    public int getCompletedHours(List<Course> completed) {
        List<Course> completedReqs = getCompletedRequirements(completed);
        return calculateTotalHours(completedReqs);
    }

    public boolean canGraduate(List<Course> completed) {
        return meetsRequirements(completed) &&
               getCompletedHours(completed) >= minHours;
    }

    public boolean meetsMinimumHours(List<Course> completed) {
        return getCompletedHours(completed) >= minHours;
    }

    // Helper: compare two Course objects by code, then id, then equals
    private boolean courseMatches(Course a, Course b) {
        if (a == null || b == null) return false;

        String codeA = tryExtractString(a, "code", "courseCode", "course_code", "getCode", "get_course_code", "getCourseCode");
        String codeB = tryExtractString(b, "code", "courseCode", "course_code", "getCode", "get_course_code", "getCourseCode");
        if (codeA != null && !codeA.isEmpty() && codeB != null && !codeB.isEmpty()) {
            if (codeA.equals(codeB)) return true;
        }

        String idA = tryExtractString(a, "id", "courseId", "course_id", "getId", "get_course_id", "getCourseId");
        String idB = tryExtractString(b, "id", "courseId", "course_id", "getId", "get_course_id", "getCourseId");
        if (idA != null && !idA.isEmpty() && idB != null && !idB.isEmpty()) {
            if (idA.equals(idB)) return true;
        }

        return Objects.equals(a, b);
    }


    // Try multiple method and field names to extract a string value from an object.
    
    private String tryExtractString(Object obj, String... names) {
        if (obj == null) return null;
        Class<?> cls = obj.getClass();

        for (String name : names) {
            // try method with the exact name
            try {
                java.lang.reflect.Method m = cls.getMethod(name);
                try {
                    Object val = m.invoke(obj);
                    if (val != null) return String.valueOf(val);
                } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                    // continue to next attempt
                }
            } catch (NoSuchMethodException ignore) {}

            // try "getX" variant if name is not already a getter
            if (!name.startsWith("get")) {
                try {
                    String getter = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
                    java.lang.reflect.Method m2 = cls.getMethod(getter);
                    try {
                        Object val = m2.invoke(obj);
                        if (val != null) return String.valueOf(val);
                    } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                        // continue
                    }
                } catch (NoSuchMethodException ignore) {}
            }

            // try field access
            try {
                java.lang.reflect.Field f = cls.getDeclaredField(name);
                f.setAccessible(true);
                try {
                    Object val = f.get(obj);
                    if (val != null) return String.valueOf(val);
                } catch (IllegalAccessException e) {
                    // continue
                }
            } catch (NoSuchFieldException ignore) {}
        }

        return null;
    }
}
