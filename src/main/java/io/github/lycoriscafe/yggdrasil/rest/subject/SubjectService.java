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

package io.github.lycoriscafe.yggdrasil.rest.subject;

import io.github.lycoriscafe.yggdrasil.commons.EntityService;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SubjectService implements EntityService<Subject> {
    public static void toDatabase(PreparedStatement statement,
                                  Subject instance,
                                  boolean isUpdate) throws SQLException {
        int nextParamIndex = 1;
        if (!isUpdate) statement.setString(nextParamIndex++, instance.getId() == null ? null : instance.getId().toString());
        statement.setInt(nextParamIndex++, instance.getGrade());
        statement.setString(nextParamIndex++, instance.getShortName());
        statement.setString(nextParamIndex++, instance.getLongName());
        statement.setString(nextParamIndex++, instance.getTeacherId() == null ? null : instance.getTeacherId().toString());
        if (isUpdate) statement.setString(nextParamIndex, instance.getId() == null ? null : instance.getId().toString());
    }

    public static void fromDatabase(ResultSet resultSet,
                                    Subject instance) throws SQLException {
        instance.setId(new BigInteger(resultSet.getString("id")))
                .setGrade(resultSet.getInt("grade"))
                .setShortName(resultSet.getString("shortName"))
                .setLongName(resultSet.getString("longName"))
                .setTeacherId(resultSet.getString("teacherId") == null ?
                        null : new BigInteger(resultSet.getString("teacherId")));
    }
}
