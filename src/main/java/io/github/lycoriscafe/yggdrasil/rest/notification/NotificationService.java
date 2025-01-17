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
import io.github.lycoriscafe.yggdrasil.configuration.database.CommonCRUD;
import io.github.lycoriscafe.yggdrasil.configuration.database.EntityColumn;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationService {
    public enum Columns implements EntityColumn {
        id,
        createTimestamp,
        updateTimestamp,
        scope,
        message,
        draft
    }

    public static Response<Notification> getNotifications(Columns[] searchBy,
                                                          String[] searchByValues,
                                                          boolean[] isCaseSensitive,
                                                          Columns[] orderBy,
                                                          Boolean isAscending,
                                                          Long resultsFrom,
                                                          Long resultsOffset) {
        try {
            var results = CommonCRUD.get(Notification.class, searchBy, searchByValues, isCaseSensitive, orderBy, isAscending, resultsFrom, resultsOffset);
            if (results.getResponse() != null) return results.getResponse();

            var resultSet = results.getResultSet();
            Long generableValues = null;
            List<Notification> notifications = new ArrayList<>();
            while (resultSet.next()) {
                if (generableValues == null) generableValues = Long.parseLong(resultSet.getString("generableValues"));
                notifications.add(new Notification(
                        Scope.valueOf(resultSet.getString("scope")),
                        resultSet.getString("message")
                ).setId(Long.parseLong(resultSet.getString("id")))
                        .setCreateTimestamp(resultSet.getString("createTimestamp") == null ?
                                null : LocalDateTime.parse(resultSet.getString("createTimestamp"), Utils.getDateTimeFormatter()))
                        .setUpdateTimestamp(resultSet.getString("updateTimestamp") == null ?
                                null : LocalDateTime.parse(resultSet.getString("updateTimestamp"), Utils.getDateTimeFormatter()))
                        .setDraft(resultSet.getBoolean("draft")));
            }

            return new Response<Notification>()
                    .setSuccess(true)
                    .setGenerableResults(generableValues)
                    .setResultsFrom(resultsFrom)
                    .setResultsOffset(resultsOffset)
                    .setData(notifications);
        } catch (Exception e) {
            return new Response<Notification>().setError(e.getMessage());
        }
    }

    public static Response<Notification> getNotificationById(Long id) {
        try {
            return getNotifications(new Columns[]{Columns.id}, new String[]{Long.toUnsignedString(id)},
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<Notification>().setError(e.getMessage());
        }
    }

    public static Response<Notification> createNotification(Notification notification) throws SQLException {
        Objects.requireNonNull(notification);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO notification (id, scope, message, draft) " +
                     "VALUES (?, ?, ?, COALESCE(?, DEFAULT(draft)))", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, notification.getId() == null ? null : Long.toUnsignedString(notification.getId()));
            statement.setString(2, notification.getScope().toString());
            statement.setString(3, notification.getMessage());
            statement.setBoolean(4, notification.getDraft());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Notification>().setError("Internal server error");
            }
            try (var resultSet = statement.getGeneratedKeys()) {
                if (!resultSet.next()) {
                    connection.rollback();
                    return new Response<Notification>().setError("Internal server error");
                }
                connection.commit();
                return getNotificationById(Long.parseLong(resultSet.getString(1)));
            }
        } catch (Exception e) {
            return new Response<Notification>().setError(e.getMessage());
        }
    }

    public static Response<Notification> updateNotification(Notification notification) {
        Objects.requireNonNull(notification);

        var oldNotification = getNotificationById(notification.getId());
        if (oldNotification.getError() != null) return oldNotification;
        var data = oldNotification.getData().getFirst();
        if (notification.getScope() == null) notification.setScope(data.getScope());
        if (notification.getMessage() == null) notification.setMessage(data.getMessage());
        if (notification.getDraft() == null) notification.setDraft(data.getDraft());

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("UPDATE notification SET scope = ?, message = ?, draft = ? WHERE id = ?")) {
            statement.setString(1, notification.getScope().toString());
            statement.setString(2, notification.getMessage());
            statement.setBoolean(3, notification.getDraft());
            statement.setString(4, Long.toUnsignedString(notification.getId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Notification>().setError("Internal server error");
            }
            connection.commit();
            return getNotificationById(notification.getId());
        } catch (Exception e) {
            return new Response<Notification>().setError(e.getMessage());
        }
    }

    public static Response<Notification> deleteNotificationById(Long id) {
        Objects.requireNonNull(id);
        return CommonCRUD.delete(Notification.class, new Columns[]{Columns.id}, new String[]{Long.toUnsignedString(id)}, null);
    }
}
