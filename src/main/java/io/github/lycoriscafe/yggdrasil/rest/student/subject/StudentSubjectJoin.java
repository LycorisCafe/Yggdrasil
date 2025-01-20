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

package io.github.lycoriscafe.yggdrasil.rest.student.subject;

import io.github.lycoriscafe.yggdrasil.configuration.commons.Entity;

import java.util.Objects;

public class StudentSubjectJoin implements Entity {
    private Long id;
    private Long studentId;
    private Long subjectId;

    public StudentSubjectJoin(Long studentId,
                              Long subjectId) {
        this.studentId = Objects.requireNonNull(studentId);
        this.subjectId = Objects.requireNonNull(subjectId);
    }

    public Long getId() {
        return id;
    }

    public StudentSubjectJoin setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getStudentId() {
        return studentId;
    }

    public StudentSubjectJoin setStudentId(Long studentId) {
        this.studentId = studentId;
        return this;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public StudentSubjectJoin setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
        return this;
    }
}
