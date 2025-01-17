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
import io.github.lycoriscafe.yggdrasil.configuration.database.CommonCRUD;
import io.github.lycoriscafe.yggdrasil.configuration.database.EntityColumn;

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

    public static Response<StudentAttendance> getStudentAttendances(Columns[] searchBy,
                                                                    String[] searchByValues,
                                                                    boolean[] isCaseSensitive,
                                                                    Columns[] orderBy,
                                                                    Boolean isAscending,
                                                                    Long resultsFrom,
                                                                    Long resultsOffset) {
        try {
            var results = CommonCRUD.get(StudentAttendance.class, searchBy, searchByValues, isCaseSensitive, orderBy, isAscending, resultsFrom, resultsOffset);
            if (results.getResponse() != null) return results.getResponse();

            var resultSet = results.getResultSet();
            Long generableValues = null;
            List<StudentAttendance> studentAttendances = new ArrayList<>();
            while (resultSet.next()) {
                if (generableValues == null) generableValues = Long.parseLong(resultSet.getString("generableValues"));
                studentAttendances.add(new StudentAttendance(
                        Long.parseLong(resultSet.getString("studentId"))
                ).setDate(LocalDate.parse(resultSet.getString("date")))
                        .setTime(LocalTime.parse(resultSet.getString("time"))));
            }

            return new Response<StudentAttendance>()
                    .setSuccess(true)
                    .setGenerableResults(generableValues)
                    .setResultsFrom(resultsFrom)
                    .setResultsOffset(resultsOffset)
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
            return getStudentAttendances(new Columns[]{Columns.studentId, Columns.date},
                    new String[]{Long.toUnsignedString(studentAttendance.getStudentId()), LocalDate.now().toString()},
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<StudentAttendance>().setError(e.getMessage());
        }
    }

    public static Response<StudentAttendance> deleteStudentAttendanceByStudentIdAndDate(Long studentId,
                                                                                        LocalDate date) {
        Objects.requireNonNull(studentId);
        Objects.requireNonNull(date);
        return CommonCRUD.delete(StudentAttendance.class, new Columns[]{Columns.studentId, Columns.date},
                new String[]{Long.toUnsignedString(studentId), date.toString()}, null);
    }
}
