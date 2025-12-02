package com.example.navisewebsite.domain;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// Minor specialization of Path with a maximum-hours limit.

public class Minor extends Path {
    private static final int DEFAULT_MAX_HOURS = 18;
    private int maxHours;

    public Minor() {
        this(null, DEFAULT_MAX_HOURS);
    }

    public Minor(String pathName) {
        this(pathName, DEFAULT_MAX_HOURS);
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

    //Return the actual mutable requirements list stored on Path.
    //If Path stored an immutable or fixed-size list, replace it with a mutable ArrayList copy
    //and set it back into the Path instance so callers can mutate it and changes persist.

    @Override
    public List<Course> getRequirements() {
        List<Course> viaReflection = ensureMutableRequirementsViaReflection();
        if (viaReflection != null) {
            return viaReflection;
        }

        List<Course> sup = super.getRequirements();
        return sup == null ? new ArrayList<>() : sup;
    }

    //ry to access Path.requirements via reflection and ensure it is a mutable ArrayList.
    //Returns the mutable list if reflection succeeded, otherwise null.

    @SuppressWarnings("unchecked")
    private List<Course> ensureMutableRequirementsViaReflection() {
        try {
            Field requirementsField = Path.class.getDeclaredField("requirements");
            requirementsField.setAccessible(true);
            Object raw = requirementsField.get(this);

            if (raw == null) {
                List<Course> newList = new ArrayList<>();
                requirementsField.set(this, newList);
                return newList;
            }

            if (raw instanceof List) {
                List<Course> list = (List<Course>) raw;
                if (list instanceof ArrayList) {
                    return list;
                }
                List<Course> mutable = new ArrayList<>(list);
                requirementsField.set(this, mutable);
                return mutable;
            }
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
            // Reflection failed; fall back to superclass behavior in caller.
        }
        return null;
    }

    // Business logic methods - use parent class helpers

    public boolean isWithinLimit(List<Course> completed) {
        int completedHours = getCompletedHours(completed);
        return completedHours <= maxHours;
    }

    public int getHoursRemaining(List<Course> completed) {
        int completedHours = getCompletedHours(completed);
        return Math.max(0, maxHours - completedHours);
    }


    // Delegate to Path.calculateTotalHours to use the same, central credit-extraction logic.

    public int getCompletedHours(List<Course> completed) {
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

}
