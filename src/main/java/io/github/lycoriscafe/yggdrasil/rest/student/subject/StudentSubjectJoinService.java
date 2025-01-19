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
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.commons.CommonService;
import io.github.lycoriscafe.yggdrasil.configuration.commons.EntityColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StudentSubjectJoinService {
    public enum Columns implements EntityColumn {
        studentId,
        subjectId
    }

    public static Response<StudentSubjectJoin> getStudentSubjectJoins(List<Columns> searchBy,
                                                                      List<String> searchByValues,
                                                                      List<Boolean> isCaseSensitive,
                                                                      List<Columns> orderBy,
                                                                      Boolean isAscending,
                                                                      Long resultsFrom,
                                                                      Long resultsOffset) {
        try {
            var results = CommonService.get(new CommonService.SearchQueryBuilder<StudentSubjectJoin, Columns>(StudentSubjectJoin.class)
                    .setSearchBy(searchBy).setSearchByValues(searchByValues).setIsCaseSensitive(isCaseSensitive).setOrderBy(orderBy)
                    .setAscending(isAscending).setResultsFrom(resultsFrom).setResultsOffset(resultsOffset));
            if (results.getResponse() != null) return results.getResponse();

            List<StudentSubjectJoin> studentSubjectJoins = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    studentSubjectJoins.add(new StudentSubjectJoin(
                            Long.parseLong(resultSet.getString("studentId")),
                            Long.parseLong(resultSet.getString("subjectId"))
                    ));
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

    public static Response<StudentSubjectJoin> createStudentSubjectJoin(StudentSubjectJoin studentSubjectJoin) {
        Objects.requireNonNull(studentSubjectJoin);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO studentsubjectjoin (studentId, subjectId) VALUES (?, ?)")) {
            statement.setString(1, Long.toUnsignedString(studentSubjectJoin.getStudentId()));
            statement.setString(2, Long.toUnsignedString(studentSubjectJoin.getSubjectId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<StudentSubjectJoin>().setError("Internal server error");
            }
            connection.commit();
            return getStudentSubjectJoins(List.of(Columns.studentId, Columns.subjectId),
                    List.of(Long.toUnsignedString(studentSubjectJoin.getStudentId()), Long.toUnsignedString(studentSubjectJoin.getSubjectId())),
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<StudentSubjectJoin>().setError(e.getMessage());
        }
    }

    public static Response<StudentSubjectJoin> deleteStudentSubjectJoinByStudentIdAndSubjectId(Long studentId,
                                                                                               Long subjectId) {
        Objects.requireNonNull(studentId);
        Objects.requireNonNull(subjectId);
        return CommonService.delete(new CommonService.SearchQueryBuilder<StudentSubjectJoin, Columns>(StudentSubjectJoin.class)
                .setSearchBy(List.of(Columns.studentId, Columns.subjectId))
                .setSearchByValues(List.of(Long.toUnsignedString(studentId), Long.toUnsignedString(subjectId))));
    }
}
