-- PostgreSQL schema and data for courses table
CREATE TABLE IF NOT EXISTS courses (
    course_id TEXT PRIMARY KEY,
    course_name TEXT,
    course_code TEXT,
    credit_hours INTEGER,
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

INSERT INTO courses VALUES('MATH 1210','Calculus I','10005',4,'Michael Joyce','MWF','9-9:50 AM','Dinwiddie Hall','103','Formal Reasoning,Math & Science','','MATH 1211','Fall,Spring');
INSERT INTO courses VALUES('CMPS 2170','Intro to Discrete Math','29052',3,'Ali Reza Shirvani Jouzdani','MWF','2-2:50 PM','Tilton Memorial Hall','305','Math & Natural Science','MATH 1210','CMPS 2171','Fall,Spring');
INSERT INTO courses VALUES('CMPS 2171','Intro to Discrete Math Lab','58374',0,'Ali Reza Shirvani Jouzdani','T','8-9:15 AM','Lindy Boggs Energy Center','102','','','CMPS 2170','Fall,Spring');
INSERT INTO courses VALUES('MATH 3090','Linear Algebra','10702',4,'Shahriyar Roshan Zamir','MWF','11-11:50 PM','Richardson Building','106','Formal Reasoning,Math & Science','','MATH 3091','Fall,Spring');
INSERT INTO courses VALUES('MATH 3091','Recitations for Linear Algebra','50694',0,'Shahriyar Roshan Zamir','T','12:30-1:45 PM','Norman Mayer','106','Math & Natural Science','','MATH 3090','Fall,Spring');
INSERT INTO courses VALUES('MATH 1230','Stats for Scientists','61609',4,'Emily Gamundi','MWF','9-9:50 AM','Tilton Memorial Hall','301','Formal Reasoning,Math & Science','MATH 1210','MATH 1231','Fall,Spring');
INSERT INTO courses VALUES('MATH 1231','Stats for Scientist Recitation','55370',0,'Lifeng Han','R','2-3:15 PM','Norman Mayer','118','','','MATH 1230','Fall,Spring');
INSERT INTO courses VALUES('MATH 3070','Intro to Probability','36941',3,'Xiang Ji','MWF','9-9:50 AM','Norman Mayer','200B','Formal Reasoning,Math & Science','','MATH 3071','Fall,Spring');
INSERT INTO courses VALUES('MATH 3071','Intro to Probability Recitation','31152',0,'Xiang Ji','R','12:30-1:45 PM','Gibson Hall','126A','','','MATH 3070','Fall,Spring');
INSERT INTO courses VALUES('CMPS 1500','Intro to Computer Science I','96313',4,'Matthew Toups','MWF','11-11:50 AM','Stanley Thomas Hall','302','','','CMPS 1501','Fall,Spring');
INSERT INTO courses VALUES('CMPS 1501','Intro to Computer Science I Lab','90255',0,'Matthew Toups','R','8-9:15 AM','Stanley Thomas Hall','302','','','CMPS 1500','Fall,Spring');
INSERT INTO courses VALUES('CMPS 1600','Intro to Computer Science II','93325',4,'Aaron Maus','MWF','11-11:50 AM','Lindy Boggs Energy Center','239','','CMPS 1500','CMPS 1601','Fall,Spring');
INSERT INTO courses VALUES('CMPS 1601','Intro to Computer Science II Lab','93326',0,'Aaron Maus','T','12:30-1:45 PM','Stanley Thomas Hall','302','','','CMPS 1600','Fall,Spring');
INSERT INTO courses VALUES('CMPS 2200','Intro to Algorithms','74473',4,'Zhengming Ding','MWF','4-4:50 PM','Stanley Thomas Hall','302','','CMPS 1600','CMPS 2201,CMPS 2170','Fall,Spring');
INSERT INTO courses VALUES('CMPS 2201','Intro to Algorithms Lab','63328',0,'Zhengming Ding','R','12:30-1:45 PM','Lindy Boggs Energy Center','102','','','CMPS 2200','Fall,Spring');
INSERT INTO courses VALUES('CMPS 2300','Intro to Comp Sys & Networking','22929',4,'Matthew Toups','MWF','4-4:50 PM','Stanley Thomas Hall','302','','CMPS 1600','CMPS 2301','Fall,Spring');
INSERT INTO courses VALUES('CMPS 2301','Intro to Comp Sys & Networking Lab','47018',0,'Matthew Toups','T','12:30-1:45 PM','Stanley Thomas Hall','302','','','CMPS 2300','Fall,Spring');
INSERT INTO courses VALUES('CMPS 4770','Operating Systems','63941',3,'Aaron Maus','MWF','1-1:50 PM','Dinwiddie Hall','108','','','','Spring');
INSERT INTO courses VALUES('CMPS 4720','Machine Learning','61800',3,'Jihun Hamm','TR','11-12:15 PM','Stanley Thomas Hall','302','','CMPS 2170,CMPS 2200','','Spring');
INSERT INTO courses VALUES('CMPS XXXX - 1','Elective','',3,'','','','','','','','','Fall,Spring');
INSERT INTO courses VALUES('CMPS XXXX - 2','Elective','',3,'','','','','','','','','Fall,Spring');
INSERT INTO courses VALUES('CMPS XXXX - 3','Elective','',3,'','','','','','','','','Fall,Spring');
INSERT INTO courses VALUES('CMPS XXXX - 4','Elective','',3,'','','','','','','','','Fall,Spring');
