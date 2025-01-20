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

package io.github.lycoriscafe.yggdrasil.configuration.commons;

import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

public class CommonService {
    public static class ResultSetHolder<T extends Entity> {
        private Response<T> response;
        private ResultSet resultSet;
        private Long generableResults;
        private Long resultsFrom;
        private Long resultsOffset;

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

        public Long getGenerableResults() {
            return generableResults;
        }

        public ResultSetHolder<T> setGenerableResults(Long generableResults) {
            this.generableResults = generableResults;
            return this;
        }

        public Long getResultsFrom() {
            return resultsFrom;
        }

        public ResultSetHolder<T> setResultsFrom(Long resultsFrom) {
            this.resultsFrom = resultsFrom;
            return this;
        }

        public Long getResultsOffset() {
            return resultsOffset;
        }

        public ResultSetHolder<T> setResultsOffset(Long resultsOffset) {
            this.resultsOffset = resultsOffset;
            return this;
        }
    }

    public static <T extends Entity,
            U extends Enum<U> & EntityColumn<T>,
            V extends EntityService<T>> ResultSetHolder<T> select(SearchQueryBuilder<T, U, V> queryBuilder) {
        Objects.requireNonNull(queryBuilder);
        if (queryBuilder.getResultsFrom() == null || queryBuilder.getResultsFrom() < 0) queryBuilder.setResultsFrom(0L);
        if (queryBuilder.getResultsOffset() == null || queryBuilder.getResultsOffset() < 0) {
            queryBuilder.setResultsOffset(YggdrasilConfig.getDefaultResultsOffset());
        }
        if (queryBuilder.getResultsFrom() > queryBuilder.getResultsOffset()) {
            return new ResultSetHolder<T>().setResponse(new Response<T>().setError("Invalid boundaries"));
        }

        StringBuilder query = new StringBuilder("SELECT * FROM " + queryBuilder.getEntity().getSimpleName().toLowerCase());
        if (queryBuilder.getSearchBy() != null) {
            if (queryBuilder.getSearchBy().size() != queryBuilder.getSearchByValues().size()) {
                return new ResultSetHolder<T>().setResponse(new Response<T>().setError("searchBy != searchByValues (length)"));
            }
            if (queryBuilder.getIsCaseSensitive() != null && queryBuilder.getSearchBy().size() != queryBuilder.getIsCaseSensitive().size()) {
                return new ResultSetHolder<T>().setResponse(new Response<T>().setError("searchBy != isCaseSensitive (length)"));
            }
            query.append(" WHERE ");
            for (int i = 0; i < queryBuilder.getSearchBy().size(); i++) {
                if (i > 0) query.append(" AND ");
                query.append(queryBuilder.getSearchBy().get(i)).append(" LIKE ");
                if (queryBuilder.getIsCaseSensitive() != null) query.append(queryBuilder.getIsCaseSensitive().get(i) ? " BINARY " : "");
                query.append("?");
            }
        }
        if (queryBuilder.getOrderBy() != null) {
            query.append(" ORDER BY ");
            for (int i = 0; i < queryBuilder.getOrderBy().size(); i++) {
                if (i > 0) query.append(", ");
                query.append(queryBuilder.getOrderBy().get(i));
            }
        }
        if (queryBuilder.getAscending() != null) {
            query.append(queryBuilder.getAscending() ? " ASC" : " DESC");
        }
        String generableResultQuery = query.toString().replaceFirst("\\*", "COUNT(1)");
        query.append(" LIMIT ?, ?");
        String resultsOffsetQuery = "SELECT COUNT(1) FROM (" + query + ") AS resultsOffset";

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString());
             var generableResultsStatement = connection.prepareStatement(generableResultQuery);
             var resultsOffsetStatement = connection.prepareStatement(resultsOffsetQuery)) {
            int statementNextParamIndex = 1;
            if (queryBuilder.getSearchByValues() != null) {
                for (int i = 1; i <= queryBuilder.getSearchByValues().size(); i++) {
                    statement.setString(i, queryBuilder.getSearchByValues().get(i - 1));
                    generableResultsStatement.setString(i, queryBuilder.getSearchByValues().get(i - 1));
                    resultsOffsetStatement.setString(i, queryBuilder.getSearchByValues().get(i - 1));
                    statementNextParamIndex++;
                }
            }
            statement.setString(statementNextParamIndex, Long.toUnsignedString(queryBuilder.getResultsFrom()));
            resultsOffsetStatement.setString(statementNextParamIndex++, Long.toUnsignedString(queryBuilder.getResultsFrom()));
            statement.setString(statementNextParamIndex, Long.toUnsignedString(queryBuilder.getResultsOffset()));
            resultsOffsetStatement.setString(statementNextParamIndex, Long.toUnsignedString(queryBuilder.getResultsOffset()));

            Long generableResults = null;
            queryBuilder.setResultsOffset(null);
            try (var generableResultsResultSet = generableResultsStatement.executeQuery();
                 var resultsOffsetResultSet = resultsOffsetStatement.executeQuery()) {
                if (generableResultsResultSet.next() && resultsOffsetResultSet.next()) {
                    generableResults = Long.parseLong(generableResultsResultSet.getString(1));
                    queryBuilder.setResultsOffset(Long.parseLong(resultsOffsetResultSet.getString(1)));
                }
            }

            connection.commit();
            return new ResultSetHolder<T>()
                    .setResultSet(statement.executeQuery())
                    .setGenerableResults(generableResults)
                    .setResultsFrom(queryBuilder.getResultsFrom())
                    .setResultsOffset(queryBuilder.getResultsOffset());
        } catch (Exception e) {
            return new ResultSetHolder<T>().setResponse(new Response<T>().setError(e.getMessage()));
        }
    }

    public static <T extends Entity,
            U extends Enum<U> & EntityColumn<T>,
            V extends EntityService<T>> Response<T> delete(SearchQueryBuilder<T, U, V> queryBuilder) {
        Objects.requireNonNull(queryBuilder);
        if (queryBuilder.getSearchBy() == null || queryBuilder.getSearchByValues() == null ||
                queryBuilder.getSearchBy().size() != queryBuilder.getSearchByValues().size()) {
            return new Response<T>().setError("Invalid search parameters");
        }

        StringBuilder query = new StringBuilder("DELETE FROM ").append(queryBuilder.getEntity().getName().toLowerCase()).append(" WHERE ");
        for (int i = 0; i < queryBuilder.getSearchBy().size(); i++) {
            if (i > 0) query.append(" AND ");
            query.append(queryBuilder.getSearchBy().get(i)).append(" LIKE ");
            if (queryBuilder.getIsCaseSensitive() != null) query.append(queryBuilder.getIsCaseSensitive().get(i) ? " BINARY " : "");
            query.append("?");
        }
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < queryBuilder.getSearchByValues().size(); i++) {
                statement.setString(i + 1, queryBuilder.getSearchByValues().get(i));
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

    @SuppressWarnings("unchecked")
    public static <T extends Entity,
            U extends Enum<U> & EntityColumn<T>,
            V extends EntityService<T>> Response<T> insert(UpdateQueryBuilder<T, U, V> queryBuilder) {
        Objects.requireNonNull(queryBuilder);
        if (queryBuilder.getColumns() == null || queryBuilder.getValues() == null ||
                queryBuilder.getColumns().size() != queryBuilder.getValues().size()) {
            return new Response<T>().setError("Invalid insert parameters");
        }

        StringBuilder query = new StringBuilder("INSERT INTO ").append(queryBuilder.getEntity().getName().toLowerCase()).append(" (");
        for (int i = 0; i < queryBuilder.getColumns().size(); i++) {
            if (i > 0) query.append(", ");
            query.append(queryBuilder.getColumns().get(i));
        }
        query.append(") ").append("VALUES").append(" (");
        for (int i = 0; i < queryBuilder.getValues().size(); i++) {
            if (i > 0) query.append(", ");
            query.append("?");
        }
        query.append(")");
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 1; i <= queryBuilder.getValues().size(); i++) {
                statement.setString(i, queryBuilder.getValues().get(i - 1));
            }
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<T>().setError("Internal server error");
            }
            connection.commit();
            try (var resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    var searchQuery = new SearchQueryBuilder<>(queryBuilder.getEntity(), queryBuilder.getEntityColumns(), queryBuilder.getEntityService())
                            .setSearchBy(List.of(Enum.valueOf(queryBuilder.getEntityColumns(), "id")))
                            .setSearchByValues(List.of(resultSet.getString(1)));
                    Class<? extends EntityService<T>> serviceClass = queryBuilder.getEntityService();
                    return (Response<T>) serviceClass.getDeclaredMethod("select", SearchQueryBuilder.class).invoke(null, searchQuery);
                }
            }
            return new Response<T>().setSuccess(true);
        } catch (Exception e) {
            return new Response<T>().setError(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity,
            U extends Enum<U> & EntityColumn<T>,
            V extends EntityService<T>> Response<T> update(UpdateQueryBuilder<T, U, V> queryBuilder) {
        Objects.requireNonNull(queryBuilder);
        if (queryBuilder.getColumns() == null || queryBuilder.getValues() == null ||
                queryBuilder.getColumns().size() != queryBuilder.getValues().size()) {
            return new Response<T>().setError("Invalid update parameters");
        }
        if (queryBuilder.getSearchBy() == null || queryBuilder.getSearchByValues() == null ||
                queryBuilder.getSearchBy().size() != queryBuilder.getSearchByValues().size()) {
            return new Response<T>().setError("Invalid search parameters");
        }

        StringBuilder query = new StringBuilder("UPDATE ").append(queryBuilder.getEntity().getName().toLowerCase()).append(" SET ");
        for (int i = 0; i < queryBuilder.getColumns().size(); i++) {
            if (i > 0) query.append(", ");
            query.append(queryBuilder.getColumns().get(i)).append(" = ").append("?");
        }
        query.append(" WHERE ");
        for (int i = 0; i < queryBuilder.getValues().size(); i++) {
            if (i > 0) query.append(" AND ");
            query.append(queryBuilder.getSearchBy().get(i)).append(" = ").append("?");
        }
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString())) {
            int nextParamIndex = 1;
            for (int i = 1; i <= queryBuilder.getValues().size(); i++) {
                statement.setString(i, queryBuilder.getValues().get(i - 1));
                nextParamIndex++;
            }
            for (int i = 0; i < queryBuilder.getSearchByValues().size(); i++) {
                statement.setString(nextParamIndex++, queryBuilder.getSearchByValues().get(i));
            }
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<T>().setError("Internal server error");
            }
            connection.commit();
            var searchQuery = new SearchQueryBuilder<>(queryBuilder.getEntity(), queryBuilder.getEntityColumns(), queryBuilder.getEntityService())
                    .setSearchBy(List.of(Enum.valueOf(queryBuilder.getEntityColumns(), "id")))
                    .setSearchByValues(List.of(queryBuilder.getSearchByValues().get(queryBuilder.getSearchByValues().indexOf("id"))));
            Class<? extends EntityService<T>> serviceClass = queryBuilder.getEntityService();
            return (Response<T>) serviceClass.getDeclaredMethod("select", SearchQueryBuilder.class).invoke(null, searchQuery);
        } catch (Exception e) {
            return new Response<T>().setError(e.getMessage());
        }
    }
}
