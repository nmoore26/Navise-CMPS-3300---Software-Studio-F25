package com.example.navisewebsite.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Course {
    private int courseID;
    private String course_name;
    private String professor_name;
    private int credit_hours;
    private String course_code;
    private String days_offered;
    private String time;
    private String building;
    private int room_number;
    private List<String> attribute = new ArrayList<>();

    public Course() {}

    public Course(int courseID, String course_name, String professor_name, int credit_hours,
                  String course_code, String days_offered, String time,
                  String building, int room_number, List<String> attribute) {
        this.courseID = courseID;
        this.course_name = course_name;
        this.professor_name = professor_name;
        this.credit_hours = credit_hours;
        this.course_code = course_code;
        this.days_offered = days_offered;
        this.time = time;
        this.building = building;
        this.room_number = room_number;
        if (attribute != null) this.attribute = new ArrayList<>(attribute);
    }

    public int get_courseID()           { return courseID; }
    public String get_course_name()     { return course_name; }
    public String get_professor_name()  { return professor_name; }
    public int get_credit_hours()       { return credit_hours; }
    public String get_course_code()     { return course_code; }
    public String get_days_offered()    { return days_offered; }
    public String get_time()            { return time; }
    public String get_building()        { return building; }
    public int get_room_number()        { return room_number; }
    public List<String> get_attribute() { return Collections.unmodifiableList(attribute); }
}