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
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.commons.CommonService;
import io.github.lycoriscafe.yggdrasil.configuration.commons.EntityColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TeacherSubjectJoinService {
    public enum Columns implements EntityColumn {
        teacherId,
        subjectId
    }

    public static Response<TeacherSubjectJoin> getTeacherSubjectJoins(List<Columns> searchBy,
                                                                      List<String> searchByValues,
                                                                      List<Boolean> isCaseSensitive,
                                                                      List<Columns> orderBy,
                                                                      Boolean isAscending,
                                                                      Long resultsFrom,
                                                                      Long resultsOffset) {
        try {
            var results = CommonService.get(new CommonService.SearchQueryBuilder<TeacherSubjectJoin, Columns>(TeacherSubjectJoin.class)
                    .setSearchBy(searchBy).setSearchByValues(searchByValues).setIsCaseSensitive(isCaseSensitive).setOrderBy(orderBy)
                    .setAscending(isAscending).setResultsFrom(resultsFrom).setResultsOffset(resultsOffset));
            if (results.getResponse() != null) return results.getResponse();

            List<TeacherSubjectJoin> teacherSubjectJoins = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    teacherSubjectJoins.add(new TeacherSubjectJoin(
                            Long.parseLong(resultSet.getString("teacherId")),
                            Long.parseLong(resultSet.getString("subjectId"))
                    ));
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

    public static Response<TeacherSubjectJoin> createTeacherSubjectJoin(TeacherSubjectJoin teacherSubjectJoin) {
        Objects.requireNonNull(teacherSubjectJoin);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO teachersubjectjoin (teacherId, subjectId) VALUES (?, ?)")) {
            statement.setString(1, Long.toUnsignedString(teacherSubjectJoin.getTeacherId()));
            statement.setString(2, Long.toUnsignedString(teacherSubjectJoin.getSubjectId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<TeacherSubjectJoin>().setError("Internal server error");
            }
            connection.commit();
            return getTeacherSubjectJoins(List.of(Columns.teacherId, Columns.subjectId),
                    List.of(Long.toUnsignedString(teacherSubjectJoin.getTeacherId()), Long.toUnsignedString(teacherSubjectJoin.getSubjectId())),
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<TeacherSubjectJoin>().setError(e.getMessage());
        }
    }

    public static Response<TeacherSubjectJoin> deleteTeacherSubjectJoinByStudentIdAndSubjectId(Long teacherId,
                                                                                               Long subjectId) {
        Objects.requireNonNull(teacherId);
        Objects.requireNonNull(subjectId);
        return CommonService.delete(new CommonService.SearchQueryBuilder<TeacherSubjectJoin, Columns>(TeacherSubjectJoin.class)
                .setSearchBy(List.of(Columns.teacherId, Columns.subjectId))
                .setSearchByValues(List.of(Long.toUnsignedString(teacherId), Long.toUnsignedString(subjectId))));
    }
}
