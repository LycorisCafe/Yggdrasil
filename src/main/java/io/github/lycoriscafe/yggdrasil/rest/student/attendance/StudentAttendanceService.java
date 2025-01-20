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
import io.github.lycoriscafe.yggdrasil.configuration.commons.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class StudentAttendanceService implements EntityService<StudentAttendance> {
    public enum Columns implements EntityColumn<StudentAttendance> {
        id,
        studentId,
        date,
        time
    }

    public static Response<StudentAttendance> select(SearchQueryBuilder<StudentAttendance, Columns, StudentAttendanceService> searchQueryBuilder) {
        try {
            var results = CommonService.select(searchQueryBuilder);
            if (results.getResponse() != null) return results.getResponse();

            List<StudentAttendance> studentAttendances = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    studentAttendances.add(new StudentAttendance(
                            Long.parseLong(resultSet.getString("studentId"))
                    ).setId(Long.parseLong(resultSet.getString("id")))
                            .setDate(LocalDate.parse(resultSet.getString("date"), Utils.getDateFormatter()))
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

    public static Response<StudentAttendance> delete(SearchQueryBuilder<StudentAttendance, Columns, StudentAttendanceService> searchQueryBuilder) {
        return CommonService.delete(searchQueryBuilder);
    }

    public static Response<StudentAttendance> insert(UpdateQueryBuilder<StudentAttendance, Columns, StudentAttendanceService> updateQueryBuilder) {
        return CommonService.insert(updateQueryBuilder);
    }

    public static Response<StudentAttendance> update(UpdateQueryBuilder<StudentAttendance, Columns, StudentAttendanceService> updateQueryBuilder) {
        return CommonService.update(updateQueryBuilder);
    }
}
