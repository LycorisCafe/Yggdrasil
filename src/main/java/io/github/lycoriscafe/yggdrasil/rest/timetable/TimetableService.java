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

package io.github.lycoriscafe.yggdrasil.rest.timetable;

import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;

import java.sql.Statement;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TimetableService {
    public enum Columns {
        id,
        teacherId,
        subjectId,
        classroomId,
        day,
        timeslot
    }

    public static Response<Timetable> getTimetables(Columns[] searchBy,
                                                    String[] searchByValues,
                                                    boolean[] isCaseSensitive,
                                                    Columns[] orderBy,
                                                    Boolean isAscending,
                                                    Long resultsFrom,
                                                    Long resultsOffset) {
        if (resultsFrom == null || resultsFrom < 0) resultsFrom = 0L;
        if (resultsOffset == null || resultsOffset < 0) resultsOffset = YggdrasilConfig.getDefaultResultsOffset();
        if (resultsFrom > resultsOffset) return new Response<Timetable>().setError("Invalid boundaries");

        StringBuilder query = new StringBuilder("SELECT * FROM timetable");
        if (searchBy != null) {
            if (searchBy.length != searchByValues.length) return new Response<Timetable>().setError("searchBy != searchByValues (length)");
            if (isCaseSensitive != null && searchBy.length != isCaseSensitive.length) {
                return new Response<Timetable>().setError("searchBy != isCaseSensitive (length)");
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
            List<Timetable> timetables = new ArrayList<>();
            try (var resultSet = statement.executeQuery()) {
                connection.commit();
                while (resultSet.next()) {
                    if (generableValues == null) generableValues = Long.parseLong(resultSet.getString("generableValues"));
                    timetables.add(new Timetable(
                            Long.parseLong(resultSet.getString("teacherId")),
                            Long.parseLong(resultSet.getString("subjectId")),
                            Long.parseLong(resultSet.getString("classroomId")),
                            DayOfWeek.of(resultSet.getInt("day")),
                            resultSet.getInt("timeslot")
                    ).setId(Long.parseLong(resultSet.getString("id"))));
                }
            }

            return new Response<Timetable>()
                    .setSuccess(true)
                    .setGenerableResults(generableValues)
                    .setResultsFrom(resultsFrom)
                    .setResultsOffset(resultsOffset)
                    .setData(timetables);
        } catch (Exception e) {
            return new Response<Timetable>().setError(e.getMessage());
        }
    }

    public static Response<Timetable> getTimetableById(Long id) {
        try {
            return getTimetables(new Columns[]{Columns.id}, new String[]{Long.toUnsignedString(id)},
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<Timetable>().setError(e.getMessage());
        }
    }

    public static Response<Timetable> createTimetable(Timetable timetable) {
        Objects.requireNonNull(timetable);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO timetable (id, teacherId, subjectId, classroomId, day, timeslot) " +
                     "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, Long.toUnsignedString(timetable.getId()));
            statement.setString(2, Long.toUnsignedString(timetable.getTeacherId()));
            statement.setString(3, Long.toUnsignedString(timetable.getSubjectId()));
            statement.setString(4, Long.toUnsignedString(timetable.getClassroomId()));
            statement.setInt(5, timetable.getDay().getValue());
            statement.setInt(6, timetable.getTimeslot());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Timetable>().setError("Internal server error");
            }
            try (var resultSet = statement.getGeneratedKeys()) {
                if (!resultSet.next()) {
                    connection.rollback();
                    return new Response<Timetable>().setError("Internal server error");
                }
                connection.commit();
                return getTimetableById(Long.parseLong(resultSet.getString(1)));
            }
        } catch (Exception e) {
            return new Response<Timetable>().setError(e.getMessage());
        }
    }

    public static Response<Timetable> updateTimetable(Timetable timetable) {
        Objects.requireNonNull(timetable);

        var oldTimetable = getTimetableById(timetable.getId());
        if (oldTimetable.getError() != null) return oldTimetable;
        var data = oldTimetable.getData().getFirst();
        if (timetable.getTeacherId() == null) timetable.setTeacherId(data.getTeacherId());
        if (timetable.getSubjectId() == null) timetable.setSubjectId(data.getSubjectId());
        if (timetable.getClassroomId() == null) timetable.setClassroomId(data.getClassroomId());
        if (timetable.getDay() == null) timetable.setDay(data.getDay());
        if (timetable.getTimeslot() == null) timetable.setTimeslot(data.getTimeslot());

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("UPDATE timetable SET teacherId = ?, subjectId = ?, classroomId = ?, day = ?, " +
                     "timeslot = ? WHERE id = ?")) {
            statement.setString(1, Long.toUnsignedString(timetable.getTeacherId()));
            statement.setString(2, Long.toUnsignedString(timetable.getSubjectId()));
            statement.setString(3, Long.toUnsignedString(timetable.getClassroomId()));
            statement.setInt(4, timetable.getDay().getValue());
            statement.setInt(5, timetable.getTimeslot());
            statement.setString(6, Long.toUnsignedString(timetable.getId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Timetable>().setError("Internal server error");
            }
            connection.commit();
            return getTimetableById(timetable.getId());
        } catch (Exception e) {
            return new Response<Timetable>().setError(e.getMessage());
        }
    }

    public static Response<Timetable> deleteTimetableById(Long id) {
        Objects.requireNonNull(id);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM timetable WHERE id = ?")) {
            statement.setString(1, Long.toUnsignedString(id));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Timetable>().setError("Internal server error");
            }
            connection.commit();
            return new Response<Timetable>().setSuccess(true);
        } catch (Exception e) {
            return new Response<Timetable>().setError(e.getMessage());
        }
    }
}
