package com.example.navisewebsite.repository;

public class StudentInfoSeeder {
    public static void main(String[] args) {
        // Ensure schemas exist
        DatabaseUtil.initializeDatabase();

        // Insert a sample student info row (linked to user_id 1 if exists; otherwise still creates row)
        StudentInfoRepository repo = new StudentInfoRepository();
        int inserted = repo.insertStudentInfo(1, "Sample", "Student", "Computer Science", "Mathematics", "CS101,CS102");
        System.out.println("Inserted rows into student_info: " + inserted);
        System.out.println("Done. Check student_info.db for the student_info table and data.");
    }
}
