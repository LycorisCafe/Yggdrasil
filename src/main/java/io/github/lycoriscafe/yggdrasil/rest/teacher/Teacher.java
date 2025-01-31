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

import io.github.lycoriscafe.yggdrasil.commons.Entity;
import io.github.lycoriscafe.yggdrasil.rest.Gender;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Objects;

public final class Teacher implements Entity {
    private BigInteger id;
    private String nic;
    private String initName;
    private String fullName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String address;
    private String email;
    private String contactNo;
    private Boolean disabled;

    public Teacher() {}

    @Override
    public BigInteger getId() {
        return id;
    }

    public Teacher setId(BigInteger id) {
        this.id = id;
        return this;
    }

    public String getNic() {
        return nic;
    }

    public Teacher setNic(String nic) {
        this.nic = Objects.requireNonNull(nic);
        return this;
    }

    public String getInitName() {
        return initName;
    }

    public Teacher setInitName(String initName) {
        this.initName = Objects.requireNonNull(initName);
        return this;
    }

    public String getFullName() {
        return fullName;
    }

    public Teacher setFullName(String fullName) {
        this.fullName = Objects.requireNonNull(fullName);
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public Teacher setGender(Gender gender) {
        this.gender = Objects.requireNonNull(gender);
        return this;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Teacher setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth);
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Teacher setAddress(String address) {
        this.address = Objects.requireNonNull(address);
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Teacher setEmail(String email) {
        this.email = Objects.requireNonNull(email);
        return this;
    }

    public String getContactNo() {
        return contactNo;
    }

    public Teacher setContactNo(String contactNo) {
        this.contactNo = Objects.requireNonNull(contactNo);
        return this;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public Teacher setDisabled(Boolean disabled) {
        this.disabled = disabled;
        return this;
    }
}
