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

import io.github.lycoriscafe.nexus.http.core.headers.auth.AuthScheme;
import io.github.lycoriscafe.nexus.http.core.headers.auth.scheme.bearer.BearerAuthentication;
import io.github.lycoriscafe.nexus.http.core.headers.auth.scheme.bearer.BearerAuthorization;
import io.github.lycoriscafe.nexus.http.core.headers.auth.scheme.bearer.BearerAuthorizationError;
import io.github.lycoriscafe.nexus.http.core.headers.content.UrlEncodedData;
import io.github.lycoriscafe.nexus.http.core.statusCodes.HttpStatusCode;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpPostRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpRes.HttpResponse;
import io.github.lycoriscafe.yggdrasil.commons.*;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;
import io.github.lycoriscafe.yggdrasil.rest.admin.AccessLevel;
import io.github.lycoriscafe.yggdrasil.rest.admin.Admin;
import io.github.lycoriscafe.yggdrasil.rest.student.Student;
import io.github.lycoriscafe.yggdrasil.rest.teacher.Teacher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    public static HttpResponse authenticate(HttpRequest httpRequest,
                                            Set<Role> targetRoles,
                                            Set<AccessLevel> accessLevels) {
        Objects.requireNonNull(httpRequest);
        Objects.requireNonNull(targetRoles);

        var httpResponse = new HttpResponse(httpRequest.getRequestId(), httpRequest.getRequestConsumer());
        if (httpRequest.getAuthorization() == null || httpRequest.getAuthorization().getAuthScheme() != AuthScheme.BEARER) {
            return httpResponse.setStatusCode(HttpStatusCode.BAD_REQUEST).addAuthentication(
                    new BearerAuthentication(BearerAuthorizationError.INVALID_REQUEST)
                            .setErrorDescription(AuthError.UNSUPPORTED_AUTHENTICATION.toString()));
        }

        var authRequest = (BearerAuthorization) httpRequest.getAuthorization();
        try {
            var device = DeviceService.getDevices(TokenType.ACCESS_TOKEN, authRequest.getAccessToken());
            if (device == null || device.isEmpty()) {
                return httpResponse.setStatusCode(HttpStatusCode.UNAUTHORIZED).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INVALID_TOKEN)
                                .setErrorDescription(AuthError.INVALID_ACCESS_TOKEN.toString()));
            }
            if (Instant.now().getEpochSecond() > device.getFirst().getExpires()) {
                return httpResponse.setStatusCode(HttpStatusCode.UNAUTHORIZED).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INVALID_TOKEN)
                                .setErrorDescription(AuthError.ACCESS_TOKEN_EXPIRED.toString()));
            }

            if (!targetRoles.contains(device.getFirst().getRole())) {
                StringBuilder scope = new StringBuilder("[");
                targetRoles.forEach(role -> scope.append(role.toString()).append(","));
                scope.deleteCharAt(scope.length() - 1).append("]");
                return httpResponse.setStatusCode(HttpStatusCode.FORBIDDEN).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INSUFFICIENT_SCOPE).setScope(scope.toString())
                                .setErrorDescription(AuthError.INSUFFICIENT_SCOPE.toString()));
            }

            if (AuthenticationService.isAccountDisabled(device.getFirst().getRole(), device.getFirst().getUserId())) {
                return httpResponse.setStatusCode(HttpStatusCode.UNAUTHORIZED).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INVALID_TOKEN)
                                .setErrorDescription(AuthError.ACCOUNT_DISABLED.toString()));
            }

            if (targetRoles.contains(Role.ADMIN) && accessLevels != null) {
                var admin = CommonService.read(Admin.class, new RequestModel<Admin>()
                        .setSearchBy(Map.of(Admin.class.getDeclaredField("id"), Map.of(device.getFirst().getUserId(), false))));
                var accessLevel = admin.getData().getFirst().getAccessLevel();
                if (accessLevel.stream().noneMatch(accessLevel::contains)) {
                    return httpResponse.setStatusCode(HttpStatusCode.FORBIDDEN).addAuthentication(
                            new BearerAuthentication(BearerAuthorizationError.INSUFFICIENT_SCOPE)
                                    .setScope(Role.ADMIN + "#" + accessLevels)
                                    .setErrorDescription(AuthError.INSUFFICIENT_SCOPE.toString()));
                }
            }
            return null;
        } catch (SQLException | NoSuchFieldException e) {
            return httpResponse.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    public static Authentication getAuthentication(Role role,
                                                   BigInteger userId) throws SQLException {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userId);
        Authentication auth;
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("SELECT * FROM authentication WHERE role = ? AND userId = ?")) {
            statement.setString(1, role.toString());
            statement.setString(2, userId.toString());
            auth = deserialize(statement.executeQuery());
            connection.commit();
        }
        return auth;
    }

    public static boolean addAuthentication(Authentication auth) throws SQLException, NoSuchAlgorithmException {
        Objects.requireNonNull(auth);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO authentication VALUES(?, ?, ?)")) {
            statement.setString(1, auth.getRole().toString());
            statement.setString(2, auth.getUserId().toString());
            statement.setString(3, encryptData(auth.getPassword().getBytes(StandardCharsets.UTF_8)));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return false;
            }
            connection.commit();
            return true;
        }
    }

    public static <T extends Entity> Response<T> updateAuthentication(HttpPostRequest req) {
        Objects.requireNonNull(req);
        UrlEncodedData data = (UrlEncodedData) req.getContent().getData();
        if (!data.containsKey("oldPassword") || !data.containsKey("newPassword")) {
            return new Response<T>().setError(ResponseError.INVALID_CONTENT_PARAMETER);
        }

        var oldPassword = data.get("oldPassword");
        var newPassword = data.get("newPassword");

        if (newPassword.length() < YggdrasilConfig.getDefaultUserPasswordBoundary()[0]
                || newPassword.length() > YggdrasilConfig.getDefaultUserPasswordBoundary()[1]) {
            return new Response<T>().setError(ResponseError.INVALID_PASSWORD_LENGTH);
        }

        try {
            var devices = DeviceService.getDevices(TokenType.ACCESS_TOKEN, ((BearerAuthorization) req.getAuthorization()).getAccessToken());
            var authentication = AuthenticationService.getAuthentication(devices.getFirst().getRole(), devices.getFirst().getUserId());

            if (authentication.getPassword().equals(AuthenticationService.encryptData(oldPassword.getBytes(StandardCharsets.UTF_8)))) {
                return new Response<T>().setError(ResponseError.INVALID_OLD_PASSWORD);
            }
            authentication.setPassword(newPassword);

            try (var connection = Utils.getDatabaseConnection();
                 var statement = connection.prepareStatement("UPDATE authentication SET password = ? WHERE role = ? AND userId = ?")) {
                statement.setString(1, encryptData(authentication.getPassword().getBytes(StandardCharsets.UTF_8)));
                statement.setString(2, authentication.getRole().toString());
                statement.setString(3, authentication.getUserId().toString());
                if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
                }
                connection.commit();
                return new Response<T>().setSuccess(true);
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            logger.atError().log(e.getMessage());
            return new Response<T>().setError(ResponseError.INTERNAL_SYSTEM_ERROR);
        }
    }

    public static boolean deleteAuthentication(Role role,
                                               BigInteger userId) throws SQLException {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userId);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM authentication WHERE role = ? AND userId = ?")) {
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

    private static Authentication deserialize(ResultSet resultSet) throws SQLException {
        Authentication auth = null;
        try (resultSet) {
            if (resultSet.next()) {
                auth = new Authentication(
                        Role.valueOf(resultSet.getString("role")),
                        new BigInteger(resultSet.getString("userId")),
                        resultSet.getString("password")
                );
            }
        }
        return auth;
    }

    public static String generateToken() throws IOException, NoSuchAlgorithmException {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        var uuid = UUID.randomUUID().toString();
        byteArrayOutputStream.write(uuid.getBytes(StandardCharsets.UTF_8));

        byte[] random = new byte[16];
        var secureRandom = new SecureRandom();
        secureRandom.nextBytes(random);
        byteArrayOutputStream.write(random);

        return encryptData(byteArrayOutputStream.toByteArray());
    }

    public static String encryptData(byte[] data) throws NoSuchAlgorithmException {
        return Base64.getEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(data));
    }

    public static boolean isAccountDisabled(Role role,
                                            BigInteger userId) throws NoSuchFieldException {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userId);
        return switch (role) {
            case ADMIN -> {
                var response = CommonService.read(Admin.class, new RequestModel<Admin>()
                        .setSearchBy(Map.of(Admin.class.getDeclaredField("id"), Map.of(userId, false))));
                yield (response.isSuccess() && !response.getData().isEmpty()) ? response.getData().getFirst().getDisabled() : true;
            }
            case TEACHER -> {
                var response = CommonService.read(Teacher.class, new RequestModel<Teacher>()
                        .setSearchBy(Map.of(Teacher.class.getDeclaredField("id"), Map.of(userId, false))));
                yield (response.isSuccess() && !response.getData().isEmpty()) ? response.getData().getFirst().getDisabled() : true;
            }
            case STUDENT -> {
                var response = CommonService.read(Student.class, new RequestModel<Student>()
                        .setSearchBy(Map.of(Student.class.getDeclaredField("id"), Map.of(userId, false))));
                yield (response.isSuccess() && !response.getData().isEmpty()) ? response.getData().getFirst().getDisabled() : true;
            }
        };
    }
}
