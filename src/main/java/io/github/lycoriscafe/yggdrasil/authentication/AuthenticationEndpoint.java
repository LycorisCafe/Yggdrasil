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
public class AuthenticationEndpoint {
    @BearerEndpoint(@POST("/"))
    public static BearerTokenResponse login(BearerTokenRequest tokenRequest)
            throws SQLException, NoSuchAlgorithmException, IOException, NoSuchFieldException {
        switch (tokenRequest.getGrantType()) {
            case "credentials" -> {
                if (tokenRequest.getParams().size() != 3) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_REQUEST)
                            .setErrorDescription(AuthError.INVALID_PARAMETERS.toString());
                }
                if (!tokenRequest.getParams().containsKey("username") ||
                        !tokenRequest.getParams().containsKey("password") ||
                        !tokenRequest.getParams().containsKey("deviceName")) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_REQUEST)
                            .setErrorDescription(AuthError.REQUIRED_PARAMETER_MISSING.toString());
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
                                .setErrorDescription(AuthError.INVALID_USERNAME_FORMAT.toString());
                    }
                }
                try {
                    userId = new BigInteger(username.substring(1));
                } catch (Exception e) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription(AuthError.INVALID_USERNAME_FORMAT.toString());
                }

                var auth = AuthenticationService.getAuthentication(role, userId);
                if (auth == null) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription(AuthError.CLIENT_NOT_FOUND.toString());
                }
                if (!auth.getPassword()
                        .equals(AuthenticationService.encryptData(tokenRequest.getParams().get("password").getBytes(StandardCharsets.UTF_8)))) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription(AuthError.INVALID_PASSWORD.toString());
                }

                if (AuthenticationService.isAccountDisabled(auth.getRole(), auth.getUserId())) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription(AuthError.ACCOUNT_DISABLED.toString());
                }

                var devices = DeviceService.getDevices(auth.getRole(), auth.getUserId());
                if (devices.size() == YggdrasilConfig.getMaxLoginDevices()) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_REQUEST)
                            .setErrorDescription(AuthError.MAX_DEVICES_EXCEEDED.toString());
                }

                var accessToken = AuthenticationService.generateToken();
                var refreshToken = AuthenticationService.generateToken();
                if (!DeviceService.addDevice(new Device(auth.getRole(), auth.getUserId(), tokenRequest.getParams().get("deviceName"), accessToken,
                        Instant.now().getEpochSecond() + YggdrasilConfig.getDefaultAuthTimeout(), refreshToken))) {
                    throw new RuntimeException("Failed to add device");
                }

                return new BearerTokenSuccessResponse(accessToken)
                        .setExpiresIn(YggdrasilConfig.getDefaultAuthTimeout())
                        .setRefreshToken(refreshToken)
                        .setScope(role.toString());
            }
            case "refresh_token" -> {
                if (tokenRequest.getParams().size() != 1) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_REQUEST)
                            .setErrorDescription(AuthError.INVALID_PARAMETERS.toString());
                }
                if (!tokenRequest.getParams().containsKey("token")) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_REQUEST)
                            .setErrorDescription(AuthError.REQUIRED_PARAMETER_MISSING.toString());
                }

                var devices = DeviceService.getDevices(TokenType.REFRESH_TOKEN, tokenRequest.getParams().get("token"));
                if (devices.isEmpty()) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription(AuthError.CLIENT_NOT_FOUND.toString());
                }
                if (!devices.getFirst().getRefreshToken().equals(
                        AuthenticationService.encryptData(tokenRequest.getParams().get("token").getBytes(StandardCharsets.UTF_8)))) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription(AuthError.INVALID_REFRESH_TOKEN.toString());
                }

                if (AuthenticationService.isAccountDisabled(devices.getFirst().getRole(), devices.getFirst().getUserId())) {
                    return new BearerTokenFailResponse(BearerTokenRequestError.INVALID_CLIENT)
                            .setErrorDescription(AuthError.ACCOUNT_DISABLED.toString());
                }

                var accessToken = AuthenticationService.generateToken();
                if (!DeviceService.updateDevice(devices.getFirst().setAccessToken(accessToken)
                        .setExpires(Instant.now().getEpochSecond() + YggdrasilConfig.getDefaultAuthTimeout()))) {
                    throw new RuntimeException("Failed to update device");
                }

                return new BearerTokenSuccessResponse(accessToken)
                        .setExpiresIn(YggdrasilConfig.getDefaultAuthTimeout());
            }
            default -> {
                return new BearerTokenFailResponse(BearerTokenRequestError.UNSUPPORTED_GRANT_TYPE)
                        .setErrorDescription(AuthError.UNSUPPORTED_GRANT_TYPE.toString());
            }
        }
    }
}