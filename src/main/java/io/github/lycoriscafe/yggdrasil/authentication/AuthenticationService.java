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
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpPatchRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpRes.HttpResponse;
import io.github.lycoriscafe.yggdrasil.commons.CommonService;
import io.github.lycoriscafe.yggdrasil.commons.Entity;
import io.github.lycoriscafe.yggdrasil.commons.ResponseModel;
import io.github.lycoriscafe.yggdrasil.commons.SearchModel;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;
import io.github.lycoriscafe.yggdrasil.rest.admin.AccessLevel;
import io.github.lycoriscafe.yggdrasil.rest.admin.Admin;
import io.github.lycoriscafe.yggdrasil.rest.admin.AdminService;
import io.github.lycoriscafe.yggdrasil.rest.student.Student;
import io.github.lycoriscafe.yggdrasil.rest.student.StudentService;
import io.github.lycoriscafe.yggdrasil.rest.teacher.Teacher;
import io.github.lycoriscafe.yggdrasil.rest.teacher.TeacherService;
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

public final class AuthenticationService {
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
                            .setErrorDescription("Unsupported authentication method. Use 'Bearer' scheme."));
        }

        var authRequest = (BearerAuthorization) httpRequest.getAuthorization();
        try {
            var device = DeviceService.getDevices(TokenType.ACCESS_TOKEN, authRequest.getAccessToken());
            if (device.isEmpty()) {
                return httpResponse.setStatusCode(HttpStatusCode.UNAUTHORIZED).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INVALID_TOKEN)
                                .setErrorDescription("Invalid access token. Check the token and try again."));
            }
            if (Instant.now().getEpochSecond() > device.getFirst().getExpires()) {
                return httpResponse.setStatusCode(HttpStatusCode.UNAUTHORIZED).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INVALID_TOKEN)
                                .setErrorDescription("Access token expired. Update the token and try again."));
            }

            if (!targetRoles.contains(device.getFirst().getRole())) {
                StringBuilder scope = new StringBuilder("[");
                targetRoles.forEach(role -> scope.append(role.toString()).append(","));
                scope.deleteCharAt(scope.length() - 1).append("]");
                return httpResponse.setStatusCode(HttpStatusCode.FORBIDDEN).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INSUFFICIENT_SCOPE).setScope(scope.toString())
                                .setErrorDescription("Insufficient scope. Contact your system administrator."));
            }

            if (AuthenticationService.isAccountDisabled(device.getFirst().getRole(), device.getFirst().getUserId())) {
                return httpResponse.setStatusCode(HttpStatusCode.UNAUTHORIZED).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INVALID_TOKEN)
                                .setErrorDescription("Target account is disabled. Contact your system administrator."));
            }

            if (targetRoles.contains(Role.ADMIN) && accessLevels != null) {
                var admin = CommonService.read(Admin.class, AdminService.class, new SearchModel()
                        .setSearchBy(Map.of("id", Map.of(device.getFirst().getUserId().toString(), false))));
                var accessLevel = admin.getData().getFirst().getAccessLevel();
                if (accessLevels.stream().noneMatch(accessLevel::contains)) {
                    return httpResponse.setStatusCode(HttpStatusCode.FORBIDDEN).addAuthentication(
                            new BearerAuthentication(BearerAuthorizationError.INSUFFICIENT_SCOPE)
                                    .setScope(Role.ADMIN + "#" + accessLevels)
                                    .setErrorDescription("Insufficient scope. Contact your system administrator."));
                }
            }
            return null;
        } catch (SQLException | NoSuchFieldException e) {
            e.printStackTrace(System.err);
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

    public static void addAuthentication(Authentication auth) throws SQLException, NoSuchAlgorithmException {
        Objects.requireNonNull(auth);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO authentication VALUES(?, ?, ?)")) {
            statement.setString(1, auth.getRole().toString());
            statement.setString(2, auth.getUserId().toString());
            statement.setString(3, encryptData(auth.getPassword().getBytes(StandardCharsets.UTF_8)));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                throw new RuntimeException("Authentication adding failed");
            }
            connection.commit();
        }
    }

    public static void updateAuthentication(Authentication authentication) throws SQLException, NoSuchAlgorithmException {
        Objects.requireNonNull(authentication);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("UPDATE authentication SET password = ? WHERE role = ? AND userId = ?")) {
            statement.setString(1, encryptData(authentication.getPassword().getBytes(StandardCharsets.UTF_8)));
            statement.setString(2, authentication.getRole().toString());
            statement.setString(3, authentication.getUserId().toString());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                throw new RuntimeException("Authentication updating failed");
            }
            connection.commit();
        }
    }

    public static <T extends Entity> ResponseModel<T> updateAuthentication(HttpPatchRequest req,
                                                                           Role role,
                                                                           boolean selfUpdate) {
        Objects.requireNonNull(req);
        Objects.requireNonNull(role);
        UrlEncodedData data = (UrlEncodedData) req.getContent().getData();
        if ((selfUpdate && !data.containsKey("oldPassword")) || !data.containsKey("newPassword")) {
            return new ResponseModel<T>().setError("Required parameters missing");
        }

        var oldPassword = data.get("oldPassword");
        var newPassword = data.get("newPassword");
        if (newPassword.length() < YggdrasilConfig.getDefaultUserPasswordBoundary()[0]
                || newPassword.length() > YggdrasilConfig.getDefaultUserPasswordBoundary()[1]) {
            return new ResponseModel<T>().setError("Password length out of bound " + Arrays.toString(YggdrasilConfig.getDefaultUserPasswordBoundary()));
        }

        try {
            if (!selfUpdate) {
                var authentication = AuthenticationService.getAuthentication(role, new BigInteger(req.getParameters().get("userId")));
                updateAuthentication(authentication.setPassword(newPassword));
                return new ResponseModel<T>().setSuccess(true);
            }

            var devices = DeviceService.getDevices(TokenType.ACCESS_TOKEN, ((BearerAuthorization) req.getAuthorization()).getAccessToken());
            var authentication = AuthenticationService.getAuthentication(devices.getFirst().getRole(), devices.getFirst().getUserId());
            if (!authentication.getPassword().equals(AuthenticationService.encryptData(oldPassword.getBytes(StandardCharsets.UTF_8)))) {
                return new ResponseModel<T>().setError("oldPassword doesn't match");
            }
            updateAuthentication(authentication.setPassword(newPassword));
            return new ResponseModel<T>().setSuccess(true);
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace(System.err);
            return new ResponseModel<T>().setError("Internal system error");
        }
    }

    public static void deleteAuthentication(Role role,
                                            BigInteger userId) throws SQLException {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userId);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM authentication WHERE role = ? AND userId = ?")) {
            statement.setString(1, role.toString());
            statement.setString(2, userId.toString());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                throw new RuntimeException("Authentication deleting failed");
            }
            connection.commit();
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
                var response = CommonService.read(Admin.class, AdminService.class, new SearchModel()
                        .setSearchBy(Map.of("id", Map.of(userId.toString(), false))));
                yield (response.isSuccess() && !response.getData().isEmpty()) ? response.getData().getFirst().getDisabled() : true;
            }
            case TEACHER -> {
                var response = CommonService.read(Teacher.class, TeacherService.class, new SearchModel()
                        .setSearchBy(Map.of("id", Map.of(userId.toString(), false))));
                yield (response.isSuccess() && !response.getData().isEmpty()) ? response.getData().getFirst().getDisabled() : true;
            }
            case STUDENT -> {
                var response = CommonService.read(Student.class, StudentService.class, new SearchModel()
                        .setSearchBy(Map.of("id", Map.of(userId.toString(), false))));
                yield (response.isSuccess() && !response.getData().isEmpty()) ? response.getData().getFirst().getDisabled() : true;
            }
        };
    }
}
