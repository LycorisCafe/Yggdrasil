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

package io.github.lycoriscafe.yggdrasil.rest.teacher.subject;

import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.commons.*;

import java.util.ArrayList;
import java.util.List;

public class TeacherSubjectJoinService implements EntityService<TeacherSubjectJoin> {
    public enum Columns implements EntityColumn<TeacherSubjectJoin> {
        id,
        teacherId,
        subjectId
    }

    public static Response<TeacherSubjectJoin> select(SearchQueryBuilder<TeacherSubjectJoin, Columns, TeacherSubjectJoinService> searchQueryBuilder) {
        try {
            var results = CommonService.select(searchQueryBuilder);
            if (results.getResponse() != null) return results.getResponse();

            List<TeacherSubjectJoin> teacherSubjectJoins = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    teacherSubjectJoins.add(new TeacherSubjectJoin(
                            resultSet.getBigDecimal("teacherId"),
                            resultSet.getBigDecimal("subjectId")
                    ).setId(resultSet.getBigDecimal("id")));
                }
            }

            return new Response<TeacherSubjectJoin>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
                    .setData(teacherSubjectJoins);
        } catch (Exception e) {
            return new Response<TeacherSubjectJoin>().setError(e.getMessage());
        }
    }

    public static Response<TeacherSubjectJoin> delete(SearchQueryBuilder<TeacherSubjectJoin, Columns, TeacherSubjectJoinService> searchQueryBuilder) {
        return CommonService.delete(searchQueryBuilder);
    }

    public static Response<TeacherSubjectJoin> insert(UpdateQueryBuilder<TeacherSubjectJoin, Columns, TeacherSubjectJoinService> updateQueryBuilder) {
        return CommonService.insert(updateQueryBuilder);
    }

    public static Response<TeacherSubjectJoin> update(UpdateQueryBuilder<TeacherSubjectJoin, Columns, TeacherSubjectJoinService> updateQueryBuilder) {
        return CommonService.update(updateQueryBuilder);
    }
}
