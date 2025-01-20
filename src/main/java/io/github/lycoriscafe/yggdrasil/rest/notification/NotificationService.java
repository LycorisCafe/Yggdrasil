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

import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.commons.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationService implements EntityService<Notification> {
    public enum Columns implements EntityColumn<Notification> {
        id,
        createTimestamp,
        updateTimestamp,
        scope,
        message,
        draft
    }

    public static Response<Notification> select(SearchQueryBuilder<Notification, Columns, NotificationService> searchQueryBuilder) {
        try {
            var results = CommonService.select(searchQueryBuilder);
            if (results.getResponse() != null) return results.getResponse();

            List<Notification> notifications = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    notifications.add(new Notification(
                            Scope.valueOf(resultSet.getString("scope")),
                            resultSet.getString("message")
                    ).setId(resultSet.getBigDecimal("id"))
                            .setCreateTimestamp(resultSet.getString("createTimestamp") == null ?
                                    null : LocalDateTime.parse(resultSet.getString("createTimestamp"), Utils.getDateTimeFormatter()))
                            .setUpdateTimestamp(resultSet.getString("updateTimestamp") == null ?
                                    null : LocalDateTime.parse(resultSet.getString("updateTimestamp"), Utils.getDateTimeFormatter()))
                            .setDraft(resultSet.getBoolean("draft")));
                }
            }

            return new Response<Notification>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
                    .setData(notifications);
        } catch (Exception e) {
            return new Response<Notification>().setError(e.getMessage());
        }
    }

    public static Response<Notification> delete(SearchQueryBuilder<Notification, Columns, NotificationService> searchQueryBuilder) {
        return CommonService.delete(searchQueryBuilder);
    }

    public static Response<Notification> insert(UpdateQueryBuilder<Notification, Columns, NotificationService> updateQueryBuilder) {
        return CommonService.insert(updateQueryBuilder);
    }

    public static Response<Notification> update(UpdateQueryBuilder<Notification, Columns, NotificationService> updateQueryBuilder) {
        return CommonService.update(updateQueryBuilder);
    }
}
