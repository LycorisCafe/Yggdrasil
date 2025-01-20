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

import io.github.lycoriscafe.nexus.http.core.HttpEndpoint;
import io.github.lycoriscafe.nexus.http.core.headers.auth.scheme.bearer.*;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.POST;
import io.github.lycoriscafe.yggdrasil.configuration.YggdrasilConfig;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.Instant;

@HttpEndpoint("/login")
public class AuthenticationEndpoint {
    @BearerEndpoint(@POST("/"))
    public static BearerTokenResponse login(BearerTokenRequest tokenRequest) throws SQLException, NoSuchAlgorithmException, IOException {
        switch (tokenRequest.getGrantType()) {
            case "credentials" -> {
                if (tokenRequest.getParams().size() != 2) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_REQUEST)
                            .setErrorDescription("Invalid parameters detected. Please provide two parameters: username, password");
                }
                if (!tokenRequest.getParams().containsKey("username") || !tokenRequest.getParams().containsKey("password")) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_REQUEST)
                            .setErrorDescription("username/password parameter(s) missing");
                }

                Role role;
                BigDecimal userId;
                try {
                    String username = tokenRequest.getParams().get("username");
                    role = switch (username.toLowerCase().charAt(0)) {
                        case 'a' -> Role.ADMIN;
                        case 't' -> Role.TEACHER;
                        case 's' -> Role.STUDENT;
                        default -> throw new IllegalStateException("Unexpected value");
                    };
                    userId = new BigDecimal(username.substring(1));
                } catch (Exception e) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Invalid username. Try again.");
                }

                var auth = AuthenticationService.getAuthentication(role, userId);
                if (auth == null) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Client not found. Recheck credentials and try again.");
                }
                if (!auth.getPassword()
                        .equals(AuthenticationService.encryptData(tokenRequest.getParams().get("password").getBytes(StandardCharsets.UTF_8)))) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Invalid password. Try again.");
                }

                if (AuthenticationService.getIsAccountDisabled(auth.getRole(), auth.getUserId())) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Target account is disabled. Contact your system admin for more details.");
                }

                var accessToken = AuthenticationService.generateToken();
                var refreshToken = AuthenticationService.generateToken();
                auth.setAccessToken(accessToken)
                        .setExpires(Instant.now().getEpochSecond() + YggdrasilConfig.getDefaultAuthTimeout())
                        .setRefreshToken(refreshToken);
                auth = AuthenticationService.updateAuthentication(auth);
                if (auth == null) throw new RuntimeException("Failed to update authentication.");
                return new BearerTokenSuccessResponse(accessToken)
                        .setExpiresIn(YggdrasilConfig.getDefaultAuthTimeout())
                        .setRefreshToken(refreshToken)
                        .setScope(role.toString());
            }
            case "refresh_token" -> {
                if (tokenRequest.getParams().size() != 1) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_REQUEST)
                            .setErrorDescription("Invalid parameters detected. Please provide two parameter: token");
                }

                var token = tokenRequest.getParams().get("token");
                if (token == null) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_REQUEST)
                            .setErrorDescription("Invalid token. Try again.");
                }

                var auth = AuthenticationService.getAuthentication(TokenType.REFRESH_TOKEN, token);
                if (auth == null) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Client not found. Recheck refresh token and try again.");
                }
                if (!auth.getRefreshToken().equals(token)) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Invalid refresh token. Use credentials or try again.");
                }

                if (AuthenticationService.getIsAccountDisabled(auth.getRole(), auth.getUserId())) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Target account is disabled. Contact your system admin for more details.");
                }

                var accessToken = AuthenticationService.generateToken();
                auth.setAccessToken(accessToken).setExpires(Instant.now().getEpochSecond() + YggdrasilConfig.getDefaultAuthTimeout());
                auth = AuthenticationService.updateAuthentication(auth);
                if (auth == null) throw new RuntimeException("Failed to update authentication.");
                return new BearerTokenSuccessResponse(accessToken)
                        .setExpiresIn(YggdrasilConfig.getDefaultAuthTimeout())
                        .setRefreshToken(auth.getRefreshToken())
                        .setScope(auth.getRole().toString());
            }
            default -> {
                return new BearerTokenFailResponse(BearerTokenRequestError.UNSUPPORTED_GRANT_TYPE)
                        .setErrorDescription("Unsupported grant type. Use credentials/refresh_token instead.");
            }
        }
    }
}