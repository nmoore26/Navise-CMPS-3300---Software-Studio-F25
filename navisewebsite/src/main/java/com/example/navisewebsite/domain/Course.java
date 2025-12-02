package com.example.navisewebsite.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Course {
    private String courseID;
    private String course_name;
    private String professor_name;
    private int credit_hours;
    private String course_code;
    private String days_offered;
    private String time;
    private String building;
    private String room_number;
    private List<String> attribute = new ArrayList<>();
    private List<String> prerequisites = new ArrayList<>();
    private List<String> corequisites = new ArrayList<>();
    private List<String> term_offered = new ArrayList<>();


    public Course() {}

    public Course(String courseID, String course_name, String course_code, int credit_hours, String professor_name,
                  String days_offered, String time,
                  String building, String room_number, List <String> attribute, List <String> prerequisites, 
                  List <String> corequisites, 
                  List <String> term_offered) {
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
        if (prerequisites != null) this.prerequisites = new ArrayList<>(prerequisites);
        if (corequisites != null) this.corequisites = new ArrayList<>(corequisites);
        if (term_offered != null) this.term_offered = new ArrayList<>(term_offered);
                  }
        
    

    
    // --- CSV helpers ---
    public void setAttributeFromCSV(String csv) {
        this.attribute = csv == null || csv.isBlank() ? new ArrayList<>() : List.of(csv.split("\\s*,\\s*"));
        }

    public void setPrerequisitesFromCSV(String csv) {
        this.prerequisites = csv == null || csv.isBlank() ? new ArrayList<>() : List.of(csv.split("\\s*,\\s*"));
        }

    public void setCorequisitesFromCSV(String csv) {
        this.corequisites = csv == null || csv.isBlank() ? new ArrayList<>() : List.of(csv.split("\\s*,\\s*"));
        }

    public void setTermOfferedFromCSV(String csv) {
        this.term_offered = csv == null || csv.isBlank() ? new ArrayList<>() : List.of(csv.split("\\s*,\\s*"));
        }    
    // --- JavaBean getters/setters for Thymeleaf/Spring form binding ---
    public String getCourseID() { return courseID; }
    public void setCourseID(String courseID) { this.courseID = courseID; }

    public String getCourseName() { return course_name; }
    public void setCourseName(String courseName) { this.course_name = courseName; }

    public String getCourseCode() { return course_code; }
    public void setCourseCode(String courseCode) { this.course_code = courseCode; }

    public int getCreditHours() { return credit_hours; }
    public void setCreditHours(int creditHours) { this.credit_hours = creditHours; }

    public String getProfessor() { return professor_name; }
    public void setProfessor(String professor) { this.professor_name = professor; }

    public String getDaysOffered() { return days_offered; }
    public void setDaysOffered(String daysOffered) { this.days_offered = daysOffered; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getRoomNumber() { return room_number; }
    public void setRoomNumber(String roomNumber) { this.room_number = roomNumber; }

    // --- Existing backend logic (unchanged) ---
    public String get_courseID()           { return courseID; }
    public String get_course_name()     { return course_name; }
    public String get_professor_name()  { return professor_name; }
    public int get_credit_hours()       { return credit_hours; }
    public String get_course_code()     { return course_code; }
    public String get_days_offered()    { return days_offered; }
    public String get_time()            { return time; }
    public String get_building()        { return building; }
    public String get_room_number()        { return room_number; }
    public List<String> get_attribute() { return Collections.unmodifiableList(attribute); }
    public List<String> get_prerequisites() { return Collections.unmodifiableList(prerequisites); }
    public List<String> get_corequisites() { return Collections.unmodifiableList(corequisites); }
    public List<String> get_term_offered() { return Collections.unmodifiableList(term_offered); }
}