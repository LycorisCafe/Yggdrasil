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
import io.github.lycoriscafe.yggdrasil.configuration.commons.CommonService;
import io.github.lycoriscafe.yggdrasil.configuration.commons.EntityColumn;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TeacherAttendanceService {
    public enum Columns implements EntityColumn {
        teacherId,
        date,
        time
    }

    public static Response<TeacherAttendance> getTeacherAttendances(List<Columns> searchBy,
                                                                    List<String> searchByValues,
                                                                    List<Boolean> isCaseSensitive,
                                                                    List<Columns> orderBy,
                                                                    Boolean isAscending,
                                                                    Long resultsFrom,
                                                                    Long resultsOffset) {
        try {
            var results = CommonService.select(new CommonService.SearchQueryBuilder<TeacherAttendance, Columns>(TeacherAttendance.class)
                    .setSearchBy(searchBy).setSearchByValues(searchByValues).setIsCaseSensitive(isCaseSensitive).setOrderBy(orderBy)
                    .setAscending(isAscending).setResultsFrom(resultsFrom).setResultsOffset(resultsOffset));
            if (results.getResponse() != null) return results.getResponse();

            List<TeacherAttendance> teacherAttendances = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    teacherAttendances.add(new TeacherAttendance(
                            Long.parseLong(resultSet.getString("teacherId"))
                    ).setDate(LocalDate.parse(resultSet.getString("date"), Utils.getDateFormatter()))
                            .setTime(LocalTime.parse(resultSet.getString("time"), Utils.getTimeFormatter())));
                }
            }

            return new Response<TeacherAttendance>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
                    .setData(teacherAttendances);
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
            return getTeacherAttendances(List.of(Columns.teacherId, Columns.date),
                    List.of(Long.toUnsignedString(teacherAttendance.getTeacherId()), teacherAttendance.getDate().format(Utils.getDateFormatter())),
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<TeacherAttendance>().setError(e.getMessage());
        }
    }

    public static Response<TeacherAttendance> deleteTeacherAttendanceByTeacherIdAndDate(Long teacherId,
                                                                                        LocalDate date) {
        Objects.requireNonNull(teacherId);
        Objects.requireNonNull(date);
        return CommonService.delete(new CommonService.SearchQueryBuilder<TeacherAttendance, Columns>(TeacherAttendance.class)
                .setSearchBy(List.of(Columns.teacherId, Columns.date))
                .setSearchByValues(List.of(Long.toUnsignedString(teacherId), date.format(Utils.getDateFormatter()))));
    }
}
