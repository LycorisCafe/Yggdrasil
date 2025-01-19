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
import io.github.lycoriscafe.nexus.http.core.statusCodes.HttpStatusCode;
import io.github.lycoriscafe.nexus.http.engine.ReqResManager.httpReq.HttpRequest;
import io.github.lycoriscafe.nexus.http.engine.ReqResManager.httpRes.HttpResponse;
import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.rest.admin.AccessLevel;
import io.github.lycoriscafe.yggdrasil.rest.admin.Admin;
import io.github.lycoriscafe.yggdrasil.rest.admin.AdminService;
import io.github.lycoriscafe.yggdrasil.rest.student.StudentService;
import io.github.lycoriscafe.yggdrasil.rest.teacher.TeacherService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class AuthenticationService {
    public static HttpResponse authenticate(HttpRequest httpRequest,
                                            Role[] targetRoles,
                                            AccessLevel... accessLevels) {
        Objects.requireNonNull(httpRequest);
        Objects.requireNonNull(targetRoles);
        Set<Role> roles = new HashSet<>(Arrays.asList(targetRoles));
        var httpResponse = new HttpResponse(httpRequest.getRequestId(), httpRequest.getRequestConsumer());
        if (httpRequest.getAuthorization() == null || httpRequest.getAuthorization().getAuthScheme() != AuthScheme.BEARER) {
            return httpResponse.setStatusCode(HttpStatusCode.BAD_REQUEST).addAuthentication(
                    new BearerAuthentication(BearerAuthorizationError.INVALID_REQUEST)
                            .setErrorDescription("Unsupported authentication method. Only supported 'Bearer'."));
        }
        var authRequest = (BearerAuthorization) httpRequest.getAuthorization();
        try {
            var auth = getAuthentication(TokenType.ACCESS_TOKEN, authRequest.getAccessToken());
            if (auth == null) {
                return httpResponse.setStatusCode(HttpStatusCode.UNAUTHORIZED).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INVALID_TOKEN)
                                .setErrorDescription("Invalid access token. Try again."));
            }
            if (Instant.now().getEpochSecond() > auth.getExpires().atZone(ZoneId.systemDefault()).toEpochSecond()) {
                return httpResponse.setStatusCode(HttpStatusCode.UNAUTHORIZED).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INVALID_TOKEN)
                                .setErrorDescription("Invalid access token. Token expired."));
            }
            if (!roles.contains(auth.getRole())) {
                StringBuilder scope = new StringBuilder("[");
                roles.forEach(role -> scope.append(role.toString()).append(","));
                scope.deleteCharAt(scope.length() - 1).append("]");
                return httpResponse.setStatusCode(HttpStatusCode.FORBIDDEN).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INSUFFICIENT_SCOPE).setScope(scope.toString())
                                .setErrorDescription("Insufficient scope. Contact your system admin for more details."));
            }

            var disabled = switch (auth.getRole()) {
                case ADMIN -> AdminService.getAdminById(auth.getUserId()).getData().getFirst().getDisabled();
                case TEACHER -> TeacherService.getTeacherById(auth.getUserId()).getData().getFirst().getDisabled();
                case STUDENT -> StudentService.getStudentById(auth.getUserId()).getData().getFirst().getDisabled();
            };
            if (disabled) {
                return httpResponse.setStatusCode(HttpStatusCode.UNAUTHORIZED).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INVALID_TOKEN)
                                .setErrorDescription("Target account is disabled. Contact your system admin for more details."));
            }

            if (roles.contains(Role.ADMIN) && accessLevels != null) {
                var admin = AdminService.getAdminById(auth.getUserId());
                var accessLevel = admin.getData().getFirst().getAccessLevel();
                if (!accessLevel.containsAll(List.of(accessLevels))) {
                    return httpResponse.setStatusCode(HttpStatusCode.FORBIDDEN).addAuthentication(
                            new BearerAuthentication(BearerAuthorizationError.INSUFFICIENT_SCOPE).setScope(Role.ADMIN + "#" + Arrays.toString(accessLevels))
                                    .setErrorDescription("Insufficient scope. Contact your system admin for more details."));
                }
            }
            return null;
        } catch (SQLException e) {
            return httpResponse.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    public static Authentication getAuthentication(TokenType tokenType,
                                                   String token) throws SQLException {
        Objects.requireNonNull(tokenType);
        Objects.requireNonNull(token);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("SELECT * FROM authentication WHERE " + tokenType.toString()
                     .toLowerCase() + " LIKE BINARY ?")) {
            statement.setString(1, token);
            try (var resultSet = statement.executeQuery()) {
                connection.commit();
                if (resultSet.next()) {
                    return new Authentication(
                            Role.valueOf(resultSet.getString("role")),
                            Long.parseLong(resultSet.getString("userId")),
                            resultSet.getString("password"))
                            .setAccessToken(resultSet.getString("accessToken"))
                            .setExpires(resultSet.getString("expires") == null ?
                                    null : LocalDateTime.parse(resultSet.getString("expires"), Utils.getDateTimeFormatter()))
                            .setRefreshToken(resultSet.getString("refreshToken"));
                }
                return null;
            }
        }
    }

    public static Authentication getAuthentication(Role role,
                                                   Long userId) throws SQLException {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userId);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("SELECT * FROM authentication WHERE role = ? AND userId = ?")) {
            statement.setString(1, role.toString());
            statement.setString(2, Long.toUnsignedString(userId));
            Authentication authentication = null;
            try (var resultSet = statement.executeQuery()) {
                connection.commit();
                if (resultSet.next()) {
                    authentication = new Authentication(
                            Role.valueOf(resultSet.getString("role")),
                            Long.parseLong(resultSet.getString("userId")),
                            resultSet.getString("password"))
                            .setAccessToken(resultSet.getString("accessToken"))
                            .setExpires(resultSet.getString("expires") == null ?
                                    null : LocalDateTime.parse(resultSet.getString("expires"), Utils.getDateTimeFormatter()))
                            .setRefreshToken(resultSet.getString("refreshToken"));
                }
            }
            return authentication;
        }
    }

    public static Authentication createAuthentication(Authentication authentication) throws SQLException {
        Objects.requireNonNull(authentication);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("INSERT INTO authentication (role, userId, password) VALUES (?, ?, ?)")) {
            statement.setString(1, authentication.getRole().toString());
            statement.setString(2, Long.toUnsignedString(authentication.getUserId()));
            statement.setString(3, authentication.getPassword());
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return null;
            }
            connection.commit();
        }
        return getAuthentication(authentication.getRole(), authentication.getUserId());
    }

    public static Authentication updateAuthentication(Authentication authentication) throws SQLException {
        Objects.requireNonNull(authentication);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("UPDATE authentication SET password = ?, accessToken = ?, expires = ?, refreshToken = ? " +
                     "WHERE role = ? AND userId = ?")) {
            statement.setString(1, authentication.getPassword());
            statement.setString(2, authentication.getAccessToken());
            statement.setString(3, authentication.getExpires() == null ?
                    null : Utils.getDateTimeFormatter().format(authentication.getExpires()));
            statement.setString(4, authentication.getRefreshToken());
            statement.setString(5, authentication.getRole().toString());
            statement.setString(6, Long.toUnsignedString(authentication.getUserId()));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
                return null;
            }
            connection.commit();
        }
        return getAuthentication(authentication.getRole(), authentication.getUserId());
    }

    public static void deleteAuthentication(Role role,
                                            Long userId) {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userId);
        try (var connection = Utils.getDatabaseConnection();
             var statement = connection.prepareStatement("DELETE FROM authentication WHERE role = ? AND userId = ?")) {
            statement.setString(1, role.toString());
            statement.setString(2, Long.toUnsignedString(userId));
            if (statement.executeUpdate() != 1) {
                connection.rollback();
            }
            connection.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Authentication updatePassword(Authentication authentication) throws SQLException, NoSuchAlgorithmException {
        Objects.requireNonNull(authentication);
        authentication.setPassword(encryptData(authentication.getPassword().getBytes(StandardCharsets.UTF_8)));
        return updateAuthentication(authentication);
    }

    public static Response<?> logoutFromAll(Role role,
                                            Long id) {
        Objects.requireNonNull(role);
        Objects.requireNonNull(id);
        try {
            var auth = AuthenticationService.getAuthentication(role, id);
            if (auth == null) return new Response<>().setError("Invalid ID");
            if (AuthenticationService.updateAuthentication(auth.setAccessToken(null).setExpires(null).setRefreshToken(null)) == null) {
                return new Response<Admin>().setError("Internal server error");
            }
            return new Response<>().setSuccess(true);
        } catch (Exception e) {
            return new Response<>().setError(e.getMessage());
        }
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
}
