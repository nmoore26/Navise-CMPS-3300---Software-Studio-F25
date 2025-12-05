package com.example.navisewebsite.config;

import com.example.navisewebsite.repository.CourseRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class CourseDataInitializer {

    @Autowired
    private CourseRepository courseRepository;

    @Bean
    public ApplicationRunner seedCoursesFromXlsx(CommandLineRunner initDatabasesRunner) {
        return args -> {
            // First ensure database tables exist
            ensureDatabaseTablesExist();
            
            // Only run if courses table is empty
            int existing = courseRepository.countCourses();
            if (existing > 0) {
                return; // Already seeded
            }

            // Load courses.xlsx from resources
            try (InputStream is = getClass().getResourceAsStream("/data/courses.xlsx")) {
                if (is == null) {
                    System.err.println("courses.xlsx not found in resources/data; skipping seed.");
                    return;
                }
                try (Workbook wb = new XSSFWorkbook(is)) {
                    // Track duplicate course IDs to create unique ones
                    java.util.Map<String, Integer> courseIdCounter = new java.util.HashMap<>();
                    
                    // First pass: extract all courses and populate courses table
                    // Process each sheet (each sheet is a program)
                    for (int sheetIdx = 0; sheetIdx < wb.getNumberOfSheets(); sheetIdx++) {
                        String sheetName = wb.getSheetName(sheetIdx);
                        
                        // Skip NTC Requirements sheet
                        if (sheetName.equals("NTC Requirements")) {
                            continue;
                        }
                        
                        Sheet sheet = wb.getSheetAt(sheetIdx);
                        boolean header = true;
                        
                        for (Row row : sheet) {
                            if (header) { header = false; continue; }
                            
                            String courseId = getCellString(row, 0);
                            if (courseId == null || courseId.isBlank()) continue;
                            
                            // Handle duplicate course IDs by appending a counter
                            int count = courseIdCounter.getOrDefault(courseId, 0);
                            courseIdCounter.put(courseId, count + 1);
                            String uniqueCourseId = courseId;
                            if (count > 0) {
                                uniqueCourseId = courseId + "-" + (count + 1);
                            }
                            
                            String courseName = getCellString(row, 1);
                            String courseCode = getCellString(row, 2);
                            // Allow blank course codes (for electives), use a default if needed
                            if (courseCode == null) courseCode = "ELEC";
                            
                            Integer creditHours = getCellInteger(row, 3);
                            String professor = getCellString(row, 4);
                            String days = getCellString(row, 5);
                            String time = getCellString(row, 6);
                            String building = getCellString(row, 7);
                            String room = getCellString(row, 8);
                            String attributes = getCellString(row, 9);
                            String prerequisites = getCellString(row, 10);
                            String corequisites = getCellString(row, 11);
                            String terms = getCellString(row, 12);

                            // Insert via SQL directly to be sure
                            insertCourseViaSql(uniqueCourseId, courseName, courseCode, creditHours == null ? 0 : creditHours,
                                    professor, days, time, building, room, attributes, prerequisites, corequisites, terms);
                        }
                    }
                    
                    // Second pass: populate programs and program_courses
                    populateProgramsAndLinks(wb);
                    
                    // Third pass: populate NTC requirements
                    populateNtcRequirements(wb);
                }
                System.out.println("Seeded courses, programs, program_courses, and NTC requirements from courses.xlsx");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
    
    private void insertCourseViaSql(String courseId, String courseName, String courseCode, int creditHours,
                                    String professor, String days, String time, String building, String room,
                                    String attributes, String prerequisites, String corequisites, String terms) {
        String dbPath = System.getProperty("user.dir") + "/courses.db";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath); 
             Statement stmt = conn.createStatement()) {
            String sql = String.format(
                "INSERT OR IGNORE INTO courses (course_id, course_name, course_code, credit_hours, professor, days, time, building, room, attributes, prerequisites, corequisites, terms) " +
                "VALUES ('%s', '%s', '%s', %d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                courseId.replace("'", "''"),
                courseName == null ? "" : courseName.replace("'", "''"),
                courseCode.replace("'", "''"),
                creditHours,
                professor == null ? "" : professor.replace("'", "''"),
                days == null ? "" : days.replace("'", "''"),
                time == null ? "" : time.replace("'", "''"),
                building == null ? "" : building.replace("'", "''"),
                room == null ? "" : room.replace("'", "''"),
                attributes == null ? "" : attributes.replace("'", "''"),
                prerequisites == null ? "" : prerequisites.replace("'", "''"),
                corequisites == null ? "" : corequisites.replace("'", "''"),
                terms == null ? "" : terms.replace("'", "''")
            );
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void ensureDatabaseTablesExist() {
        String dbPath = System.getProperty("user.dir") + "/courses.db";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath); 
             Statement stmt = conn.createStatement()) {
            
            // Create courses table
            stmt.execute("CREATE TABLE IF NOT EXISTS courses (course_id TEXT PRIMARY KEY, course_name TEXT, course_code TEXT, credit_hours INTEGER, professor TEXT, days TEXT, time TEXT, building TEXT, room TEXT, attributes TEXT, prerequisites TEXT, corequisites TEXT, terms TEXT)");
            
            // Create programs table
            stmt.execute("CREATE TABLE IF NOT EXISTS programs (program_id INTEGER PRIMARY KEY AUTOINCREMENT, program_name TEXT, program_type TEXT)");
            
            // Create program_courses junction table
            stmt.execute("CREATE TABLE IF NOT EXISTS program_courses (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER NOT NULL, course_id TEXT NOT NULL, UNIQUE(program_id, course_id), FOREIGN KEY(program_id) REFERENCES programs(program_id) ON DELETE CASCADE, FOREIGN KEY(course_id) REFERENCES courses(course_id) ON DELETE CASCADE)");
            
            // Create ntc_requirements table
            stmt.execute("CREATE TABLE IF NOT EXISTS ntc_requirements (id INTEGER PRIMARY KEY AUTOINCREMENT, requirement_name TEXT, description TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void populateProgramsAndLinks(Workbook wb) throws SQLException {
        String dbPath = System.getProperty("user.dir") + "/courses.db";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath); 
             Statement stmt = conn.createStatement()) {
            
            // Clear existing data
            stmt.executeUpdate("DELETE FROM program_courses");
            stmt.executeUpdate("DELETE FROM programs");
            
            // Track duplicate course IDs to match the unique ones created in first pass
            java.util.Map<String, Integer> courseIdCounter = new java.util.HashMap<>();
            
            // Process each sheet as a program
            for (int sheetIdx = 0; sheetIdx < wb.getNumberOfSheets(); sheetIdx++) {
                String programName = wb.getSheetName(sheetIdx);
                
                // Skip NTC Requirements sheet
                if (programName.equals("NTC Requirements")) {
                    continue;
                }
                
                // Determine program type
                String programType = programName.contains("Minor") ? "Minor" : "Major";
                
                // Insert program
                stmt.executeUpdate(String.format(
                    "INSERT INTO programs (program_name, program_type) VALUES ('%s', '%s')",
                    programName.replace("'", "''"), programType
                ));
                
                // Get the program_id (last insert)
                int programId = 1;
                try (var rs = stmt.executeQuery("SELECT last_insert_rowid() as id")) {
                    if (rs.next()) {
                        programId = rs.getInt("id");
                    }
                }
                
                // Link all courses from this sheet to the program
                Sheet sheet = wb.getSheetAt(sheetIdx);
                boolean header = true;
                
                for (Row row : sheet) {
                    if (header) { header = false; continue; }
                    
                    String courseId = getCellString(row, 0);
                    if (courseId == null || courseId.isBlank()) {
                        continue;
                    }
                    
                    // Handle duplicate course IDs the same way as in first pass
                    int count = courseIdCounter.getOrDefault(courseId, 0);
                    courseIdCounter.put(courseId, count + 1);
                    String uniqueCourseId = courseId;
                    if (count > 0) {
                        uniqueCourseId = courseId + "-" + (count + 1);
                    }
                    
                    try {
                        stmt.executeUpdate(String.format(
                            "INSERT OR IGNORE INTO program_courses (program_id, course_id) VALUES (%d, '%s')",
                            programId, uniqueCourseId.replace("'", "''")
                        ));
                    } catch (SQLException ignored) {
                        // Ignore duplicate entries
                    }
                }
            }
        }
    }
    
    private void populateNtcRequirements(Workbook wb) throws SQLException {
        String dbPath = System.getProperty("user.dir") + "/courses.db";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath); 
             Statement stmt = conn.createStatement()) {
            
            // Clear existing data
            stmt.executeUpdate("DELETE FROM ntc_requirements");
            
            // Find and process NTC Requirements sheet
            for (int sheetIdx = 0; sheetIdx < wb.getNumberOfSheets(); sheetIdx++) {
                String sheetName = wb.getSheetName(sheetIdx);
                
                if (!sheetName.equals("NTC Requirements")) {
                    continue;
                }
                
                Sheet sheet = wb.getSheetAt(sheetIdx);
                boolean header = true;
                
                for (Row row : sheet) {
                    if (header) { header = false; continue; }
                    
                    String requirementName = getCellString(row, 0);
                    if (requirementName == null || requirementName.isBlank()) continue;
                    
                    // Column 1 contains "# of Classes" but we'll use it as description
                    String classCount = getCellString(row, 1);
                    String description = classCount != null ? classCount : "Required courses";
                    
                    try {
                        stmt.executeUpdate(String.format(
                            "INSERT INTO ntc_requirements (requirement_name, description) VALUES ('%s', '%s')",
                            requirementName.replace("'", "''"), description.replace("'", "''")
                        ));
                    } catch (SQLException ignored) {
                        // Ignore errors
                    }
                }
            }
        }
    }

    private static String getCellString(Row row, int idx) {
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((int)cell.getNumericCellValue());
        return null;
    }

    private static Integer getCellInteger(Row row, int idx) {
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try { return Integer.parseInt(cell.getStringCellValue().trim()); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}