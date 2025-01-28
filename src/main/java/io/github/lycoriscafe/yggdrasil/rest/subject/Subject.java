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

import io.github.lycoriscafe.yggdrasil.commons.Entity;

import java.math.BigInteger;
import java.util.Objects;

public class Subject implements Entity {
    private BigInteger id;
    private Integer grade;
    private String shortName;
    private String longName;
    private BigInteger teacherId;

    public Subject(Integer grade,
                   String shortName) {
        this.grade = Objects.requireNonNull(grade);
        this.shortName = Objects.requireNonNull(shortName);
    }

    public BigInteger getId() {
        return id;
    }

    public Subject setId(BigInteger id) {
        this.id = id;
        return this;
    }

    public Integer getGrade() {
        return grade;
    }

    public Subject setGrade(Integer grade) {
        this.grade = Objects.requireNonNull(grade);
        return this;
    }

    public String getShortName() {
        return shortName;
    }

    public Subject setShortName(String shortName) {
        this.shortName = Objects.requireNonNull(shortName);
        return this;
    }

    public String getLongName() {
        return longName;
    }

    public Subject setLongName(String longName) {
        this.longName = longName;
        return this;
    }

    public BigInteger getTeacherId() {
        return teacherId;
    }

    public Subject setTeacherId(BigInteger teacherId) {
        this.teacherId = teacherId;
        return this;
    }
}
