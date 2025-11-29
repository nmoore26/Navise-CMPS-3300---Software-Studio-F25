package com.example.navisewebsite.service;
import java.io.IOException;
import java.util.Optional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Service;

import com.example.navisewebsite.domain.Course;
import com.example.navisewebsite.domain.ICourse;
import java.util.ArrayList;
import java.util.List;


import com.example.navisewebsite.repository.ExcelCourseRepository;

@Service
public class AdminCourseService implements ICourse {
    // Path to the Excel file
    private final ExcelCourseRepository repository;

    public AdminCourseService() throws IOException {
        this.repository = new ExcelCourseRepository();
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("course", new Course());
        return "admin";
    }

    @Override
    public void add_course(Course course, String sheetName) {
        repository.addCourseToSheet(course, sheetName);
    }

    @Override
    public Optional<Course> findById(String courseID, String sheetName) {
        return repository.findCourseById(courseID, sheetName);
    }

    @Override
    public void remove_course(String courseID, String sheetName) {
        //repository.removeCourse(courseID, sheetName);
    }

    @Override
    public List<Course> findAll() {
        // repository.findAll();
        return new ArrayList<>();
    }
}
