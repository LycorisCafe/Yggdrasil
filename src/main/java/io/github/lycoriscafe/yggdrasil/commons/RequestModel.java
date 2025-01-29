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

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.lycoriscafe.yggdrasil.configuration.GsonTypeAdapters;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.rest.admin.Admin;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class RequestModel<T extends Entity> {
    private Map<String, Map<Object, Boolean>> searchBy;
    private List<String> orderBy;
    private Boolean isAscending;
    private BigInteger resultsFrom;
    private BigInteger resultsOffset;
    private T entityInstance;

    public Map<String, Map<Object, Boolean>> getSearchBy() {
        return searchBy;
    }

    public RequestModel<T> setSearchBy(Map<String, Map<Object, Boolean>> searchBy) {
        this.searchBy = searchBy;
        return this;
    }

    public List<String> getOrderBy() {
        return orderBy;
    }

    public RequestModel<T> setOrderBy(List<String> orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public Boolean getAscending() {
        return isAscending;
    }

    public RequestModel<T> setAscending(Boolean ascending) {
        isAscending = ascending;
        return this;
    }

    public BigInteger getResultsFrom() {
        return resultsFrom;
    }

    public RequestModel<T> setResultsFrom(BigInteger resultsFrom) {
        this.resultsFrom = resultsFrom;
        return this;
    }

    public BigInteger getResultsOffset() {
        return resultsOffset;
    }

    public RequestModel<T> setResultsOffset(BigInteger resultsOffset) {
        this.resultsOffset = resultsOffset;
        return this;
    }

    public T getEntityInstance() {
        return entityInstance;
    }

    public RequestModel<T> setEntityInstance(T entityInstance) {
        this.entityInstance = entityInstance;
        return this;
    }

    public static <T extends Entity> RequestModel<T> fromJson(Class<T> entity,
                                                              String json) {
        return new GsonBuilder()
                .serializeNulls()
                .setDateFormat(Utils.DATE_TIME_FORMAT)
                .registerTypeAdapter(LocalDate.class, new GsonTypeAdapters.Date())
                .registerTypeAdapter(LocalTime.class, new GsonTypeAdapters.Time())
                .registerTypeAdapter(LocalDateTime.class, new GsonTypeAdapters.DateTime())
                .create().fromJson(json, TypeToken.getParameterized(RequestModel.class, entity).getType());
    }

    public static void main(String[] args) {
        var reqm = fromJson(Admin.class, "{\"searchBy\":{\"id\":{\"1\":true, \"2\":false}},\"orderBy\":[\"id\",\"name\"],\"isAscending\":true,\"resultsFrom\":5,\"resultsOffset\":5,\"entityInstance\":{\"id\":null,\"name\":\"anme\",\"accessLevel\":[\"TIMETABLE\"],\"disabled\":null}}");
        System.out.println(new GsonBuilder()
                .serializeNulls()
                .setDateFormat(Utils.DATE_TIME_FORMAT)
                .registerTypeAdapter(LocalDate.class, new GsonTypeAdapters.Date())
                .registerTypeAdapter(LocalTime.class, new GsonTypeAdapters.Time())
                .registerTypeAdapter(LocalDateTime.class, new GsonTypeAdapters.DateTime())
                .create().toJson(reqm));
    }
}
