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

import java.math.BigDecimal;
import java.util.Objects;

public class Authentication {
    private Role role;
    private BigDecimal userId;
    private String password;
    private String accessToken;
    private Long expires;
    private String refreshToken;

    public Authentication(Role role,
                          BigDecimal userId,
                          String password) {
        this.role = Objects.requireNonNull(role);
        this.userId = Objects.requireNonNull(userId);
        this.password = Objects.requireNonNull(password);
    }

    public Role getRole() {
        return role;
    }

    public Authentication setRole(Role role) {
        this.role = Objects.requireNonNull(role);
        return this;
    }

    public BigDecimal getUserId() {
        return userId;
    }

    public Authentication setUserId(BigDecimal userId) {
        this.userId = Objects.requireNonNull(userId);
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Authentication setPassword(String password) {
        this.password = Objects.requireNonNull(password);
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Authentication setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public Long getExpires() {
        return expires;
    }

    public Authentication setExpires(Long expires) {
        this.expires = expires;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Authentication setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }
}
