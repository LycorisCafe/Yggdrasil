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

package io.github.lycoriscafe.yggdrasil.rest.subject;

import io.github.lycoriscafe.nexus.http.core.headers.content.MultipartFormData;
import io.github.lycoriscafe.yggdrasil.configuration.database.Entity;

import java.util.List;
import java.util.Objects;

public class Subject implements Entity {
    private Long id;
    private Integer grade;
    private String shortName;
    private String longName;
    private Long teacherId;

    private Subject() {}

    public Subject(Integer grade,
                   String shortName) {
        this.grade = Objects.requireNonNull(grade);
        this.shortName = Objects.requireNonNull(shortName);
    }

    public Long getId() {
        return id;
    }

    public Subject setId(Long id) {
        this.id = id;
        return this;
    }

    public Integer getGrade() {
        return grade;
    }

    public Subject setGrade(Integer grade) {
        this.grade = grade;
        return this;
    }

    public String getShortName() {
        return shortName;
    }

    public Subject setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public String getLongName() {
        return longName;
    }

    public Subject setLongName(String longName) {
        this.longName = longName;
        return this;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public Subject setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
        return this;
    }

    public static Subject toSubject(List<MultipartFormData> multipartFormData) {
        var subject = new Subject();
        for (var formData : multipartFormData) {
            switch (formData.getName()) {
                case "id" -> subject.setId(Long.parseLong(new String(formData.getData())));
                case "grade" -> subject.setGrade(Integer.parseInt(new String(formData.getData())));
                case "shortName" -> subject.setShortName(new String(formData.getData()));
                case "longName" -> subject.setLongName(new String(formData.getData()));
                case "teacherId" -> subject.setTeacherId(Long.parseLong(new String(formData.getData())));
                default -> throw new IllegalStateException("Unexpected value: " + formData.getName());
            }
        }
        return subject;
    }
}
