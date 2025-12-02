package com.example.navisewebsite.domain;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Minor extends Path {
    private static final int DEFAULT_MAX_HOURS = 18;
    private int maxHours;
    
    public Minor() {
        super();
        this.maxHours = DEFAULT_MAX_HOURS;
    }
    
    public Minor(String pathName) {
        super(pathName);
        this.maxHours = DEFAULT_MAX_HOURS;
    }
    
    public Minor(String pathName, int maxHours) {
        super(pathName);
        this.maxHours = maxHours;
    }
    
    public Minor(String pathName, List<Course> requirements, int maxHours) {
        super(pathName, requirements);
        this.maxHours = maxHours;
    }
    
    public int getMaxHours() { 
        return maxHours; 
    }
    
    public void setMaxHours(int maxHours) { 
        this.maxHours = maxHours; 
    }

    /**
     * Return the actual mutable requirements list stored on Path.
     * If Path stored an immutable or fixed-size list, replace it with a mutable ArrayList copy
     * and set it back into the Path instance so callers can mutate it and changes persist.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Course> getRequirements() {
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
            // fallback below
        }
        List<Course> sup = super.getRequirements();
        if (sup == null) {
            return new ArrayList<>();
        }
        return sup;
    }
    
    // Business logic methods - now using parent class helpers
    public boolean isWithinLimit(List<Course> completed) {
        int completedHours = getCompletedHours(completed);
        return completedHours <= maxHours;
    }
    
    public int getHoursRemaining(List<Course> completed) {
        int completedHours = getCompletedHours(completed);
        return Math.max(0, maxHours - completedHours);
    }
    
    /**
     * Robust credit extraction:
     * Try common getter names and field names to obtain credit hours from Course objects.
     * If none found, treat that course as 0 credits.
     */
    
    public int getCompletedHours(List<Course> completed) {
    // Delegate to Path.calculateTotalHours to use the same, central credit-extraction logic
        return calculateTotalHours(completed);
    }

    
    public boolean isComplete(List<Course> completed) {
        if (completed == null || completed.isEmpty()) {
            return false;
        }
        
        // Minor is complete if all requirements met and within hour limit
        List<Course> uncompleted = getUncompletedRequirements(completed);
        return uncompleted.isEmpty() && isWithinLimit(completed);
    }
    
    public boolean meetsRequirements(List<Course> completed) {
        if (completed == null || getRequirements().isEmpty()) {
            return false;
        }
        List<Course> uncompleted = getUncompletedRequirements(completed);
        return uncompleted.isEmpty();
    }

    // Try multiple method and field names to extract an int credit value
private int extractCredits(Course c) {
    if (c == null) return 0;
    Class<?> cls = c.getClass();

    // Try common getter methods
    String[] methodNames = new String[] {
        "get_credit_hours", "getCreditHours", "getCredits", "get_creditHours", "get_credit_hours", "get_credit"
    };
    for (String mname : methodNames) {
        try {
            java.lang.reflect.Method m = cls.getMethod(mname);
            try {
                Object val = m.invoke(c);
                if (val instanceof Number) return ((Number) val).intValue();
                if (val != null) {
                    try { return Integer.parseInt(String.valueOf(val)); } catch (NumberFormatException ignore) {}
                }
            } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                // invocation failed; try next
            }
        } catch (NoSuchMethodException ignore) {}

        // try "getX" variant if not already a getter
        if (!mname.startsWith("get")) {
            try {
                String getter = "get" + Character.toUpperCase(mname.charAt(0)) + mname.substring(1);
                java.lang.reflect.Method m2 = cls.getMethod(getter);
                try {
                    Object val = m2.invoke(c);
                    if (val instanceof Number) return ((Number) val).intValue();
                    if (val != null) {
                        try { return Integer.parseInt(String.valueOf(val)); } catch (NumberFormatException ignore) {}
                    }
                } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                    // ignore and continue
                }
            } catch (NoSuchMethodException ignore) {}
        }
    }

    // Try common field names
    String[] fieldNames = new String[] { "credit_hours", "credits", "creditHours", "credit" };
    for (String fname : fieldNames) {
        try {
            java.lang.reflect.Field f = cls.getDeclaredField(fname);
            f.setAccessible(true);
            try {
                Object val = f.get(c);
                if (val instanceof Number) return ((Number) val).intValue();
                if (val != null) {
                    try { return Integer.parseInt(String.valueOf(val)); } catch (NumberFormatException ignore) {}
                }
            } catch (IllegalAccessException e) {
                // cannot access field; continue
            }
        } catch (NoSuchFieldException ignore) {}
    }

    // Fallback: 0 credits if nothing found
    return 0;
}

}
