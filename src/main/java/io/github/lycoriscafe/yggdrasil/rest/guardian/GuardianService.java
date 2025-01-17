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
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;
import io.github.lycoriscafe.yggdrasil.rest.Gender;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GuardianService {
    public enum Columns {
        id,
        nic,
        initName,
        fullName,
        gender,
        address,
        email,
        contactNo
    }

    public static Response<Guardian> getGuardians(Columns[] searchBy,
                                                  String[] searchByValues,
                                                  boolean[] isCaseSensitive,
                                                  Columns[] orderBy,
                                                  Boolean isAscending,
                                                  Long resultsFrom,
                                                  Long resultsOffset) {
        if (resultsFrom == null || resultsFrom < 0) resultsFrom = 0L;
        if (resultsOffset == null || resultsOffset < 0) resultsOffset = YggdrasilConfig.getDefaultResultsOffset();
        if (resultsFrom > resultsOffset) return new Response<Guardian>().setError("Invalid boundaries");

        StringBuilder query = new StringBuilder("SELECT * FROM guardian");
        if (searchBy != null) {
            if (searchBy.length != searchByValues.length) return new Response<Guardian>().setError("searchBy != searchByValues (length)");
            if (isCaseSensitive != null && searchBy.length != isCaseSensitive.length) {
                return new Response<Guardian>().setError("searchBy != isCaseSensitive (length)");
            }
            query.append(" WHERE ");
            for (int i = 0; i < searchBy.length; i++) {
                if (i > 0) query.append(" AND ");
                query.append(searchBy[i]).append(" LIKE ");
                if (isCaseSensitive != null) query.append(isCaseSensitive[i] ? " BINARY " : "");
                query.append("?");
            }
        }
        if (orderBy != null) {
            query.append(" ORDER BY ");
            for (int i = 0; i < orderBy.length; i++) {
                if (i > 0) query.append(", ");
                query.append(orderBy[i]);
            }
        }
        if (isAscending != null) {
            query.append(isAscending ? " ASC" : " DESC");
        }
        query.replace(7, 8, "*, (" + query.toString().replace("*", "COUNT(id)") + ") AS generableValues");
        query.append(" LIMIT ").append(Long.toUnsignedString(resultsFrom)).append(", ").append(Long.toUnsignedString(resultsOffset));

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(query.toString())) {
            if (searchByValues != null) {
                for (int i = 0; i < searchByValues.length; i++) {
                    statement.setString(i + 1, searchByValues[i]);
                }
            }

            Long generableValues = null;
            List<Guardian> guardians = new ArrayList<>();
            try (var resultSet = statement.executeQuery()) {
                connection.commit();
                while (resultSet.next()) {
                    if (generableValues == null) generableValues = Long.parseLong(resultSet.getString("generableValues"));
                    guardians.add(new Guardian(
                            resultSet.getString("nic"),
                            resultSet.getString("initName"),
                            resultSet.getString("fullName"),
                            Gender.valueOf(resultSet.getString("gender")),
                            resultSet.getString("address"),
                            resultSet.getString("contactNo")
                    ).setId(Long.parseLong(resultSet.getString("id")))
                            .setEmail(resultSet.getString("email")));
                }
            }

            return new Response<Guardian>()
                    .setSuccess(true)
                    .setGenerableResults(generableValues)
                    .setResultsFrom(resultsFrom)
                    .setResultsOffset(resultsOffset)
                    .setData(guardians);
        } catch (Exception e) {
            return new Response<Guardian>().setError(e.getMessage());
        }
    }

    public static Response<Guardian> getGuardianById(Long id) {
        try {
            return getGuardians(new Columns[]{Columns.id}, new String[]{Long.toUnsignedString(id)},
                    null, null, null, null, 1L);
        } catch (Exception e) {
            return new Response<Guardian>().setError(e.getMessage());
        }
    }

    public static Response<Guardian> createGuardian(Guardian guardian) {
        Objects.requireNonNull(guardian);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO guardian (id, nic, initName, fullName, gender, address, email, contactNo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, guardian.getId() == null ? null : Long.toUnsignedString(guardian.getId()));
            statement.setString(2, guardian.getNic());
            statement.setString(3, guardian.getInitName());
            statement.setString(4, guardian.getFullName());
            statement.setString(5, guardian.getGender().toString());
            statement.setString(6, guardian.getAddress());
            statement.setString(7, guardian.getEmail());
            statement.setString(8, guardian.getContactNo());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Guardian>().setError("Internal server error");
            }
            try (var resultSet = statement.getGeneratedKeys()) {
                if (!resultSet.next()) {
                    connection.rollback();
                    return new Response<Guardian>().setError("Internal server error");
                }
                connection.commit();
                return getGuardianById(Long.parseLong(resultSet.getString(1)));
            }
        } catch (Exception e) {
            return new Response<Guardian>().setError(e.getMessage());
        }
    }

    public static Response<Guardian> updateGuardian(Guardian guardian) {
        Objects.requireNonNull(guardian);

        var oldGuardian = getGuardianById(guardian.getId());
        if (oldGuardian.getError() != null) return oldGuardian;
        var data = oldGuardian.getData().getFirst();
        if (guardian.getNic() == null) guardian.setNic(data.getNic());
        if (guardian.getInitName() == null) guardian.setInitName(data.getInitName());
        if (guardian.getFullName() == null) guardian.setFullName(data.getFullName());
        if (guardian.getGender() == null) guardian.setGender(data.getGender());
        if (guardian.getAddress() == null) guardian.setAddress(data.getAddress());
        if (guardian.getEmail() == null) guardian.setEmail(data.getEmail());
        if (guardian.getContactNo() == null) guardian.setContactNo(data.getContactNo());

        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("UPDATE guardian SET nic = ?, initName = ?, fullName = ?, gender = ?, address = ?, " +
                     "email = ?, contactNo = ? WHERE id = ?")) {
            statement.setString(1, guardian.getNic());
            statement.setString(2, guardian.getInitName());
            statement.setString(3, guardian.getFullName());
            statement.setString(4, guardian.getGender().toString());
            statement.setString(5, guardian.getAddress());
            statement.setString(6, guardian.getEmail());
            statement.setString(7, guardian.getContactNo());
            statement.setString(8, Long.toUnsignedString(guardian.getId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Guardian>().setError("Internal server error");
            }
            connection.commit();
            return getGuardianById(guardian.getId());
        } catch (Exception e) {
            return new Response<Guardian>().setError(e.getMessage());
        }
    }

    public static Response<Guardian> deleteGuardianById(Long id) {
        Objects.requireNonNull(id);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM guardian WHERE id = ?")) {
            statement.setString(1, Long.toUnsignedString(id));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return new Response<Guardian>().setError("Internal server error");
            }
            connection.commit();
            return new Response<Guardian>().setSuccess(true);
        } catch (Exception e) {
            return new Response<Guardian>().setError(e.getMessage());
        }
    }
}
