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
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpGetRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpPatchRequest;
import io.github.lycoriscafe.yggdrasil.commons.Entity;
import io.github.lycoriscafe.yggdrasil.commons.ResponseModel;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DeviceService {
    public static List<Device> getDevices(Role role,
                                          BigInteger userId) throws SQLException {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userId);
        List<Device> devices;
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("SELECT * FROM device WHERE role = ? AND userid = ?")) {
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
             var statement = connection.prepareStatement("SELECT * FROM device WHERE " + tokenType + " = BINARY ?")) {
            statement.setString(1, token);
            devices = deserialize(statement.executeQuery());
            connection.commit();
        }
        return devices;
    }

    public static String getDevices(HttpGetRequest req) {
        Objects.requireNonNull(req);
        try {
            var device = DeviceService.getDevices(TokenType.ACCESS_TOKEN, ((BearerAuthorization) req.getAuthorization()).getAccessToken());
            var devices = DeviceService.getDevices(device.getFirst().getRole(), device.getFirst().getUserId());
            return Utils.getGson().toJson(devices);
        } catch (SQLException e) {
            return "\"error\": \"" + e.getMessage() + "\"";
        }
    }

    public static void addDevice(Device device) throws SQLException, NoSuchAlgorithmException {
        Objects.requireNonNull(device);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO device (role, userId, deviceName, accessToken, expires, refreshToken) " +
                     "VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, device.getRole().toString());
            statement.setString(2, device.getUserId().toString());
            statement.setString(3, device.getDeviceName());
            statement.setString(4, device.getAccessToken());
            statement.setLong(5, device.getExpires());
            statement.setString(6, AuthenticationService.encryptData(device.getRefreshToken().getBytes(StandardCharsets.UTF_8)));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                throw new RuntimeException("Device adding failed");
            }
            connection.commit();
        }
    }

    public static void removeDevices(Role role,
                                     BigInteger userId) throws SQLException {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userId);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM device WHERE role = ? AND userid = ?")) {
            statement.setString(1, role.toString());
            statement.setString(2, userId.toString());
            connection.commit();
        }
    }

    public static void removeDevice(TokenType tokenType,
                                    String token) throws SQLException {
        Objects.requireNonNull(tokenType);
        Objects.requireNonNull(token);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM device WHERE " + tokenType + " = BINARY ?")) {
            statement.setString(1, token);
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                throw new RuntimeException("Device removing failed");
            }
            connection.commit();
        }
    }

    public static <T extends Entity> ResponseModel<T> removeDevice(HttpPatchRequest req,
                                                                   Role role,
                                                                   boolean selfRemove) {
        Objects.requireNonNull(req);
        Objects.requireNonNull(role);
        try {
            if (!selfRemove) {
                BigInteger userId;
                try {
                    userId = new BigInteger(req.getParameters().get("userId"));
                } catch (NumberFormatException e) {
                    return new ResponseModel<T>().setError("Unparsable `userId`");
                }
                removeDevices(role, userId);
                return new ResponseModel<T>().setSuccess(true);
            }

            if (req.getParameters() == null) {
                removeDevice(TokenType.ACCESS_TOKEN, ((BearerAuthorization) req.getAuthorization()).getAccessToken());
                return new ResponseModel<T>().setSuccess(true);
            }

            var device = getDevices(TokenType.ACCESS_TOKEN, ((BearerAuthorization) req.getAuthorization()).getAccessToken()).getFirst();
            removeDevices(device.getRole(), device.getUserId());
            return new ResponseModel<T>().setSuccess(true);
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            return new ResponseModel<T>().setError("Internal system error");
        }
    }

    public static void updateDevice(Device device) throws SQLException {
        Objects.requireNonNull(device);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("UPDATE device SET accessToken = ?, expires = ? WHERE refreshToken = BINARY ?")) {
            statement.setString(1, device.getAccessToken());
            statement.setLong(2, device.getExpires());
            statement.setString(3, device.getRefreshToken());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                throw new RuntimeException("Device updating failed");
            }
            connection.commit();
        }
    }

    private static List<Device> deserialize(ResultSet resultSet) throws SQLException {
        List<Device> devices = new ArrayList<>();
        try (resultSet) {
            while (resultSet.next()) {
                devices.add(new Device(
                        Role.valueOf(resultSet.getString("role")),
                        new BigInteger(resultSet.getString("userId")),
                        resultSet.getString("deviceName"),
                        resultSet.getString("accessToken"),
                        resultSet.getLong("expires"),
                        resultSet.getString("refreshToken")
                ).setLastLogin(LocalDateTime.parse(resultSet.getString("lastLogin"), Utils.getDateTimeFormatter())));
            }
        }
        return devices;
    }
}
