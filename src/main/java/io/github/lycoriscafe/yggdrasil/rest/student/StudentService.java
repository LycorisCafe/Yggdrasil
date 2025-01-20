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

package io.github.lycoriscafe.yggdrasil.rest.student;

import io.github.lycoriscafe.yggdrasil.authentication.Authentication;
import io.github.lycoriscafe.yggdrasil.authentication.AuthenticationService;
import io.github.lycoriscafe.yggdrasil.authentication.Role;
import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.commons.*;
import io.github.lycoriscafe.yggdrasil.rest.Gender;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StudentService implements EntityService<Student> {
    public enum Columns implements EntityColumn<Student> {
        id,
        guardianId,
        classroomId,
        initName,
        fullName,
        gender,
        dateOfBirth,
        nic,
        address,
        regYear,
        contactNo,
        email,
        disabled
    }

    public static Response<Student> select(SearchQueryBuilder<Student, Columns, StudentService> searchQueryBuilder) {
        try {
            var results = CommonService.select(searchQueryBuilder);
            if (results.getResponse() != null) return results.getResponse();

            List<Student> students = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    students.add(new Student(
                            Long.parseLong(resultSet.getString("guardianId")),
                            resultSet.getString("initName"),
                            resultSet.getString("fullName"),
                            Gender.valueOf(resultSet.getString("gender")),
                            LocalDate.parse(resultSet.getString("dateOfBirth"), Utils.getDateFormatter()),
                            resultSet.getString("address"),
                            Year.parse(resultSet.getString("regYear"))
                    ).setId(Long.parseLong(resultSet.getString("id")))
                            .setClassroomId(resultSet.getString("classroomId") == null ?
                                    null : Long.parseLong(resultSet.getString("classroomId")))
                            .setNic(resultSet.getString("nic"))
                            .setContactNo(resultSet.getString("contactNo"))
                            .setEmail(resultSet.getString("email"))
                            .setDisabled(resultSet.getBoolean("disabled")));
                }
            }

            return new Response<Student>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
                    .setData(students);
        } catch (Exception e) {
            return new Response<Student>().setError(e.getMessage());
        }
    }

    public static Response<Student> delete(SearchQueryBuilder<Student, Columns, StudentService> searchQueryBuilder) {
        var result = CommonService.delete(searchQueryBuilder);
        if (result.isSuccess()) AuthenticationService.deleteAuthentication(Role.STUDENT, result.getData().getFirst().getId());
        return result;
    }

    public static Response<Student> insert(UpdateQueryBuilder<Student, Columns, StudentService> updateQueryBuilder) {
        var result = CommonService.insert(updateQueryBuilder);
        if (result.isSuccess()) {
            try {
                AuthenticationService.updatePassword(
                        AuthenticationService.createAuthentication(
                                new Authentication(Role.STUDENT,
                                        result.getData().getFirst().getId(),
                                        "S" + Long.toUnsignedString(result.getData().getFirst().getId()))));
            } catch (SQLException | NoSuchAlgorithmException e) {
                delete(new SearchQueryBuilder<>(Student.class, Columns.class, StudentService.class)
                        .setSearchBy(List.of(Columns.id))
                        .setSearchByValues(List.of(Long.toUnsignedString(result.getData().getFirst().getId()))));
                return new Response<Student>().setError(e.getMessage());
            }
        }
        return result;
    }

    public static Response<Student> update(UpdateQueryBuilder<Student, Columns, StudentService> updateQueryBuilder) {
        return CommonService.update(updateQueryBuilder);
    }

    public static Response<Student> resetPassword(Long id,
                                                  String oldPassword,
                                                  String newPassword) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(oldPassword);
        Objects.requireNonNull(newPassword);
        if (newPassword.length() < 8 || newPassword.length() > 50) return new Response<Student>().setError("Password length must between 8 and 50");
        try {
            var auth = AuthenticationService.getAuthentication(Role.STUDENT, id);
            if (auth == null) return new Response<Student>().setError("Invalid ID");
            if (auth.getPassword().equals(AuthenticationService.encryptData(oldPassword.getBytes(StandardCharsets.UTF_8)))) {
                auth.setPassword(newPassword);
                if (AuthenticationService.updatePassword(auth) == null) return new Response<Student>().setError("Internal server error");
                return new Response<Student>().setSuccess(true);
            }
            return new Response<Student>().setError("Old password doesn't match");
        } catch (Exception e) {
            return new Response<Student>().setError(e.getMessage());
        }
    }
}
