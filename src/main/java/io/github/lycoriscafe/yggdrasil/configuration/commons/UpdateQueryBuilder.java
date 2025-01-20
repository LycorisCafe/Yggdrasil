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

import io.github.lycoriscafe.nexus.http.core.headers.content.MultipartFormData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UpdateQueryBuilder<T extends Entity, U extends Enum<U> & EntityColumn<T>, V extends EntityService<T>> {
    private Class<T> entity;
    private Class<U> entityColumns;
    private Class<V> entityService;
    private List<U> columns;
    private List<String> values;
    private List<U> searchBy;
    private List<String> searchByValues;
    private List<Boolean> isCaseSensitive;

    public UpdateQueryBuilder(Class<T> entity,
                              Class<U> entityColumns,
                              Class<V> entityService) {
        this.entity = Objects.requireNonNull(entity);
        this.entityColumns = Objects.requireNonNull(entityColumns);
        this.entityService = Objects.requireNonNull(entityService);
    }

    public Class<T> getEntity() {
        return entity;
    }

    public UpdateQueryBuilder<T, U, V> setEntity(Class<T> entity) {
        this.entity = entity;
        return this;
    }

    public Class<U> getEntityColumns() {
        return entityColumns;
    }

    public UpdateQueryBuilder<T, U, V> setEntityColumns(Class<U> entityColumns) {
        this.entityColumns = entityColumns;
        return this;
    }

    public Class<V> getEntityService() {
        return entityService;
    }

    public UpdateQueryBuilder<T, U, V> setEntityService(Class<V> entityService) {
        this.entityService = entityService;
        return this;
    }

    public List<U> getColumns() {
        return columns;
    }

    public UpdateQueryBuilder<T, U, V> setColumns(List<U> columns) {
        this.columns = columns;
        return this;
    }

    public List<String> getValues() {
        return values;
    }

    public UpdateQueryBuilder<T, U, V> setValues(List<String> values) {
        this.values = values;
        return this;
    }

    public List<U> getSearchBy() {
        return searchBy;
    }

    public UpdateQueryBuilder<T, U, V> setSearchBy(List<U> searchBy) {
        this.searchBy = searchBy;
        return this;
    }

    public List<String> getSearchByValues() {
        return searchByValues;
    }

    public UpdateQueryBuilder<T, U, V> setSearchByValues(List<String> searchByValues) {
        this.searchByValues = searchByValues;
        return this;
    }

    public List<Boolean> getIsCaseSensitive() {
        return isCaseSensitive;
    }

    public UpdateQueryBuilder<T, U, V> setIsCaseSensitive(List<Boolean> isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
        return this;
    }

    public static <T extends Entity,
            U extends Enum<U> & EntityColumn<T>,
            V extends EntityService<T>> UpdateQueryBuilder<T, U, V> build(Class<T> entity,
                                                                          Class<U> entityColumns,
                                                                          Class<V> entityService,
                                                                          Map<String, String> parameters,
                                                                          List<MultipartFormData> multipartFormData) {
        var updateQueryBuilder = new UpdateQueryBuilder<>(entity, entityColumns, entityService);

        List<U> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (MultipartFormData formData : multipartFormData) {
            try {
                U col = Enum.valueOf(entityColumns, formData.getName());
                columns.add(col);
                values.add(new String(formData.getData()));
            } catch (Exception ignored) {}
        }
        updateQueryBuilder.setColumns(columns.isEmpty() ? null : columns)
                .setValues(values.isEmpty() ? null : values);

        if (parameters == null) return updateQueryBuilder;
        List<U> searchBy = new ArrayList<>();
        List<String> searchByValues = new ArrayList<>();
        List<Boolean> isCaseSensitive = new ArrayList<>();
        for (String key : parameters.keySet()) {
            try {
                U col = Enum.valueOf(entityColumns, key);
                searchBy.add(col);
                String[] val = parameters.get(key).split(",", 0);
                searchByValues.add(val[0]);
                isCaseSensitive.add(val.length == 2 && Boolean.parseBoolean(val[1]));
            } catch (Exception ignored) {}
        }
        updateQueryBuilder.setSearchBy(searchBy.isEmpty() ? null : searchBy)
                .setSearchByValues(searchByValues.isEmpty() ? null : searchByValues)
                .setIsCaseSensitive(isCaseSensitive.isEmpty() ? null : isCaseSensitive);

        return updateQueryBuilder;
    }
}
