package com.example.navisewebsite.repository;

import com.example.navisewebsite.domain.Course;
import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
/*
    The purpose of the ExcelToDatabaseImporter class is to read course and program data from an Excel file
    and import that data into the database.
    */
 

public class ExcelToDatabaseImporter {

    private final CourseRepository courseRepo = new CourseRepository();
    private final ProgramRepository programRepo = new ProgramRepository();

    public void importExcel(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                // ---------------- NTC SHEET ----------------
                if (sheetName.equalsIgnoreCase("NTC Requirements")) {
                    for (Row row : sheet) {
                        if (row.getRowNum() == 0) continue; // skip header
                        String requirement = getString(row.getCell(0));
                        int numClasses = (int) row.getCell(1).getNumericCellValue();
                        courseRepo.addNTCRequirement(requirement, numClasses);
                    }
                    continue;
                }

                // ---------------- Program Sheet (Major/Minor) ----------------
                boolean isMinor = sheetName.toLowerCase().contains("minor");
                String type = isMinor ? "Minor" : "Major";
                int programId = programRepo.addProgram(sheetName, type);

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue; // skip header

                    String courseID      = getString(row.getCell(0));
                    String courseName    = getString(row.getCell(1));
                    String courseCode    = getString(row.getCell(2));

                    // Credit hours numeric
                    int creditHours = 0;
                    Cell creditCell = row.getCell(3);
                    if (creditCell != null && creditCell.getCellType() == CellType.NUMERIC) {
                        creditHours = (int) creditCell.getNumericCellValue();
                    }

                    String professor     = getString(row.getCell(4));
                    String daysOffered   = getString(row.getCell(5));
                    String time          = getString(row.getCell(6));
                    String building      = getString(row.getCell(7));
                    String roomNumber    = getString(row.getCell(8));
                    List<String> attribute     = toList(getString(row.getCell(9))); // comma-separated
                    List<String> prerequisites = toList(getString(row.getCell(10)));
                    List<String> corequisites  = toList(getString(row.getCell(11)));
                    List<String> termsOffered  = toList(getString(row.getCell(12))); // comma-separated

                    // --------- Build Course object ---------
                    Course course = new Course(
                            courseID,
                            courseName,
                            courseCode,
                            creditHours,
                            professor,
                            daysOffered,
                            time,
                            building,
                            roomNumber,
                            attribute,
                            prerequisites,
                            corequisites,
                            termsOffered
                    );

                    // Insert into DB
                    courseRepo.addCourse(course);

                    // Link to program
                    programRepo.addCourseToProgram(programId, courseID);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Convert comma-separated string into List<String>
    private List<String> toList(String s) {
        if (s == null || s.trim().isEmpty()) return Collections.emptyList();
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toList());
    }

    // Safely extract string from Excel cell
    private String getString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((int) cell.getNumericCellValue()); // used for non-CreditHrs
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return cell.toString().trim();
        }
    }
}
