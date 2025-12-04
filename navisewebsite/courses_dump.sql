PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;
CREATE TABLE courses (
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
INSERT INTO courses VALUES('CMPS XXXX - 5','Elective','',3,'','','','','','','','','Fall,Spring');
INSERT INTO courses VALUES('CMPS 4010','Capstone Project I','63336',2,'Aaron Maus','MWF','3-3:50','Lindy Boggs Energy Center','122','','CMPS 2200,CMPS 2300','','Fall');
INSERT INTO courses VALUES('CMPS 4020','Capstone Project II','63938',2,'Victor Bankston','MWF','3-3:50','Stanley Thomas Hall','302','','CMPS 4010','','Spring');
INSERT INTO courses VALUES('NSCI 3300','Brain and Behavior','63903',3,'Jonathan Fadok','TR','3:30-4:45 PM','Joseph Merrick Memorial','102','Social & Behavioral Sci','PSYC 1000','','Fall,Spring');
INSERT INTO courses VALUES('PSYC 1000','Introductory Psych','11174',3,'Melinda Fabian','MWF','8-8:50 AM','Joseph Merrick Memorial','102','Social & Behavioral Sci','','PSYC 1001','Fall,Spring');
INSERT INTO courses VALUES('PSYC 1001','Psychology Beyond Classroom','20066',0,'Nieve Mahood','','','Online','','','','','Fall,Spring');
INSERT INTO courses VALUES('NSCI 3310','Cellular Neuroscience','39300',3,'John Home','MWF','11-11:50 AM','Paul Hall','100','','CELL 1010,NSCI 3300','','Fall,Spring');
INSERT INTO courses VALUES('NSCI 3320','Systems Neuroscience','93174',3,'Laura Schrader-Kriek','MWF','9-9:50 AM','Howard-Tilton Memorial Library','B07','Math & Natural Science','CELL 1010,NSCI 3300','','Fall,Spring');
INSERT INTO courses VALUES('NSCI 4910','Experiential Learning in Neuroscience','',0,'','','','','','','','NSCI 4910','');
INSERT INTO courses VALUES('CELL 1010','Intro to Cell & Molec Biology','46312',3,'Robert Dotson','TR','2-3:15 PM','McCalister Auditorium','AUD','Math & Natural Science','','','Fall,Spring');
INSERT INTO courses VALUES('CELL 2050','Genetics','50409',3,'Christian Burr','TR','11-12:15 PM','Paul Hall','100','Math & Natural Science','CELL 1010','','Fall,Spring');
INSERT INTO courses VALUES('CHEM 1070','General Chemistry  I','36032',3,'Muhammad Tahir','MWF','1-1:50 PM','Paul Hall','100','Math & Natural Science','','CHEM 1075','Fall');
INSERT INTO courses VALUES('CHEM 1075','General Chemistry I Lab','71366',1,'Muhammad Tahir','M','12-2:55 PM','Isreal Environmental Science','220','Science with Laboratory','','CHEM 1070','Fall');
INSERT INTO courses VALUES('CHEM 1080','General Chemistry II','10320',3,'Muhammad Tahir','MWF','10-10:50 AM','Richardson Building','117','Math & Natural Science','CHEM 1070,CHEM 1075','CHEM 1085','Spring');
INSERT INTO courses VALUES('CHEM 1085','General Chemistry II Lab','19717',1,'Muhammad Tahir','M','12-2:59 PM','Isreal Environmental Science','220','Science with Laboratory','CHEM 1070,CHEM 1075','CHEM 1080','Spring');
INSERT INTO courses VALUES('CHEM 2410','Organic Chemistry I','36096',3,'Mallory Cortez','TR','9:30-10:45','McCalister Auditorium','AUD','','CHEM 1080,CHEM 1085','CHEM 2415','Fall');
INSERT INTO courses VALUES('CHEM 2415','Organic Chemistry I Lab','71376',1,'Mallory Cortez','M','1-5:00 PM','Isreal Environmental Science','520','','CHEM 1080,CHEM 1085','CHEM 2410','Fall');
INSERT INTO courses VALUES('PHYS 1210','Introductory Physics I','36133',4,'Lu Xin','MWF','9-9:50 AM','Lindy Boggs Energy Center','104','Math & Natural Science,Science with Laboratory','','PHYS 1211','Fall');
INSERT INTO courses VALUES('PHYS 1211','Introductory Physics I Lab','82513',0,'Timothy Schuler','T','1-2:50 PM','Percival Stern','2022','Math & Natural Science,Science with Laboratory','','PHYS 1210','Fall');
INSERT INTO courses VALUES('PSYC/NSCI XXXX','Elective','',0,'','','','','','','','','Fall,Spring');
INSERT INTO courses VALUES('CELL/NSCI XXXX','Elective','',0,'','','','','','','','','Fall,Spring');
INSERT INTO courses VALUES('PSYC/NSCI/CELL/CHEM XXXX','Elective','',0,'','','','','','','','','Fall,Spring');
INSERT INTO courses VALUES('LAB XXXX -1','Neuroscience Lab','',0,'','','','','','','','','Fall,Spring');
INSERT INTO courses VALUES('AFRS 2000','Introduction to Africana Studies','96828',3,'Corey Miles','TR','11-12:15 PM','Mayer Hall','102','Global Perspectives,Textual & Hist Perspectives','','','Fall,Spring');
INSERT INTO courses VALUES('African Studies','Elective','',0,'','','','','','','','','');
INSERT INTO courses VALUES('African Diaspora','Elective','',0,'','','','','','','','','');
INSERT INTO courses VALUES('XXXX 3000 - 1','Elective','',0,'','','','','','','','','');
INSERT INTO courses VALUES('XXXX 3000 - 2','Elective','',0,'','','','','','','','','');
INSERT INTO courses VALUES('XXXX 3000 - 3','Elective','',0,'','','','','','','','','');
INSERT INTO courses VALUES('LAST 1010','Introduction to Latin American Studies','37470',3,'Erin Hannahan','MWF','9-9:50','Joseph Merrick Memorial','100','Global Perspectives,Perspectives - Non European','','','Fall,Spring');
INSERT INTO courses VALUES('LAST XXXX - 1','Elective- 2000-level or higher','',0,'','','','','','','','','');
INSERT INTO courses VALUES('LAST XXXX - 2','Elective- 2000-level or higher','',0,'','','','','','','','','');
INSERT INTO courses VALUES('LAST XXXX - 3','Elective- 4000-level or higher','',0,'','','','','','','','','');
INSERT INTO courses VALUES('LAST XXXX - 4','Elective- 4000-level or higher','',0,'','','','','','','','','');
INSERT INTO courses VALUES('CENG 2110','Matl & Energy Balances','38398',3,'Matthew Montemore','TR','12:30 PM - 01:45 PM','Lindy Boggs Energy Center','242','Math & Natural Science','CHEM 1080,MATH 1220','CENG 2110,CENG 2320','Fall');
CREATE TABLE programs (
        program_id INTEGER PRIMARY KEY AUTOINCREMENT,
        program_name TEXT,
        program_type TEXT
    );
INSERT INTO programs VALUES(1,'Computer Science','Major');
INSERT INTO programs VALUES(2,'Neuroscience','Major');
INSERT INTO programs VALUES(3,'Africana Studies Minor','Minor');
INSERT INTO programs VALUES(4,'Latin American Studies Minor','Minor');
INSERT INTO programs VALUES(5,'Chemical Engineering','Major');
INSERT INTO programs VALUES(6,'Arabic','Minor');
CREATE TABLE program_courses (
        program_id INTEGER,
        course_id TEXT,
        FOREIGN KEY(program_id) REFERENCES programs(program_id),
        FOREIGN KEY(course_id) REFERENCES courses(course_id)
    );
INSERT INTO program_courses VALUES(1,'MATH 1210');
INSERT INTO program_courses VALUES(1,'CMPS 2170');
INSERT INTO program_courses VALUES(1,'CMPS 2171');
INSERT INTO program_courses VALUES(1,'MATH 3090');
INSERT INTO program_courses VALUES(1,'MATH 3091');
INSERT INTO program_courses VALUES(1,'MATH 1230');
INSERT INTO program_courses VALUES(1,'MATH 1231');
INSERT INTO program_courses VALUES(1,'MATH 3070');
INSERT INTO program_courses VALUES(1,'MATH 3071');
INSERT INTO program_courses VALUES(1,'CMPS 1500');
INSERT INTO program_courses VALUES(1,'CMPS 1501');
INSERT INTO program_courses VALUES(1,'CMPS 1600');
INSERT INTO program_courses VALUES(1,'CMPS 1601');
INSERT INTO program_courses VALUES(1,'CMPS 2200');
INSERT INTO program_courses VALUES(1,'CMPS 2201');
INSERT INTO program_courses VALUES(1,'CMPS 2300');
INSERT INTO program_courses VALUES(1,'CMPS 2301');
INSERT INTO program_courses VALUES(1,'CMPS 4770');
INSERT INTO program_courses VALUES(1,'CMPS 4720');
INSERT INTO program_courses VALUES(1,'CMPS XXXX - 1');
INSERT INTO program_courses VALUES(1,'CMPS XXXX - 2');
INSERT INTO program_courses VALUES(1,'CMPS XXXX - 3');
INSERT INTO program_courses VALUES(1,'CMPS XXXX - 4');
INSERT INTO program_courses VALUES(1,'CMPS XXXX - 5');
INSERT INTO program_courses VALUES(1,'CMPS 4010');
INSERT INTO program_courses VALUES(1,'CMPS 4020');
INSERT INTO program_courses VALUES(2,'NSCI 3300');
INSERT INTO program_courses VALUES(2,'PSYC 1000');
INSERT INTO program_courses VALUES(2,'PSYC 1001');
INSERT INTO program_courses VALUES(2,'NSCI 3310');
INSERT INTO program_courses VALUES(2,'NSCI 3320');
INSERT INTO program_courses VALUES(2,'NSCI 4910');
INSERT INTO program_courses VALUES(2,'CELL 1010');
INSERT INTO program_courses VALUES(2,'CELL 2050');
INSERT INTO program_courses VALUES(2,'CHEM 1070');
INSERT INTO program_courses VALUES(2,'CHEM 1075');
INSERT INTO program_courses VALUES(2,'CHEM 1080');
INSERT INTO program_courses VALUES(2,'CHEM 1085');
INSERT INTO program_courses VALUES(2,'CHEM 2410');
INSERT INTO program_courses VALUES(2,'CHEM 2415');
INSERT INTO program_courses VALUES(2,'PHYS 1210');
INSERT INTO program_courses VALUES(2,'PHYS 1211');
INSERT INTO program_courses VALUES(2,'PSYC/NSCI XXXX');
INSERT INTO program_courses VALUES(2,'CELL/NSCI XXXX');
INSERT INTO program_courses VALUES(2,'PSYC/NSCI/CELL/CHEM XXXX');
INSERT INTO program_courses VALUES(2,'LAB XXXX -1');
INSERT INTO program_courses VALUES(3,'AFRS 2000');
INSERT INTO program_courses VALUES(3,'African Studies');
INSERT INTO program_courses VALUES(3,'African Diaspora');
INSERT INTO program_courses VALUES(3,'XXXX 3000 - 1');
INSERT INTO program_courses VALUES(3,'XXXX 3000 - 2');
INSERT INTO program_courses VALUES(3,'XXXX 3000 - 3');
INSERT INTO program_courses VALUES(4,'LAST 1010');
INSERT INTO program_courses VALUES(4,'LAST XXXX - 1');
INSERT INTO program_courses VALUES(4,'LAST XXXX - 2');
INSERT INTO program_courses VALUES(4,'LAST XXXX - 3');
INSERT INTO program_courses VALUES(4,'LAST XXXX - 4');
INSERT INTO program_courses VALUES(5,'CENG 2110');
CREATE TABLE ntc_requirements (
        ntc_requirement TEXT,
        num_classes INTEGER
    );
INSERT INTO ntc_requirements VALUES('Aesthetics & Creative Arts',1);
INSERT INTO ntc_requirements VALUES('Foreign Language',3);
INSERT INTO ntc_requirements VALUES('Formal Reasoning',1);
INSERT INTO ntc_requirements VALUES('Global Perspectives',1);
INSERT INTO ntc_requirements VALUES('Math & Natural Science',3);
INSERT INTO ntc_requirements VALUES('Public Service',2);
INSERT INTO ntc_requirements VALUES('Race and Inclusion',1);
INSERT INTO ntc_requirements VALUES('Social & Behavioral Sci',2);
INSERT INTO ntc_requirements VALUES('Textual & Hist Perspect',2);
INSERT INTO ntc_requirements VALUES('Writing Tier-1',1);
INSERT INTO ntc_requirements VALUES('Writing Intensive Tier-2',1);
INSERT INTO ntc_requirements VALUES('TIDES',1);
DELETE FROM sqlite_sequence;
INSERT INTO sqlite_sequence VALUES('programs',6);
COMMIT;
