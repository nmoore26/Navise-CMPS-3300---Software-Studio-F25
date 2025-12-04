package com.example.navisewebsite.util;

import com.example.navisewebsite.domain.Course;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class to extract credit hours from Course objects.
 * Centralizes reflection logic to handle various Course implementations
 * and naming conventions (snake_case vs camelCase).
 * 
 * This eliminates code duplication across Student, Path, Major, and Minor classes.
 */
public class CourseCreditsExtractor {
    
    private static final String[] GETTER_NAMES = {
        "getCredits", "getCreditHours", "get_credit_hours"
    };
    
    private static final String[] FIELD_NAMES = {
        "credits", "credit_hours", "creditHours"
    };
    
    /**
     * Extract credit hours from a Course object using multiple strategies:
     * 1. Direct method call (get_credit_hours)
     * 2. Reflection on common getter names
     * 3. Reflection on common field names
     * 
     * @param course the Course object
     * @return credit hours or 0 if unable to extract
     */
    public static int extractCredits(Course course) {
        if (course == null) {
            return 0;
        }
        
        // Strategy 1: Try direct call
        try {
            return course.get_credit_hours();
        } catch (Throwable ignored) {
            // Continue to reflection strategies
        }
        
        // Strategy 2: Try getter methods
        for (String getterName : GETTER_NAMES) {
            Integer credits = tryInvokeIntMethod(course, getterName);
            if (credits != null) {
                return credits;
            }
        }
        
        // Strategy 3: Try field access
        for (String fieldName : FIELD_NAMES) {
            Integer credits = tryReadIntField(course, fieldName);
            if (credits != null) {
                return credits;
            }
        }
        
        return 0;
    }
    
    private static Integer tryInvokeIntMethod(Course course, String methodName) {
        try {
            Method method = course.getClass().getMethod(methodName);
            Object result = method.invoke(course);
            return convertToInt(result);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }
    
    private static Integer tryReadIntField(Course course, String fieldName) {
        try {
            Field field = course.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(course);
            return convertToInt(value);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return null;
        }
    }
    
    private static Integer convertToInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                // Not parseable
            }
        }
        return null;
    }
}
