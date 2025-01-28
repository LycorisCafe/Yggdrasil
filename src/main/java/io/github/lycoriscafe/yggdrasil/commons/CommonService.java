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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommonService {
    private static final Logger logger = LoggerFactory.getLogger(CommonService.class);

    public static <T extends Entity> Response<T> create(Class<T> entity,
                                                        RequestModel<T> requestModel) {
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
            for (int i = 0; i < entity.getDeclaredFields().length; i++) {
                String firstChar = String.valueOf(entity.getDeclaredFields()[i].getName().charAt(0));
                String getterName = "get" + entity.getDeclaredFields()[i].getName()
                        .replaceFirst(firstChar, firstChar.toUpperCase(Locale.ROOT));
                Object obj = entity.getMethod(getterName).invoke(requestModel.getEntityInstance());
                statement.setObject(i + 1, obj);
            }

            if (statement.executeUpdate() != 1) return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
            try (var resultSet = statement.getGeneratedKeys()) {
                if (!resultSet.next()) return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
                return read(entity, new RequestModel<T>().setSearchBy(
                        Map.of(entity.getDeclaredField("id"), Map.of(resultSet.getString(1), false))));
            }
        } catch (Exception e) {
            logger.atError().log(e.getMessage());
            return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
        }
    }

    public static <T extends Entity> Response<T> read(Class<T> entity,
                                                      RequestModel<T> requestModel) {
        if (entity == null) return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
        if (requestModel.getResultsFrom() == null) requestModel.setResultsFrom(new BigInteger("0"));
        if (requestModel.getResultsOffset() == null) {
            requestModel.setResultsOffset(new BigInteger(String.valueOf(YggdrasilConfig.getDefaultResultsOffset())));
        }
        if (requestModel.getResultsFrom().compareTo(new BigInteger("0")) < 0) {
            return new Response<T>().setError(ResponseError.INVALID_RESULTS_FROM);
        }
        if (requestModel.getResultsOffset().compareTo(new BigInteger("0")) < 0) {
            return new Response<T>().setError(ResponseError.INVALID_RESULTS_OFFSET);
        }

        String generableResultsQuery;
        String resultsOffsetQuery;
        StringBuilder query = new StringBuilder("SELECT * FROM ").append(entity.getSimpleName());
        if (requestModel.getSearchBy() != null) {
            query.append(" WHERE ");
            List<Field> fields = requestModel.getSearchBy().keySet().stream().toList();
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) query.append(" AND ");
                List<Object> objects = requestModel.getSearchBy().get(fields.get(i)).keySet().stream().toList();
                for (int j = 0; j < objects.size(); j++) {
                    if (j > 0) query.append(" OR ");
                    query.append(fields.get(i).getName()).append(" LIKE ")
                            .append(requestModel.getSearchBy().get(fields.get(i)).get(objects.get(j)) ? " BINARY " : "")
                            .append("?");
                }
            }
        }
        if (requestModel.getOrderBy() != null) {
            query.append(" ORDER BY ");
            for (int i = 0; i < requestModel.getOrderBy().size(); i++) {
                if (i > 0) query.append(", ");
                query.append(requestModel.getOrderBy().get(i).getName());
            }
        }
        if (requestModel.getAscending() != null) {
            query.append(requestModel.getAscending() ? " ASC" : " DESC");
        }
        generableResultsQuery = query.toString().replaceFirst("\\*", "COUNT(1)");
        query.append(" LIMIT ").append("?, ?");
        resultsOffsetQuery = "SELECT COUNT(1) FROM (" + query + ") AS resultsOffset";

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString());
             var generableResultsStatement = connection.prepareStatement(generableResultsQuery);
             var resultsOffsetStatement = connection.prepareStatement(resultsOffsetQuery)) {
            int nextParamIndex = 1;
            if (requestModel.getSearchBy() != null) {
                for (Field field : requestModel.getSearchBy().keySet()) {
                    for (Object obj : requestModel.getSearchBy().get(field).keySet()) {
                        statement.setObject(nextParamIndex, obj);
                        generableResultsStatement.setObject(nextParamIndex, obj);
                        resultsOffsetStatement.setObject(nextParamIndex++, obj);
                    }
                }
            }
            statement.setString(nextParamIndex, requestModel.getResultsFrom().toString());
            resultsOffsetStatement.setString(nextParamIndex++, requestModel.getResultsFrom().toString());
            statement.setString(nextParamIndex, requestModel.getResultsOffset().toString());
            resultsOffsetStatement.setString(nextParamIndex, requestModel.getResultsOffset().toString());

            var response = new Response<T>();
            try (var resultSet = statement.executeQuery();
                 var generableResultsResultSet = generableResultsStatement.executeQuery();
                 var resultsOffsetResultSet = resultsOffsetStatement.executeQuery()) {
                List<T> data = new ArrayList<>();
                while (resultSet.next()) {
                    T instance = entity.getConstructor().newInstance();
                    for (Field field : entity.getDeclaredFields()) {
                        String firstChar = String.valueOf(field.getName().charAt(0));
                        String setterName = "set" + field.getName().replaceFirst(firstChar, firstChar.toUpperCase(Locale.ROOT));
                        Method method = entity.getMethod(setterName, Object.class);
                        method.invoke(instance, resultSet.getObject(field.getName()));
                    }
                    data.add(instance);
                }
                response.setData(data);

                if (!generableResultsResultSet.next()) return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
                response.setGenerableResults(new BigInteger(generableResultsResultSet.getString(1)));
                if (!resultsOffsetResultSet.next()) return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
                response.setResultsOffset(new BigInteger(resultsOffsetResultSet.getString(1)));
            }
            return response.setSuccess(true);
        } catch (Exception e) {
            logger.atError().log(e.getMessage());
            return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
        }
    }

    public static <T extends Entity> Response<T> update(Class<T> entity,
                                                        Map<Field, Map<Object, Boolean>> searchBy,
                                                        T entityInstance) {
        if (entity == null || entityInstance == null) return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
        if (searchBy == null) return new Response<T>().setError(ResponseError.INVALID_SEARCH_PARAMETER);

        StringBuilder query = new StringBuilder("UPDATE ").append(entity.getSimpleName()).append(" SET ");
        for (int i = 0; i < entity.getDeclaredFields().length; i++) {
            if (i > 0) query.append(", ");
            if (entity.getDeclaredFields()[i].getName().equals("id")) continue;
            query.append(entity.getDeclaredFields()[i].getName()).append(" = ?");
        }
        query.append(" WHERE ");
        List<Field> fields = searchBy.keySet().stream().toList();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) query.append(" AND ");
            List<Object> objects = searchBy.get(fields.get(i)).keySet().stream().toList();
            for (int j = 0; j < objects.size(); j++) {
                if (j > 0) query.append(" OR ");
                query.append(fields.get(i).getName()).append(" LIKE ")
                        .append(searchBy.get(fields.get(i)).get(j) ? " BINARY " : "")
                        .append("?");
            }
        }

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString())) {
            int nextParamIndex = 1;
            for (Field field : entity.getDeclaredFields()) {
                if (field.getName().equals("id")) continue;
                String firstChar = String.valueOf(field.getName().charAt(0));
                String getterName = "get" + field.getName().replaceFirst(firstChar, firstChar.toUpperCase(Locale.ROOT));
                Object obj = entity.getMethod(getterName).invoke(entityInstance);
                statement.setObject(nextParamIndex++, obj);
            }
            for (Field field : searchBy.keySet()) {
                for (Object obj : searchBy.get(field).keySet()) {
                    statement.setObject(nextParamIndex++, obj);
                }
            }

            if (statement.executeUpdate() != 1) return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
            return read(entity, new RequestModel<T>().setSearchBy(
                    Map.of(entity.getDeclaredField("id"), Map.of(entity.getMethod("getId").invoke(entityInstance), false))));
        } catch (Exception e) {
            logger.atError().log(e.getMessage());
            return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
        }
    }

    public static <T extends Entity> Response<T> delete(Class<T> entity,
                                                        Map<Field, Map<Object, Boolean>> searchBy) {
        if (entity == null) return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
        if (searchBy == null) return new Response<T>().setError(ResponseError.INVALID_SEARCH_PARAMETER);

        StringBuilder query = new StringBuilder("DELETE FROM ").append(entity.getSimpleName()).append(" WHERE ");
        List<Field> fields = searchBy.keySet().stream().toList();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) query.append(" AND ");
            List<Object> objects = searchBy.get(fields.get(i)).keySet().stream().toList();
            for (int j = 0; j < objects.size(); j++) {
                if (j > 0) query.append(" OR ");
                query.append(fields.get(i).getName()).append(" LIKE ")
                        .append(searchBy.get(fields.get(i)).get(j) ? " BINARY " : "")
                        .append("?");
            }
        }

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString())) {
            int nextParamIndex = 1;
            for (Field field : searchBy.keySet()) {
                for (Object obj : searchBy.get(field).keySet()) {
                    statement.setObject(nextParamIndex++, obj);
                }
            }

            if (statement.executeUpdate() != 1) return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
            return new Response<T>().setSuccess(true);
        } catch (Exception e) {
            logger.atError().log(e.getMessage());
            return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
        }
    }
}
