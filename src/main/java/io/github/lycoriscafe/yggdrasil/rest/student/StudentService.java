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
import io.github.lycoriscafe.yggdrasil.configuration.commons.CommonService;
import io.github.lycoriscafe.yggdrasil.configuration.commons.EntityColumn;
import io.github.lycoriscafe.yggdrasil.rest.Gender;

import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StudentService {
    public enum Columns implements EntityColumn {
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

    public static Response<Student> getStudents(List<Columns> searchBy,
                                                List<String> searchByValues,
                                                List<Boolean> isCaseSensitive,
                                                List<Columns> orderBy,
                                                Boolean isAscending,
                                                Long resultsFrom,
                                                Long resultsOffset) {
        try {
            var results = CommonService.get(new CommonService.SearchQueryBuilder<Student, Columns>(Student.class)
                    .setSearchBy(searchBy).setSearchByValues(searchByValues).setIsCaseSensitive(isCaseSensitive).setOrderBy(orderBy)
                    .setAscending(isAscending).setResultsFrom(resultsFrom).setResultsOffset(resultsOffset));
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

    public static Response<Student> getStudentById(Long id) {
        try {
            return getStudents(List.of(Columns.id), List.of(Long.toUnsignedString(id)),
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<Student>().setError(e.getMessage());
        }
    }

    public static Response<Student> createStudent(Student student) {
        Objects.requireNonNull(student);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO student (id, guardianId, classroomId, initName, fullName, gender, " +
                     "dateOfBirth, nic, address, regYear, contactNo, email, disabled) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, COALESCE(?, DEFAULT(disabled)))", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, student.getId() == null ? null : Long.toUnsignedString(student.getId()));
            statement.setString(2, Long.toUnsignedString(student.getGuardianId()));
            statement.setString(3, student.getClassroomId() == null ? null : Long.toUnsignedString(student.getClassroomId()));
            statement.setString(4, student.getInitName());
            statement.setString(5, student.getFullName());
            statement.setString(6, student.getGender().toString());
            statement.setString(7, student.getDateOfBirth().format(Utils.getDateFormatter()));
            statement.setString(8, student.getNic());
            statement.setString(9, student.getAddress());
            statement.setString(10, student.getRegYear().toString());
            statement.setString(11, student.getContactNo());
            statement.setString(12, student.getEmail());
            statement.setBoolean(13, student.getDisabled());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Student>().setError("Internal server error");
            }
            try (var resultSet = statement.getGeneratedKeys()) {
                if (!resultSet.next()) {
                    connection.rollback();
                    return new Response<Student>().setError("Internal server error");
                }
                if (AuthenticationService.updatePassword(
                        AuthenticationService.createAuthentication(
                                new Authentication(Role.STUDENT,
                                        Long.parseLong(resultSet.getString(1)),
                                        "S" + resultSet.getString(1)))) == null) {
                    connection.rollback();
                    return new Response<Student>().setError("Internal server error");
                }
                connection.commit();
                return getStudentById(Long.parseLong(resultSet.getString(1)));
            }
        } catch (Exception e) {
            return new Response<Student>().setError(e.getMessage());
        }
    }

    public static Response<Student> updateStudent(Student student) {
        Objects.requireNonNull(student);

        var oldStudent = getStudentById(student.getId());
        if (oldStudent.getError() != null) return oldStudent;
        var data = oldStudent.getData().getFirst();
        if (student.getGuardianId() == null) student.setGuardianId(data.getGuardianId());
        if (student.getClassroomId() == null) student.setClassroomId(data.getClassroomId());
        if (student.getInitName() == null) student.setInitName(data.getInitName());
        if (student.getFullName() == null) student.setFullName(data.getFullName());
        if (student.getGender() == null) student.setGender(data.getGender());
        if (student.getDateOfBirth() == null) student.setDateOfBirth(data.getDateOfBirth());
        if (student.getNic() == null) student.setNic(data.getNic());
        if (student.getAddress() == null) student.setAddress(data.getAddress());
        if (student.getRegYear() == null) student.setRegYear(data.getRegYear());
        if (student.getContactNo() == null) student.setContactNo(data.getContactNo());
        if (student.getEmail() == null) student.setEmail(data.getEmail());
        if (student.getDisabled() == null) student.setDisabled(data.getDisabled());

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("UPDATE student SET guardianId = ?, classroomId = ?, initName = ?, fullName = ?, " +
                     "gender = ?, dateOfBirth = ?, nic = ?, address = ?, regYear = ?, contactNo = ?, email = ?, disabled = ? WHERE id = ?")) {
            statement.setString(1, Long.toUnsignedString(student.getGuardianId()));
            statement.setString(2, student.getClassroomId() == null ? null : Long.toUnsignedString(student.getClassroomId()));
            statement.setString(3, student.getInitName());
            statement.setString(4, student.getFullName());
            statement.setString(5, student.getGender().toString());
            statement.setString(6, student.getDateOfBirth().format(Utils.getDateFormatter()));
            statement.setString(7, student.getNic());
            statement.setString(8, student.getAddress());
            statement.setString(9, student.getRegYear().toString());
            statement.setString(10, student.getContactNo());
            statement.setString(11, student.getEmail());
            statement.setBoolean(12, student.getDisabled());
            statement.setString(13, Long.toUnsignedString(student.getId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Student>().setError("Internal server error");
            }
            connection.commit();
            return getStudentById(student.getId());
        } catch (Exception e) {
            return new Response<Student>().setError(e.getMessage());
        }
    }

    public static Response<Student> deleteStudentById(Long id) {
        Objects.requireNonNull(id);
        var result = CommonService.delete(new CommonService.SearchQueryBuilder<Student, Columns>(Student.class).setSearchBy(List.of(Columns.id))
                .setSearchByValues(List.of(Long.toUnsignedString(id))));
        if (result.isSuccess()) AuthenticationService.deleteAuthentication(Role.STUDENT, id);
        return result;
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
