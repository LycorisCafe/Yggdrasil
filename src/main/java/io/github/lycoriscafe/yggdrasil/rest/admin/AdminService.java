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

package io.github.lycoriscafe.yggdrasil.rest.admin;

import io.github.lycoriscafe.yggdrasil.commons.EntityService;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminService implements EntityService<Admin> {
    public static void toDatabase(PreparedStatement statement,
                                  Admin instance,
                                  boolean isUpdate) throws SQLException {
        int nextParamIndex = 1;
        if (!isUpdate) statement.setString(nextParamIndex++, instance.getId().toString());
        statement.setString(nextParamIndex++, instance.getName());
        StringBuilder accessLevels = new StringBuilder();
        List<AccessLevel> accessLevelList = instance.getAccessLevel().stream().toList();
        for (int i = 0; i < accessLevelList.size(); i++) {
            if (i > 0) accessLevels.append(",");
            accessLevels.append(accessLevelList.get(i).toString());
        }
        statement.setString(nextParamIndex++, accessLevels.toString());
        statement.setBoolean(nextParamIndex++, instance.getDisabled() != null && instance.getDisabled());
        if (isUpdate) statement.setString(nextParamIndex, instance.getId().toString());
    }

    public static void fromDatabase(ResultSet resultSet,
                                    Admin instance) throws SQLException {
        instance.setId(new BigInteger(resultSet.getString("id")))
                .setName(resultSet.getString("name"));
        Set<AccessLevel> accessLevels = new HashSet<>();
        Arrays.stream(resultSet.getString("accessLevels").split(",", 0)).forEach(e -> accessLevels.add(AccessLevel.valueOf(e)));
        instance.setAccessLevel(accessLevels)
                .setDisabled(resultSet.getBoolean("disabled"));
    }
}
