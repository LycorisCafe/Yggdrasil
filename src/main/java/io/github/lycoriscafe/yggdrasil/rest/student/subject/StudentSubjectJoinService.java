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

package io.github.lycoriscafe.yggdrasil.rest.student.subject;

import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.commons.*;

import java.util.ArrayList;
import java.util.List;

public class StudentSubjectJoinService implements EntityService<StudentSubjectJoin> {
    public enum Columns implements EntityColumn<StudentSubjectJoin> {
        id,
        studentId,
        subjectId
    }

    public static Response<StudentSubjectJoin> select(SearchQueryBuilder<StudentSubjectJoin, Columns, StudentSubjectJoinService> searchQueryBuilder) {
        try {
            var results = CommonService.select(searchQueryBuilder);
            if (results.getResponse() != null) return results.getResponse();

            List<StudentSubjectJoin> studentSubjectJoins = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    studentSubjectJoins.add(new StudentSubjectJoin(
                            Long.parseLong(resultSet.getString("studentId")),
                            Long.parseLong(resultSet.getString("subjectId"))
                    ).setId(Long.parseLong(resultSet.getString("id"))));
                }
            }

            return new Response<StudentSubjectJoin>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
                    .setData(studentSubjectJoins);
        } catch (Exception e) {
            return new Response<StudentSubjectJoin>().setError(e.getMessage());
        }
    }

    public static Response<StudentSubjectJoin> delete(SearchQueryBuilder<StudentSubjectJoin, Columns, StudentSubjectJoinService> searchQueryBuilder) {
        return CommonService.delete(searchQueryBuilder);
    }

    public static Response<StudentSubjectJoin> insert(UpdateQueryBuilder<StudentSubjectJoin, Columns, StudentSubjectJoinService> updateQueryBuilder) {
        return CommonService.insert(updateQueryBuilder);
    }

    public static Response<StudentSubjectJoin> update(UpdateQueryBuilder<StudentSubjectJoin, Columns, StudentSubjectJoinService> updateQueryBuilder) {
        return CommonService.update(updateQueryBuilder);
    }
}
