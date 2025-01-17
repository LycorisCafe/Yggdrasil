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

import io.github.lycoriscafe.nexus.http.core.headers.content.MultipartFormData;

import java.util.List;
import java.util.Objects;

public class TeacherSubjectJoin {
    private Long teacherId;
    private Long subjectId;

    private TeacherSubjectJoin() {}

    public TeacherSubjectJoin(Long teacherId,
                              Long subjectId) {
        this.teacherId = Objects.requireNonNull(teacherId);
        this.subjectId = Objects.requireNonNull(subjectId);
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public TeacherSubjectJoin setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
        return this;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public TeacherSubjectJoin setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public static TeacherSubjectJoin toTeacherSubjectJoin(List<MultipartFormData> multipartFormData) {
        var teacherSubjectJoin = new TeacherSubjectJoin();
        for (var formData : multipartFormData) {
            switch (formData.getName()) {
                case "teacherId" -> teacherSubjectJoin.setTeacherId(Long.parseLong(new String(formData.getData())));
                case "subjectId" -> teacherSubjectJoin.setSubjectId(Long.parseLong(new String(formData.getData())));
                default -> throw new IllegalStateException("Unexpected value: " + formData.getName());
            }
        }
        return teacherSubjectJoin;
    }
}
