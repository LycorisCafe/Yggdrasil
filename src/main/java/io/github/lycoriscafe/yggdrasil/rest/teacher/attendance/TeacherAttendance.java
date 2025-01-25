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

package io.github.lycoriscafe.yggdrasil.rest.teacher.attendance;

import io.github.lycoriscafe.yggdrasil.commons.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class TeacherAttendance implements Entity {
    private BigDecimal id;
    private BigDecimal teacherId;
    private LocalDate date;
    private LocalTime time;

    public TeacherAttendance(BigDecimal teacherId) {
        this.teacherId = Objects.requireNonNull(teacherId);
    }

    public BigDecimal getId() {
        return id;
    }

    public TeacherAttendance setId(BigDecimal id) {
        this.id = id;
        return this;
    }

    public BigDecimal getTeacherId() {
        return teacherId;
    }

    public TeacherAttendance setTeacherId(BigDecimal teacherId) {
        this.teacherId = Objects.requireNonNull(teacherId);
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public TeacherAttendance setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public LocalTime getTime() {
        return time;
    }

    public TeacherAttendance setTime(LocalTime time) {
        this.time = time;
        return this;
    }
}
