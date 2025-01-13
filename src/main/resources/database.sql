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
 *
 */

# Create the database
# CREATE DATABASE yggdrasil CHARACTER SET 'utf8mb4';

# ======================================================================================================================
# Start the constructions of the structure
START TRANSACTION;

## ADMIN
CREATE TABLE admin
(
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    disabled BOOLEAN DEFAULT FALSE # Used for disabling without deleting
);

## CLASSROOM
CREATE TABLE classroom
(
    id         SERIAL PRIMARY KEY,
    teacher_id BIGINT UNSIGNED UNIQUE,
    grade      TINYINT(2)  NOT NULL,
    name       VARCHAR(10) NOT NULL,
    disabled   BOOLEAN DEFAULT FALSE, # Used for disabling without deleting
    UNIQUE (grade, name)
);

## GUARDIAN
CREATE TABLE guardian
(
    id         SERIAL PRIMARY KEY,
    nic        VARCHAR(12)             NOT NULL UNIQUE,
    init_name  VARCHAR(50)             NOT NULL,
    full_name  VARCHAR(200)            NOT NULL,
    gender     ENUM ('MALE', 'FEMALE') NOT NULL,
    address    VARCHAR(300)            NOT NULL,
    email      VARCHAR(300),
    contact_no VARCHAR(10)             NOT NULL,
    disabled   BOOLEAN DEFAULT FALSE # Used for disabling without deleting
);

## NOTIFICATION
CREATE TABLE notification
(
    id        SERIAL PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT (NOW()),
    scope     SET ('STUDENT', 'TEACHER') NOT NULL,
    message   JSON                       NOT NULL,
    disabled  BOOLEAN   DEFAULT FALSE # Used for disabling without deleting
);

## RELIEF
CREATE TABLE relief
(
    id           SERIAL PRIMARY KEY,
    timetable_id BIGINT UNSIGNED NOT NULL,
    teacher_id   BIGINT UNSIGNED NOT NULL,
    date         DATE            NOT NULL,
    disabled     BOOLEAN DEFAULT FALSE, # Used for disabling without deleting
    UNIQUE (timetable_id, date)
);

## STUDENT
CREATE TABLE student
(
    id            SERIAL PRIMARY KEY,
    guardian_id   BIGINT UNSIGNED         NOT NULL,
    classroom_id  BIGINT UNSIGNED,
    initName      VARCHAR(50)             NOT NULL,
    fullName      VARCHAR(200)            NOT NULL,
    gender        ENUM ('MALE', 'FEMALE') NOT NULL,
    date_of_birth DATE                    NOT NULL,
    nic           VARCHAR(12) UNIQUE,
    address       VARCHAR(300)            NOT NULL,
    reg_year      YEAR,
    contact_no    VARCHAR(10),
    email         VARCHAR(300),
    disabled      BOOLEAN DEFAULT FALSE # Used for disabling without deleting
);

## STUDENT ATTENDANCE
CREATE TABLE student_attendance
(
    student_id BIGINT UNSIGNED NOT NULL,
    date       DATE    DEFAULT (DATE(NOW())),
    time       TIME    DEFAULT (TIME(NOW())),
    disabled   BOOLEAN DEFAULT FALSE, # Used for disabling without deleting
    UNIQUE (student_id, date)
);

## STUDENT SUBJECT JOIN
CREATE TABLE student_subject_join
(
    student_id BIGINT UNSIGNED NOT NULL,
    subject_id BIGINT UNSIGNED NOT NULL,
    disabled   BOOLEAN DEFAULT FALSE, # Used for disabling without deleting
    UNIQUE (student_id, subject_id)
);

## SUBJECT
CREATE TABLE subject
(
    id         SERIAL PRIMARY KEY,
    grade      TINYINT(2)  NOT NULL,
    short_name VARCHAR(10) NOT NULL,
    long_name  VARCHAR(50),
    teacher_id BIGINT UNSIGNED,
    disabled   BOOLEAN DEFAULT FALSE, # Used for disabling without deleting
    UNIQUE (grade, short_name)
);

