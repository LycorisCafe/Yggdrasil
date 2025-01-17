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

import io.github.lycoriscafe.nexus.http.core.headers.content.MultipartFormData;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Objects;

public class Timetable {
    private Long id;
    private Long teacherId;
    private Long subjectId;
    private Long classroomId;
    private DayOfWeek day;
    private Integer timeslot;

    private Timetable() {}

    public Timetable(Long teacherId,
                     Long subjectId,
                     Long classroomId,
                     DayOfWeek day,
                     Integer timeslot) {
        this.teacherId = Objects.requireNonNull(teacherId);
        this.subjectId = Objects.requireNonNull(subjectId);
        this.classroomId = Objects.requireNonNull(classroomId);
        this.day = Objects.requireNonNull(day);
        this.timeslot = Objects.requireNonNull(timeslot);
    }

    public Long getId() {
        return id;
    }

    public Timetable setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public Timetable setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
        return this;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public Timetable setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public Long getClassroomId() {
        return classroomId;
    }

    public Timetable setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
        return this;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public Timetable setDay(DayOfWeek day) {
        this.day = day;
        return this;
    }

    public Integer getTimeslot() {
        return timeslot;
    }

    public Timetable setTimeslot(Integer timeslot) {
        this.timeslot = timeslot;
        return this;
    }

    public static Timetable toTimetable(List<MultipartFormData> multipartFormData) {
        var timetable = new Timetable();
        for (var formData : multipartFormData) {
            switch (formData.getName()) {
                case "id" -> timetable.setId(Long.parseLong(new String(formData.getData())));
                case "teacherId" -> timetable.setTeacherId(Long.parseLong(new String(formData.getData())));
                case "subjectId" -> timetable.setSubjectId(Long.parseLong(new String(formData.getData())));
                case "classroomId" -> timetable.setClassroomId(Long.parseLong(new String(formData.getData())));
                case "day" -> timetable.setDay(DayOfWeek.of(Integer.parseInt(new String(formData.getData()))));
                case "timeslot" -> timetable.setTimeslot(Integer.parseInt(new String(formData.getData())));
                default -> throw new IllegalStateException("Unexpected value: " + formData.getName());
            }
        }
        return timetable;
    }
}
