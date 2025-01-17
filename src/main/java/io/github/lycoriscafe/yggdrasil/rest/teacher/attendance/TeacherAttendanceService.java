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
import io.github.lycoriscafe.yggdrasil.configuration.database.CommonCRUD;
import io.github.lycoriscafe.yggdrasil.configuration.database.EntityColumn;

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

    public static Response<TeacherAttendance> getTeacherAttendances(Columns[] searchBy,
                                                                    String[] searchByValues,
                                                                    boolean[] isCaseSensitive,
                                                                    Columns[] orderBy,
                                                                    Boolean isAscending,
                                                                    Long resultsFrom,
                                                                    Long resultsOffset) {
        try {
            var results = CommonCRUD.get(TeacherAttendance.class, searchBy, searchByValues, isCaseSensitive, orderBy, isAscending, resultsFrom, resultsOffset);
            if (results.getResponse() != null) return results.getResponse();

            var resultSet = results.getResultSet();
            Long generableValues = null;
            List<TeacherAttendance> teacherAttendances = new ArrayList<>();
            while (resultSet.next()) {
                if (generableValues == null) generableValues = Long.parseLong(resultSet.getString("generableValues"));
                teacherAttendances.add(new TeacherAttendance(
                        Long.parseLong(resultSet.getString("teacherId"))
                ).setDate(LocalDate.parse(resultSet.getString("date")))
                        .setTime(LocalTime.parse(resultSet.getString("time"))));
            }

            return new Response<TeacherAttendance>()
                    .setSuccess(true)
                    .setGenerableResults(generableValues)
                    .setResultsFrom(resultsFrom)
                    .setResultsOffset(resultsOffset)
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
            return getTeacherAttendances(new Columns[]{Columns.teacherId, Columns.date},
                    new String[]{Long.toUnsignedString(teacherAttendance.getTeacherId()), LocalDate.now().toString()},
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<TeacherAttendance>().setError(e.getMessage());
        }
    }

    public static Response<TeacherAttendance> deleteTeacherAttendanceByTeacherIdAndDate(Long id,
                                                                                        LocalDate date) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(date);
        return CommonCRUD.delete(TeacherAttendance.class, new Columns[]{Columns.teacherId, Columns.date},
                new String[]{Long.toUnsignedString(id), date.toString()}, null);
    }
}
