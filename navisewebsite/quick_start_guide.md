# Navise Website - Quick Start Guide

## 🚀 Implementation Checklist

### Phase 1: Add New Files (Copy these into your project)

#### 1.1 Utility Classes
- [ ] Create `src/main/java/com/example/navisewebsite/util/CourseCreditsExtractor.java`

#### 1.2 Domain Classes
- [ ] Create `src/main/java/com/example/navisewebsite/domain/ScheduleDomain.java`

#### 1.3 Repository Layer
- [ ] Create `src/main/java/com/example/navisewebsite/repository/ScheduleRepository.java`
- [ ] Create `src/main/java/com/example/navisewebsite/repository/SQLiteScheduleRepository.java`

#### 1.4 Service Layer
- [ ] Create `src/main/java/com/example/navisewebsite/service/ScheduleProjectionService.java`
- [ ] Create `src/main/java/com/example/navisewebsite/service/ScheduleMergeService.java`
- [ ] Create `src/main/java/com/example/navisewebsite/service/ScheduleLoaderService.java`

#### 1.5 Controllers
- [ ] Create `src/main/java/com/example/navisewebsite/controller/StudentDataController.java`

#### 1.6 HTML Templates
- [ ] Create `src/main/resources/templates/student-my-courses.html`
- [ ] Create `src/main/resources/templates/student-degree-progress.html`
- [ ] Create `src/main/resources/templates/student-projected-schedule.html`
- [ ] Update `src/main/resources/templates/student-home.html`

### Phase 2: Refactor Existing Files

#### 2.1 Update Student.java
Replace the credit extraction logic:
```java
// REMOVE these methods:
- getCourseCredits()
- tryInvokeIntMethod()
- tryReadIntField()

// UPDATE these methods to use CourseCreditsExtractor:
public int getTotalCreditsCompleted() {
    return pastCourses.stream()
            .mapToInt(CourseCreditsExtractor::extractCredits)
            .sum();
}
```

#### 2.2 Update Path.java
Replace the credit extraction logic:
```java
// REMOVE these methods:
- getCourseCredits()
- tryInvokeIntMethod()
- tryReadIntField()

// UPDATE this method:
protected int calculateTotalHours(List<Course> courses) {
    if (courses == null) return 0;
    return courses.stream()
            .mapToInt(CourseCreditsExtractor::extractCredits)
            .sum();
}
```

#### 2.3 Major.java and Minor.java
- [ ] No changes needed - they inherit from Path.java

### Phase 3: Database Setup

#### 3.1 Verify Database Schemas

**studentinfo.db should have:**
```sql
CREATE TABLE IF NOT EXISTS students (
    email TEXT PRIMARY KEY,
    password TEXT,
    major TEXT,
    minor TEXT,
    past_courses TEXT,  -- Comma-separated course codes
    first_name TEXT,
    last_name TEXT,
    school_year TEXT
);
```

**courses.db should have:**
```sql
CREATE TABLE IF NOT EXISTS courses (
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

CREATE TABLE IF NOT EXISTS programs (
    program_name TEXT PRIMARY KEY,
    program_type TEXT  -- 'major' or 'minor'
);

CREATE TABLE IF NOT EXISTS program_courses (
    program_name TEXT,
    course_id TEXT,
    FOREIGN KEY (program_name) REFERENCES programs(program_name),
    FOREIGN KEY (course_id) REFERENCES courses(courseID)
);
```

#### 3.2 Populate Sample Data (if empty)
```sql
-- Insert sample programs
INSERT INTO programs VALUES ('Computer Science', 'major');
INSERT INTO programs VALUES ('Mathematics', 'major');
INSERT INTO programs VALUES ('Data Science', 'minor');
INSERT INTO programs VALUES ('Business', 'minor');

-- Link courses to programs
INSERT INTO program_courses VALUES ('Computer Science', 'CMPS-101');
INSERT INTO program_courses VALUES ('Computer Science', 'CMPS-201');
-- etc...
```

### Phase 4: Test Locally

#### 4.1 Build and Run
```bash
./mvnw clean install
./mvnw spring-boot:run
```

#### 4.2 Test Each Page
1. Navigate to http://localhost:8080/
2. Log in as a student
3. Test My Courses: http://localhost:8080/student/my-courses
4. Test Degree Progress: http://localhost:8080/student/degree-progress
5. Test Projected Schedule: http://localhost:8080/student/projected-schedule

#### 4.3 Common Issues

**Issue:** Pages show 404 error
- **Solution:** Ensure StudentDataController is in the correct package
- **Solution:** Check that Spring is scanning the controller package

**Issue:** Database connection errors
- **Solution:** Verify database files are in project root
- **Solution:** Check file permissions

**Issue:** Empty dropdowns in Projected Schedule
- **Solution:** Populate `programs` table with major/minor data
- **Solution:** Check program_type values are exactly 'major' or 'minor'

