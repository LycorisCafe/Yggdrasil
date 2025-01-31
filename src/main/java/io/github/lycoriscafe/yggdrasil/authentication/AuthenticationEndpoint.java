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
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.Instant;

@HttpEndpoint("/login")
public final class AuthenticationEndpoint {
    @BearerEndpoint(@POST("/"))
    public static BearerTokenResponse login(BearerTokenRequest tokenRequest)
            throws SQLException, NoSuchAlgorithmException, IOException, NoSuchFieldException {
        switch (tokenRequest.getGrantType()) {
            case "credentials" -> {
                if (tokenRequest.getParams().size() != 3 ||
                        !tokenRequest.getParams().containsKey("username") ||
                        !tokenRequest.getParams().containsKey("password") ||
                        !tokenRequest.getParams().containsKey("deviceName")) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_REQUEST)
                            .setErrorDescription("Required parameter expected. Try again.");
                }

                Role role;
                BigInteger userId;
                String username = tokenRequest.getParams().get("username");
                switch (username.toLowerCase().charAt(0)) {
                    case 'a' -> role = Role.ADMIN;
                    case 't' -> role = Role.TEACHER;
                    case 's' -> role = Role.STUDENT;
                    default -> {
                        return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_REQUEST)
                                .setErrorDescription("Invalid username. Recheck and try again.");
                    }
                }
                try {
                    userId = new BigInteger(username.substring(1));
                } catch (Exception e) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Invalid username. Recheck and try again.");
                }

                var auth = AuthenticationService.getAuthentication(role, userId);
                if (auth == null) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Client not found. Contact your system administrator.");
                }
                if (!auth.getPassword()
                        .equals(AuthenticationService.encryptData(tokenRequest.getParams().get("password").getBytes(StandardCharsets.UTF_8)))) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Invalid password. Try again.");
                }

                if (AuthenticationService.isAccountDisabled(auth.getRole(), auth.getUserId())) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Target account is disabled. Contact your system administrator.");
                }

                var devices = DeviceService.getDevices(auth.getRole(), auth.getUserId());
                if (devices.size() >= YggdrasilConfig.getMaxLoginDevices()) {
                    DeviceService.removeDevice(TokenType.REFRESH_TOKEN, devices.getFirst().getRefreshToken());
                }

                if (tokenRequest.getParams().get("deviceName").equals("self") || tokenRequest.getParams().get("deviceName").equals("all")) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Invalid device name. Try again.");
                }

                var accessToken = AuthenticationService.generateToken();
                var refreshToken = AuthenticationService.generateToken();
                DeviceService.addDevice(new Device(auth.getRole(), auth.getUserId(), tokenRequest.getParams().get("deviceName"),
                        accessToken, Instant.now().getEpochSecond() + YggdrasilConfig.getDefaultAuthTimeout(), refreshToken));

                return new BearerTokenSuccessResponse(accessToken)
                        .setExpiresIn(YggdrasilConfig.getDefaultAuthTimeout())
                        .setRefreshToken(refreshToken)
                        .setScope(role.toString());
            }
            case "refresh_token" -> {
                if (tokenRequest.getParams().size() != 1 ||
                        !tokenRequest.getParams().containsKey("token")) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_REQUEST)
                            .setErrorDescription("Required parameter expected. Try again.");
                }

                var devices = DeviceService.getDevices(TokenType.REFRESH_TOKEN,
                        AuthenticationService.encryptData(tokenRequest.getParams().get("token").getBytes(StandardCharsets.UTF_8)));
                if (devices.isEmpty()) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Client not found. Contact your system administrator.");
                }

                if (AuthenticationService.isAccountDisabled(devices.getFirst().getRole(), devices.getFirst().getUserId())) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription("Target account is disabled. Contact your system administrator.");
                }

                var accessToken = AuthenticationService.generateToken();
                DeviceService.updateDevice(devices.getFirst().setAccessToken(accessToken)
                        .setExpires(Instant.now().getEpochSecond() + YggdrasilConfig.getDefaultAuthTimeout()));

                return new BearerTokenSuccessResponse(accessToken)
                        .setExpiresIn(YggdrasilConfig.getDefaultAuthTimeout());
            }
            default -> {
                return new BearerTokenFailResponse(BearerTokenRequestError.UNSUPPORTED_GRANT_TYPE)
                        .setErrorDescription("Unsupported authentication method. Use 'Bearer' scheme.");
            }
        }
    }
}