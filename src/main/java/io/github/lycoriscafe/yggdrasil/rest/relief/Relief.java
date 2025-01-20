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

package io.github.lycoriscafe.yggdrasil.rest.relief;

import io.github.lycoriscafe.yggdrasil.configuration.commons.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Relief implements Entity {
    private BigDecimal id;
    private BigDecimal timetableId;
    private BigDecimal teacherId;
    private LocalDate date;

    public Relief(BigDecimal timetableId,
                  BigDecimal teacherId,
                  LocalDate date) {
        this.timetableId = Objects.requireNonNull(timetableId);
        this.teacherId = Objects.requireNonNull(teacherId);
        this.date = Objects.requireNonNull(date);
    }

    public BigDecimal getId() {
        return id;
    }

    public Relief setId(BigDecimal id) {
        this.id = id;
        return this;
    }

    public BigDecimal getTimetableId() {
        return timetableId;
    }

    public Relief setTimetableId(BigDecimal timetableId) {
        this.timetableId = Objects.requireNonNull(timetableId);
        return this;
    }

    public BigDecimal getTeacherId() {
        return teacherId;
    }

    public Relief setTeacherId(BigDecimal teacherId) {
        this.teacherId = Objects.requireNonNull(teacherId);
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public Relief setDate(LocalDate date) {
        this.date = Objects.requireNonNull(date);
        return this;
    }
}
