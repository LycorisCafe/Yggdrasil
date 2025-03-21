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

package io.github.lycoriscafe.yggdrasil.rest.notification;

import io.github.lycoriscafe.yggdrasil.commons.Entity;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Objects;

public final class Notification implements Entity {
    private BigInteger id;
    private LocalDateTime createTimestamp;
    private LocalDateTime updateTimestamp;
    private Scope scope;
    private String message;
    private Boolean draft;

    public Notification() {}

    @Override
    public BigInteger getId() {
        return id;
    }

    public Notification setId(BigInteger id) {
        this.id = id;
        return this;
    }

    public LocalDateTime getCreateTimestamp() {
        return createTimestamp;
    }

    public Notification setCreateTimestamp(LocalDateTime createTimestamp) {
        this.createTimestamp = createTimestamp;
        return this;
    }

    public LocalDateTime getUpdateTimestamp() {
        return updateTimestamp;
    }

    public Notification setUpdateTimestamp(LocalDateTime updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
        return this;
    }

    public Scope getScope() {
        return scope;
    }

    public Notification setScope(Scope scope) {
        this.scope = Objects.requireNonNull(scope);
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Notification setMessage(String message) {
        this.message = Objects.requireNonNull(message);
        return this;
    }

    public Boolean getDraft() {
        return draft;
    }

    public Notification setDraft(Boolean draft) {
        this.draft = draft;
        return this;
    }
}
