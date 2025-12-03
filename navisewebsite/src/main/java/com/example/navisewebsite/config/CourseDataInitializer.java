package com.example.navisewebsite.config;

import com.example.navisewebsite.repository.CourseRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class CourseDataInitializer {

    @Autowired
    private CourseRepository courseRepository;

    @Bean
    public ApplicationRunner seedCoursesFromXlsx() {
        return args -> {
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
                    Sheet sheet = wb.getSheetAt(0);
                    boolean header = true;
                    for (Row row : sheet) {
                        if (header) { header = false; continue; }
                        // Expected columns: course_id, course_name, course_code, credit_hours, professor, days, time, building, room, attributes, prerequisites, corequisites, terms
                        String courseId = getCellString(row, 0);
                        if (courseId == null || courseId.isBlank()) continue;
                        String courseName = getCellString(row, 1);
                        String courseCode = getCellString(row, 2);
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

                        courseRepository.insertCourse(
                                courseId,
                                courseName,
                                courseCode,
                                creditHours == null ? 0 : creditHours,
                                professor,
                                days,
                                time,
                                building,
                                room,
                                attributes,
                                prerequisites,
                                corequisites,
                                terms
                        );
                    }
                }
                System.out.println("Seeded courses from courses.xlsx");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
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