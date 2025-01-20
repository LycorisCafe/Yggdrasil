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
import io.github.lycoriscafe.yggdrasil.configuration.commons.CommonService;
import io.github.lycoriscafe.yggdrasil.configuration.commons.EntityColumn;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubjectService {
    public enum Columns implements EntityColumn {
        id,
        grade,
        shortName,
        longGame,
        teacherId
    }

    public static Response<Subject> getSubjects(List<Columns> searchBy,
                                                List<String> searchByValues,
                                                List<Boolean> isCaseSensitive,
                                                List<Columns> orderBy,
                                                Boolean isAscending,
                                                Long resultsFrom,
                                                Long resultsOffset) {
        try {
            var results = CommonService.select(new CommonService.SearchQueryBuilder<Subject, Columns>(Subject.class)
                    .setSearchBy(searchBy).setSearchByValues(searchByValues).setIsCaseSensitive(isCaseSensitive).setOrderBy(orderBy)
                    .setAscending(isAscending).setResultsFrom(resultsFrom).setResultsOffset(resultsOffset));
            if (results.getResponse() != null) return results.getResponse();

            List<Subject> subjects = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    subjects.add(new Subject(
                            resultSet.getInt("grade"),
                            resultSet.getString("shortName")
                    ).setId(Long.parseLong(resultSet.getString("id")))
                            .setLongName(resultSet.getString("longName"))
                            .setTeacherId(resultSet.getString("teacherId") == null ?
                                    null : Long.parseLong(resultSet.getString("teacherId"))));
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

    public static Response<Subject> getSubjectById(Long id) {
        try {
            return getSubjects(List.of(Columns.id), List.of(Long.toUnsignedString(id)),
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
        return CommonService.delete(new CommonService.SearchQueryBuilder<Subject, Columns>(Subject.class).setSearchBy(List.of(Columns.id))
                .setSearchByValues(List.of(Long.toUnsignedString(id))));
    }
}
