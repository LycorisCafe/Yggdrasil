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

package io.github.lycoriscafe.yggdrasil.rest.timetable;

import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.commons.*;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class TimetableService implements EntityService<Timetable> {
    public enum Columns implements EntityColumn<Timetable> {
        id,
        teacherId,
        subjectId,
        classroomId,
        day,
        timeslot
    }

    public static Response<Timetable> select(SearchQueryBuilder<Timetable, Columns, TimetableService> searchQueryBuilder) {
        try {
            var results = CommonService.select(searchQueryBuilder);
            if (results.getResponse() != null) return results.getResponse();

            List<Timetable> timetables = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    timetables.add(new Timetable(
                            resultSet.getBigDecimal("teacherId"),
                            resultSet.getBigDecimal("subjectId"),
                            resultSet.getBigDecimal("classroomId"),
                            DayOfWeek.of(resultSet.getInt("day")),
                            resultSet.getInt("timeslot")
                    ).setId(resultSet.getBigDecimal("id")));
                }
            }

            return new Response<Timetable>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
                    .setData(timetables);
        } catch (Exception e) {
            return new Response<Timetable>().setError(e.getMessage());
        }
    }

    public static Response<Timetable> delete(SearchQueryBuilder<Timetable, Columns, TimetableService> searchQueryBuilder) {
        return CommonService.delete(searchQueryBuilder);
    }

    public static Response<Timetable> insert(UpdateQueryBuilder<Timetable, Columns, TimetableService> updateQueryBuilder) {
        return CommonService.insert(updateQueryBuilder);
    }

    public static Response<Timetable> update(UpdateQueryBuilder<Timetable, Columns, TimetableService> updateQueryBuilder) {
        return CommonService.update(updateQueryBuilder);
    }
}
