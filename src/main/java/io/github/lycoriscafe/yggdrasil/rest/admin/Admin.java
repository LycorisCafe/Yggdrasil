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

package io.github.lycoriscafe.yggdrasil.rest.admin;

import io.github.lycoriscafe.yggdrasil.commons.Entity;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Set;

public class Admin implements Entity {
    private BigInteger id;
    private String name;
    private Set<AccessLevel> accessLevel;
    private Boolean disabled;

    public Admin() {}

    public Admin(String name,
                 Set<AccessLevel> accessLevel) {
        this.name = Objects.requireNonNull(name);
        this.accessLevel = Objects.requireNonNull(accessLevel);
    }

    public BigInteger getId() {
        return id;
    }

    public Admin setId(BigInteger id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Admin setName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    public Set<AccessLevel> getAccessLevel() {
        return accessLevel;
    }

    public Admin setAccessLevel(Set<AccessLevel> accessLevel) {
        Objects.requireNonNull(accessLevel);
        if (accessLevel.isEmpty()) throw new NullPointerException("empty accessLevel set");
        this.accessLevel = accessLevel;
        return this;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public Admin setDisabled(Boolean disabled) {
        this.disabled = disabled;
        return this;
    }
}
