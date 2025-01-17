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
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TeacherSubjectJoinService {
    public enum Columns {
        teacherId,
        subjectId
    }

    public static Response<TeacherSubjectJoin> getTeacherSubjectJoins(Columns[] searchBy,
                                                                      String[] searchByValues,
                                                                      boolean[] isCaseSensitive,
                                                                      Columns[] orderBy,
                                                                      Boolean isAscending,
                                                                      Long resultsFrom,
                                                                      Long resultsOffset) {
        if (resultsFrom == null || resultsFrom < 0) resultsFrom = 0L;
        if (resultsOffset == null || resultsOffset < 0) resultsOffset = YggdrasilConfig.getDefaultResultsOffset();
        if (resultsFrom > resultsOffset) return new Response<TeacherSubjectJoin>().setError("Invalid boundaries");

        StringBuilder query = new StringBuilder("SELECT * FROM teacherSubjectJoin");
        if (searchBy != null) {
            if (searchBy.length != searchByValues.length) return new Response<TeacherSubjectJoin>().setError("searchBy != searchByValues (length)");
            if (isCaseSensitive != null && searchBy.length != isCaseSensitive.length) {
                return new Response<TeacherSubjectJoin>().setError("searchBy != isCaseSensitive (length)");
            }
            query.append(" WHERE ");
            for (int i = 0; i < searchBy.length; i++) {
                if (i > 0) query.append(" AND ");
                query.append(searchBy[i]).append(" LIKE ");
                if (isCaseSensitive != null) query.append(isCaseSensitive[i] ? " BINARY " : "");
                query.append("?");
            }
        }
        if (orderBy != null) {
            query.append(" ORDER BY ");
            for (int i = 0; i < orderBy.length; i++) {
                if (i > 0) query.append(", ");
                query.append(orderBy[i]);
            }
        }
        if (isAscending != null) {
            query.append(isAscending ? " ASC" : " DESC");
        }
        query.replace(7, 8, "*, (" + query.toString().replace("*", "COUNT(id)") + ") AS generableValues");
        query.append(" LIMIT ").append(Long.toUnsignedString(resultsFrom)).append(", ").append(Long.toUnsignedString(resultsOffset));

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString())) {
            if (searchByValues != null) {
                for (int i = 0; i < searchByValues.length; i++) {
                    statement.setString(i + 1, searchByValues[i]);
                }
            }

            Long generableValues = null;
            List<TeacherSubjectJoin> teacherSubjectJoins = new ArrayList<>();
            try (var resultSet = statement.executeQuery()) {
                connection.commit();
                while (resultSet.next()) {
                    if (generableValues == null) generableValues = Long.parseLong(resultSet.getString("generableValues"));
                    teacherSubjectJoins.add(new TeacherSubjectJoin(
                            Long.parseLong(resultSet.getString("teacherId")),
                            Long.parseLong(resultSet.getString("subjectId"))
                    ));
                }
            }

            return new Response<TeacherSubjectJoin>()
                    .setSuccess(true)
                    .setGenerableResults(generableValues)
                    .setResultsFrom(resultsFrom)
                    .setResultsOffset(resultsOffset)
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
            return getTeacherSubjectJoins(new Columns[]{Columns.teacherId, Columns.subjectId},
                    new String[]{Long.toUnsignedString(teacherSubjectJoin.getTeacherId()), Long.toUnsignedString(teacherSubjectJoin.getSubjectId())},
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<TeacherSubjectJoin>().setError(e.getMessage());
        }
    }

    public static Response<TeacherSubjectJoin> deleteTeacherSubjectJoinByStudentIdAndSubjectId(Long teacherId,
                                                                                               Long subjectId) {
        Objects.requireNonNull(teacherId);
        Objects.requireNonNull(subjectId);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM teachersubjectjoin WHERE teacherId = ? AND subjectId = ?")) {
            statement.setString(1, Long.toUnsignedString(teacherId));
            statement.setString(2, Long.toUnsignedString(subjectId));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<TeacherSubjectJoin>().setError("Internal server error");
            }
            connection.commit();
            return new Response<TeacherSubjectJoin>().setSuccess(true);
        } catch (Exception e) {
            return new Response<TeacherSubjectJoin>().setError(e.getMessage());
        }
    }
}
