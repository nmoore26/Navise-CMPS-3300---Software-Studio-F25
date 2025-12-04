PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;
CREATE TABLE student_info (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        first_name TEXT,
        last_name TEXT,
        major TEXT,
        minor TEXT,
        school_year TEXT,
        past_courses TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY(user_id) REFERENCES users(user_id)
    );
INSERT INTO student_info VALUES(1,2,'Nina','Moore','Computer Science','','Freshman','CMPS 1500, MATH 1220,MATH 1210','2025-12-04 04:57:45');
DELETE FROM sqlite_sequence;
INSERT INTO sqlite_sequence VALUES('student_info',1);
COMMIT;
