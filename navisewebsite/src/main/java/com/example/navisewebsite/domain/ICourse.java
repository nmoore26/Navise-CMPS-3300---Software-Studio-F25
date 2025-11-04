package com.example.navisewebsite.domain;
import java.io.IOException;
import java.util.Optional;
import java.util.List;



public interface ICourse {
    void add_course(Course course, String sheet_name) throws IOException; // load from CSV (pandas later)
    void remove_course(String courseID,String sheetName);                     // delete by id

    // Useful extras so your app can read data:
    Optional<Course> findById(String id, String sheet_name);
    List<Course> findAll(); // get all courses
}