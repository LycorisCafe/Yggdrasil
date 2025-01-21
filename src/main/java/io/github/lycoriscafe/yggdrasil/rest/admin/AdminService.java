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

import io.github.lycoriscafe.nexus.http.core.headers.content.UrlEncodedData;
import io.github.lycoriscafe.yggdrasil.authentication.Authentication;
import io.github.lycoriscafe.yggdrasil.authentication.AuthenticationService;
import io.github.lycoriscafe.yggdrasil.authentication.Role;
import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;
import io.github.lycoriscafe.yggdrasil.configuration.commons.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminService implements EntityService<Admin> {
    public enum Columns implements EntityColumn<Admin> {
        id,
        name,
        accessLevel,
        disabled
    }

    public static Response<Admin> select(SearchQueryBuilder<Admin, Columns, AdminService> searchQueryBuilder) {
        try {
            var results = CommonService.select(searchQueryBuilder);
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
                            .setId(resultSet.getBigDecimal("id"))
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

    public static Response<Admin> delete(SearchQueryBuilder<Admin, Columns, AdminService> searchQueryBuilder) {
        var result = CommonService.delete(searchQueryBuilder);
        if (result.isSuccess()) AuthenticationService.deleteAuthentication(Role.ADMIN, result.getData().getFirst().getId());
        return result;
    }

    public static Response<Admin> insert(UpdateQueryBuilder<Admin, Columns, AdminService> updateQueryBuilder) {
        var result = CommonService.insert(updateQueryBuilder);
        if (result.isSuccess()) {
            try {
                AuthenticationService.updatePassword(
                        AuthenticationService.createAuthentication(
                                new Authentication(Role.ADMIN,
                                        result.getData().getFirst().getId(),
                                        "A" + result.getData().getFirst().getId().toPlainString())));
            } catch (SQLException | NoSuchAlgorithmException e) {
                delete(new SearchQueryBuilder<>(Admin.class, Columns.class, AdminService.class)
                        .setSearchBy(List.of(Columns.id))
                        .setSearchByValues(List.of(result.getData().getFirst().getId().toPlainString())));
                return new Response<Admin>().setError(e.getMessage());
            }
        }
        return result;
    }

    public static Response<Admin> update(UpdateQueryBuilder<Admin, Columns, AdminService> updateQueryBuilder) {
        return CommonService.update(updateQueryBuilder);
    }

    public static Response<Admin> resetPassword(UrlEncodedData encodedData) {
        BigDecimal id = null;
        try {
            id = new BigDecimal(encodedData.get("id"));
        } catch (Exception e) {
            new Response<Admin>().setError("Invalid id");
        }

        String oldPassword = encodedData.get("oldPassword");
        String newPassword = encodedData.get("newPassword");
        if (oldPassword == null) return new Response<Admin>().setError("oldPassword cannot be null");
        if (newPassword == null) return new Response<Admin>().setError("newPassword cannot be null");
        if (newPassword.length() < YggdrasilConfig.getDefaultUserPasswordBoundary()[0] ||
                newPassword.length() > YggdrasilConfig.getDefaultUserPasswordBoundary()[1]) {
            return new Response<Admin>().setError("Password length must between 8 and 50");
        }

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
