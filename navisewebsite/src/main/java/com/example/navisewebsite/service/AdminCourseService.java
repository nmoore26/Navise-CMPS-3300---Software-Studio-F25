package com.example.navisewebsite.service;

import com.example.navisewebsite.domain.Course;
import org.springframework.stereotype.Service;
import com.example.navisewebsite.repository.CourseRepository;
import com.example.navisewebsite.repository.ProgramRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AdminCourseService {

    private final CourseRepository courseRepository;
    private final ProgramRepository programRepository;

    public AdminCourseService() {
        this.courseRepository = new CourseRepository();
        this.programRepository = new ProgramRepository();
    }

    /**
     * Adds a course and links it to a program (major or minor)
     *
     * @param course       Course object to add
     * @param programName  Name of the program
     * @param programType  Either "Major" or "Minor"
     */
    public void add_course(Course course, String programName, String programType) {
        // 1. Add the course to the courses table
        courseRepository.addCourse(course);

        // 2. Add the program (major/minor) if it doesn't exist
        int programId = programRepository.addProgram(programName, programType);

        // 3. Link the course to the program in program_courses table
        programRepository.addCourseToProgram(programId, course.get_courseID());
    }

    /**
     * Retrieve a course by ID
     *
     * @param courseID Course ID
     * @return Optional<Course>
     */
    public Optional<Course> findById(String courseID) {
        return courseRepository.findById(courseID);
    }

    /**
     * Retrieve all courses
     *
     * @return List of all courses
     */
    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * Remove a course by ID
     *
     * @param courseID ID of the course to remove
     */
    public void remove_course(Course course) {
        courseRepository.removeCourse(course);
        // Optional: also remove from program_courses if desired
    }
}

