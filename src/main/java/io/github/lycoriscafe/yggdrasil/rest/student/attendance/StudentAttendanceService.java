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

package io.github.lycoriscafe.yggdrasil.rest.student.attendance;

import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.commons.CommonService;
import io.github.lycoriscafe.yggdrasil.configuration.commons.EntityColumn;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StudentAttendanceService {
    public enum Columns implements EntityColumn {
        studentId,
        date,
        time
    }

    public static Response<StudentAttendance> getStudentAttendances(List<Columns> searchBy,
                                                                    List<String> searchByValues,
                                                                    List<Boolean> isCaseSensitive,
                                                                    List<Columns> orderBy,
                                                                    Boolean isAscending,
                                                                    Long resultsFrom,
                                                                    Long resultsOffset) {
        try {
            var results = CommonService.get(new CommonService.SearchQueryBuilder<StudentAttendance, Columns>(StudentAttendance.class)
                    .setSearchBy(searchBy).setSearchByValues(searchByValues).setIsCaseSensitive(isCaseSensitive).setOrderBy(orderBy)
                    .setAscending(isAscending).setResultsFrom(resultsFrom).setResultsOffset(resultsOffset));
            if (results.getResponse() != null) return results.getResponse();

            List<StudentAttendance> studentAttendances = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    studentAttendances.add(new StudentAttendance(
                            Long.parseLong(resultSet.getString("studentId"))
                    ).setDate(LocalDate.parse(resultSet.getString("date"), Utils.getDateFormatter()))
                            .setTime(LocalTime.parse(resultSet.getString("time"), Utils.getTimeFormatter())));
                }
            }

            return new Response<StudentAttendance>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
                    .setData(studentAttendances);
        } catch (Exception e) {
            return new Response<StudentAttendance>().setError(e.getMessage());
        }
    }

    public static Response<StudentAttendance> createStudentAttendance(StudentAttendance studentAttendance) {
        Objects.requireNonNull(studentAttendance);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO studentattendance (studentId) VALUES (?)")) {
            statement.setString(1, Long.toUnsignedString(studentAttendance.getStudentId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<StudentAttendance>().setError("Internal server error");
            }
            connection.commit();
            return getStudentAttendances(List.of(Columns.studentId, Columns.date),
                    List.of(Long.toUnsignedString(studentAttendance.getStudentId()), LocalDate.now().format(Utils.getDateFormatter())),
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<StudentAttendance>().setError(e.getMessage());
        }
    }

    public static Response<StudentAttendance> deleteStudentAttendanceByStudentIdAndDate(Long studentId,
                                                                                        LocalDate date) {
        Objects.requireNonNull(studentId);
        Objects.requireNonNull(date);
        return CommonService.delete(new CommonService.SearchQueryBuilder<StudentAttendance, Columns>(StudentAttendance.class)
                .setSearchBy(List.of(Columns.studentId, Columns.date))
                .setSearchByValues(List.of(Long.toUnsignedString(studentId), date.format(Utils.getDateFormatter()))));
    }
}
