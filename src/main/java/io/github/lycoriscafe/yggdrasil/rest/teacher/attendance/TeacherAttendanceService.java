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
import io.github.lycoriscafe.yggdrasil.configuration.commons.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TeacherAttendanceService implements EntityService<TeacherAttendance> {
    public enum Columns implements EntityColumn<TeacherAttendance> {
        id,
        teacherId,
        date,
        time
    }

    public static Response<TeacherAttendance> select(SearchQueryBuilder<TeacherAttendance, Columns, TeacherAttendanceService> searchQueryBuilder) {
        try {
            var results = CommonService.select(searchQueryBuilder);
            if (results.getResponse() != null) return results.getResponse();

            List<TeacherAttendance> teacherAttendances = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    teacherAttendances.add(new TeacherAttendance(
                            resultSet.getBigDecimal("teacherId")
                    ).setId(resultSet.getBigDecimal("id"))
                            .setDate(LocalDate.parse(resultSet.getString("date"), Utils.getDateFormatter()))
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

    public static Response<TeacherAttendance> delete(SearchQueryBuilder<TeacherAttendance, Columns, TeacherAttendanceService> searchQueryBuilder) {
        if (searchQueryBuilder.getSearchBy() == null || !searchQueryBuilder.getSearchBy().contains(Columns.id)) {
            return new Response<TeacherAttendance>().setError("Required parameters not found");
        }
        return CommonService.delete(searchQueryBuilder);
    }

    public static Response<TeacherAttendance> insert(UpdateQueryBuilder<TeacherAttendance, Columns, TeacherAttendanceService> updateQueryBuilder) {
        if (updateQueryBuilder.getColumns() == null || !updateQueryBuilder.getColumns().contains(Columns.teacherId)) {
            return new Response<TeacherAttendance>().setError("Required parameters not found");
        }
        return CommonService.insert(updateQueryBuilder);
    }

    public static Response<TeacherAttendance> update(UpdateQueryBuilder<TeacherAttendance, Columns, TeacherAttendanceService> updateQueryBuilder) {
        if (updateQueryBuilder.getSearchBy() == null || !updateQueryBuilder.getSearchBy().contains(Columns.id)) {
            return new Response<TeacherAttendance>().setError("Required parameters not found");
        }
        if (updateQueryBuilder.getColumns() != null && updateQueryBuilder.getColumns().contains(Columns.id)) {
            return new Response<TeacherAttendance>().setError("'id' cannot be updated");
        }
        return CommonService.update(updateQueryBuilder);
    }
}
