# Navise Website Implementation Guide

## Overview of Changes

This document outlines the comprehensive refactoring and new features implemented for the Navise student course planning website.

## 1. Deployment Instructions

### Option A: Deploy to Render.com (Recommended - Free Tier Available)

1. **Create necessary files in project root:**

```properties
# system.properties
java.runtime.version=21
```

```yaml
# render.yaml
services:
  - type: web
    name: navise-website
    env: java
    buildCommand: ./mvnw clean package -DskipTests
    startCommand: java -jar target/*.jar
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: production
```

2. **Build the JAR:**
```bash
./mvnw clean package
```

3. **Deploy to Render:**
   - Push your code to GitHub
   - Connect your GitHub repository to Render.com
   - Render will automatically detect the Spring Boot app
   - Your app will be accessible at: `https://your-app-name.onrender.com`

### Option B: Deploy to Heroku

1. **Create Procfile:**
```
web: java -jar target/*.jar
```

2. **Deploy:**
```bash
heroku login
heroku create navise-website
git push heroku main
```

### Option C: Traditional Server Deployment

1. **Package application:**
```bash
./mvnw clean package
```

2. **Run on server:**
```bash
java -jar target/navise-website-0.0.1-SNAPSHOT.jar
```

3. **Configure reverse proxy (nginx):**
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 2. Code Refactoring Summary

### SOLID Principles Improvements

#### Single Responsibility Principle (SRP)
**Before:** `ProjectedSchedule.java` was a massive class handling:
- Domain models
- Repository interfaces
- Database access
- Business logic
- Utility functions

**After:** Separated into focused classes:
- `ScheduleDomain.java` - Domain models only
- `ScheduleRepository.java` - Repository interfaces
- `SQLiteScheduleRepository.java` - Database implementation
- `ScheduleProjectionService.java` - Projection business logic
- `ScheduleMergeService.java` - Merging business logic
- `ScheduleLoaderService.java` - Database loading logic

#### Open/Closed Principle (OCP)
- Services are now extensible through interfaces
- Can swap database implementations without changing business logic
- New packing algorithms can be added without modifying existing code

#### Dependency Inversion Principle (DIP)
- High-level services depend on repository interfaces, not concrete implementations
- Database access is abstracted behind repository interfaces

### Code Smell Elimination

#### Duplicated Code
**Before:** Credit extraction logic was duplicated in `Student.java`, `Path.java`, `Major.java`, and `Minor.java`

**After:** Centralized in `CourseCreditsExtractor.java` utility class

```java
// Now all classes use:
int credits = CourseCreditsExtractor.extractCredits(course);
```

#### Long Classes
- Broke 500+ line `ProjectedSchedule.java` into 6 focused classes
- Each class under 200 lines with clear responsibility

#### Reflection Code Duplication
- Consolidated reflection logic into utility methods
- Improved error handling and null safety

### New File Structure

```
src/main/java/com/example/navisewebsite/
├── controller/
│   ├── StudentController.java (existing)
│   └── StudentDataController.java (NEW)
├── domain/
│   ├── Course.java (existing)
│   ├── Student.java (existing - to be refactored)
│   ├── Path.java (existing - to be refactored)
│   ├── Major.java (existing - to be refactored)
│   ├── Minor.java (existing - to be refactored)
│   └── ScheduleDomain.java (NEW)
├── repository/
│   ├── ScheduleRepository.java (NEW)
│   └── SQLiteScheduleRepository.java (NEW)
├── service/
│   ├── ScheduleProjectionService.java (NEW)
│   ├── ScheduleMergeService.java (NEW)
│   └── ScheduleLoaderService.java (NEW)
└── util/
    └── CourseCreditsExtractor.java (NEW)

src/main/resources/templates/
├── student-home.html (UPDATED)
├── student-my-courses.html (NEW)
├── student-degree-progress.html (NEW)
└── student-projected-schedule.html (NEW)
```

## 3. New Functionality

### A. My Courses Page (`/student/my-courses`)

**What it does:**
- Displays student's declared major from `studentinfo.db`
- Displays student's declared minor from `studentinfo.db`
- Shows complete course history from `past_courses` field
- Shows "Information not provided" for empty fields

**Database Schema Required:**
```sql
-- studentinfo.db
CREATE TABLE students (
    email TEXT PRIMARY KEY,
    major TEXT,
    minor TEXT,
    past_courses TEXT  -- Comma-separated course codes
);
```

### B. Degree Progress Page (`/student/degree-progress`)

**What it does:**
- Compares major/minor requirements from `courses.db`
- Compares against completed courses from `studentinfo.db`
- Calculates and displays remaining courses
- Shows full course details (professor, time, location, credits)
- Separates "Course History" (completed) from "Courses Remaining"

**Database Schema Required:**
```sql
-- courses.db
CREATE TABLE courses (
    courseID TEXT PRIMARY KEY,
    course_code TEXT,
    course_name TEXT,
    credit_hours INTEGER,
    professor_name TEXT,
    days_offered TEXT,
    time TEXT,
    building TEXT,
    room_number TEXT
);

CREATE TABLE program_courses (
    program_name TEXT,
    course_id TEXT,
    FOREIGN KEY (course_id) REFERENCES courses(courseID)
);

CREATE TABLE programs (
    program_name TEXT PRIMARY KEY,
    program_type TEXT  -- 'major' or 'minor'
);
```

