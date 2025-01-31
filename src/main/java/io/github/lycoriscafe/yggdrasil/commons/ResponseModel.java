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

import io.github.lycoriscafe.nexus.http.core.headers.content.Content;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

public final class ResponseModel<T extends Entity> {
    private boolean success;
    private LocalDateTime timestamp;
    private String error;
    private BigInteger generableResults;
    private BigInteger resultsFrom;
    private BigInteger resultsOffset;
    private List<T> data;

    public ResponseModel() {
        timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return success;
    }

    public ResponseModel<T> setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public ResponseModel<T> setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getError() {
        return error;
    }

    public ResponseModel<T> setError(String error) {
        this.error = error;
        return this;
    }

    public BigInteger getGenerableResults() {
        return generableResults;
    }

    public ResponseModel<T> setGenerableResults(BigInteger generableResults) {
        this.generableResults = generableResults;
        return this;
    }

    public BigInteger getResultsFrom() {
        return resultsFrom;
    }

    public ResponseModel<T> setResultsFrom(BigInteger resultsFrom) {
        this.resultsFrom = resultsFrom;
        return this;
    }

    public BigInteger getResultsOffset() {
        return resultsOffset;
    }

    public ResponseModel<T> setResultsOffset(BigInteger resultsOffset) {
        this.resultsOffset = resultsOffset;
        return this;
    }

    public List<T> getData() {
        return data;
    }

    public ResponseModel<T> setData(List<T> data) {
        this.data = data;
        return this;
    }

    public Content parse() {
        return new Content("application/json", Utils.getGson().toJson(this));
    }
}
