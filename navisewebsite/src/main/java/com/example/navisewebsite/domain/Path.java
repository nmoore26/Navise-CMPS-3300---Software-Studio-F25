package com.example.navisewebsite.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Path {
    private String path_name;
    private List<Course> requirements;
    
    public Path() {
        this.requirements = new ArrayList<>();
    }
    
    public Path(String path_name) {
        this();
        this.path_name = path_name;
    }
    
    public Path(String path_name, List<Course> requirements) {
        this.path_name = path_name;
        this.requirements = (requirements != null) ? new ArrayList<>(requirements) : new ArrayList<>();
    }
    
    public String get_path_name() { return path_name; }
    public List<Course> get_requirements() { return Collections.unmodifiableList(requirements); }
    
    public void set_path_name(String path_name) { this.path_name = path_name; }
    public void set_requirements(List<Course> requirements) { 
        this.requirements = new ArrayList<>(requirements); 
    }
    
    public void add_req(Course c) {
        if (c != null) requirements.add(c);
    }
    
    public boolean rm_req(Course c) {
        return requirements.remove(c);
    }
    
    public boolean has_req(Course c) {
        return requirements.contains(c);
    }
    
    public int total_hours() {
        return requirements.stream()
                .mapToInt(Course::get_credit_hours)
                .sum();
    }
    
    public int req_count() {
        return requirements.size();
    }
    
    public boolean is_empty() {
        return requirements.isEmpty();
    }
}