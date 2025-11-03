package com.example.navisewebsite.service;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Service;

import com.example.navisewebsite.domain.Course;
import com.example.navisewebsite.domain.ICourse;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;




@Service
public class AdminCourseService implements ICourse {
    // Path to the Excel file
    private static final String EXCEL_FILE_PATH = "courses.xlsx";
    private Workbook workbook;

    //Show the admin page with empty course form
    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("course", new Course()); //bind empty Course object to the form
        return "admin"; // This will look for admin.html in src/main/resources/templates/
    }
    public AdminCourseService() throws IOException {
        File file = new File(EXCEL_FILE_PATH);
        if (!file.exists()) {
            workbook = new XSSFWorkbook(); // create new workbook if file doesn't exist
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = new XSSFWorkbook(fis);
            }
        }
    }
    private void createHeaderRow(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("CourseID");  
        header.createCell(1).setCellValue("CourseName"); 
        header.createCell(2).setCellValue("CRN: Course Code");
        header.createCell(3).setCellValue("CreditHrs");
        header.createCell(4).setCellValue("Professor");
        header.createCell(5).setCellValue("DaysOffered");
        header.createCell(6).setCellValue("Time");
        header.createCell(7).setCellValue("Building");
        header.createCell(8).setCellValue("Room Number");
        header.createCell(9).setCellValue("Attribute/NTC Requirement");
        header.createCell(10).setCellValue("Prerequisites");
        header.createCell(11).setCellValue("Corequisites");
        header.createCell(12).setCellValue("Term Offered");
    }
    private void saveWorkbook() {
        try (FileOutputStream fos = new FileOutputStream(new File(EXCEL_FILE_PATH))) {
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void add_course(Course course, String sheet_name) {
        Sheet sheet = workbook.getSheet(sheet_name);
        if (sheet == null) {
            sheet = workbook.createSheet(sheet_name);
            createHeaderRow(sheet);
        }

        int lastRowNum = sheet.getLastRowNum();
        Row newRow = sheet.createRow(lastRowNum + 1);

        newRow.createCell(0).setCellValue(course.get_courseID());
        newRow.createCell(1).setCellValue(course.get_course_name());
        newRow.createCell(2).setCellValue(course.get_course_code());
        newRow.createCell(3).setCellValue(course.get_credit_hours());
        newRow.createCell(4).setCellValue(course.get_professor_name());
        newRow.createCell(5).setCellValue(course.get_days_offered());
        newRow.createCell(6).setCellValue(course.get_time());
        newRow.createCell(7).setCellValue(course.get_building());
        newRow.createCell(8).setCellValue(course.get_room_number());
        newRow.createCell(9).setCellValue(String.join(", ", course.get_attribute()));
        newRow.createCell(10).setCellValue(String.join(", ", course.get_prerequisites()));
        newRow.createCell(11).setCellValue(String.join(", ", course.get_corequisites()));
        newRow.createCell(12).setCellValue(String.join(", ", course.get_term_offered()));

        saveWorkbook();
    }
    @Override
    public void remove_course(String courseID, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) return;

        Iterator<Row> rowIterator = sheet.iterator();
        if(rowIterator.hasNext()) rowIterator.next(); // skip header

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell cell = row.getCell(0);
            if (cell != null && courseID.equals(cell.getStringCellValue())) {
                int rowIndex = row.getRowNum();
                sheet.removeRow(row);

                int lastRowNum = sheet.getLastRowNum();
                if (rowIndex < lastRowNum) {
                    sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
                }
                break;
            }
        }
        saveWorkbook();
    } 

    public Optional<Course> findById(String courseID, String sheetName) {
    Sheet sheet = workbook.getSheet(sheetName);
    if (sheet == null) return Optional.empty();

    Iterator<Row> rowIterator = sheet.iterator();
    if (rowIterator.hasNext()) rowIterator.next(); // skip header

    while (rowIterator.hasNext()) {
        Row row = rowIterator.next();
        Cell cell = row.getCell(0);
        if (cell != null && courseID.equals(cell.getStringCellValue())) {

            // Convert CSV strings to List<String>
            List<String> attributeList = Arrays.asList(row.getCell(9).getStringCellValue().split("\\s*,\\s*"));
            List<String> prereqList = Arrays.asList(row.getCell(10).getStringCellValue().split("\\s*,\\s*"));
            List<String> coreqList = Arrays.asList(row.getCell(11).getStringCellValue().split("\\s*,\\s*"));
            List<String> termList = Arrays.asList(row.getCell(12).getStringCellValue().split("\\s*,\\s*"));

            return Optional.of(new Course(
                    row.getCell(0).getStringCellValue(), // courseID
                    row.getCell(1).getStringCellValue(), // course_name
                    row.getCell(2).getStringCellValue(), // course_code
                    (int) row.getCell(3).getNumericCellValue(), // credit_hours
                    row.getCell(4).getStringCellValue(), // professor_name
                    row.getCell(5).getStringCellValue(), // days_offered
                    row.getCell(6).getStringCellValue(), // time
                    row.getCell(7).getStringCellValue(), // building
                    row.getCell(8).getStringCellValue(), // room_number
                    attributeList, // attribute as List<String>
                    prereqList,    // prerequisites as List<String>
                    coreqList,     // corequisites as List<String>
                    termList       // term_offered as List<String>
            ));
        }
    }

    return Optional.empty();
}

    @Override
    public List<Course> findAll() {
        return new ArrayList<>();
}
}