## TEACHER
CREATE TABLE teacher
(
    id         SERIAL PRIMARY KEY,
    nic        VARCHAR(12)             NOT NULL UNIQUE,
    init_name  VARCHAR(50)             NOT NULL,
    full_name  VARCHAR(200)            NOT NULL,
    gender     ENUM ('MALE', 'FEMALE') NOT NULL,
    address    VARCHAR(300)            NOT NULL,
    email      VARCHAR(300)            NOT NULL UNIQUE,
    contact_no VARCHAR(10)             NOT NULL UNIQUE,
    disabled   BOOLEAN DEFAULT FALSE # Used for disabling without deleting
);

## TEACHER ATTENDANCE
CREATE TABLE teacher_attendance
(
    teacher_id BIGINT UNSIGNED NOT NULL,
    date       DATE    DEFAULT (DATE(NOW())),
    time       TIME    DEFAULT (TIME(NOW())),
    disabled   BOOLEAN DEFAULT FALSE, # Used for disabling without deleting
    UNIQUE (teacher_id, date)
);

## TEACHER SUBJECT JOIN
CREATE TABLE teacher_subject_join
(
    teacher_id BIGINT UNSIGNED NOT NULL,
    subject_id BIGINT UNSIGNED NOT NULL,
    disabled   BOOLEAN DEFAULT FALSE, # Used for disabling without deleting
    UNIQUE (teacher_id, subject_id)
);

## TIMETABLE
CREATE TABLE timetable
(
    id           SERIAL PRIMARY KEY,
    teacher_id   BIGINT UNSIGNED NOT NULL,
    subject_id   BIGINT UNSIGNED NOT NULL,
    classroom_id BIGINT UNSIGNED NOT NULL,
    day          TINYINT(1)      NOT NULL,
    timeslot     TINYINT(1)      NOT NULL, # Typical 8 periods of daily timetable
    disabled     BOOLEAN DEFAULT FALSE,    # Used for disabling without deleting
    UNIQUE (teacher_id, day, timeslot)
);

## AUTHENTICATION
CREATE TABLE authentication
(
    role          ENUM ('STUDENT', 'TEACHER', 'ADMIN') NOT NULL,
    user_id       BIGINT UNSIGNED                      NOT NULL,
    password      VARBINARY(100)                       NOT NULL,
    access_token  VARBINARY(100) UNIQUE,
    expires       TIMESTAMP DEFAULT (TIMESTAMPADD(HOUR, 1, NOW())),
    refresh_token VARBINARY(100) UNIQUE,
    disabled      BOOLEAN   DEFAULT FALSE,
    UNIQUE (role, user_id)
);

ALTER TABLE classroom
    ADD FOREIGN KEY (teacher_id) REFERENCES teacher (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE relief
    ADD FOREIGN KEY (timetable_id) REFERENCES timetable (id) ON UPDATE CASCADE ON DELETE CASCADE,
    ADD FOREIGN KEY (teacher_id) REFERENCES teacher (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE student
    ADD FOREIGN KEY (guardian_id) REFERENCES guardian (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE student_attendance
    ADD FOREIGN KEY (student_id) REFERENCES student (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE student_subject_join
    ADD FOREIGN KEY (student_id) REFERENCES student (id) ON UPDATE CASCADE ON DELETE CASCADE,
    ADD FOREIGN KEY (subject_id) REFERENCES subject (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE subject
    ADD FOREIGN KEY (teacher_id) REFERENCES teacher (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE teacher_attendance
    ADD FOREIGN KEY (teacher_id) REFERENCES teacher (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE teacher_subject_join
    ADD FOREIGN KEY (teacher_id) REFERENCES teacher (id) ON UPDATE CASCADE ON DELETE CASCADE,
    ADD FOREIGN KEY (subject_id) REFERENCES subject (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE timetable
    ADD FOREIGN KEY (teacher_id) REFERENCES teacher (id) ON UPDATE CASCADE ON DELETE CASCADE,
    ADD FOREIGN KEY (subject_id) REFERENCES subject (id) ON UPDATE CASCADE ON DELETE CASCADE,
    ADD FOREIGN KEY (classroom_id) REFERENCES classroom (id) ON UPDATE CASCADE ON DELETE CASCADE;

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

# End the constructions of the structure
COMMIT;

# ======================================================================================================================

## INSERT SUPERUSER (DEFAULT)
START TRANSACTION;

INSERT INTO admin (name, disabled)
VALUES ('SUPERUSER', false);

SET @userId = LAST_INSERT_ID();

INSERT INTO authentication (role, user_id, password)
VALUES ('ADMIN', @userId, 'SUPERUSER');

COMMIT;