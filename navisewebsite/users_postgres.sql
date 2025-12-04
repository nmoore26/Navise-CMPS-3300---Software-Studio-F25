-- PostgreSQL schema and data for users table
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    first_name TEXT,
    last_name TEXT,
    user_type TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users VALUES(1,'lsawaf@tulane.edu','naviseproject','Lulu','Sawaf','student','2025-12-04 03:31:01');
INSERT INTO users VALUES(2,'symone.moore2004@gmail.com','CSCapstone25!','Nina','Moore','student','2025-12-04 04:35:14');
