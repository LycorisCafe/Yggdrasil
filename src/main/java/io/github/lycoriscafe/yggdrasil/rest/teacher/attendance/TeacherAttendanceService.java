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

package io.github.lycoriscafe.yggdrasil.rest.teacher.attendance;

import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TeacherAttendanceService {
    public enum Columns {
        teacherId,
        date,
        time
    }

    public static Response<TeacherAttendance> getTeacherAttendances(Columns[] searchBy,
                                                                    String[] searchByValues,
                                                                    boolean[] isCaseSensitive,
                                                                    Columns[] orderBy,
                                                                    Boolean isAscending,
                                                                    Long resultsFrom,
                                                                    Long resultsOffset) {
        if (resultsFrom == null || resultsFrom < 0) resultsFrom = 0L;
        if (resultsOffset == null || resultsOffset < 0) resultsOffset = YggdrasilConfig.getDefaultResultsOffset();
        if (resultsFrom > resultsOffset) return new Response<TeacherAttendance>().setError("Invalid boundaries");

        StringBuilder query = new StringBuilder("SELECT * FROM teacherAttendance");
        if (searchBy != null) {
            if (searchBy.length != searchByValues.length) return new Response<TeacherAttendance>().setError("searchBy != searchByValues (length)");
            if (isCaseSensitive != null && searchBy.length != isCaseSensitive.length) {
                return new Response<TeacherAttendance>().setError("searchBy != isCaseSensitive (length)");
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
                List<TeacherAttendance> teacherAttendances = new ArrayList<>();
                while (resultSet.next()) {
                    teacherAttendances.add(new TeacherAttendance(
                            Long.parseLong(resultSet.getString("teacherId"))
                    ).setDate(LocalDate.parse(resultSet.getString("date")))
                            .setTime(LocalTime.parse(resultSet.getString("time"))));
                }

                return new Response<TeacherAttendance>()
                        .setSuccess(true)
                        .setGenerableResults(Long.parseLong(resultSet.getString("generableValues")))
                        .setResultsFrom(resultsFrom)
                        .setResultsOffset(resultsOffset)
                        .setData(teacherAttendances);
            }
        } catch (Exception e) {
            return new Response<TeacherAttendance>().setError(e.getMessage());
        }
    }

    public static Response<TeacherAttendance> createTeacherAttendance(TeacherAttendance teacherAttendance) {
        Objects.requireNonNull(teacherAttendance);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO teacherattendance (teacherId) VALUES (?)")) {
            statement.setString(1, Long.toUnsignedString(teacherAttendance.getTeacherId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<TeacherAttendance>().setError("Internal server error");
            }
            connection.commit();
            return getTeacherAttendances(new Columns[]{Columns.teacherId, Columns.date},
                    new String[]{Long.toUnsignedString(teacherAttendance.getTeacherId()), LocalDate.now().toString()},
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<TeacherAttendance>().setError(e.getMessage());
        }
    }

    public static Response<TeacherAttendance> deleteTeacherAttendanceByStudentIdAndDate(Long id,
                                                                                        LocalDate date) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(date);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM teacherattendance WHERE teacherId = ? AND date = ?")) {
            statement.setString(1, Long.toUnsignedString(id));
            statement.setString(2, date.toString());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<TeacherAttendance>().setError("Internal server error");
            }
            connection.commit();
            return new Response<TeacherAttendance>().setSuccess(true);
        } catch (Exception e) {
            return new Response<TeacherAttendance>().setError(e.getMessage());
        }
    }
}
