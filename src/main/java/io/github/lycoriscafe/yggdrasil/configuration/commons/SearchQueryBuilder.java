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

import java.util.*;

public class SearchQueryBuilder<T extends Entity, U extends Enum<U> & EntityColumn<T>, V extends EntityService<T>> {
    private Class<T> entity;
    private Class<U> entityColumns;
    private Class<V> entityService;
    private List<U> searchBy;
    private List<String> searchByValues;
    private List<Boolean> isCaseSensitive;
    private List<U> orderBy;
    private Boolean isAscending;
    private Long resultsFrom;
    private Long resultsOffset;

    public SearchQueryBuilder(Class<T> entity,
                              Class<U> entityColumns,
                              Class<V> entityService) {
        this.entity = Objects.requireNonNull(entity);
        this.entityColumns = Objects.requireNonNull(entityColumns);
        this.entityService = Objects.requireNonNull(entityService);
    }

    public Class<T> getEntity() {
        return entity;
    }

    public SearchQueryBuilder<T, U, V> setEntity(Class<T> entity) {
        this.entity = entity;
        return this;
    }

    public Class<U> getEntityColumns() {
        return entityColumns;
    }

    public SearchQueryBuilder<T, U, V> setEntityColumns(Class<U> entityColumns) {
        this.entityColumns = entityColumns;
        return this;
    }

    public Class<V> getEntityService() {
        return entityService;
    }

    public SearchQueryBuilder<T, U, V> setEntityService(Class<V> entityService) {
        this.entityService = entityService;
        return this;
    }

    public List<U> getSearchBy() {
        return searchBy;
    }

    public SearchQueryBuilder<T, U, V> setSearchBy(List<U> searchBy) {
        this.searchBy = searchBy;
        return this;
    }

    public List<String> getSearchByValues() {
        return searchByValues;
    }

    public SearchQueryBuilder<T, U, V> setSearchByValues(List<String> searchByValues) {
        this.searchByValues = searchByValues;
        return this;
    }

    public List<Boolean> getIsCaseSensitive() {
        return isCaseSensitive;
    }

    public SearchQueryBuilder<T, U, V> setIsCaseSensitive(List<Boolean> isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
        return this;
    }

    public List<U> getOrderBy() {
        return orderBy;
    }

    public SearchQueryBuilder<T, U, V> setOrderBy(List<U> orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public Boolean getAscending() {
        return isAscending;
    }

    public SearchQueryBuilder<T, U, V> setAscending(Boolean ascending) {
        isAscending = ascending;
        return this;
    }

    public Long getResultsFrom() {
        return resultsFrom;
    }

    public SearchQueryBuilder<T, U, V> setResultsFrom(Long resultsFrom) {
        this.resultsFrom = resultsFrom;
        return this;
    }

    public Long getResultsOffset() {
        return resultsOffset;
    }

    public SearchQueryBuilder<T, U, V> setResultsOffset(Long resultsOffset) {
        this.resultsOffset = resultsOffset;
        return this;
    }

    public static <T extends Entity,
            U extends Enum<U> & EntityColumn<T>,
            V extends EntityService<T>> SearchQueryBuilder<T, U, V> build(Class<T> entity,
                                                                          Class<U> entityColumns,
                                                                          Class<V> entityService,
                                                                          Map<String, String> parameters) {
        var searchQuery = new SearchQueryBuilder<>(entity, entityColumns, entityService);
        if (parameters == null) return searchQuery;

        List<U> searchBy = new ArrayList<>();
        List<String> searchByValues = new ArrayList<>();
        List<Boolean> isCaseSensitive = new ArrayList<>();
        for (String key : parameters.keySet()) {
            var col = Arrays.stream(entityColumns.getEnumConstants()).filter(e -> e.name().equalsIgnoreCase(key)).findAny().orElse(null);
            if (col == null) continue;
            searchBy.add(col);
            String[] values = parameters.get(key).split(",", 0);
            searchByValues.add(values[0]);
            isCaseSensitive.add(values.length == 2 && Boolean.parseBoolean(values[1]));
        }
        searchQuery.setSearchBy(searchBy.isEmpty() ? null : searchBy)
                .setSearchByValues(searchByValues.isEmpty() ? null : searchByValues)
                .setIsCaseSensitive(isCaseSensitive.isEmpty() ? null : isCaseSensitive);

        List<U> orderBy = new ArrayList<>();
        parameters.keySet().stream().filter(p -> p.equalsIgnoreCase("orderBy")).findAny()
                .ifPresent(key -> Arrays.stream(parameters.get(key).split(",", 0))
                        .forEach(value -> Arrays.stream(entityColumns.getEnumConstants()).filter(e -> e.name().equalsIgnoreCase(value))
                                .findAny().ifPresent(orderBy::add)));
        searchQuery.setOrderBy(orderBy.isEmpty() ? null : orderBy);

        parameters.keySet().stream().filter(p -> p.equalsIgnoreCase("isAscending")).findAny()
                .ifPresent(string -> searchQuery.setAscending(Boolean.parseBoolean(parameters.get(string))));
        parameters.keySet().stream().filter(p -> p.equalsIgnoreCase("resultsFrom")).findAny()
                .ifPresent(string -> searchQuery.setResultsFrom(Long.parseLong(parameters.get(string))));
        parameters.keySet().stream().filter(p -> p.equalsIgnoreCase("resultsOffset")).findAny()
                .ifPresent(string -> searchQuery.setResultsOffset(Long.parseLong(parameters.get(string))));
        return searchQuery;
    }
}
