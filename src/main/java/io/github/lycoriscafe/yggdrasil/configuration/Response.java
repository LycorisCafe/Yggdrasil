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

package io.github.lycoriscafe.yggdrasil.configuration;

import io.github.lycoriscafe.nexus.http.core.headers.content.Content;
import io.github.lycoriscafe.yggdrasil.configuration.database.Entity;

import java.time.LocalDateTime;
import java.util.List;

public class Response<T extends Entity> {
    private boolean success;
    private LocalDateTime timestamp;
    private String error;
    private Long generableResults;
    private Long resultsFrom;
    private Long resultsOffset;
    private List<T> data;

    public Response() {
        timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return success;
    }

    public Response<T> setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Response<T> setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getError() {
        return error;
    }

    public Response<T> setError(String error) {
        this.error = error;
        return this;
    }

    public Long getGenerableResults() {
        return generableResults;
    }

    public Response<T> setGenerableResults(Long generableResults) {
        this.generableResults = generableResults;
        return this;
    }

    public Long getResultsFrom() {
        return resultsFrom;
    }

    public Response<T> setResultsFrom(Long resultsFrom) {
        this.resultsFrom = (resultsFrom == null ? 0L : resultsFrom);
        return this;
    }

    public Long getResultsOffset() {
        return resultsOffset;
    }

    public Response<T> setResultsOffset(Long resultsOffset) {
        this.resultsOffset = resultsOffset;
        return this;
    }

    public List<T> getData() {
        return data;
    }

    public Response<T> setData(List<T> data) {
        this.data = data;
        return this;
    }

    public Content parse() {
        return new Content("application/json", Utils.toJson(this));
    }
}
