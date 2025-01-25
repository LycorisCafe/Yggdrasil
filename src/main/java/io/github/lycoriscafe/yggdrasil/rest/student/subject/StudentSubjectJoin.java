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

import io.github.lycoriscafe.yggdrasil.commons.Entity;

import java.math.BigDecimal;
import java.util.Objects;

public class StudentSubjectJoin implements Entity {
    private BigDecimal id;
    private BigDecimal studentId;
    private BigDecimal subjectId;

    public StudentSubjectJoin(BigDecimal studentId,
                              BigDecimal subjectId) {
        this.studentId = Objects.requireNonNull(studentId);
        this.subjectId = Objects.requireNonNull(subjectId);
    }

    public BigDecimal getId() {
        return id;
    }

    public StudentSubjectJoin setId(BigDecimal id) {
        this.id = id;
        return this;
    }

    public BigDecimal getStudentId() {
        return studentId;
    }

    public StudentSubjectJoin setStudentId(BigDecimal studentId) {
        this.studentId = Objects.requireNonNull(studentId);
        return this;
    }

    public BigDecimal getSubjectId() {
        return subjectId;
    }

    public StudentSubjectJoin setSubjectId(BigDecimal subjectId) {
        this.subjectId = Objects.requireNonNull(subjectId);
        return this;
    }
}
