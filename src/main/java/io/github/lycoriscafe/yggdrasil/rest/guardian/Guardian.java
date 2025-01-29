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

package io.github.lycoriscafe.yggdrasil.rest.guardian;

import io.github.lycoriscafe.yggdrasil.commons.Entity;
import io.github.lycoriscafe.yggdrasil.rest.Gender;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Objects;

public class Guardian implements Entity {
    private BigInteger id;
    private String nic;
    private String initName;
    private String fullName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String address;
    private String email;
    private String contactNo;

    public Guardian() {}

    public Guardian(String nic,
                    String initName,
                    String fullName,
                    Gender gender,
                    LocalDate dateOfBirth,
                    String address,
                    String contactNo) {
        this.nic = Objects.requireNonNull(nic);
        this.initName = Objects.requireNonNull(initName);
        this.fullName = Objects.requireNonNull(fullName);
        this.gender = Objects.requireNonNull(gender);
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth);
        this.address = Objects.requireNonNull(address);
        this.contactNo = Objects.requireNonNull(contactNo);
    }

    public BigInteger getId() {
        return id;
    }

    public Guardian setId(BigInteger id) {
        this.id = id;
        return this;
    }

    public String getNic() {
        return nic;
    }

    public Guardian setNic(String nic) {
        this.nic = Objects.requireNonNull(nic);
        return this;
    }

    public String getInitName() {
        return initName;
    }

    public Guardian setInitName(String initName) {
        this.initName = Objects.requireNonNull(initName);
        return this;
    }

    public String getFullName() {
        return fullName;
    }

    public Guardian setFullName(String fullName) {
        this.fullName = Objects.requireNonNull(fullName);
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public Guardian setGender(Gender gender) {
        this.gender = Objects.requireNonNull(gender);
        return this;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth);
    }

    public String getAddress() {
        return address;
    }

    public Guardian setAddress(String address) {
        this.address = Objects.requireNonNull(address);
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Guardian setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getContactNo() {
        return contactNo;
    }

    public Guardian setContactNo(String contactNo) {
        this.contactNo = Objects.requireNonNull(contactNo);
        return this;
    }
}
