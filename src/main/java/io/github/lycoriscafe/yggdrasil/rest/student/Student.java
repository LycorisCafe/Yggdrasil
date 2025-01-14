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

package io.github.lycoriscafe.yggdrasil.rest.student;

import io.github.lycoriscafe.yggdrasil.rest.Gender;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.Year;

@Data
@NoArgsConstructor
public class Student {
    private Long id;
    @NonNull
    private Long guardianId;
    private Long classroomId;
    @NonNull
    private String initName;
    @NonNull
    private String fullName;
    @NonNull
    private Gender gender;
    @NonNull
    private LocalDate dateOfBirth;
    private String nic;
    @NonNull
    private String address;
    private Year regYear;
    private String contactNo;
    private String email;
    private Boolean disabled;
}
