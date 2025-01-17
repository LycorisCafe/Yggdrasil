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
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClassroomService {
    public enum Columns {
        id,
        teacherId,
        grade,
        name
    }

    public static Response<Classroom> getClassrooms(Columns[] searchBy,
                                                    String[] searchByValues,
                                                    boolean[] isCaseSensitive,
                                                    Columns[] orderBy,
                                                    Boolean isAscending,
                                                    Long resultsFrom,
                                                    Long resultsOffset) {
        if (resultsFrom == null || resultsFrom < 0) resultsFrom = 0L;
        if (resultsOffset == null || resultsOffset < 0) resultsOffset = YggdrasilConfig.getDefaultResultsOffset();
        if (resultsFrom > resultsOffset) return new Response<Classroom>().setError("Invalid boundaries");

        StringBuilder query = new StringBuilder("SELECT * FROM classroom");
        if (searchBy != null) {
            if (searchBy.length != searchByValues.length) return new Response<Classroom>().setError("searchBy != searchByValues (length)");
            if (isCaseSensitive != null && searchBy.length != isCaseSensitive.length) {
                return new Response<Classroom>().setError("searchBy != isCaseSensitive (length)");
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

            long generableValues;
            List<Classroom> classrooms = new ArrayList<>();
            try (var resultSet = statement.executeQuery()) {
                connection.commit();
                while (resultSet.next()) {
                    classrooms.add(new Classroom(
                            resultSet.getInt("grade"),
                            resultSet.getString("name")
                    ).setId(Long.parseLong(resultSet.getString("id")))
                            .setTeacherId(resultSet.getString("teacherId") == null ?
                                    null : Long.parseLong(resultSet.getString("teacherId"))));
                }
                generableValues = Long.parseLong(resultSet.getString("generableValues"));
            }

            return new Response<Classroom>()
                    .setSuccess(true)
                    .setGenerableResults(generableValues)
                    .setResultsFrom(resultsFrom)
                    .setResultsOffset(resultsOffset)
                    .setData(classrooms);
        } catch (Exception e) {
            return new Response<Classroom>().setError(e.getMessage());
        }
    }

    public static Response<Classroom> getClassroomById(Long id) {
        try {
            return getClassrooms(new Columns[]{Columns.id}, new String[]{Long.toUnsignedString(id)},
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<Classroom>().setError(e.getMessage());
        }
    }

    public static Response<Classroom> createClassroom(Classroom classroom) {
        Objects.requireNonNull(classroom);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO classroom (id, teacherId, grade, name) " +
                     "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, classroom.getId() == null ? null : Long.toUnsignedString(classroom.getId()));
            statement.setString(2, classroom.getTeacherId() == null ? null : Long.toUnsignedString(classroom.getTeacherId()));
            statement.setInt(3, classroom.getGrade());
            statement.setString(4, classroom.getName());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Classroom>().setError("Internal server error");
            }
            try (var resultSet = statement.getGeneratedKeys()) {
                if (!resultSet.next()) {
                    connection.rollback();
                    return new Response<Classroom>().setError("Internal server error");
                }
                connection.commit();
                return getClassroomById(Long.parseLong(resultSet.getString(1)));
            }
        } catch (Exception e) {
            return new Response<Classroom>().setError(e.getMessage());
        }
    }

    public static Response<Classroom> updateClassroom(Classroom classroom) {
        Objects.requireNonNull(classroom);

        var oldClassroom = getClassroomById(classroom.getId());
        if (oldClassroom.getError() != null) return oldClassroom;
        var data = oldClassroom.getData().getFirst();
        if (classroom.getTeacherId() == null) classroom.setTeacherId(data.getTeacherId());
        if (classroom.getGrade() == null) classroom.setGrade(data.getGrade());
        if (classroom.getName() == null) classroom.setName(data.getName());

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("UPDATE classroom SET teacherId = ?, grade = ?, name = ? WHERE id = ?")) {
            statement.setString(1, Long.toUnsignedString(classroom.getTeacherId()));
            statement.setInt(2, classroom.getGrade());
            statement.setString(3, classroom.getName());
            statement.setString(4, Long.toUnsignedString(classroom.getId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Classroom>().setError("Internal server error");
            }
            connection.commit();
            return getClassroomById(classroom.getId());
        } catch (Exception e) {
            return new Response<Classroom>().setError(e.getMessage());
        }
    }

    public static Response<Classroom> deleteClassroomById(Long id) {
        Objects.requireNonNull(id);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM classroom WHERE id = ?")) {
            statement.setString(1, Long.toUnsignedString(id));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Classroom>().setError("Internal server error");
            }
            connection.commit();
            return new Response<Classroom>().setSuccess(true);
        } catch (Exception e) {
            return new Response<Classroom>().setError(e.getMessage());
        }
    }
}