### Phase 5: Deploy to Production

#### Option A: Render.com (Easiest)
```bash
# 1. Create system.properties in project root
echo "java.runtime.version=21" > system.properties

# 2. Create render.yaml (see Deployment Configuration Files artifact)

# 3. Push to GitHub
git add .
git commit -m "Add new student data features"
git push origin main

# 4. Connect to Render.com
# - Sign up at render.com
# - Create new Web Service
# - Connect your GitHub repo
# - Render auto-detects Spring Boot
```

#### Option B: Heroku
```bash
# 1. Create Procfile
echo "web: java -Dserver.port=\$PORT -jar target/*.jar" > Procfile

# 2. Deploy
heroku create navise-website
git push heroku main
```

#### Option C: Traditional Server
```bash
# 1. Build JAR
./mvnw clean package

# 2. Copy to server
scp target/navise-website-*.jar user@yourserver:/opt/navise/

# 3. Run as service (see systemd configuration in Deployment Configuration Files)
sudo systemctl enable navise
sudo systemctl start navise
```

## 📊 What Changed?

### Before:
```
ProjectedSchedule.java (500+ lines)
├── Domain models
├── Repository interfaces
├── Database code
├── Business logic
└── Utility functions

Student.java (250+ lines)
└── Duplicate credit extraction code

Path.java (200+ lines)
└── Duplicate credit extraction code
```

### After:
```
util/
└── CourseCreditsExtractor.java (90 lines)

domain/
└── ScheduleDomain.java (80 lines)

repository/
├── ScheduleRepository.java (30 lines)
└── SQLiteScheduleRepository.java (150 lines)

service/
├── ScheduleProjectionService.java (140 lines)
├── ScheduleMergeService.java (110 lines)
└── ScheduleLoaderService.java (90 lines)

Student.java (refactored, -50 lines)
Path.java (refactored, -80 lines)
```

## 🎯 Benefits Achieved

### Code Quality
✅ **Eliminated 200+ lines of duplicate code**
✅ **Separated concerns** - each class has one responsibility
✅ **Improved testability** - services can be unit tested independently
✅ **Better maintainability** - changes are localized

### SOLID Principles
✅ **Single Responsibility** - each class has one job
✅ **Open/Closed** - extensible without modification
✅ **Dependency Inversion** - depends on interfaces, not implementations

### New Features
✅ **My Courses** - displays student's academic information
✅ **Degree Progress** - tracks completed vs remaining courses
✅ **Projected Schedule** - plans hypothetical academic paths

## 🔍 Verification Steps

### 1. Code Compiles
```bash
./mvnw clean compile
# Should complete without errors
```

### 2. All Tests Pass
```bash
./mvnw test
# All tests should pass
```

### 3. Application Starts
```bash
./mvnw spring-boot:run
# Should see: "Started NaviseWebsiteApplication in X seconds"
```

### 4. Pages Load
- [ ] Homepage loads: http://localhost:8080/
- [ ] Student login works
- [ ] Dashboard loads: http://localhost:8080/student-home
- [ ] My Courses loads: http://localhost:8080/student/my-courses
- [ ] Degree Progress loads: http://localhost:8080/student/degree-progress
- [ ] Projected Schedule loads: http://localhost:8080/student/projected-schedule

### 5. Data Displays Correctly
- [ ] My Courses shows major/minor/history from database
- [ ] Degree Progress compares requirements vs completed
- [ ] Projected Schedule dropdowns populate from database
- [ ] Projected Schedule generates valid output

## 💡 Tips

### Development
- Use Spring Boot DevTools for auto-reload during development
- Enable SQL logging to debug database queries
- Use browser DevTools to inspect form submissions

### Debugging
- Check console logs for SQL errors
- Verify database file paths are correct
- Ensure proper case sensitivity (major vs Major)
- Test with sample data before using real data

### Performance
- Database files should be small (<100MB)
- Consider adding indexes on frequently queried columns
- Cache program lists in production

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Thymeleaf Documentation](https://www.thymeleaf.org/documentation.html)
- [SQLite Documentation](https://www.sqlite.org/docs.html)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)

## 🆘 Getting Help

If you encounter issues:
1. Check the IMPLEMENTATION_GUIDE.md for detailed explanations
2. Review error logs in console
3. Verify database schemas match expected structure
4. Ensure all files are in correct packages
5. Check that dependencies are properly imported

## ✅ Final Checklist

Before deploying to production:
- [ ] All new files added
- [ ] Existing files refactored
- [ ] Database schemas verified
- [ ] Local testing completed
- [ ] All pages load correctly
- [ ] Data displays properly
- [ ] Deployment configuration created
- [ ] Production environment tested

---

**Ready to deploy!** 🚀

Choose your deployment method from Phase 5 and follow the steps for your selected platform.
