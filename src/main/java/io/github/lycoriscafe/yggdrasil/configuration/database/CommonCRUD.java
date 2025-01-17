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

package io.github.lycoriscafe.yggdrasil.configuration.database;

import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;

import java.sql.ResultSet;
import java.util.Objects;

public class CommonCRUD {
    public static class ResultSetHolder<T extends Entity> {
        private Response<T> response;
        private ResultSet resultSet;

        public Response<T> getResponse() {
            return response;
        }

        public ResultSetHolder<T> setResponse(Response<T> response) {
            this.response = response;
            return this;
        }

        public ResultSet getResultSet() {
            return resultSet;
        }

        public ResultSetHolder<T> setResultSet(ResultSet resultSet) {
            this.resultSet = resultSet;
            return this;
        }
    }

    public static <T extends Entity, U extends EntityColumn> ResultSetHolder<T> get(Class<T> entity,
                                                                                    U[] searchBy,
                                                                                    String[] searchByValues,
                                                                                    boolean[] isCaseSensitive,
                                                                                    U[] orderBy,
                                                                                    Boolean isAscending,
                                                                                    Long resultsFrom,
                                                                                    Long resultsOffset) {
        if (resultsFrom == null || resultsFrom < 0) resultsFrom = 0L;
        if (resultsOffset == null || resultsOffset < 0) resultsOffset = YggdrasilConfig.getDefaultResultsOffset();
        if (resultsFrom > resultsOffset) return new ResultSetHolder<T>().setResponse(new Response<T>().setError("Invalid boundaries"));

        StringBuilder query = new StringBuilder("SELECT * FROM " + entity.getName().toLowerCase());
        if (searchBy != null) {
            if (searchBy.length != searchByValues.length) {
                return new ResultSetHolder<T>().setResponse(new Response<T>().setError("searchBy != searchByValues (length)"));
            }
            if (isCaseSensitive != null && searchBy.length != isCaseSensitive.length) {
                return new ResultSetHolder<T>().setResponse(new Response<T>().setError("searchBy != isCaseSensitive (length)"));
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
        query.append(" LIMIT ").append("?").append(", ").append("?");

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString())) {
            int nextParamIndex = 1;
            if (searchByValues != null) {
                for (int i = 0; i < searchByValues.length; i++) {
                    statement.setString(i + 1, searchByValues[i]);
                    statement.setString(i + searchByValues.length + 1, searchByValues[i]);
                    nextParamIndex = i + searchByValues.length + 2;
                }
            }
            statement.setString(nextParamIndex++, Long.toUnsignedString(resultsFrom));
            statement.setString(nextParamIndex, Long.toUnsignedString(resultsOffset));

            try (var resultSet = statement.executeQuery()) {
                connection.commit();
                return new ResultSetHolder<T>().setResultSet(resultSet);
            }
        } catch (Exception e) {
            return new ResultSetHolder<T>().setResponse(new Response<T>().setError(e.getMessage()));
        }
    }

    public static <T extends Entity, U extends EntityColumn> Response<T> delete(Class<T> entity,
                                                                                U[] searchBy,
                                                                                String[] searchByValues,
                                                                                boolean[] isCaseSensitive) {
        Objects.requireNonNull(searchBy);
        Objects.requireNonNull(searchByValues);
        StringBuilder query = new StringBuilder("DELETE FROM " + entity.getName().toLowerCase() + " WHERE ");
        for (int i = 0; i < searchBy.length; i++) {
            if (i > 0) query.append(" AND ");
            query.append(searchBy[i]).append(" LIKE ");
            if (isCaseSensitive != null) query.append(isCaseSensitive[i] ? " BINARY " : "");
            query.append("?");
        }
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < searchBy.length; i++) {
                statement.setString(i + 1, searchByValues[i]);
            }
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<T>().setError("Internal server error");
            }
            connection.commit();
            return new Response<T>().setSuccess(true);
        } catch (Exception e) {
            return new Response<T>().setError(e.getMessage());
        }
    }
}
