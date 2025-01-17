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
import io.github.lycoriscafe.yggdrasil.rest.Gender;

import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TeacherService {
    public enum Columns {
        id,
        nic,
        initName,
        fullName,
        gender,
        address,
        email,
        contactNo,
        disabled
    }

    public static Response<Teacher> getTeachers(Columns[] searchBy,
                                                String[] searchByValues,
                                                boolean[] isCaseSensitive,
                                                Columns[] orderBy,
                                                Boolean isAscending,
                                                Long resultsFrom,
                                                Long resultsOffset) {
        if (resultsFrom == null || resultsFrom < 0) resultsFrom = 0L;
        if (resultsOffset == null || resultsOffset < 0) resultsOffset = YggdrasilConfig.getDefaultResultsOffset();
        if (resultsFrom > resultsOffset) return new Response<Teacher>().setError("Invalid boundaries");

        StringBuilder query = new StringBuilder("SELECT * FROM teacher");
        if (searchBy != null) {
            if (searchBy.length != searchByValues.length) return new Response<Teacher>().setError("searchBy != searchByValues (length)");
            if (isCaseSensitive != null && searchBy.length != isCaseSensitive.length) {
                return new Response<Teacher>().setError("searchBy != isCaseSensitive (length)");
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
                List<Teacher> teachers = new ArrayList<>();
                while (resultSet.next()) {
                    teachers.add(new Teacher(
                            resultSet.getString("nic"),
                            resultSet.getString("initName"),
                            resultSet.getString("fullName"),
                            Gender.valueOf(resultSet.getString("gender")),
                            resultSet.getString("address"),
                            resultSet.getString("email"),
                            resultSet.getString("contactNo")
                    ).setId(Long.parseLong(resultSet.getString("id")))
                            .setDisabled(resultSet.getBoolean("disabled")));
                }

                return new Response<Teacher>()
                        .setSuccess(true)
                        .setGenerableResults(Long.parseLong(resultSet.getString("generableValues")))
                        .setResultsFrom(resultsFrom)
                        .setResultsOffset(resultsOffset)
                        .setData(teachers);
            }
        } catch (Exception e) {
            return new Response<Teacher>().setError(e.getMessage());
        }
    }

    public static Response<Teacher> getTeacherById(Long id) {
        try {
            return getTeachers(new Columns[]{Columns.id}, new String[]{Long.toUnsignedString(id)}, null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<Teacher>().setError(e.getMessage());
        }
    }

    public static Response<Teacher> createTeacher(Teacher teacher) {
        Objects.requireNonNull(teacher);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO teacher (id, nic, initName, fullName, gender, address, email, contactNo, disabled) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, COALESCE(?, DEFAULT(disabled)))", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, teacher.getId() == null ? null : Long.toUnsignedString(teacher.getId()));
            statement.setString(2, teacher.getNic());
            statement.setString(3, teacher.getInitName());
            statement.setString(4, teacher.getFullName());
            statement.setString(5, teacher.getGender().toString());
            statement.setString(6, teacher.getAddress());
            statement.setString(7, teacher.getEmail());
            statement.setString(8, teacher.getContactNo());
            statement.setBoolean(9, teacher.getDisabled());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Teacher>().setError("Internal server error");
            }
            try (var resultSet = statement.getGeneratedKeys()) {
                if (!resultSet.next()) {
                    connection.rollback();
                    return new Response<Teacher>().setError("Internal server error");
                }
                if (AuthenticationService.updatePassword(
                        AuthenticationService.createAuthentication(
                                new Authentication()
                                        .setRole(Role.TEACHER)
                                        .setUserId(Long.parseLong(resultSet.getString(1)))
                                        .setPassword("T" + resultSet.getString(1)))) == null) {
                    connection.rollback();
                    return new Response<Teacher>().setError("Internal server error");
                }
                connection.commit();
                return getTeacherById(Long.parseLong(resultSet.getString(1)));
            }
        } catch (Exception e) {
            return new Response<Teacher>().setError(e.getMessage());
        }
    }

    public static Response<Teacher> updateTeacher(Teacher teacher) {
        Objects.requireNonNull(teacher);

        var oldTeacher = getTeacherById(teacher.getId());
        if (oldTeacher.getError() != null) return oldTeacher;
        var data = oldTeacher.getData().getFirst();
        if (teacher.getNic() == null) teacher.setNic(data.getNic());
        if (teacher.getInitName() == null) teacher.setInitName(data.getInitName());
        if (teacher.getFullName() == null) teacher.setFullName(data.getFullName());
        if (teacher.getGender() == null) teacher.setGender(data.getGender());
        if (teacher.getAddress() == null) teacher.setAddress(data.getAddress());
        if (teacher.getEmail() == null) teacher.setEmail(data.getEmail());
        if (teacher.getContactNo() == null) teacher.setContactNo(data.getContactNo());
        if (teacher.getDisabled() == null) teacher.setDisabled(data.getDisabled());

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("UPDATE teacher SET nic = ?, initName = ?, fullName = ?, gender = ?, address = ?, " +
                     "email = ?, contactNo = ?, disabled = ? WHERE id = ?")) {
            statement.setString(1, teacher.getNic());
            statement.setString(2, teacher.getInitName());
            statement.setString(3, teacher.getFullName());
            statement.setString(4, teacher.getGender().toString());
            statement.setString(5, teacher.getAddress());
            statement.setString(6, teacher.getEmail());
            statement.setString(7, teacher.getContactNo());
            statement.setBoolean(8, teacher.getDisabled());
            statement.setString(9, Long.toUnsignedString(teacher.getId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Teacher>().setError("Internal server error");
            }
            connection.commit();
            return getTeacherById(teacher.getId());
        } catch (Exception e) {
            return new Response<Teacher>().setError(e.getMessage());
        }
    }

    public static Response<Teacher> deleteTeacherById(Long id) {
        Objects.requireNonNull(id);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM teacher WHERE id = ?")) {
            statement.setString(1, Long.toUnsignedString(id));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Teacher>().setError("Internal server error");
            }
            if (!AuthenticationService.deleteAuthentication(Role.TEACHER, id)) {
                connection.rollback();
                return new Response<Teacher>().setError("Internal server error");
            }
            connection.commit();
            return new Response<Teacher>().setSuccess(true);
        } catch (Exception e) {
            return new Response<Teacher>().setError(e.getMessage());
        }
    }

    public static Response<Teacher> resetPassword(Long id,
                                                  String oldPassword,
                                                  String newPassword) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(oldPassword);
        Objects.requireNonNull(newPassword);
        if (newPassword.length() < 8 || newPassword.length() > 50) return new Response<Teacher>().setError("Password length must between 8 and 50");
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
