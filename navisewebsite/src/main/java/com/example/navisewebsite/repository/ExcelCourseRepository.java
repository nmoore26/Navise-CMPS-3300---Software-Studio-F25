package com.example.navisewebsite.repository;
import com.example.navisewebsite.domain.Course; // Your Course class
import org.apache.poi.ss.usermodel.Cell;       // For Excel cells
import org.apache.poi.ss.usermodel.Row;        // For Excel rows
import org.apache.poi.ss.usermodel.Sheet;      // For Excel sheets
import org.apache.poi.ss.usermodel.Workbook;   // For Excel workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // For XLSX workbooks

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class ExcelCourseRepository {

    private static final String EXCEL_FILE_PATH = "courses.xlsx";
    private Workbook workbook;

    public ExcelCourseRepository() throws IOException {
        File file = new File(EXCEL_FILE_PATH);
        if (!file.exists()) {
            workbook = new XSSFWorkbook();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = new XSSFWorkbook(fis);
            }
        }
    }

    public void saveWorkbook() {
        try (FileOutputStream fos = new FileOutputStream(EXCEL_FILE_PATH)) {
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get existing sheet or create it if it doesn't exist
    public Sheet getOrCreateSheet(String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            createHeaderRow(sheet);
        }
        return sheet;
    }

    private void createHeaderRow(Sheet sheet) {
        Row header = sheet.createRow(0);
        String[] headers = {"CourseID", "CourseName", "CRN: Course Code", "CreditHrs",
                            "Professor", "DaysOffered", "Time", "Building", "Room Number",
                            "Attribute/NTC Requirement", "Prerequisites", "Corequisites",
                            "Term Offered"};
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }
    }

    public void addCourseToSheet(Course course, String sheetName) {
        Sheet sheet = getOrCreateSheet(sheetName);
        int lastRowNum = sheet.getLastRowNum();
        Row row = sheet.createRow(lastRowNum + 1);

        row.createCell(0).setCellValue(course.get_courseID());
        row.createCell(1).setCellValue(course.get_course_name());
        row.createCell(2).setCellValue(course.get_course_code());
        row.createCell(3).setCellValue(course.get_credit_hours());
        row.createCell(4).setCellValue(course.get_professor_name());
        row.createCell(5).setCellValue(course.get_days_offered());
        row.createCell(6).setCellValue(course.get_time());
        row.createCell(7).setCellValue(course.get_building());
        row.createCell(8).setCellValue(course.get_room_number());
        row.createCell(9).setCellValue(String.join(", ", course.get_attribute()));
        row.createCell(10).setCellValue(String.join(", ", course.get_prerequisites()));
        row.createCell(11).setCellValue(String.join(", ", course.get_corequisites()));
        row.createCell(12).setCellValue(String.join(", ", course.get_term_offered()));

        saveWorkbook();
    }

    public Optional<Course> findCourseById(String courseID, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) return Optional.empty();

        Iterator<Row> rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) rowIterator.next(); // skip header

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell cell = row.getCell(0);
            if (cell != null && courseID.equals(cell.getStringCellValue())) {
                return Optional.of(mapRowToCourse(row));
            }
        }
        return Optional.empty();
    }

    private Course mapRowToCourse(Row row) {
        List<String> attributeList = Arrays.asList(row.getCell(9).getStringCellValue().split("\\s*,\\s*"));
        List<String> prereqList = Arrays.asList(row.getCell(10).getStringCellValue().split("\\s*,\\s*"));
        List<String> coreqList = Arrays.asList(row.getCell(11).getStringCellValue().split("\\s*,\\s*"));
        List<String> termList = Arrays.asList(row.getCell(12).getStringCellValue().split("\\s*,\\s*"));

        return new Course(
                row.getCell(0).getStringCellValue(),
                row.getCell(1).getStringCellValue(),
                row.getCell(2).getStringCellValue(),
                (int) row.getCell(3).getNumericCellValue(),
                row.getCell(4).getStringCellValue(),
                row.getCell(5).getStringCellValue(),
                row.getCell(6).getStringCellValue(),
                row.getCell(7).getStringCellValue(),
                row.getCell(8).getStringCellValue(),
                attributeList,
                prereqList,
                coreqList,
                termList
        );
    }
}


