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

package io.github.lycoriscafe.yggdrasil.rest.teacher.subject;

import io.github.lycoriscafe.yggdrasil.commons.Entity;

import java.math.BigDecimal;
import java.util.Objects;

public class TeacherSubjectJoin implements Entity {
    private BigDecimal id;
    private BigDecimal teacherId;
    private BigDecimal subjectId;

    public TeacherSubjectJoin(BigDecimal teacherId,
                              BigDecimal subjectId) {
        this.teacherId = Objects.requireNonNull(teacherId);
        this.subjectId = Objects.requireNonNull(subjectId);
    }

    public BigDecimal getId() {
        return id;
    }

    public TeacherSubjectJoin setId(BigDecimal id) {
        this.id = id;
        return this;
    }

    public BigDecimal getTeacherId() {
        return teacherId;
    }

    public TeacherSubjectJoin setTeacherId(BigDecimal teacherId) {
        this.teacherId = Objects.requireNonNull(teacherId);
        return this;
    }

    public BigDecimal getSubjectId() {
        return subjectId;
    }

    public TeacherSubjectJoin setSubjectId(BigDecimal subjectId) {
        this.subjectId = Objects.requireNonNull(subjectId);
        return this;
    }
}
