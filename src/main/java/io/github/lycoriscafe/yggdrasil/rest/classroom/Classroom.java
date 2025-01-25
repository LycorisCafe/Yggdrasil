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

import io.github.lycoriscafe.yggdrasil.commons.Entity;

import java.math.BigDecimal;
import java.util.Objects;

public class Classroom implements Entity {
    private BigDecimal id;
    private BigDecimal teacherId;
    private Integer grade;
    private String name;

    public Classroom(Integer grade,
                     String name) {
        this.grade = Objects.requireNonNull(grade);
        this.name = Objects.requireNonNull(name);
    }

    public BigDecimal getId() {
        return id;
    }

    public Classroom setId(BigDecimal id) {
        this.id = id;
        return this;
    }

    public BigDecimal getTeacherId() {
        return teacherId;
    }

    public Classroom setTeacherId(BigDecimal teacherId) {
        this.teacherId = teacherId;
        return this;
    }

    public Integer getGrade() {
        return grade;
    }

    public Classroom setGrade(Integer grade) {
        this.grade = Objects.requireNonNull(grade);
        return this;
    }

    public String getName() {
        return name;
    }

    public Classroom setName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }
}
