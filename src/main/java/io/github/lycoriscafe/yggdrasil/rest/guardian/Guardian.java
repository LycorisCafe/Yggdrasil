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

import io.github.lycoriscafe.nexus.http.core.headers.content.MultipartFormData;
import io.github.lycoriscafe.yggdrasil.configuration.database.Entity;
import io.github.lycoriscafe.yggdrasil.rest.Gender;

import java.util.List;
import java.util.Objects;

public class Guardian implements Entity {
    private Long id;
    private String nic;
    private String initName;
    private String fullName;
    private Gender gender;
    private String address;
    private String email;
    private String contactNo;

    private Guardian() {}

    public Guardian(String nic,
                    String initName,
                    String fullName,
                    Gender gender,
                    String address,
                    String contactNo) {
        this.nic = Objects.requireNonNull(nic);
        this.initName = Objects.requireNonNull(initName);
        this.fullName = Objects.requireNonNull(fullName);
        this.gender = Objects.requireNonNull(gender);
        this.address = Objects.requireNonNull(address);
        this.contactNo = Objects.requireNonNull(contactNo);
    }

    public Long getId() {
        return id;
    }

    public Guardian setId(Long id) {
        this.id = id;
        return this;
    }

    public String getNic() {
        return nic;
    }

    public Guardian setNic(String nic) {
        this.nic = nic;
        return this;
    }

    public String getInitName() {
        return initName;
    }

    public Guardian setInitName(String initName) {
        this.initName = initName;
        return this;
    }

    public String getFullName() {
        return fullName;
    }

    public Guardian setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public Guardian setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Guardian setAddress(String address) {
        this.address = address;
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
        this.contactNo = contactNo;
        return this;
    }

    public static Guardian toGuardian(List<MultipartFormData> multipartFormData) {
        var guardian = new Guardian();
        for (var formData : multipartFormData) {
            switch (formData.getName()) {
                case "id" -> guardian.setId(Long.parseLong(new String(formData.getData())));
                case "nic" -> guardian.setNic(new String(formData.getData()));
                case "initName" -> guardian.setInitName(new String(formData.getData()));
                case "fullName" -> guardian.setFullName(new String(formData.getData()));
                case "gender" -> guardian.setGender(Gender.valueOf(new String(formData.getData())));
                case "address" -> guardian.setAddress(new String(formData.getData()));
                case "email" -> guardian.setEmail(new String(formData.getData()));
                case "contactNo" -> guardian.setContactNo(new String(formData.getData()));
                default -> throw new IllegalStateException("Unexpected value: " + formData.getName());
            }
        }
        return guardian;
    }
}
