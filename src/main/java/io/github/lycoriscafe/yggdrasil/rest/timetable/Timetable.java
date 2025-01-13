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

package io.github.lycoriscafe.yggdrasil.rest.timetable;

import io.github.lycoriscafe.yggdrasil.rest.classroom.Classroom;
import io.github.lycoriscafe.yggdrasil.rest.subject.Subject;
import io.github.lycoriscafe.yggdrasil.rest.teacher.Teacher;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.DayOfWeek;

@Data
@NoArgsConstructor
@Entity
public class Timetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    @OneToOne(fetch = FetchType.LAZY)
    private Teacher teacher;
    @NonNull
    @OneToOne(fetch = FetchType.LAZY)
    private Subject subject;
    @NonNull
    @OneToOne(fetch = FetchType.LAZY)
    private Classroom classroom;
    @NonNull
    private DayOfWeek day;
    @NonNull
    private Integer timeslot;
    private Boolean disabled;
}
