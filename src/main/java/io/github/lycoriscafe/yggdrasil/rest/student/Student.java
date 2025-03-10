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

import io.github.lycoriscafe.yggdrasil.commons.Entity;
import io.github.lycoriscafe.yggdrasil.rest.Gender;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Year;
import java.util.Objects;

public final class Student implements Entity {
    private BigInteger id;
    private BigInteger guardianId;
    private BigInteger classroomId;
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

    public Student() {}

    @Override
    public BigInteger getId() {
        return id;
    }

    public Student setId(BigInteger id) {
        this.id = id;
        return this;
    }

    public BigInteger getGuardianId() {
        return guardianId;
    }

    public Student setGuardianId(BigInteger guardianId) {
        this.guardianId = Objects.requireNonNull(guardianId);
        return this;
    }

    public BigInteger getClassroomId() {
        return classroomId;
    }

    public Student setClassroomId(BigInteger classroomId) {
        this.classroomId = classroomId;
        return this;
    }

    public String getInitName() {
        return initName;
    }

    public Student setInitName(String initName) {
        this.initName = Objects.requireNonNull(initName);
        return this;
    }

    public String getFullName() {
        return fullName;
    }

    public Student setFullName(String fullName) {
        this.fullName = Objects.requireNonNull(fullName);
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public Student setGender(Gender gender) {
        this.gender = Objects.requireNonNull(gender);
        return this;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Student setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth);
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
        this.address = Objects.requireNonNull(address);
        return this;
    }

    public Year getRegYear() {
        return regYear;
    }

    public Student setRegYear(Year regYear) {
        this.regYear = Objects.requireNonNull(regYear);
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
}
