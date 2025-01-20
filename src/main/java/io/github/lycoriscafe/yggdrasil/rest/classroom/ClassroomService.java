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

package io.github.lycoriscafe.yggdrasil.rest.classroom;

import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.commons.*;

import java.util.ArrayList;
import java.util.List;

public class ClassroomService implements EntityService<Classroom> {
    public enum Columns implements EntityColumn<Classroom> {
        id,
        teacherId,
        grade,
        name
    }

    public static Response<Classroom> select(SearchQueryBuilder<Classroom, Columns, ClassroomService> searchQueryBuilder) {
        try {
            var results = CommonService.select(searchQueryBuilder);
            if (results.getResponse() != null) return results.getResponse();

            List<Classroom> classrooms = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    classrooms.add(new Classroom(
                            resultSet.getInt("grade"),
                            resultSet.getString("name")
                    ).setId(Long.parseLong(resultSet.getString("id")))
                            .setTeacherId(resultSet.getString("teacherId") == null ?
                                    null : Long.parseLong(resultSet.getString("teacherId"))));
                }
            }

            return new Response<Classroom>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
                    .setData(classrooms);
        } catch (Exception e) {
            return new Response<Classroom>().setError(e.getMessage());
        }
    }

    public static Response<Classroom> delete(SearchQueryBuilder<Classroom, Columns, ClassroomService> searchQueryBuilder) {
        return CommonService.delete(searchQueryBuilder);
    }

    public static Response<Classroom> insert(UpdateQueryBuilder<Classroom, Columns, ClassroomService> updateQueryBuilder) {
        return CommonService.insert(updateQueryBuilder);
    }

    public static Response<Classroom> update(UpdateQueryBuilder<Classroom, Columns, ClassroomService> updateQueryBuilder) {
        return CommonService.update(updateQueryBuilder);
    }
}
