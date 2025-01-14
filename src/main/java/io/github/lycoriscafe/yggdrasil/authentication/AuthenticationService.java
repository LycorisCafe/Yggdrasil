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
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.rest.admin.AccessLevel;
import lombok.Cleanup;
import lombok.NonNull;

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
import java.util.Base64;
import java.util.UUID;

public class AuthenticationService {
    public static HttpResponse authenticate(@NonNull HttpRequest httpRequest,
                                            @NonNull Role targetRole,
                                            AccessLevel accessLevel) {
        var httpResponse = new HttpResponse(httpRequest.getRequestId(), httpRequest.getRequestConsumer());
        if (httpRequest.getAuthorization() == null || httpRequest.getAuthorization().getAuthScheme() != AuthScheme.BEARER) {
            return httpResponse.setStatusCode(HttpStatusCode.BAD_REQUEST).addAuthentication(
                    new BearerAuthentication(BearerAuthorizationError.INVALID_REQUEST)
                            .setErrorDescription("Unsupported authentication method. Only supported 'Bearer'."));
        }
        var authRequest = (BearerAuthorization) httpRequest.getAuthorization();
        try {
            var authChecked = getAuthentication(TokenType.ACCESS_TOKEN, authRequest.getAccessToken());
            if (authChecked == null) {
                return httpResponse.setStatusCode(HttpStatusCode.UNAUTHORIZED).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INVALID_TOKEN)
                                .setErrorDescription("Invalid access token. Try again."));
            }
            if (Instant.now().getEpochSecond() > authChecked.getExpires().atZone(ZoneId.systemDefault()).toEpochSecond()) {
                return httpResponse.setStatusCode(HttpStatusCode.UNAUTHORIZED).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INVALID_TOKEN)
                                .setErrorDescription("Invalid access token. Token expired."));
            }
            if (authChecked.getRole() != targetRole) {
                return httpResponse.setStatusCode(HttpStatusCode.FORBIDDEN).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INSUFFICIENT_SCOPE).setScope(targetRole.toString())
                                .setErrorDescription("Insufficient scope. Contact your system admin for more details."));
            }
            if (authChecked.getDisabled()) {
                return httpResponse.setStatusCode(HttpStatusCode.UNAUTHORIZED).addAuthentication(
                        new BearerAuthentication(BearerAuthorizationError.INVALID_TOKEN)
                                .setErrorDescription("Target account is disabled. Contact your system admin for more details."));
            }
            if (targetRole == Role.ADMIN && accessLevel != null) {
                // TODO implement
//                var admin = AdminService.getAdmin(authChecked.getUserId());
//                if (!admin.getAccessLevel().contains(accessLevel)) {
//                    return httpResponse.setStatusCode(HttpStatusCode.FORBIDDEN).addAuthentication(
//                            new BearerAuthentication(BearerAuthorizationError.INSUFFICIENT_SCOPE).setScope(targetRole + "#" + admin.getAccessLevel())
//                                    .setErrorDescription("Insufficient scope. Contact your system admin for more details."));
//                }
            }
            return null;
        } catch (SQLException e) {
            return httpResponse.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    public static Authentication getAuthentication(@NonNull TokenType tokenType,
                                                   @NonNull String token) throws SQLException {
        @Cleanup
        var connection = Utils.getDatabaseConnection();
        @Cleanup
        var statement = connection.prepareStatement("SELECT * FROM authentication WHERE " + tokenType.toString().toLowerCase() + " = ?");
        statement.setString(1, token);
        @Cleanup
        var resultSet = statement.executeQuery();
        connection.commit();
        if (resultSet.next()) {
            var authentication = new Authentication();
            authentication.setRole(Role.valueOf(resultSet.getString("role")));
            authentication.setUserId(Long.parseLong(resultSet.getString("userId")));
            authentication.setPassword(resultSet.getString("password"));
            authentication.setAccessToken(resultSet.getString("accessToken"));
            authentication.setExpires(LocalDateTime.parse(resultSet.getString("expires")));
            authentication.setRefreshToken(resultSet.getString("refreshToken"));
            authentication.setDisabled(resultSet.getBoolean("disabled"));
            return authentication;
        }
        return null;
    }

    public static Authentication getAuthentication(@NonNull Role role,
                                                   @NonNull Long userId) throws SQLException {
        @Cleanup
        var connection = Utils.getDatabaseConnection();
        @Cleanup
        var statement = connection.prepareStatement("SELECT * FROM authentication WHERE role = ? AND userId = ?");
        statement.setString(1, role.toString());
        statement.setString(2, Long.toUnsignedString(userId));
        @Cleanup
        var resultSet = statement.executeQuery();
        connection.commit();
        if (resultSet.next()) {
            var authentication = new Authentication();
            authentication.setRole(Role.valueOf(resultSet.getString("role")));
            authentication.setUserId(Long.parseLong(resultSet.getString("userId")));
            authentication.setPassword(resultSet.getString("password"));
            authentication.setAccessToken(resultSet.getString("accessToken"));
            authentication.setExpires(LocalDateTime.parse(resultSet.getString("expires")));
            authentication.setRefreshToken(resultSet.getString("refreshToken"));
            authentication.setDisabled(resultSet.getBoolean("disabled"));
            return authentication;
        }
        return null;
    }

    public static Authentication createAuthentication(@NonNull Authentication authentication) throws SQLException {
        @Cleanup
        var connection = Utils.getDatabaseConnection();
        @Cleanup
        var statement = connection.prepareStatement("INSERT INTO authentication (role, userId, password) VALUES (?, ?, ?)");
        statement.setString(1, authentication.getRole().toString());
        statement.setString(2, Long.toUnsignedString(authentication.getUserId()));
        statement.setString(3, authentication.getPassword());
        if (statement.executeUpdate() != 1) {
            connection.rollback();
            return null;
        }
        connection.commit();
        return getAuthentication(authentication.getRole(), authentication.getUserId());
    }

    public static Authentication updateAuthentication(@NonNull Authentication authentication) throws SQLException {
        @Cleanup
        var connection = Utils.getDatabaseConnection();
        @Cleanup
        var statement = connection.prepareStatement("UPDATE authentication SET password = ?, accessToken = ?, expires = ?, refreshToken = ?, " +
                "disabled = ? WHERE role = ? AND userId = ?");
        statement.setString(1, authentication.getPassword());
        statement.setString(2, authentication.getAccessToken());
        statement.setString(3, Utils.getDateTimeFormatter().format(authentication.getExpires()));
        statement.setString(4, authentication.getRefreshToken());
        statement.setBoolean(5, authentication.getDisabled());
        statement.setString(6, authentication.getRole().toString());
        statement.setString(7, Long.toUnsignedString(authentication.getUserId()));
        if (statement.executeUpdate() != 1) {
            connection.rollback();
            return null;
        }
        connection.commit();
        return getAuthentication(authentication.getRole(), authentication.getUserId());
    }

    public static boolean deleteAuthentication(@NonNull Role role,
                                               @NonNull Long userId) throws SQLException {
        @Cleanup
        var connection = Utils.getDatabaseConnection();
        @Cleanup
        var statement = connection.prepareStatement("DELETE FROM authentication WHERE role = ? AND userId = ?");
        statement.setString(1, role.toString());
        statement.setString(2, Long.toUnsignedString(userId));
        if (statement.executeUpdate() != 1) {
            connection.rollback();
            return false;
        }
        connection.commit();
        return true;
    }

    public static Authentication updatePassword(@NonNull Authentication authentication) throws SQLException, NoSuchAlgorithmException {
        authentication.setPassword(encryptData(authentication.getPassword().getBytes(StandardCharsets.UTF_8)));
        return updateAuthentication(authentication);
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
