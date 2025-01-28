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

import java.math.BigInteger;
import java.util.Objects;

public class TeacherSubjectJoin implements Entity {
    private BigInteger id;
    private BigInteger teacherId;
    private BigInteger subjectId;

    public TeacherSubjectJoin(BigInteger teacherId,
                              BigInteger subjectId) {
        this.teacherId = Objects.requireNonNull(teacherId);
        this.subjectId = Objects.requireNonNull(subjectId);
    }

    public BigInteger getId() {
        return id;
    }

    public TeacherSubjectJoin setId(BigInteger id) {
        this.id = id;
        return this;
    }

    public BigInteger getTeacherId() {
        return teacherId;
    }

    public TeacherSubjectJoin setTeacherId(BigInteger teacherId) {
        this.teacherId = Objects.requireNonNull(teacherId);
        return this;
    }

    public BigInteger getSubjectId() {
        return subjectId;
    }

    public TeacherSubjectJoin setSubjectId(BigInteger subjectId) {
        this.subjectId = Objects.requireNonNull(subjectId);
        return this;
    }
}
