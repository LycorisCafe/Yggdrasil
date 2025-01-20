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

package io.github.lycoriscafe.yggdrasil.rest.guardian;

import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.commons.*;
import io.github.lycoriscafe.yggdrasil.rest.Gender;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GuardianService implements EntityService<Guardian> {
    public enum Columns implements EntityColumn<Guardian> {
        id,
        nic,
        initName,
        fullName,
        gender,
        dateOfBirth,
        address,
        email,
        contactNo
    }

    public static Response<Guardian> select(SearchQueryBuilder<Guardian, Columns, GuardianService> searchQueryBuilder) {
        try {
            var results = CommonService.select(searchQueryBuilder);
            if (results.getResponse() != null) return results.getResponse();

            List<Guardian> guardians = new ArrayList<>();
            try (var resultSet = results.getResultSet()) {
                while (resultSet.next()) {
                    guardians.add(new Guardian(
                            resultSet.getString("nic"),
                            resultSet.getString("initName"),
                            resultSet.getString("fullName"),
                            Gender.valueOf(resultSet.getString("gender")),
                            LocalDate.parse(resultSet.getString("dateOfBirth"), Utils.getDateFormatter()),
                            resultSet.getString("address"),
                            resultSet.getString("contactNo")
                    ).setId(resultSet.getBigDecimal("id"))
                            .setEmail(resultSet.getString("email")));
                }
            }

            return new Response<Guardian>()
                    .setSuccess(true)
                    .setGenerableResults(results.getGenerableResults())
                    .setResultsFrom(results.getResultsFrom())
                    .setResultsOffset(results.getResultsOffset())
                    .setData(guardians);
        } catch (Exception e) {
            return new Response<Guardian>().setError(e.getMessage());
        }
    }

    public static Response<Guardian> delete(SearchQueryBuilder<Guardian, Columns, GuardianService> searchQueryBuilder) {
        return CommonService.delete(searchQueryBuilder);
    }

    public static Response<Guardian> insert(UpdateQueryBuilder<Guardian, Columns, GuardianService> updateQueryBuilder) {
        return CommonService.insert(updateQueryBuilder);
    }

    public static Response<Guardian> update(UpdateQueryBuilder<Guardian, Columns, GuardianService> updateQueryBuilder) {
        return CommonService.update(updateQueryBuilder);
    }
}
