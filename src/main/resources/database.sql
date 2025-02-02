# Create the database
# CREATE DATABASE yggdrasil CHARACTER SET 'utf8mb4';

# ======================================================================================================================

# Admin
CREATE TABLE admin
(
    id          SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    accessLevel SET ('SUPERUSER', 'CLASSROOM', 'GUARDIAN', 'NOTIFICATION',
        'RELIEF', 'STUDENT', 'SUBJECT', 'TEACHER', 'TIMETABLE') NOT NULL,
    disabled    BOOLEAN DEFAULT FALSE # Used for disabling without deleting
);

# Classroom
CREATE TABLE classroom
(
    id        SERIAL PRIMARY KEY,
    teacherId BIGINT UNSIGNED UNIQUE,
    grade TINYINT(2) UNSIGNED NOT NULL,
    name  VARCHAR(10)         NOT NULL,
    UNIQUE (grade, name)
);

# Guardian
CREATE TABLE guardian
(
    id          SERIAL PRIMARY KEY,
    nic         VARCHAR(12)             NOT NULL UNIQUE,
    initName    VARCHAR(50)             NOT NULL,
    fullName    VARCHAR(200)            NOT NULL,
    gender      ENUM ('MALE', 'FEMALE') NOT NULL,
    dateOfBirth DATE                    NOT NULL,
    address     VARCHAR(300)            NOT NULL,
    email       VARCHAR(300),
    contactNo   VARCHAR(10)             NOT NULL
);

# Notification
CREATE TABLE notification
(
    id              SERIAL PRIMARY KEY,
    createTimestamp DATETIME,
    updateTimestamp DATETIME,
    scope           SET ('STUDENT', 'TEACHER') NOT NULL,
    message         VARCHAR(2000) NOT NULL,
    draft           BOOLEAN DEFAULT FALSE
);

# Relief
CREATE TABLE relief
(
    id          SERIAL PRIMARY KEY,
    timetableId BIGINT UNSIGNED NOT NULL,
    teacherId   BIGINT UNSIGNED NOT NULL,
    date        DATE            NOT NULL,
    UNIQUE (timetableId, date)
);

# Student
CREATE TABLE student
(
    id          SERIAL PRIMARY KEY,
    guardianId  BIGINT UNSIGNED         NOT NULL,
    classroomId BIGINT UNSIGNED,
    initName    VARCHAR(50)             NOT NULL,
    fullName    VARCHAR(200)            NOT NULL,
    gender      ENUM ('MALE', 'FEMALE') NOT NULL,
    dateOfBirth DATE                    NOT NULL,
    nic         VARCHAR(12) UNIQUE,
    address     VARCHAR(300)            NOT NULL,
    regYear YEAR NOT NULL,
    contactNo   VARCHAR(10),
    email       VARCHAR(300),
    disabled    BOOLEAN DEFAULT FALSE # Used for disabling without deleting
);

# Student Attendance
CREATE TABLE studentAttendance
(
    id   SERIAL PRIMARY KEY,
    studentId BIGINT UNSIGNED NOT NULL,
    date DATE,
    time TIME,
    UNIQUE (studentId, date)
);

# Student Subject Join
CREATE TABLE studentSubjectJoin
(
    id SERIAL PRIMARY KEY,
    studentId BIGINT UNSIGNED NOT NULL,
    subjectId BIGINT UNSIGNED NOT NULL,
    UNIQUE (studentId, subjectId)
);

# Subject
CREATE TABLE subject
(
    id        SERIAL PRIMARY KEY,
    grade     TINYINT(2) UNSIGNED NOT NULL,
    shortName VARCHAR(10)         NOT NULL,
    longName  VARCHAR(50),
    teacherId BIGINT UNSIGNED,
    UNIQUE (grade, shortName)
);

# Teacher
CREATE TABLE teacher
(
    id          SERIAL PRIMARY KEY,
    nic         VARCHAR(12)             NOT NULL UNIQUE,
    initName    VARCHAR(50)             NOT NULL,
    fullName    VARCHAR(200)            NOT NULL,
    gender      ENUM ('MALE', 'FEMALE') NOT NULL,
    dateOfBirth DATE                    NOT NULL,
    address     VARCHAR(300)            NOT NULL,
    email       VARCHAR(300)            NOT NULL UNIQUE,
    contactNo   VARCHAR(10)             NOT NULL UNIQUE,
    disabled    BOOLEAN DEFAULT FALSE # Used for disabling without deleting
);

# Teacher Attendance
CREATE TABLE teacherAttendance
(
    id   SERIAL PRIMARY KEY,
    teacherId BIGINT UNSIGNED NOT NULL,
    date DATE,
    time TIME,
    UNIQUE (teacherId, date)
);

# Teacher Subject Join
CREATE TABLE teacherSubjectJoin
(
    id SERIAL PRIMARY KEY,
    teacherId BIGINT UNSIGNED NOT NULL,
    subjectId BIGINT UNSIGNED NOT NULL,
    UNIQUE (teacherId, subjectId)
);

