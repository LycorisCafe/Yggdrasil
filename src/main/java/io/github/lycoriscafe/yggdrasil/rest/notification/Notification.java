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

import io.github.lycoriscafe.nexus.http.core.headers.content.MultipartFormData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Notification {
    private Long id;
    private LocalDateTime createTimestamp;
    private LocalDateTime updateTimestamp;
    private Scope scope;
    private String message;
    private Boolean draft;

    private Notification() {}

    public Notification(Scope scope,
                        String message) {
        this.scope = Objects.requireNonNull(scope);
        this.message = Objects.requireNonNull(message);
    }

    public Long getId() {
        return id;
    }

    public Notification setId(Long id) {
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
        this.scope = scope;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Notification setMessage(String message) {
        this.message = message;
        return this;
    }

    public Boolean getDraft() {
        return draft;
    }

    public Notification setDraft(Boolean draft) {
        this.draft = draft;
        return this;
    }

    public static Notification toNotification(List<MultipartFormData> multipartFormData) {
        var notification = new Notification();
        for (var formData : multipartFormData) {
            switch (formData.getName()) {
                case "id" -> notification.setId(Long.parseLong(new String(formData.getData())));
                case "createTimestamp" -> notification.setCreateTimestamp(LocalDateTime.parse(new String(formData.getData())));
                case "updateTimestamp" -> notification.setUpdateTimestamp(LocalDateTime.parse(new String(formData.getData())));
                case "scope" -> notification.setScope(Scope.valueOf(new String(formData.getData())));
                case "message" -> notification.setMessage(new String(formData.getData()));
                case "draft" -> notification.setDraft(Boolean.parseBoolean(new String(formData.getData())));
                default -> throw new IllegalStateException("Unexpected value: " + formData.getName());
            }
        }
        return notification;
    }
}
