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

import io.github.lycoriscafe.nexus.http.core.headers.content.MultipartFormData;
import io.github.lycoriscafe.yggdrasil.configuration.database.Entity;
import io.github.lycoriscafe.yggdrasil.rest.Gender;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Objects;

public class Student implements Entity {
    private Long id;
    private Long guardianId;
    private Long classroomId;
    private String initName;
    private String fullName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String nic;
    private String address;
    private Year regYear;
    private String contactNo;
    private String email;
    private Boolean disabled;

    private Student() {}

    public Student(Long guardianId,
                   String initName,
                   String fullName,
                   Gender gender,
                   LocalDate dateOfBirth,
                   String address,
                   Year regYear) {
        this.guardianId = Objects.requireNonNull(guardianId);
        this.initName = Objects.requireNonNull(initName);
        this.fullName = Objects.requireNonNull(fullName);
        this.gender = Objects.requireNonNull(gender);
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth);
        this.address = Objects.requireNonNull(address);
        this.regYear = Objects.requireNonNull(regYear);
    }

    public Long getId() {
        return id;
    }

    public Student setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getGuardianId() {
        return guardianId;
    }

    public Student setGuardianId(Long guardianId) {
        this.guardianId = guardianId;
        return this;
    }

    public Long getClassroomId() {
        return classroomId;
    }

    public Student setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
        return this;
    }

    public String getInitName() {
        return initName;
    }

    public Student setInitName(String initName) {
        this.initName = initName;
        return this;
    }

    public String getFullName() {
        return fullName;
    }

    public Student setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public Student setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Student setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public String getNic() {
        return nic;
    }

    public Student setNic(String nic) {
        this.nic = nic;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Student setAddress(String address) {
        this.address = address;
        return this;
    }

    public Year getRegYear() {
        return regYear;
    }

    public Student setRegYear(Year regYear) {
        this.regYear = regYear;
        return this;
    }

    public String getContactNo() {
        return contactNo;
    }

    public Student setContactNo(String contactNo) {
        this.contactNo = contactNo;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Student setEmail(String email) {
        this.email = email;
        return this;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public Student setDisabled(Boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public static Student toStudent(List<MultipartFormData> multipartFormData) {
        var student = new Student();
        for (var formData : multipartFormData) {
            switch (formData.getName()) {
                case "id" -> student.setId(Long.parseLong(new String(formData.getData())));
                case "guardianId" -> student.setGuardianId(Long.parseLong(new String(formData.getData())));
                case "classroomId" -> student.setClassroomId(Long.parseLong(new String(formData.getData())));
                case "initName" -> student.setInitName(new String(formData.getData()));
                case "fullName" -> student.setFullName(new String(formData.getData()));
                case "gender" -> student.setGender(Gender.valueOf(new String(formData.getData())));
                case "dateOfBirth" -> student.setDateOfBirth(LocalDate.parse(new String(formData.getData())));
                case "nic" -> student.setNic(new String(formData.getData()));
                case "address" -> student.setAddress(new String(formData.getData()));
                case "regYear" -> student.setRegYear(Year.parse(new String(formData.getData())));
                case "contactNo" -> student.setContactNo(new String(formData.getData()));
                case "email" -> student.setEmail(new String(formData.getData()));
                case "disabled" -> student.setDisabled(Boolean.parseBoolean(new String(formData.getData())));
                default -> throw new IllegalStateException("Unexpected value: " + formData.getName());
            }
        }
        return student;
    }
}
