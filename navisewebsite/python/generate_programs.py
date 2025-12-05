import pandas as pd
import sqlite3
import os

def generate_programs_from_excel(excel_path, db_path):
    # Connect to SQLite DB
    conn = sqlite3.connect(db_path)
    cur = conn.cursor()

    # Create all required tables if not exist
    cur.execute('''CREATE TABLE IF NOT EXISTS courses (
        course_id TEXT PRIMARY KEY,
        course_code TEXT,
        course_name TEXT,
        credit_hours INTEGER,
        professor TEXT,
        days TEXT,
        time TEXT,
        building TEXT,
        room TEXT
    )''')
    cur.execute('''CREATE TABLE IF NOT EXISTS programs (
        program_name TEXT PRIMARY KEY,
        program_type TEXT
    )''')
    cur.execute('''CREATE TABLE IF NOT EXISTS program_courses (
        program_name TEXT,
        course_id TEXT,
        FOREIGN KEY (program_name) REFERENCES programs(program_name)
    )''')
    cur.execute('''CREATE TABLE IF NOT EXISTS ntc_requirements (
        requirement_id INTEGER PRIMARY KEY AUTOINCREMENT,
        requirement_name TEXT,
        course_id TEXT
    )''')
    conn.commit()

    # Read all sheets
    xls = pd.ExcelFile(excel_path)
    for sheet_name in xls.sheet_names:
        program_type = 'minor' if 'minor' in sheet_name.lower() else 'major'
        cur.execute('INSERT OR IGNORE INTO programs (program_name, program_type) VALUES (?, ?)',
                    (sheet_name, program_type))
        df = pd.read_excel(excel_path, sheet_name=sheet_name)
        for _, row in df.iterrows():
            # Try to get course_id and other fields
            course_id = str(row.get('course_id') or row.get('course_code') or row.get('CourseID') or row.get('Course Code'))
            course_code = str(row.get('course_code') or row.get('course_id') or row.get('Course Code') or row.get('CourseID'))
            course_name = str(row.get('course_name') or row.get('Course Name') or row.get('name'))
            credit_hours = row.get('credit_hours') or row.get('Credit Hours') or row.get('credits') or 0
            professor = str(row.get('professor') or row.get('Professor') or '')
            days = str(row.get('days') or row.get('Days') or '')
            time = str(row.get('time') or row.get('Time') or '')
            building = str(row.get('building') or row.get('Building') or '')
            room = str(row.get('room') or row.get('Room') or '')

            # Insert into courses table (ignore duplicates)
            if course_id and course_id != 'nan':
                cur.execute('''INSERT OR IGNORE INTO courses (course_id, course_code, course_name, credit_hours, professor, days, time, building, room)
                               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)''',
                            (course_id, course_code, course_name, credit_hours, professor, days, time, building, room))
                # Insert into program_courses
                cur.execute('INSERT INTO program_courses (program_name, course_id) VALUES (?, ?)',
                            (sheet_name, course_id))

    # Optionally: populate ntc_requirements if you have a sheet named 'NTC Requirements'
    if 'NTC Requirements' in xls.sheet_names:
        df_ntc = pd.read_excel(excel_path, sheet_name='NTC Requirements')
        for _, row in df_ntc.iterrows():
            req_name = str(row.get('requirement_name') or row.get('Requirement Name') or row.get('name'))
            course_id = str(row.get('course_id') or row.get('course_code') or row.get('CourseID') or row.get('Course Code'))
            if req_name and course_id and course_id != 'nan':
                cur.execute('INSERT INTO ntc_requirements (requirement_name, course_id) VALUES (?, ?)',
                            (req_name, course_id))

    conn.commit()
    conn.close()
    print('All tables (courses, programs, program_courses, ntc_requirements) generated from Excel.')

if __name__ == "__main__":
    excel_path = os.path.join('src', 'main', 'resources', 'data', 'courses.xlsx')
    db_path = 'courses.db'
    generate_programs_from_excel(excel_path, db_path)
