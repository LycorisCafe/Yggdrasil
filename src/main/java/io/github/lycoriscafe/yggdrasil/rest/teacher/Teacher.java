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

import io.github.lycoriscafe.nexus.http.core.headers.content.MultipartFormData;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.commons.Entity;
import io.github.lycoriscafe.yggdrasil.rest.Gender;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class Teacher implements Entity {
    private Long id;
    private String nic;
    private String initName;
    private String fullName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String address;
    private String email;
    private String contactNo;
    private Boolean disabled;

    private Teacher() {}

    public Teacher(String nic,
                   String initName,
                   String fullName,
                   Gender gender,
                   LocalDate dateOfBirth,
                   String address,
                   String email,
                   String contactNo) {
        this.nic = Objects.requireNonNull(nic);
        this.initName = Objects.requireNonNull(initName);
        this.fullName = Objects.requireNonNull(fullName);
        this.gender = Objects.requireNonNull(gender);
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth);
        this.address = Objects.requireNonNull(address);
        this.email = Objects.requireNonNull(email);
        this.contactNo = Objects.requireNonNull(contactNo);
    }

    public Long getId() {
        return id;
    }

    public Teacher setId(Long id) {
        this.id = id;
        return this;
    }

    public String getNic() {
        return nic;
    }

    public Teacher setNic(String nic) {
        this.nic = nic;
        return this;
    }

    public String getInitName() {
        return initName;
    }

    public Teacher setInitName(String initName) {
        this.initName = initName;
        return this;
    }

    public String getFullName() {
        return fullName;
    }

    public Teacher setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public Teacher setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public Teacher setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Teacher setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getContactNo() {
        return contactNo;
    }

    public Teacher setContactNo(String contactNo) {
        this.contactNo = contactNo;
        return this;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public Teacher setDisabled(Boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public static Teacher toTeacher(List<MultipartFormData> multipartFormData) {
        var teacher = new Teacher();
        for (var formData : multipartFormData) {
            switch (formData.getName()) {
                case "id" -> teacher.setId(Long.parseLong(new String(formData.getData())));
                case "nic" -> teacher.setNic(new String(formData.getData()));
                case "initName" -> teacher.setInitName(new String(formData.getData()));
                case "fullName" -> teacher.setFullName(new String(formData.getData()));
                case "gender" -> teacher.setGender(Gender.valueOf(new String(formData.getData())));
                case "dateOfBirth" -> teacher.setDateOfBirth(LocalDate.parse(new String(formData.getData()), Utils.getDateFormatter()));
                case "address" -> teacher.setAddress(new String(formData.getData()));
                case "email" -> teacher.setEmail(new String(formData.getData()));
                case "contactNo" -> teacher.setContactNo(new String(formData.getData()));
                case "disabled" -> teacher.setDisabled(Boolean.parseBoolean(new String(formData.getData())));
                default -> throw new IllegalStateException("Unexpected value: " + formData.getName());
            }
        }
        return teacher;
    }
}
