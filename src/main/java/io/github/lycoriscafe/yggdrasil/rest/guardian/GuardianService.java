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

package io.github.lycoriscafe.yggdrasil.rest.guardian;

import io.github.lycoriscafe.yggdrasil.commons.EntityService;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.rest.Gender;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class GuardianService implements EntityService<Guardian> {
    public static void toDatabase(PreparedStatement statement,
                                  Guardian instance,
                                  boolean isUpdate) throws SQLException {
        int nextParamIndex = 1;
        if (!isUpdate) statement.setString(nextParamIndex++, instance.getId().toString());
        statement.setString(nextParamIndex++, instance.getNic());
        statement.setString(nextParamIndex++, instance.getInitName());
        statement.setString(nextParamIndex++, instance.getFullName());
        statement.setString(nextParamIndex++, instance.getGender().toString());
        statement.setString(nextParamIndex++, instance.getDateOfBirth().format(Utils.getDateFormatter()));
        statement.setString(nextParamIndex++, instance.getAddress());
        statement.setString(nextParamIndex++, instance.getEmail());
        statement.setString(nextParamIndex++, instance.getContactNo());
        if (isUpdate) statement.setString(nextParamIndex, instance.getId().toString());
    }

    public static void fromDatabase(ResultSet resultSet,
                                    Guardian instance) throws SQLException {
        instance.setId(new BigInteger(resultSet.getString("id")))
                .setNic(resultSet.getString("nic"))
                .setInitName(resultSet.getString("init_name"))
                .setFullName(resultSet.getString("full_name"))
                .setGender(Gender.valueOf(resultSet.getString("gender")))
                .setDateOfBirth(LocalDate.parse(resultSet.getString("dateOfBirth"), Utils.getDateFormatter()))
                .setAddress(resultSet.getString("address"))
                .setEmail(resultSet.getString("email"))
                .setContactNo(resultSet.getString("contactNo"));
    }
}
