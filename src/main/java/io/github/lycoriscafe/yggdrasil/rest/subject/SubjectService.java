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

package io.github.lycoriscafe.yggdrasil.rest.subject;

import io.github.lycoriscafe.yggdrasil.commons.*;
import io.github.lycoriscafe.yggdrasil.configuration.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SubjectService implements EntityService<Subject> {
    public enum Columns implements EntityColumn<Subject> {
        id,
        grade,
        shortName,
        longGame,
        teacherId
    }

    public static Response<Subject> select(SearchQueryBuilder<Subject, Columns, SubjectService> searchQueryBuilder) {
        try {
            var results = CommonService.select(searchQueryBuilder);
            if (results.getResponse() != null) return results.getResponse();

            List<Subject> subjects = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    subjects.add(new Subject(
                            resultSet.getInt("grade"),
                            resultSet.getString("shortName")
                    ).setId(resultSet.getBigDecimal("id"))
                            .setLongName(resultSet.getString("longName"))
                            .setTeacherId(resultSet.getBigDecimal("teacherId")));
                }
            }

            return new Response<Subject>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
                    .setData(subjects);
        } catch (Exception e) {
            return new Response<Subject>().setError(e.getMessage());
        }
    }

    public static Response<Subject> delete(SearchQueryBuilder<Subject, Columns, SubjectService> searchQueryBuilder) {
        if (searchQueryBuilder.getSearchBy() == null || !searchQueryBuilder.getSearchBy().contains(Columns.id)) {
            return new Response<Subject>().setError("Required parameters not found");
        }
        return CommonService.delete(searchQueryBuilder);
    }

    public static Response<Subject> insert(UpdateQueryBuilder<Subject, Columns, SubjectService> updateQueryBuilder) {
        if (updateQueryBuilder.getColumns() == null || !updateQueryBuilder.getColumns().containsAll(Set.of(Columns.grade, Columns.shortName))) {
            return new Response<Subject>().setError("Required parameters not found");
        }
        return CommonService.insert(updateQueryBuilder);
    }

    public static Response<Subject> update(UpdateQueryBuilder<Subject, Columns, SubjectService> updateQueryBuilder) {
        if (updateQueryBuilder.getSearchBy() == null || !updateQueryBuilder.getSearchBy().contains(Columns.id)) {
            return new Response<Subject>().setError("Required parameters not found");
        }
        if (updateQueryBuilder.getColumns() != null && updateQueryBuilder.getColumns().contains(Columns.id)) {
            return new Response<Subject>().setError("'id' cannot be updated");
        }
        return CommonService.update(updateQueryBuilder);
    }
}
