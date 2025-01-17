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
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StudentAttendanceService {
    public enum Columns {
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
        if (resultsFrom == null || resultsFrom < 0) resultsFrom = 0L;
        if (resultsOffset == null || resultsOffset < 0) resultsOffset = YggdrasilConfig.getDefaultResultsOffset();
        if (resultsFrom > resultsOffset) return new Response<StudentAttendance>().setError("Invalid boundaries");

        StringBuilder query = new StringBuilder("SELECT * FROM studentAttendance");
        if (searchBy != null) {
            if (searchBy.length != searchByValues.length) return new Response<StudentAttendance>().setError("searchBy != searchByValues (length)");
            if (isCaseSensitive != null && searchBy.length != isCaseSensitive.length) {
                return new Response<StudentAttendance>().setError("searchBy != isCaseSensitive (length)");
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

            long generableValues;
            List<StudentAttendance> studentAttendances = new ArrayList<>();
            try (var resultSet = statement.executeQuery()) {
                connection.commit();
                while (resultSet.next()) {
                    studentAttendances.add(new StudentAttendance(
                            Long.parseLong(resultSet.getString("studentId"))
                    ).setDate(LocalDate.parse(resultSet.getString("date")))
                            .setTime(LocalTime.parse(resultSet.getString("time"))));
                }
                generableValues = Long.parseLong(resultSet.getString("generableValues"));
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

    public static Response<StudentAttendance> deleteStudentAttendanceByStudentIdAndDate(Long id,
                                                                                        LocalDate date) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(date);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM studentattendance WHERE studentId = ? AND date = ?")) {
            statement.setString(1, Long.toUnsignedString(id));
            statement.setString(2, date.toString());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<StudentAttendance>().setError("Internal server error");
            }
            connection.commit();
            return new Response<StudentAttendance>().setSuccess(true);
        } catch (Exception e) {
            return new Response<StudentAttendance>().setError(e.getMessage());
        }
    }
}