### C. Projected Schedule Page (`/student/projected-schedule`)

**What it does:**
- Form with dropdown menus for majors and minors from `courses.db`
- Both dropdowns are optional (can select one or both)
- Projects hypothetical schedule based on selections
- Uses existing `ProjectedSchedule` logic
- Displays semester-by-semester breakdown
- Shows course details with credits and meeting times

**How it works:**
1. User selects major and/or minor
2. System retrieves requirements from `courses.db`
3. Compares against completed courses in `studentinfo.db`
4. Calculates remaining courses
5. Uses greedy packing algorithm (15 credits/semester target)
6. Displays projected schedule with all course information

## 4. Refactoring Student, Path, Major, Minor Classes

### Updated Student.java

Replace the `getCourseCredits()` method and related reflection code with:

```java
import com.example.navisewebsite.util.CourseCreditsExtractor;

// Remove all the getCourseCredits, tryInvokeIntMethod, tryReadIntField methods

// Update getTotalCreditsCompleted:
public int getTotalCreditsCompleted() {
    return pastCourses.stream()
            .mapToInt(CourseCreditsExtractor::extractCredits)
            .sum();
}

// Update getTotalCreditsInProgress:
public int getTotalCreditsInProgress() {
    return currentCourses.stream()
            .mapToInt(CourseCreditsExtractor::extractCredits)
            .sum();
}
```

### Updated Path.java

Replace the `getCourseCredits()` and reflection methods with:

```java
import com.example.navisewebsite.util.CourseCreditsExtractor;

// Remove getCourseCredits, tryInvokeIntMethod, tryReadIntField methods

// Update calculateTotalHours:
protected int calculateTotalHours(List<Course> courses) {
    if (courses == null) {
        return 0;
    }
    return courses.stream()
            .mapToInt(CourseCreditsExtractor::extractCredits)
            .sum();
}
```

### Updated Major.java and Minor.java

No changes needed - they inherit the updated `calculateTotalHours()` from Path.java

## 5. Testing the New Features

### Test My Courses
1. Navigate to `/student-home`
2. Click "My Courses" card
3. Verify major, minor, and course history display correctly
4. Test with missing data to ensure "Information not provided" shows

### Test Degree Progress
1. Navigate to `/student-home`
2. Click "Degree Progress" card
3. Verify completed courses show under "Course History"
4. Verify remaining courses show under "Courses Remaining"
5. Check that all course details display correctly

### Test Projected Schedule
1. Navigate to `/student-home`
2. Click "Projected Schedule" card
3. Try selecting only a major
4. Try selecting only a minor
5. Try selecting both major and minor
6. Verify schedule generates correctly with semester breakdown
7. Verify course details display properly

## 6. Database Migration Notes

If your current database schema differs, you'll need to either:

1. **Migrate your data** to match the expected schema
2. **Update the SQL queries** in `StudentDataController.java` to match your schema
3. **Add fallback queries** like those in `SQLiteScheduleRepository.java`

## 7. Next Steps

1. ✅ Replace `ProjectedSchedule.java` with new service classes
2. ✅ Update `Student.java`, `Path.java` to use `CourseCreditsExtractor`
3. ✅ Add new controller `StudentDataController.java`
4. ✅ Add new HTML templates
5. ✅ Update `student-home.html` with new links
6. ⬜ Test all new endpoints
7. ⬜ Update database if schema doesn't match
8. ⬜ Deploy to production

## 8. Benefits of Refactoring

### Maintainability
- Smaller, focused classes are easier to understand
- Changes are isolated to specific concerns
- Less risk of breaking unrelated functionality

### Testability
- Services can be tested independently
- Mock repositories for unit testing
- Clear boundaries between layers

### Extensibility
- Easy to add new scheduling algorithms
- Can support multiple database types
- Simple to add new features without modifying existing code

### Performance
- Repository pattern enables caching
- Service layer can implement optimization
- Database queries are centralized

## 9. Common Issues and Solutions

### Issue: Database not found
**Solution:** Ensure `studentinfo.db` and `courses.db` are in the project root or update paths in controllers

### Issue: Course credits showing as 0
**Solution:** Verify Course.java has proper getters and Course objects are properly initialized

### Issue: Empty dropdowns in Projected Schedule
**Solution:** Ensure `programs` table exists and is populated with major/minor program names

### Issue: 404 errors on new pages
**Solution:** Verify controllers are in correct package and Spring is scanning them

## 10. Configuration Tips

### Database Path Configuration
Consider moving database paths to `application.properties`:

```properties
# application.properties
navise.db.studentinfo=studentinfo.db
navise.db.courses=courses.db
```

Then inject with `@Value`:
```java
@Value("${navise.db.studentinfo}")
private String studentInfoDb;
```

### Logging
Enable SQL logging for debugging:
```properties
logging.level.org.springframework.jdbc=DEBUG
spring.jpa.show-sql=true
```

---

## Questions or Issues?

If you encounter any problems during implementation:
1. Check the console logs for SQL errors
2. Verify database schemas match expected structure
3. Ensure all new files are in correct packages
4. Confirm Spring Boot is scanning all packages
