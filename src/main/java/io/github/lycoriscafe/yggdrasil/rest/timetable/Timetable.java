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

package io.github.lycoriscafe.yggdrasil.rest.timetable;

import io.github.lycoriscafe.yggdrasil.configuration.commons.Entity;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Objects;

public class Timetable implements Entity {
    private BigDecimal id;
    private BigDecimal teacherId;
    private BigDecimal subjectId;
    private BigDecimal classroomId;
    private DayOfWeek day;
    private Integer timeslot;

    public Timetable(BigDecimal teacherId,
                     BigDecimal subjectId,
                     BigDecimal classroomId,
                     DayOfWeek day,
                     Integer timeslot) {
        this.teacherId = Objects.requireNonNull(teacherId);
        this.subjectId = Objects.requireNonNull(subjectId);
        this.classroomId = Objects.requireNonNull(classroomId);
        this.day = Objects.requireNonNull(day);
        this.timeslot = Objects.requireNonNull(timeslot);
    }

    public BigDecimal getId() {
        return id;
    }

    public Timetable setId(BigDecimal id) {
        this.id = id;
        return this;
    }

    public BigDecimal getTeacherId() {
        return teacherId;
    }

    public Timetable setTeacherId(BigDecimal teacherId) {
        this.teacherId = Objects.requireNonNull(teacherId);
        return this;
    }

    public BigDecimal getSubjectId() {
        return subjectId;
    }

    public Timetable setSubjectId(BigDecimal subjectId) {
        this.subjectId = Objects.requireNonNull(subjectId);
        return this;
    }

    public BigDecimal getClassroomId() {
        return classroomId;
    }

    public Timetable setClassroomId(BigDecimal classroomId) {
        this.classroomId = Objects.requireNonNull(classroomId);
        return this;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public Timetable setDay(DayOfWeek day) {
        this.day = Objects.requireNonNull(day);
        return this;
    }

    public Integer getTimeslot() {
        return timeslot;
    }

    public Timetable setTimeslot(Integer timeslot) {
        this.timeslot = Objects.requireNonNull(timeslot);
        return this;
    }
}
