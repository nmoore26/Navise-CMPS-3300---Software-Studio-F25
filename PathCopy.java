package com.example.navisewebsite.domain;

import com.example.navisewebsite.util.CourseCreditsExtractor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents an academic path (major/minor) with a list of course requirements.
 * 
 * REFACTORED: Removed duplicate credit extraction logic.
 * Now uses CourseCreditsExtractor utility class.
 */
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

    // Getters and Setters
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

    // Hour calculation methods
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

    /**
     * REFACTORED: Now uses CourseCreditsExtractor utility.
     * Removed all reflection code - centralized in one place.
     * 
     * This method is used by Major and Minor subclasses.
     */
    protected int calculateTotalHours(List<Course> courses) {
        if (courses == null) {
            return 0;
        }
        return courses.stream()
                .mapToInt(CourseCreditsExtractor::extractCredits)
                .sum();
    }
}
