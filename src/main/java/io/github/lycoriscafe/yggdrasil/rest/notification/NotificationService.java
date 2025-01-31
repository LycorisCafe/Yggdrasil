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

import io.github.lycoriscafe.yggdrasil.commons.EntityService;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public final class NotificationService implements EntityService<Notification> {
    public static void toDatabase(PreparedStatement statement,
                                  Notification instance,
                                  boolean isUpdate) throws SQLException {
        int nextParamIndex = 1;
        if (!isUpdate) statement.setString(nextParamIndex++, instance.getId() == null ? null : instance.getId().toString());
        statement.setString(nextParamIndex++, null);
        statement.setString(nextParamIndex++, null);
        statement.setString(nextParamIndex++, instance.getScope().toString());
        statement.setString(nextParamIndex++, instance.getMessage());
        statement.setBoolean(nextParamIndex++, instance.getDraft() != null && instance.getDraft());
        if (isUpdate) statement.setString(nextParamIndex, instance.getId() == null ? null : instance.getId().toString());
    }

    public static void fromDatabase(ResultSet resultSet,
                                    Notification instance) throws SQLException {
        instance.setId(new BigInteger(resultSet.getString("id")))
                .setCreateTimestamp(LocalDateTime.parse(resultSet.getString("createTimestamp"), Utils.getDateTimeFormatter()))
                .setUpdateTimestamp(LocalDateTime.parse(resultSet.getString("updateTimestamp"), Utils.getDateTimeFormatter()))
                .setScope(Scope.valueOf(resultSet.getString("scope")))
                .setMessage(resultSet.getString("message"))
                .setDraft(resultSet.getBoolean("draft"));
    }
}
