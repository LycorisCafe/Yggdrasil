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

package io.github.lycoriscafe.yggdrasil.rest.relief;

import io.github.lycoriscafe.yggdrasil.commons.CommonService;
import io.github.lycoriscafe.yggdrasil.commons.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReliefService implements EntityService<Relief> {
    public enum Columns implements EntityColumn<Relief> {
        id,
        timetableId,
        teacherId,
        date
    }

    public static Response<Relief> select(SearchQueryBuilder<Relief, Columns, ReliefService> searchQueryBuilder) {
        try {
            var results = CommonService.select(searchQueryBuilder);
            if (results.getResponse() != null) return results.getResponse();

            List<Relief> reliefs = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    reliefs.add(new Relief(
                            resultSet.getBigInteger("timetableId"),
                            resultSet.getBigInteger("teacherId"),
                            LocalDate.parse(resultSet.getString("date"), Utils.getDateFormatter())
                    ).setId(resultSet.getBigInteger("id")));
                }
            }

            return new Response<Relief>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
                    .setData(reliefs);
        } catch (Exception e) {
            return new Response<Relief>().setError(e.getMessage());
        }
    }

    public static Response<Relief> delete(SearchQueryBuilder<Relief, Columns, ReliefService> searchQueryBuilder) {
        if (searchQueryBuilder.getSearchBy() == null || !searchQueryBuilder.getSearchBy().contains(Columns.id)) {
            return new Response<Relief>().setError("Required parameters not found");
        }
        return CommonService.delete(searchQueryBuilder);
    }

    public static Response<Relief> insert(UpdateQueryBuilder<Relief, Columns, ReliefService> updateQueryBuilder) {
        if (updateQueryBuilder.getColumns() == null ||
                !updateQueryBuilder.getColumns().containsAll(Set.of(Columns.timetableId, Columns.teacherId, Columns.date))) {
            return new Response<Relief>().setError("Required parameters not found");
        }
        return CommonService.insert(updateQueryBuilder);
    }

    public static Response<Relief> update(UpdateQueryBuilder<Relief, Columns, ReliefService> updateQueryBuilder) {
        if (updateQueryBuilder.getSearchBy() == null || !updateQueryBuilder.getSearchBy().contains(Columns.id)) {
            return new Response<Relief>().setError("Required parameters not found");
        }
        if (updateQueryBuilder.getColumns() != null && updateQueryBuilder.getColumns().contains(Columns.id)) {
            return new Response<Relief>().setError("'id' cannot be updated");
        }
        return CommonService.update(updateQueryBuilder);
    }
}