# Timetable
CREATE TABLE timetable
(
    id          SERIAL PRIMARY KEY,
    teacherId   BIGINT UNSIGNED     NOT NULL,
    subjectId   BIGINT UNSIGNED     NOT NULL,
    classroomId BIGINT UNSIGNED     NOT NULL,
    day      TINYINT(1) UNSIGNED NOT NULL, # Days begin as MONDAY = 1
    timeslot TINYINT(1) UNSIGNED NOT NULL  # Typical 8 periods of daily timetable
);

# Authentication
CREATE TABLE authentication
(
    role     ENUM ('STUDENT', 'TEACHER', 'ADMIN') NOT NULL,
    userId   BIGINT UNSIGNED                      NOT NULL,
    password VARBINARY(100)                       NOT NULL,
    PRIMARY KEY (role, userId)
);

# Device
CREATE TABLE device
(
    role         ENUM ('STUDENT', 'TEACHER', 'ADMIN') NOT NULL,
    userId       BIGINT UNSIGNED                      NOT NULL,
    deviceName   VARCHAR(20)           NOT NULL,
    accessToken  VARBINARY(100) UNIQUE NOT NULL,
    expires      BIGINT                NOT NULL,
    refreshToken VARBINARY(100) UNIQUE NOT NULL,
    lastLogin    DATETIME DEFAULT NOW() ON UPDATE NOW()
);

ALTER TABLE classroom
    ADD FOREIGN KEY (teacherId) REFERENCES teacher (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE relief
    ADD FOREIGN KEY (timetableId) REFERENCES timetable (id) ON UPDATE CASCADE ON DELETE CASCADE,
    ADD FOREIGN KEY (teacherId) REFERENCES teacher (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE student
    ADD FOREIGN KEY (guardianId) REFERENCES guardian (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE studentAttendance
    ADD FOREIGN KEY (studentId) REFERENCES student (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE studentSubjectJoin
    ADD FOREIGN KEY (studentId) REFERENCES student (id) ON UPDATE CASCADE ON DELETE CASCADE,
    ADD FOREIGN KEY (subjectId) REFERENCES subject (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE subject
    ADD FOREIGN KEY (teacherId) REFERENCES teacher (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE teacherAttendance
    ADD FOREIGN KEY (teacherId) REFERENCES teacher (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE teacherSubjectJoin
    ADD FOREIGN KEY (teacherId) REFERENCES teacher (id) ON UPDATE CASCADE ON DELETE CASCADE,
    ADD FOREIGN KEY (subjectId) REFERENCES subject (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE timetable
    ADD FOREIGN KEY (teacherId) REFERENCES teacher (id) ON UPDATE CASCADE ON DELETE CASCADE,
    ADD FOREIGN KEY (subjectId) REFERENCES subject (id) ON UPDATE CASCADE ON DELETE CASCADE,
    ADD FOREIGN KEY (classroomId) REFERENCES classroom (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE device
    ADD FOREIGN KEY (role, userId) REFERENCES authentication (role, userId) ON UPDATE CASCADE ON DELETE CASCADE;

# Triggers for StudentAttendance
DELIMITER //
CREATE TRIGGER studentAttendance_prevent_null_timestamp_insert
    BEFORE INSERT
    ON studentAttendance
    FOR EACH ROW
BEGIN
    SET NEW.date = DATE(NOW());
    SET NEW.time = TIME(NOW());
END;
//
DELIMITER ;

# Triggers for TeacherAttendance
DELIMITER //
CREATE TRIGGER teacherAttendance_prevent_null_timestamp_insert
    BEFORE INSERT
    ON teacherAttendance
    FOR EACH ROW
BEGIN
    SET NEW.date = DATE(NOW());
    SET NEW.time = TIME(NOW());
END;
//
DELIMITER ;

# Triggers for Notification
DELIMITER //
CREATE TRIGGER notification_prevent_null_timestamp_insert
    BEFORE INSERT
    ON notification
    FOR EACH ROW
BEGIN
    SET NEW.createTimestamp = NOW();
    SET NEW.updateTimestamp = NOW();
END;
//
DELIMITER ;
DELIMITER //
CREATE TRIGGER notification_prevent_null_timestamp_update
    BEFORE UPDATE
    ON notification
    FOR EACH ROW
BEGIN
    SET NEW.createTimestamp = OLD.createTimestamp;
    SET NEW.updateTimestamp = NOW();
END;
//
DELIMITER ;

/*
 * Copyright 2025 Lycoris Café
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

# ======================================================================================================================

# INSERT SUPERUSER (DEFAULT)
START TRANSACTION;

INSERT INTO admin (id, name, accessLevel)
VALUES (1, 'SUPERUSER', 'SUPERUSER');

INSERT INTO authentication (role, userId, password) # password is SUPERUSER
VALUES ('ADMIN', 1, '0ah66cwBDYMR1Gft+FRFe4y02jwep3Mmrsx19TLlI+c');

COMMIT;