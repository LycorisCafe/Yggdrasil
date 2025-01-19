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
import io.github.lycoriscafe.yggdrasil.configuration.commons.CommonService;
import io.github.lycoriscafe.yggdrasil.configuration.commons.EntityColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.*;

public class AdminService {
    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    public enum Columns implements EntityColumn {
        id,
        name,
        accessLevel,
        disabled
    }

    public static Response<Admin> getAdmins(List<Columns> searchBy,
                                            List<String> searchByValues,
                                            List<Boolean> isCaseSensitive,
                                            List<Columns> orderBy,
                                            Boolean isAscending,
                                            Long resultsFrom,
                                            Long resultsOffset) {
        try {
            var results = CommonService.get(new CommonService.SearchQueryBuilder<Admin, Columns>(Admin.class)
                    .setSearchBy(searchBy).setSearchByValues(searchByValues).setIsCaseSensitive(isCaseSensitive).setOrderBy(orderBy)
                    .setAscending(isAscending).setResultsFrom(resultsFrom).setResultsOffset(resultsOffset));
            if (results.getResponse() != null) return results.getResponse();

            List<Admin> admins = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    String[] accessLevelsSet = resultSet.getString("accessLevel").split(",", 0);
                    Set<AccessLevel> accessLevels = new HashSet<>();
                    for (String accessLevel : accessLevelsSet) {
                        accessLevels.add(AccessLevel.valueOf(accessLevel));
                    }
                    admins.add(new Admin(resultSet.getString("name"), accessLevels)
                            .setId(Long.parseLong(resultSet.getString("id")))
                            .setDisabled(resultSet.getBoolean("disabled")));
                }
            }

            return new Response<Admin>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
                    .setData(admins);
        } catch (Exception e) {
            return new Response<Admin>().setError(e.getMessage());
        }
    }

    public static Response<Admin> getAdminById(Long id) {
        try {
            return getAdmins(List.of(Columns.id), List.of(Long.toUnsignedString(id)),
                    null, null, null, null, 1L);
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
        if (admin.getName() == null || admin.getName().isEmpty()) {
            return new Response<Admin>().setError("name is null/empty");
        }
        if (admin.getAccessLevel() == null || admin.getAccessLevel().isEmpty()) {
            return new Response<Admin>().setError("accessLevel is null/empty");
        }

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
            statement.setString(4, admin.getDisabled() == null ? null : admin.getDisabled().toString());
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
                                new Authentication(Role.ADMIN,
                                        Long.parseLong(resultSet.getString(1)),
                                        "A" + resultSet.getString(1)))) == null) {
                    connection.rollback();
                    return new Response<Admin>().setError("Internal server error");
                }
                connection.commit();
                return getAdminById(Long.parseLong(resultSet.getString(1)));
            }
        } catch (Exception e) {
            log.error("e: ", e);
            return new Response<Admin>().setError(e.getMessage());
        }
    }

    public static Response<Admin> updateAdmin(Admin admin) {
        Objects.requireNonNull(admin);
        if (admin.getId() == null) return new Response<Admin>().setError("id is null");

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
        var result = CommonService.delete(new CommonService.SearchQueryBuilder<Admin, Columns>(Admin.class).setSearchBy(List.of(Columns.id))
                .setSearchByValues(List.of(Long.toUnsignedString(id))));
        if (result.isSuccess()) AuthenticationService.deleteAuthentication(Role.ADMIN, id);
        return result;
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
