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

package io.github.lycoriscafe.yggdrasil.rest.teacher;

import io.github.lycoriscafe.yggdrasil.authentication.Authentication;
import io.github.lycoriscafe.yggdrasil.authentication.AuthenticationService;
import io.github.lycoriscafe.yggdrasil.authentication.Role;
import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;
import io.github.lycoriscafe.yggdrasil.configuration.commons.*;
import io.github.lycoriscafe.yggdrasil.rest.Gender;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TeacherService implements EntityService<Teacher> {
    public enum Columns implements EntityColumn<Teacher> {
        id,
        nic,
        initName,
        fullName,
        gender,
        dateOfBirth,
        address,
        email,
        contactNo,
        disabled
    }

    public static Response<Teacher> select(SearchQueryBuilder<Teacher, Columns, TeacherService> searchQueryBuilder) {
        try {
            var results = CommonService.select(searchQueryBuilder);
            if (results.getResponse() != null) return results.getResponse();

            List<Teacher> teachers = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    teachers.add(new Teacher(
                            resultSet.getString("nic"),
                            resultSet.getString("initName"),
                            resultSet.getString("fullName"),
                            Gender.valueOf(resultSet.getString("gender")),
                            LocalDate.parse(resultSet.getString("dateOfBirth"), Utils.getDateFormatter()),
                            resultSet.getString("address"),
                            resultSet.getString("email"),
                            resultSet.getString("contactNo")
                    ).setId(resultSet.getBigDecimal("id"))
                            .setDisabled(resultSet.getBoolean("disabled")));
                }
            }

            return new Response<Teacher>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
                    .setData(teachers);
        } catch (Exception e) {
            return new Response<Teacher>().setError(e.getMessage());
        }
    }

    public static Response<Teacher> delete(SearchQueryBuilder<Teacher, Columns, TeacherService> searchQueryBuilder) {
        var result = CommonService.delete(searchQueryBuilder);
        if (result.isSuccess()) AuthenticationService.deleteAuthentication(Role.TEACHER, result.getData().getFirst().getId());
        return result;
    }

    public static Response<Teacher> insert(UpdateQueryBuilder<Teacher, Columns, TeacherService> updateQueryBuilder) {
        var result = CommonService.insert(updateQueryBuilder);
        if (result.isSuccess()) {
            try {
                AuthenticationService.updatePassword(
                        AuthenticationService.createAuthentication(
                                new Authentication(Role.TEACHER,
                                        result.getData().getFirst().getId(),
                                        "S" + result.getData().getFirst().getId().toPlainString())));
            } catch (SQLException | NoSuchAlgorithmException e) {
                delete(new SearchQueryBuilder<>(Teacher.class, Columns.class, TeacherService.class)
                        .setSearchBy(List.of(Columns.id))
                        .setSearchByValues(List.of(result.getData().getFirst().getId().toPlainString())));
                return new Response<Teacher>().setError(e.getMessage());
            }
        }
        return result;
    }

    public static Response<Teacher> update(UpdateQueryBuilder<Teacher, Columns, TeacherService> updateQueryBuilder) {
        return CommonService.update(updateQueryBuilder);
    }

    public static Response<Teacher> resetPassword(BigDecimal id,
                                                  String oldPassword,
                                                  String newPassword) {
        if (id == null) return new Response<Teacher>().setError("id cannot be null");
        if (oldPassword == null) return new Response<Teacher>().setError("oldPassword cannot be null");
        if (newPassword == null) return new Response<Teacher>().setError("newPassword cannot be null");
        if (newPassword.length() < YggdrasilConfig.getDefaultUserPasswordBoundary()[0] ||
                newPassword.length() > YggdrasilConfig.getDefaultUserPasswordBoundary()[1]) {
            return new Response<Teacher>().setError("Password length must between 8 and 50");
        }

        try {
            var auth = AuthenticationService.getAuthentication(Role.TEACHER, id);
            if (auth == null) return new Response<Teacher>().setError("Invalid ID");
            if (auth.getPassword().equals(AuthenticationService.encryptData(oldPassword.getBytes(StandardCharsets.UTF_8)))) {
                auth.setPassword(newPassword);
                if (AuthenticationService.updatePassword(auth) == null) return new Response<Teacher>().setError("Internal server error");
                return new Response<Teacher>().setSuccess(true);
            }
            return new Response<Teacher>().setError("Old password doesn't match");
        } catch (Exception e) {
            return new Response<Teacher>().setError(e.getMessage());
        }
    }
}
