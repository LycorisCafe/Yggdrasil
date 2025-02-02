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

package io.github.lycoriscafe.yggdrasil.commons;

import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CommonService {
    private static final Logger logger = LoggerFactory.getLogger(CommonService.class);

    public static <T extends Entity, U extends EntityService<T>> ResponseModel<T> create(Class<T> entity,
                                                                                         Class<U> entityService,
                                                                                         T instance) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(entityService);
        Objects.requireNonNull(instance);

        StringBuilder query = new StringBuilder("INSERT INTO ").append(entity.getSimpleName()).append(" (");
        for (int i = 0; i < entity.getDeclaredFields().length; i++) {
            if (i > 0) query.append(", ");
            query.append(entity.getDeclaredFields()[i].getName());
        }
        query.append(") VALUES (");
        for (int i = 0; i < entity.getDeclaredFields().length; i++) {
            if (i > 0) query.append(", ");
            query.append("?");
        }
        query.append(")");

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS)) {
            Method method = entityService.getMethod("toDatabase", PreparedStatement.class, entity, boolean.class);
            method.invoke(null, statement, instance, false);
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new ResponseModel<T>().setError("Internal system error");
            }
            try (var resultSet = statement.getGeneratedKeys()) {
                if (!resultSet.next()) {
                    connection.rollback();
                    return new ResponseModel<T>().setError("Internal system error");
                }
                connection.commit();
                return read(entity, entityService,
                        new SearchModel().setSearchBy(Map.of("id", Map.of(resultSet.getString(1), false))));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Entity, U extends EntityService<T>> ResponseModel<T> read(Class<T> entity,
                                                                                       Class<U> entityService,
                                                                                       SearchModel searchModel) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(entityService);
        if (searchModel.getResultsFrom() == null) searchModel.setResultsFrom(new BigInteger("0"));
        if (searchModel.getResultsOffset() == null) {
            searchModel.setResultsOffset(new BigInteger(String.valueOf(YggdrasilConfig.getDefaultResultsOffset())));
        }
        if (searchModel.getResultsFrom().compareTo(new BigInteger("0")) < 0) {
            return new ResponseModel<T>().setError("Invalid 'resultsFrom'");
        }
        if (searchModel.getResultsOffset().compareTo(new BigInteger("0")) < 0) {
            return new ResponseModel<T>().setError("Invalid 'resultsOffset'");
        }

        String generableResultsQuery;
        String resultsOffsetQuery;
        StringBuilder query = new StringBuilder("SELECT * FROM ").append(entity.getSimpleName());
        if (searchModel.getSearchBy() != null) {
            query.append(" WHERE ");
            List<String> fields = searchModel.getSearchBy().keySet().stream().toList();
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) query.append(" AND ");
                List<String> values = searchModel.getSearchBy().get(fields.get(i)).keySet().stream().toList();
                for (int j = 0; j < values.size(); j++) {
                    if (j > 0) query.append(" OR ");
                    query.append(fields.get(i)).append(" LIKE ")
                            .append(searchModel.getSearchBy().get(fields.get(i)).get(values.get(j)) ? " BINARY " : "")
                            .append("?");
                }
            }
        }
        if (searchModel.getOrderBy() != null) {
            query.append(" ORDER BY ");
            for (int i = 0; i < searchModel.getOrderBy().size(); i++) {
                if (i > 0) query.append(", ");
                query.append(searchModel.getOrderBy().get(i));
            }
        }
        if (searchModel.getAscending() != null) {
            query.append(searchModel.getAscending() ? " ASC" : " DESC");
        }
        generableResultsQuery = query.toString().replaceFirst("\\*", "COUNT(1)");
        query.append(" LIMIT ").append("?, ?");
        resultsOffsetQuery = "SELECT COUNT(1) FROM (" + query + ") AS resultsOffset";

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString());
             var generableResultsStatement = connection.prepareStatement(generableResultsQuery);
             var resultsOffsetStatement = connection.prepareStatement(resultsOffsetQuery)) {
            int nextParamIndex = 1;
            if (searchModel.getSearchBy() != null) {
                for (String field : searchModel.getSearchBy().keySet()) {
                    for (String value : searchModel.getSearchBy().get(field).keySet()) {
                        statement.setString(nextParamIndex, value);
                        generableResultsStatement.setString(nextParamIndex, value);
                        resultsOffsetStatement.setString(nextParamIndex++, value);
                    }
                }
            }
            statement.setString(nextParamIndex, searchModel.getResultsFrom().toString());
            resultsOffsetStatement.setString(nextParamIndex++, searchModel.getResultsFrom().toString());
            statement.setString(nextParamIndex, searchModel.getResultsOffset().toString());
            resultsOffsetStatement.setString(nextParamIndex, searchModel.getResultsOffset().toString());

            var response = new ResponseModel<T>();
            try (var resultSet = statement.executeQuery();
                 var generableResultsResultSet = generableResultsStatement.executeQuery();
                 var resultsOffsetResultSet = resultsOffsetStatement.executeQuery()) {
                List<T> data = new ArrayList<>();
                while (resultSet.next()) {
                    T instance = entity.getConstructor().newInstance();
                    Method method = entityService.getMethod("fromDatabase", ResultSet.class, entity);
                    method.invoke(null, resultSet, instance);
                    data.add(instance);
                }
                response.setData(data);

                if (!generableResultsResultSet.next()) return new ResponseModel<T>().setError("Internal system error");
                response.setGenerableResults(new BigInteger(generableResultsResultSet.getString(1)));
                if (!resultsOffsetResultSet.next()) return new ResponseModel<T>().setError("Internal system error");
                response.setResultsOffset(new BigInteger(resultsOffsetResultSet.getString(1)));
            }
            connection.commit();
            return response.setResultsFrom(searchModel.getResultsFrom()).setSuccess(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Entity, U extends EntityService<T>> ResponseModel<T> update(Class<T> entity,
                                                                                         Class<U> entityService,
                                                                                         T instance) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(entityService);
        Objects.requireNonNull(instance);

        StringBuilder query = new StringBuilder("UPDATE ").append(entity.getSimpleName()).append(" SET ");
        for (int i = 0; i < entity.getDeclaredFields().length; i++) {
            if (i > 1) query.append(", ");
            if (i == 0) continue;
            query.append(entity.getDeclaredFields()[i].getName()).append(" = ?");
        }
        query.append(" WHERE id = ?");

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString())) {
            Method method = entityService.getMethod("toDatabase", PreparedStatement.class, entity, boolean.class);
            method.invoke(null, statement, instance, true);
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new ResponseModel<T>().setError("Internal system error");
            }
            connection.commit();
            return read(entity, entityService,
                    new SearchModel().setSearchBy(Map.of("id", Map.of(instance.getId().toString(), false))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Entity> ResponseModel<T> delete(Class<T> entity,
                                                             BigInteger id) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(id);

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM " + entity.getSimpleName() + " WHERE id = ?")) {
            statement.setString(1, id.toString());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new ResponseModel<T>().setError("Internal system error");
            }
            connection.commit();
            return new ResponseModel<T>().setSuccess(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
