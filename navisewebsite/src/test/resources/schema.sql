-- Users table (for authentication)
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    first_name TEXT DEFAULT '',
    last_name TEXT DEFAULT '',
    user_type TEXT NOT NULL
);

-- Student info table
CREATE TABLE IF NOT EXISTS student_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE,
    first_name TEXT,
    last_name TEXT,
    major TEXT,
    minor TEXT,
    school_year TEXT,
    past_courses TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Courses table
CREATE TABLE IF NOT EXISTS courses (
    course_id TEXT PRIMARY KEY,
    course_name TEXT NOT NULL,
    course_code TEXT NOT NULL,
    credit_hours INTEGER NOT NULL,
    professor TEXT,
    days TEXT,
    time TEXT,
    building TEXT,
    room TEXT,
    attributes TEXT,
    prerequisites TEXT,
    corequisites TEXT,
    terms TEXT
);

-- Programs (majors/minors) table
CREATE TABLE IF NOT EXISTS programs (
    program_id INTEGER PRIMARY KEY AUTOINCREMENT,
    program_name TEXT NOT NULL,
    program_type TEXT NOT NULL
);

-- Program courses junction table
CREATE TABLE IF NOT EXISTS program_courses (
    program_id INTEGER NOT NULL,
    course_id TEXT NOT NULL,
    PRIMARY KEY (program_id, course_id),
    FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);

-- NTC requirements table
CREATE TABLE IF NOT EXISTS ntc_requirements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    requirement_name TEXT NOT NULL,
    credits_required INTEGER NOT NULL
);
