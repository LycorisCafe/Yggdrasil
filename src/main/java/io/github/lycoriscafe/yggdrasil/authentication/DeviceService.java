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

package io.github.lycoriscafe.yggdrasil.authentication;

import io.github.lycoriscafe.nexus.http.core.headers.auth.scheme.bearer.BearerAuthorization;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpPostRequest;
import io.github.lycoriscafe.yggdrasil.commons.Entity;
import io.github.lycoriscafe.yggdrasil.commons.ResponseModel;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeviceService {
    public static List<Device> getDevices(Role role,
                                          BigInteger userId) throws SQLException {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userId);
        List<Device> devices;
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("SELECT * FROM devices WHERE role = ? AND userid = ?")) {
            statement.setString(1, role.toString());
            statement.setString(2, userId.toString());
            devices = deserialize(statement.executeQuery());
            connection.commit();
        }
        return devices;
    }

    public static List<Device> getDevices(TokenType tokenType,
                                          String token) throws SQLException {
        Objects.requireNonNull(tokenType);
        Objects.requireNonNull(token);
        List<Device> devices;
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("SELECT * FROM devices WHERE " + tokenType + " = BINARY ?")) {
            statement.setString(1, token);
            devices = deserialize(statement.executeQuery());
            connection.commit();
        }
        return devices;
    }

    public static boolean addDevice(Device device) throws SQLException, NoSuchAlgorithmException {
        Objects.requireNonNull(device);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO devices VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, device.getRole().toString());
            statement.setString(2, device.getUserId().toString());
            statement.setString(3, device.getName());
            statement.setString(4, device.getAccessToken());
            statement.setLong(5, device.getExpires());
            statement.setString(6, AuthenticationService.encryptData(device.getRefreshToken().getBytes(StandardCharsets.UTF_8)));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return false;
            }
            connection.commit();
            return true;
        }
    }

    public static boolean removeDevices(Role role,
                                        BigInteger userId) throws SQLException {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userId);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM devices WHERE role = ? AND userid = ?")) {
            statement.setString(1, role.toString());
            statement.setString(2, userId.toString());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return false;
            }
            connection.commit();
            return true;
        }
    }

    public static boolean removeDevice(String accessToken) throws SQLException {
        Objects.requireNonNull(accessToken);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM devices WHERE accessToken = BINARY ?")) {
            statement.setString(1, accessToken);
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return false;
            }
            connection.commit();
            return true;
        }
    }

    public static <T extends Entity> ResponseModel<T> removeDevice(HttpPostRequest req) {
        try {
            if (req.getParameters() != null && req.getParameters().containsKey("all")) {
                var devices = DeviceService.getDevices(TokenType.ACCESS_TOKEN, ((BearerAuthorization) req.getAuthorization()).getAccessToken());
                if (!DeviceService.removeDevices(devices.getFirst().getRole(), devices.getFirst().getUserId())) {
                    return new ResponseModel<T>().setError("Internal system error");
                }
                return new ResponseModel<T>().setSuccess(true);
            }

            if (!DeviceService.removeDevice(((BearerAuthorization) req.getAuthorization()).getAccessToken())) {
                return new ResponseModel<T>().setError("Internal system error");
            }
            return new ResponseModel<T>().setSuccess(true);
        } catch (SQLException e) {
            return new ResponseModel<T>().setError("Internal system error");
        }
    }

    public static boolean updateDevice(Device device) throws SQLException {
        Objects.requireNonNull(device);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement(
                     "UPDATE devices SET name = ?, accessToken = ?, expires = ? WHERE refreshToken = BINARY ?")) {
            statement.setString(1, device.getName());
            statement.setString(2, device.getAccessToken());
            statement.setLong(3, device.getExpires());
            statement.setString(4, device.getRefreshToken());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return false;
            }
            connection.commit();
            return true;
        }
    }

    private static List<Device> deserialize(ResultSet resultSet) throws SQLException {
        List<Device> devices = new ArrayList<>();
        try (resultSet) {
            while (resultSet.next()) {
                devices.add(new Device(
                        Role.valueOf(resultSet.getString("role")),
                        new BigInteger(resultSet.getString("userId")),
                        resultSet.getString("name"),
                        resultSet.getString("accessToken"),
                        resultSet.getLong("expires"),
                        resultSet.getString("refreshToken")
                ));
            }
        }
        return devices;
    }
}
