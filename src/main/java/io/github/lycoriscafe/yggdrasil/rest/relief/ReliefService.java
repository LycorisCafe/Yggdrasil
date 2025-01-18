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
import io.github.lycoriscafe.yggdrasil.configuration.database.CommonCRUD;
import io.github.lycoriscafe.yggdrasil.configuration.database.EntityColumn;

import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReliefService {
    public enum Columns implements EntityColumn {
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
        try {
            var results = CommonCRUD.get(Relief.class, searchBy, searchByValues, isCaseSensitive, orderBy, isAscending, resultsFrom, resultsOffset);
            if (results.getResponse() != null) return results.getResponse();

            List<Relief> reliefs = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    reliefs.add(new Relief(
                            Long.parseLong(resultSet.getString("timetableId")),
                            Long.parseLong(resultSet.getString("teacherId")),
                            LocalDate.parse(resultSet.getString("date"), Utils.getDateFormatter())
                    ).setId(Long.parseLong(resultSet.getString("id"))));
                }
            }

            return new Response<Relief>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
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
            statement.setString(4, relief.getDate().format(Utils.getDateFormatter()));
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
            statement.setString(3, relief.getDate().format(Utils.getDateFormatter()));
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
        return CommonCRUD.delete(Relief.class, new Columns[]{Columns.id}, new String[]{Long.toUnsignedString(id)}, null);
    }
}
