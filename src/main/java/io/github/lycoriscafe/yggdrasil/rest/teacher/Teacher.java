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

package io.github.lycoriscafe.yggdrasil.rest.teacher;

import io.github.lycoriscafe.yggdrasil.rest.Gender;
import io.github.lycoriscafe.yggdrasil.rest.subject.Subject;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private String nic;
    @NonNull
    private String initName;
    @NonNull
    private String fullName;
    @NonNull
    private Gender gender;
    @NonNull
    private String address;
    @NonNull
    private String email;
    @NonNull
    private String contactNo;
    @NonNull
    @OneToMany(fetch = FetchType.LAZY)
    private List<Subject> subjects;
    private Boolean disabled;
}
