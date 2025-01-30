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

public enum AuthError {
    UNSUPPORTED_AUTHENTICATION,
    INVALID_ACCESS_TOKEN,
    ACCESS_TOKEN_EXPIRED,
    INSUFFICIENT_SCOPE,
    ACCOUNT_DISABLED,
    INVALID_PARAMETERS,
    REQUIRED_PARAMETER_MISSING,
    INVALID_USERNAME_FORMAT,
    CLIENT_NOT_FOUND,
    INVALID_PASSWORD,
    MAX_DEVICES_EXCEEDED,
    INVALID_REFRESH_TOKEN,
    UNSUPPORTED_GRANT_TYPE
}
