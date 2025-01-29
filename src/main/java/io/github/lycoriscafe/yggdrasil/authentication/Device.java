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

package io.github.lycoriscafe.yggdrasil.authentication;

import java.math.BigInteger;
import java.util.Objects;

public class Device {
    private Role role;
    private BigInteger userId;
    private String name;
    private String accessToken;
    private Long expires;
    private String refreshToken;

    public Device(Role role,
                  BigInteger userId,
                  String name,
                  String accessToken,
                  Long expires,
                  String refreshToken) {
        this.role = Objects.requireNonNull(role);
        this.userId = Objects.requireNonNull(userId);
        this.name = Objects.requireNonNull(name);
        this.accessToken = Objects.requireNonNull(accessToken);
        this.expires = Objects.requireNonNull(expires);
        this.refreshToken = Objects.requireNonNull(refreshToken);
    }

    public Role getRole() {
        return role;
    }

    public Device setRole(Role role) {
        this.role = role;
        return this;
    }

    public BigInteger getUserId() {
        return userId;
    }

    public Device setUserId(BigInteger userId) {
        this.userId = userId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Device setName(String name) {
        this.name = name;
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Device setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public Long getExpires() {
        return expires;
    }

    public Device setExpires(Long expires) {
        this.expires = expires;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Device setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }
}
