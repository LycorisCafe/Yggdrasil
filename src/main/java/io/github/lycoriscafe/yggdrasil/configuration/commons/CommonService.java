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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommonService {
    public static class SearchQueryBuilder<T extends Entity, U extends Enum<U> & EntityColumn> {
        private Class<T> entity;
        private List<U> searchBy;
        private List<String> searchByValues;
        private List<Boolean> isCaseSensitive;
        private List<U> orderBy;
        private Boolean isAscending;
        private Long resultsFrom;
        private Long resultsOffset;

        public SearchQueryBuilder(Class<T> entity) {
            this.entity = Objects.requireNonNull(entity);
        }

        public Class<T> getEntity() {
            return entity;
        }

        public SearchQueryBuilder<T, U> setEntity(Class<T> entity) {
            this.entity = entity;
            return this;
        }

        public List<U> getSearchBy() {
            return searchBy;
        }

        public SearchQueryBuilder<T, U> setSearchBy(List<U> searchBy) {
            this.searchBy = searchBy;
            return this;
        }

        public List<String> getSearchByValues() {
            return searchByValues;
        }

        public SearchQueryBuilder<T, U> setSearchByValues(List<String> searchByValues) {
            this.searchByValues = searchByValues;
            return this;
        }

        public List<Boolean> getIsCaseSensitive() {
            return isCaseSensitive;
        }

        public SearchQueryBuilder<T, U> setIsCaseSensitive(List<Boolean> isCaseSensitive) {
            this.isCaseSensitive = isCaseSensitive;
            return this;
        }

        public List<U> getOrderBy() {
            return orderBy;
        }

        public SearchQueryBuilder<T, U> setOrderBy(List<U> orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public Boolean getAscending() {
            return isAscending;
        }

        public SearchQueryBuilder<T, U> setAscending(Boolean ascending) {
            isAscending = ascending;
            return this;
        }

        public Long getResultsFrom() {
            return resultsFrom;
        }

        public SearchQueryBuilder<T, U> setResultsFrom(Long resultsFrom) {
            this.resultsFrom = resultsFrom;
            return this;
        }

        public Long getResultsOffset() {
            return resultsOffset;
        }

        public SearchQueryBuilder<T, U> setResultsOffset(Long resultsOffset) {
            this.resultsOffset = resultsOffset;
            return this;
        }

        public static <T extends Entity, U extends Enum<U> & EntityColumn> SearchQueryBuilder<T, U> build(Class<T> entity,
                                                                                                          Class<U> columns,
                                                                                                          Map<String, String> parameters) {
            var searchQuery = new SearchQueryBuilder<T, U>(entity);
            if (parameters == null) return searchQuery;

            List<U> searchBy = new ArrayList<>();
            List<String> searchByValues = new ArrayList<>();
            List<Boolean> isCaseSensitive = new ArrayList<>();
            for (String key : parameters.keySet()) {
                try {
                    U col = Enum.valueOf(columns, key);
                    searchBy.add(col);
                    String[] values = parameters.get(key).split(",", 0);
                    searchByValues.add(values[0]);
                    isCaseSensitive.add(values.length == 2 && Boolean.parseBoolean(values[1]));
                } catch (Exception ignored) {}
            }
            searchQuery.setSearchBy(searchBy).setSearchByValues(searchByValues).setIsCaseSensitive(isCaseSensitive);
            List<U> orderBy = null;
            if (parameters.containsKey("orderBy")) {
                orderBy = new ArrayList<>();
                String[] values = parameters.get("orderBy").split(",", 0);
                for (String value : values) {
                    U col = Enum.valueOf(columns, value);
                    orderBy.add(col);
                }
            }
            searchQuery.setOrderBy(orderBy);
            if (parameters.containsKey("isAscending")) {
                searchQuery.setAscending(Boolean.parseBoolean(parameters.get("isAscending")));
            }
            if (parameters.containsKey("resultsFrom")) {
                searchQuery.setResultsFrom(Long.parseLong(parameters.get("resultsFrom")));
            }
            if (parameters.containsKey("resultsOffset")) {
                searchQuery.setResultsOffset(Long.parseLong(parameters.get("resultsOffset")));
            }
            return searchQuery;
        }
    }

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

    public static <T extends Entity, U extends Enum<U> & EntityColumn> ResultSetHolder<T> get(SearchQueryBuilder<T, U> queryBuilder) {
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
        String subQuery = query.toString().replaceFirst("\\*", "COUNT(*) AS resultsOffset, (" +
                query.toString().replaceFirst("\\*", "COUNT(*)") + ") AS generableValues");
        String limit = " LIMIT ?, ?";
        query.append(limit);
        subQuery += limit;

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString());
             var subStatement = connection.prepareStatement(subQuery)) {
            int statementNextParamIndex = 1;
            int subStatementNextParamIndex = 1;
            if (queryBuilder.getSearchByValues() != null) {
                subStatementNextParamIndex = queryBuilder.getSearchByValues().size() + 1;
                for (int i = 1; i <= queryBuilder.getSearchByValues().size(); i++) {
                    statement.setString(i, queryBuilder.getSearchByValues().get(i - 1));
                    subStatement.setString(i, queryBuilder.getSearchByValues().get(i - 1));
                    subStatement.setString(i + queryBuilder.getSearchByValues().size(), queryBuilder.getSearchByValues().get(i - 1));
                    statementNextParamIndex++;
                    subStatementNextParamIndex++;
                }
            }
            statement.setString(statementNextParamIndex++, Long.toUnsignedString(queryBuilder.getResultsFrom()));
            statement.setString(statementNextParamIndex, Long.toUnsignedString(queryBuilder.getResultsOffset()));

            subStatement.setString(subStatementNextParamIndex++, Long.toUnsignedString(queryBuilder.getResultsFrom()));
            subStatement.setString(subStatementNextParamIndex, Long.toUnsignedString(queryBuilder.getResultsOffset()));

            Long generableResults = null;
            queryBuilder.setResultsOffset(null);
            try (var resultSet = subStatement.executeQuery()) {
                if (resultSet.next()) {
                    generableResults = Long.parseLong(resultSet.getString("generableValues"));
                    queryBuilder.setResultsOffset(Long.parseLong(resultSet.getString("resultsOffset")));
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

    public static <T extends Entity, U extends Enum<U> & EntityColumn> Response<T> delete(SearchQueryBuilder<T, U> queryBuilder) {
        Objects.requireNonNull(queryBuilder);
        Objects.requireNonNull(queryBuilder.getSearchBy());
        Objects.requireNonNull(queryBuilder.getSearchByValues());
        StringBuilder query = new StringBuilder("DELETE FROM " + queryBuilder.getEntity().getName().toLowerCase() + " WHERE ");
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
}
