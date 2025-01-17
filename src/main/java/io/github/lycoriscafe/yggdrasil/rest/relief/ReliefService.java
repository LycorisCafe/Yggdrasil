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

package io.github.lycoriscafe.yggdrasil.rest.relief;

import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;

import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReliefService {
    public enum Columns {
        id,
        timetableId,
        teacherId,
        date
    }

    public static Response<Relief> getReliefs(Columns[] searchBy,
                                              String[] searchByValues,
                                              boolean[] isCaseSensitive,
                                              Columns[] orderBy,
                                              Boolean isAscending,
                                              Long resultsFrom,
                                              Long resultsOffset) {
        if (resultsFrom == null || resultsFrom < 0) resultsFrom = 0L;
        if (resultsOffset == null || resultsOffset < 0) resultsOffset = YggdrasilConfig.getDefaultResultsOffset();
        if (resultsFrom > resultsOffset) return new Response<Relief>().setError("Invalid boundaries");

        StringBuilder query = new StringBuilder("SELECT * FROM relief");
        if (searchBy != null) {
            if (searchBy.length != searchByValues.length) return new Response<Relief>().setError("searchBy != searchByValues (length)");
            if (isCaseSensitive != null && searchBy.length != isCaseSensitive.length) {
                return new Response<Relief>().setError("searchBy != isCaseSensitive (length)");
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
            List<Relief> reliefs = new ArrayList<>();
            try (var resultSet = statement.executeQuery()) {
                connection.commit();
                while (resultSet.next()) {
                    reliefs.add(new Relief(
                            Long.parseLong(resultSet.getString("timetableId")),
                            Long.parseLong(resultSet.getString("teacherId")),
                            LocalDate.parse(resultSet.getString("date"))
                    ).setId(Long.parseLong(resultSet.getString("id"))));
                }
                generableValues = Long.parseLong(resultSet.getString("generableValues"));
            }

            return new Response<Relief>()
                    .setSuccess(true)
                    .setGenerableResults(generableValues)
                    .setResultsFrom(resultsFrom)
                    .setResultsOffset(resultsOffset)
                    .setData(reliefs);
        } catch (Exception e) {
            return new Response<Relief>().setError(e.getMessage());
        }
    }

    public static Response<Relief> getReliefById(Long id) {
        try {
            return getReliefs(new Columns[]{Columns.id}, new String[]{Long.toUnsignedString(id)},
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<Relief>().setError(e.getMessage());
        }
    }

    public static Response<Relief> createClassroom(Relief relief) {
        Objects.requireNonNull(relief);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO relief (id, timetableId, teacherId, date) " +
                     "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, relief.getId() == null ? null : Long.toUnsignedString(relief.getId()));
            statement.setString(2, Long.toUnsignedString(relief.getTimetableId()));
            statement.setString(3, Long.toUnsignedString(relief.getTeacherId()));
            statement.setString(4, relief.getDate().toString());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Relief>().setError("Internal server error");
            }
            try (var resultSet = statement.getGeneratedKeys()) {
                if (!resultSet.next()) {
                    connection.rollback();
                    return new Response<Relief>().setError("Internal server error");
                }
                connection.commit();
                return getReliefById(Long.parseLong(resultSet.getString(1)));
            }
        } catch (Exception e) {
            return new Response<Relief>().setError(e.getMessage());
        }
    }

    public static Response<Relief> updateRelief(Relief relief) {
        Objects.requireNonNull(relief);

        var oldRelief = getReliefById(relief.getId());
        if (oldRelief.getError() != null) return oldRelief;
        var data = oldRelief.getData().getFirst();
        if (relief.getTimetableId() == null) relief.setTimetableId(data.getTimetableId());
        if (relief.getTeacherId() == null) relief.setTeacherId(data.getTeacherId());
        if (relief.getDate() == null) relief.setDate(data.getDate());

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("UPDATE relief SET timetableId = ?, teacherId = ?, date = ? WHERE id = ?")) {
            statement.setString(1, Long.toUnsignedString(relief.getTimetableId()));
            statement.setString(2, Long.toUnsignedString(relief.getTeacherId()));
            statement.setString(3, relief.getDate().toString());
            statement.setString(4, Long.toUnsignedString(relief.getId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Relief>().setError("Internal server error");
            }
            connection.commit();
            return getReliefById(relief.getId());
        } catch (Exception e) {
            return new Response<Relief>().setError(e.getMessage());
        }
    }

    public static Response<Relief> deleteReliefById(Long id) {
        Objects.requireNonNull(id);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM relief WHERE id = ?")) {
            statement.setString(1, Long.toUnsignedString(id));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Relief>().setError("Internal server error");
            }
            connection.commit();
            return new Response<Relief>().setSuccess(true);
        } catch (Exception e) {
            return new Response<Relief>().setError(e.getMessage());
        }
    }
}
