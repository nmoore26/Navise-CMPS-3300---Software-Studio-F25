package com.example.navisewebsite.repository;

import com.example.navisewebsite.domain.Course;
import java.util.List;
import java.util.Optional;

public interface CourseRepositoryInterface {
    void addCourse(Course course);
    void removeCourse(Course course);
    Optional<Course> findById(String courseID);
    List<Course> findAll();
    int countCourses();
    void insertCourse(String courseId, String courseName, String courseCode, 
                     int creditHours, String professor, String days, String time, 
                     String building, String room, String attributes, 
                     String prerequisites, String corequisites, String terms);
    void addNTCRequirement(String requirement, int num);
}