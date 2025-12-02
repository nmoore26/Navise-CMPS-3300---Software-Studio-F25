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

CREATE TABLE IF NOT EXISTS programs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS program_courses (
    program_id INTEGER NOT NULL,
    course_id TEXT NOT NULL,
    PRIMARY KEY (program_id, course_id),
    FOREIGN KEY (program_id) REFERENCES programs(id),
    FOREIGN KEY (course_id) REFERENCES courses(course_id)
);
