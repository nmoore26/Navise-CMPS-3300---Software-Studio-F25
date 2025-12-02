package com.example.navisewebsite.domain;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Represents an academic path (major/minor) with a list of course requirements.

public class Path {
    private String pathName;
    private List<Course> requirements;

    public Path() {
        this.requirements = new ArrayList<>();
    }

    public Path(String pathName) {
        this();
        this.pathName = pathName;
    }

    public Path(String pathName, List<Course> requirements) {
        this.pathName = pathName;
        this.requirements = (requirements != null) ? new ArrayList<>(requirements) : new ArrayList<>();
    }

    // Getters and Setters with Java conventions
    public String getPathName() {
        return pathName;
    }

    public List<Course> getRequirements() {
        return requirements;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public void setRequirements(List<Course> requirements) {
        this.requirements = (requirements != null) ? new ArrayList<>(requirements) : new ArrayList<>();
    }

    // Course management methods
    public void addRequirement(Course course) {
        if (course != null) {
            requirements.add(course);
        }
    }

    public boolean removeRequirement(Course course) {
        return requirements.remove(course);
    }

    public boolean hasRequirement(Course course) {
        return requirements.contains(course);
    }

    // Hour calculation methods - extracted for reuse
    public int getTotalRequiredHours() {
        return calculateTotalHours(requirements);
    }

    public int getRequirementCount() {
        return requirements.size();
    }

    public boolean isEmpty() {
        return requirements.isEmpty();
    }

    // Protected helper methods for subclasses
    protected List<Course> getUncompletedRequirements(List<Course> completed) {
        if (completed == null) {
            return new ArrayList<>(requirements);
        }
        return requirements.stream()
                .filter(req -> !completed.contains(req))
                .collect(Collectors.toList());
    }

    protected List<Course> getCompletedRequirements(List<Course> completed) {
        if (completed == null) {
            return new ArrayList<>();
        }
        return requirements.stream()
                .filter(completed::contains)
                .collect(Collectors.toList());
    }

    protected int calculateTotalHours(List<Course> courses) {
        if (courses == null) {
            return 0;
        }
        return courses.stream()
                .mapToInt(this::getCourseCredits)
                .sum();
    }

    /*
     Attempt to extract credit hours from a Course instance using a sequence of strategies:
       1) Direct call to a known method if present on the compile-time type.
       2) Reflection: try common getter method names.
       3) Reflection: try common field names.
     
      If all attempts fail, returns 0.
     */
    private int getCourseCredits(Course course) {
        if (course == null) return 0;

        // 1) Try direct method call if available on the Course type used at compile time.
        try {
            return course.get_credit_hours();
        } catch (Throwable ignored) {
            // fall through to reflective attempts
        }

        // 2) Try common getter methods via reflection
        String[] getterNames = new String[] { "getCredits", "getCreditHours", "get_credit_hours" };
        for (String name : getterNames) {
            Integer val = tryInvokeIntMethod(course, name);
            if (val != null) return val;
        }

        // 3) Try common field names via reflection
        String[] fieldNames = new String[] { "credits", "credit_hours", "creditHours" };
        for (String fname : fieldNames) {
            Integer val = tryReadIntField(course, fname);
            if (val != null) return val;
        }

        return 0;
    }

    private Integer tryInvokeIntMethod(Course course, String methodName) {
        try {
            Method m = course.getClass().getMethod(methodName);
            Object result = m.invoke(course);
            if (result instanceof Number) {
                return ((Number) result).intValue();
            }
            if (result != null) {
                try {
                    return Integer.parseInt(String.valueOf(result));
                } catch (NumberFormatException ignored) {
                    // not parseable; continue
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            // method not present or not accessible; continue
        }
        return null;
    }

    private Integer tryReadIntField(Course course, String fieldName) {
        try {
            Field f = course.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object val = f.get(course);
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
            if (val != null) {
                try {
                    return Integer.parseInt(String.valueOf(val));
                } catch (NumberFormatException ignored) {
                    // not parseable; continue
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            // field not present or not accessible; continue
        }
        return null;
    }
}
