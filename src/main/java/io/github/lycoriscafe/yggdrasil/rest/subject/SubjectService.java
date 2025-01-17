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

import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubjectService {
    public enum Columns {
        id,
        grade,
        shortName,
        longGame,
        teacherId
    }

    public static Response<Subject> getSubjects(Columns[] searchBy,
                                                String[] searchByValues,
                                                boolean[] isCaseSensitive,
                                                Columns[] orderBy,
                                                Boolean isAscending,
                                                Long resultsFrom,
                                                Long resultsOffset) {
        if (resultsFrom == null || resultsFrom < 0) resultsFrom = 0L;
        if (resultsOffset == null || resultsOffset < 0) resultsOffset = YggdrasilConfig.getDefaultResultsOffset();
        if (resultsFrom > resultsOffset) return new Response<Subject>().setError("Invalid boundaries");

        StringBuilder query = new StringBuilder("SELECT * FROM subject");
        if (searchBy != null) {
            if (searchBy.length != searchByValues.length) return new Response<Subject>().setError("searchBy != searchByValues (length)");
            if (isCaseSensitive != null && searchBy.length != isCaseSensitive.length) {
                return new Response<Subject>().setError("searchBy != isCaseSensitive (length)");
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

            try (var resultSet = statement.executeQuery()) {
                connection.commit();
                List<Subject> subjects = new ArrayList<>();
                while (resultSet.next()) {
                    subjects.add(new Subject(
                            resultSet.getInt("grade"),
                            resultSet.getString("shortName")
                    ).setId(Long.parseLong(resultSet.getString("id")))
                            .setLongName(resultSet.getString("longName"))
                            .setTeacherId(Long.parseLong(resultSet.getString("teacherId"))));
                }

                return new Response<Subject>()
                        .setSuccess(true)
                        .setGenerableResults(Long.parseLong(resultSet.getString("generableValues")))
                        .setResultsFrom(resultsFrom)
                        .setResultsOffset(resultsOffset)
                        .setData(subjects);
            }
        } catch (Exception e) {
            return new Response<Subject>().setError(e.getMessage());
        }
    }

    public static Response<Subject> getSubjectById(Long id) {
        try {
            return getSubjects(new Columns[]{Columns.id}, new String[]{Long.toUnsignedString(id)},
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<Subject>().setError(e.getMessage());
        }
    }

    public static Response<Subject> createSubject(Subject subject) {
        Objects.requireNonNull(subject);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO subject (id, grade, shortName, longName, teacherId) " +
                     "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, subject.getId() == null ? null : Long.toUnsignedString(subject.getId()));
            statement.setInt(2, subject.getGrade());
            statement.setString(3, subject.getShortName());
            statement.setString(4, subject.getLongName());
            statement.setString(5, Long.toUnsignedString(subject.getTeacherId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Subject>().setError("Internal server error");
            }
            try (var resultSet = statement.getGeneratedKeys()) {
                if (!resultSet.next()) {
                    connection.rollback();
                    return new Response<Subject>().setError("Internal server error");
                }
                connection.commit();
                return getSubjectById(Long.parseLong(resultSet.getString(1)));
            }
        } catch (Exception e) {
            return new Response<Subject>().setError(e.getMessage());
        }
    }

    public static Response<Subject> updateSubject(Subject subject) {
        Objects.requireNonNull(subject);

        var oldSubject = getSubjectById(subject.getId());
        if (oldSubject.getError() != null) return oldSubject;
        var data = oldSubject.getData().getFirst();
        if (subject.getGrade() == null) subject.setGrade(data.getGrade());
        if (subject.getShortName() == null) subject.setShortName(data.getShortName());
        if (subject.getLongName() == null) subject.setLongName(data.getLongName());
        if (subject.getTeacherId() == null) subject.setTeacherId(data.getTeacherId());

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("UPDATE subject SET grade = ?, shortName = ?, longName = ?, teacherId = ? WHERE id = ?")) {
            statement.setInt(1, subject.getGrade());
            statement.setString(2, subject.getShortName());
            statement.setString(3, subject.getLongName());
            statement.setString(4, Long.toUnsignedString(subject.getTeacherId()));
            statement.setString(5, Long.toUnsignedString(subject.getId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Subject>().setError("Internal server error");
            }
            connection.commit();
            return getSubjectById(subject.getId());
        } catch (Exception e) {
            return new Response<Subject>().setError(e.getMessage());
        }
    }

    public static Response<Subject> deleteSubjectById(Long id) {
        Objects.requireNonNull(id);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM subject WHERE id = ?")) {
            statement.setString(1, Long.toUnsignedString(id));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Subject>().setError("Internal server error");
            }
            connection.commit();
            return new Response<Subject>().setSuccess(true);
        } catch (Exception e) {
            return new Response<Subject>().setError(e.getMessage());
        }
    }
}
