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

package io.github.lycoriscafe.yggdrasil.rest.classroom;

import io.github.lycoriscafe.nexus.http.core.headers.content.MultipartFormData;

import java.util.List;
import java.util.Objects;

public class Classroom {
    private Long id;
    private Long teacherId;
    private Integer grade;
    private String name;

    private Classroom() {}

    public Classroom(Integer grade,
                     String name) {
        this.grade = Objects.requireNonNull(grade);
        this.name = Objects.requireNonNull(name);
    }

    public Long getId() {
        return id;
    }

    public Classroom setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public Classroom setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
        return this;
    }

    public Integer getGrade() {
        return grade;
    }

    public Classroom setGrade(Integer grade) {
        this.grade = grade;
        return this;
    }

    public String getName() {
        return name;
    }

    public Classroom setName(String name) {
        this.name = name;
        return this;
    }

    public static Classroom toClassroom(List<MultipartFormData> multipartFormData) {
        var classroom = new Classroom();
        for (var formData : multipartFormData) {
            switch (formData.getName()) {
                case "id" -> classroom.setId(Long.parseLong(new String(formData.getData())));
                case "teacherId" -> classroom.setTeacherId(Long.parseLong(new String(formData.getData())));
                case "grade" -> classroom.setGrade(Integer.parseInt(new String(formData.getData())));
                case "name" -> classroom.setName(new String(formData.getData()));
                default -> throw new IllegalStateException("Unexpected value: " + formData.getName());
            }
        }
        return classroom;
    }
}
