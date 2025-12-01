package com.example.navisewebsite.repository;

import java.io.File;

/*
 The purpose of the DatabaseInitializer class is to initialize the database
 and import data from an Excel file into the database.
*/

public class DatabaseInitializer {

    public static void main(String[] args) {
        try {

            // 1. Initialize database (creates tables)
            DatabaseUtil.initializeDatabase();
            System.out.println("Database initialized successfully.");

            // 2. Import courses from Excel
            ExcelToDatabaseImporter importer = new ExcelToDatabaseImporter();

            // 👉 Update this path to your actual Excel file location
            File excelFile = new File("navisewebsite/src/main/java/com/example/navisewebsite/repository/courses.xlsx");

            importer.importExcel(excelFile);
            System.out.println("Excel data imported successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
