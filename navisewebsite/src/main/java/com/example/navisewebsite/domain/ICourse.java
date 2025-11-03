package com.example.navisewebsite.domain;

import com.example.navisewebsite.domain.Course;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

public interface ICourse {
    void add_course(String file_name) throws IOException; // load from CSV (pandas later)
    void remove_course(int courseID);                     // delete by id

    // Useful extras so your app can read data:
    Optional<Course> findById(int id);
    Collection<Course> findAll();
}