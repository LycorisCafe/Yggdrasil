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
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationService {
    public enum Columns {
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
        if (resultsFrom == null || resultsFrom < 0) resultsFrom = 0L;
        if (resultsOffset == null || resultsOffset < 0) resultsOffset = YggdrasilConfig.getDefaultResultsOffset();
        if (resultsFrom > resultsOffset) return new Response<Notification>().setError("Invalid boundaries");

        StringBuilder query = new StringBuilder("SELECT * FROM notification");
        if (searchBy != null) {
            if (searchBy.length != searchByValues.length) return new Response<Notification>().setError("searchBy != searchByValues (length)");
            if (isCaseSensitive != null && searchBy.length != isCaseSensitive.length) {
                return new Response<Notification>().setError("searchBy != isCaseSensitive (length)");
            }
            query.append(" WHERE ");
            for (int i = 0; i < searchBy.length; i++) {
                if (i > 0) query.append(" AND ");
                query.append(searchBy[i]).append(" LIKE ");
                if (isCaseSensitive != null) query.append(isCaseSensitive[i] ? " BINARY " : "");
                query.append("?");
            }
        }
        if (orderBy != null) {
            query.append(" ORDER BY ");
            for (int i = 0; i < orderBy.length; i++) {
                if (i > 0) query.append(", ");
                query.append(orderBy[i]);
            }
        }
        if (isAscending != null) {
            query.append(isAscending ? " ASC" : " DESC");
        }
        query.replace(7, 8, "*, (" + query.toString().replace("*", "COUNT(id)") + ") AS generableValues");
        query.append(" LIMIT ").append(Long.toUnsignedString(resultsFrom)).append(", ").append(Long.toUnsignedString(resultsOffset));

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString())) {
            if (searchByValues != null) {
                for (int i = 0; i < searchByValues.length; i++) {
                    statement.setString(i + 1, searchByValues[i]);
                }
            }

            long generableValues;
            List<Notification> notifications = new ArrayList<>();
            try (var resultSet = statement.executeQuery()) {
                connection.commit();
                while (resultSet.next()) {
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
                generableValues = Long.parseLong(resultSet.getString("generableValues"));
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
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM notification WHERE id = ?")) {
            statement.setString(1, Long.toUnsignedString(id));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Notification>().setError("Internal server error");
            }
            connection.commit();
            return new Response<Notification>().setSuccess(true);
        } catch (Exception e) {
            return new Response<Notification>().setError(e.getMessage());
        }
    }
}
