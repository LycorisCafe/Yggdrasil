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

import io.github.lycoriscafe.yggdrasil.authentication.Authentication;
import io.github.lycoriscafe.yggdrasil.authentication.AuthenticationService;
import io.github.lycoriscafe.yggdrasil.authentication.Role;
import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;

import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.*;

public class AdminService {
    public enum Columns {
        id,
        name,
        accessLevel,
        disabled
    }

    public static Response<Admin> getAdmins(Columns[] searchBy,
                                            String[] searchByValues,
                                            boolean[] isCaseSensitive,
                                            Columns[] orderBy,
                                            Boolean isAscending,
                                            Long resultsFrom,
                                            Long resultsOffset) {
        if (resultsFrom == null || resultsFrom < 0) resultsFrom = 0L;
        if (resultsOffset == null || resultsOffset < 0) resultsOffset = YggdrasilConfig.getDefaultResultsOffset();
        if (resultsFrom > resultsOffset) return new Response<Admin>().setError("Invalid boundaries");

        StringBuilder query = new StringBuilder("SELECT * FROM admin");
        if (searchBy != null) {
            if (searchBy.length != searchByValues.length) new Response<Admin>().setError("searchBy != searchByValues (length)");
            if (isCaseSensitive != null && searchBy.length != isCaseSensitive.length) {
                return new Response<Admin>().setError("searchBy != isCaseSensitive (length)");
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

            try (var resultSet = statement.executeQuery()) {
                connection.commit();
                List<Admin> admins = new ArrayList<>();
                while (resultSet.next()) {
                    String[] accessLevelsSet = resultSet.getString("accessLevel").split(",", 0);
                    Set<AccessLevel> accessLevels = new HashSet<>();
                    for (String accessLevel : accessLevelsSet) {
                        accessLevels.add(AccessLevel.valueOf(accessLevel));
                    }
                    admins.add(new Admin(resultSet.getString("name"), accessLevels)
                            .setId(Long.parseLong(resultSet.getString("id")))
                            .setAccessLevel(accessLevels)
                            .setDisabled(resultSet.getBoolean("disabled")));
                }

                return new Response<Admin>()
                        .setSuccess(true)
                        .setGenerableResults(Long.parseLong(resultSet.getString("generableValues")))
                        .setResultsFrom(resultsFrom)
                        .setResultsOffset(resultsOffset)
                        .setData(admins);
            }
        } catch (Exception e) {
            return new Response<Admin>().setError(e.getMessage());
        }
    }

    public static Response<Admin> getAdminById(Long id) {
        try {
            return getAdmins(new Columns[]{Columns.id}, new String[]{Long.toUnsignedString(id)}, null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<Admin>().setError(e.getMessage());
        }
    }

    public static Response<Admin> getAllAdmins(Long resultsFrom,
                                               Long resultsOffset) {
        try {
            return getAdmins(null, null, null, null, null, resultsFrom, resultsOffset);
        } catch (Exception e) {
            return new Response<Admin>().setError(e.getMessage());
        }
    }

    public static Response<Admin> createAdmin(Admin admin) {
        Objects.requireNonNull(admin);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO admin (id, name, accessLevel, disabled) " +
                             "VALUES (?, ?, ?, COALESCE(?, DEFAULT(disabled)))",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, admin.getId() == null ? null : Long.toUnsignedString(admin.getId()));
            statement.setString(2, admin.getName());
            List<AccessLevel> accessLevelList = admin.getAccessLevel().stream().toList();
            StringBuilder accessLevels = new StringBuilder();
            for (int i = 0; i < accessLevelList.size(); i++) {
                if (i > 0) accessLevels.append(",");
                accessLevels.append(accessLevelList.get(i).toString());
            }
            statement.setString(3, accessLevels.toString());
            statement.setString(4, admin.getDisabled().toString());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Admin>().setError("Internal server error");
            }

            try (var resultSet = statement.getGeneratedKeys()) {
                if (!resultSet.next()) {
                    connection.rollback();
                    return new Response<Admin>().setError("Internal server error");
                }
                if (AuthenticationService.updatePassword(
                        AuthenticationService.createAuthentication(
                                new Authentication()
                                        .setRole(Role.ADMIN)
                                        .setUserId(Long.parseLong(resultSet.getString(1)))
                                        .setPassword("A" + resultSet.getString(1)))) == null) {
                    connection.rollback();
                    return new Response<Admin>().setError("Internal server error");
                }
                connection.commit();
                return getAdminById(Long.parseLong(resultSet.getString(1)));
            }
        } catch (Exception e) {
            return new Response<Admin>().setError(e.getMessage());
        }
    }

    public static Response<Admin> updateAdminById(Admin admin) {
        Objects.requireNonNull(admin);

        var oldAdmin = getAdminById(admin.getId());
        if (oldAdmin.getError() != null) return oldAdmin;
        if (admin.getName() == null) admin.setName(oldAdmin.getData().getFirst().getName());
        if (admin.getAccessLevel() == null) admin.setAccessLevel(oldAdmin.getData().getFirst().getAccessLevel());
        if (admin.getDisabled() == null) admin.setDisabled(oldAdmin.getData().getFirst().getDisabled());

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("UPDATE admin SET name = ?, accessLevel = ?, disabled = ? WHERE id = ?")) {
            statement.setString(1, admin.getName());
            List<AccessLevel> accessLevelList = admin.getAccessLevel().stream().toList();
            StringBuilder accessLevels = new StringBuilder();
            for (int i = 0; i < accessLevelList.size(); i++) {
                if (i > 0) accessLevels.append(",");
                accessLevels.append(accessLevelList.get(i).toString());
            }
            statement.setString(2, accessLevels.toString());
            statement.setString(3, admin.getDisabled().toString());
            statement.setString(4, Long.toUnsignedString(admin.getId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Admin>().setError("Internal server error");
            }
            connection.commit();
            return getAdminById(admin.getId());
        } catch (Exception e) {
            return new Response<Admin>().setError(e.getMessage());
        }
    }

    public static Response<Admin> deleteAdminById(Long id) {
        Objects.requireNonNull(id);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM admin WHERE id = ?")) {
            statement.setString(1, Long.toUnsignedString(id));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Admin>().setError("Internal server error");
            }
            if (!AuthenticationService.deleteAuthentication(Role.ADMIN, id)) {
                connection.rollback();
                return new Response<Admin>().setError("Internal server error");
            }
            connection.commit();
            return new Response<Admin>().setSuccess(true);
        } catch (Exception e) {
            return new Response<Admin>().setError(e.getMessage());
        }
    }

    public static Response<Admin> resetPassword(Long id,
                                                String oldPassword,
                                                String newPassword) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(oldPassword);
        Objects.requireNonNull(newPassword);
        if (newPassword.length() < 8 || newPassword.length() > 50) return new Response<Admin>().setError("Password length must between 8 and 50");
        try {
            var auth = AuthenticationService.getAuthentication(Role.ADMIN, id);
            if (auth == null) return new Response<Admin>().setError("Invalid ID");
            if (auth.getPassword().equals(AuthenticationService.encryptData(oldPassword.getBytes(StandardCharsets.UTF_8)))) {
                auth.setPassword(newPassword);
                if (AuthenticationService.updatePassword(auth) == null) return new Response<Admin>().setError("Internal server error");
                return new Response<Admin>().setSuccess(true);
            }
            return new Response<Admin>().setError("Old password doesn't match");
        } catch (Exception e) {
            return new Response<Admin>().setError(e.getMessage());
        }
    }
}
