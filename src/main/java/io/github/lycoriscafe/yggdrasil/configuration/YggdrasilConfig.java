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

package io.github.lycoriscafe.yggdrasil.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.lycoriscafe.nexus.http.HttpServer;
import io.github.lycoriscafe.nexus.http.core.headers.auth.scheme.bearer.BearerAuthentication;
import io.github.lycoriscafe.nexus.http.helper.configuration.HttpServerConfiguration;
import io.github.lycoriscafe.nexus.http.helper.scanners.ScannerException;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class YggdrasilConfig {
    private static HikariDataSource database;
    private static HttpServer httpServer;
    private static Long defaultResultsOffset = 50L;
    private static Long defaultAuthTimeout = 3600L;
    private static Integer[] defaultUserPasswordBoundary = {8, 50};

    public static void initialize() throws IOException, ScannerException, SQLException, URISyntaxException {
        initializeDatabase();
        initializeHttpServer();

        Path path = Paths.get(Objects.requireNonNull(YggdrasilConfig.class.getResource("/yggdrasil.properties")).toURI()).toAbsolutePath();
        try (FileInputStream inputStream = new FileInputStream(path.toFile())) {
            Properties properties = new Properties();
            properties.load(inputStream);

            String defaultResultsOffsetString = properties.getProperty("defaultResultsOffset");
            if (defaultResultsOffsetString != null) defaultResultsOffset = Long.parseLong(defaultResultsOffsetString);

            String defaultAuthTimeoutString = properties.getProperty("defaultAuthTimeout");
            if (defaultAuthTimeoutString != null) defaultAuthTimeout = Long.parseLong(defaultAuthTimeoutString);

            String defaultUserPasswordBoundaryString = properties.getProperty("defaultUserPasswordBoundary");
            if (defaultUserPasswordBoundaryString != null) {
                String[] userPasswordBoundary = defaultUserPasswordBoundaryString.split(",");
                if (userPasswordBoundary.length != 2) throw new IllegalArgumentException("Invalid defaultUserPasswordBoundary");
                defaultUserPasswordBoundary = new Integer[]{Integer.parseInt(userPasswordBoundary[0]), Integer.parseInt(userPasswordBoundary[1])};
            }
        }
    }

    private static void initializeDatabase() throws URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(YggdrasilConfig.class.getResource("/hikari.properties")).toURI()).toAbsolutePath();
        var hikariConfig = new HikariConfig(path.toString());
        database = new HikariDataSource(hikariConfig);
    }

    private static void initializeHttpServer() throws ScannerException, SQLException, IOException {
        var httpServerConfiguration = new HttpServerConfiguration("io.github.lycoriscafe.yggdrasil", "YggdrasilTemp")
                .setUrlPrefix("/api/v1")
                .addDefaultAuthentication(new BearerAuthentication("Access for Yggdrasil API"));
        httpServer = new HttpServer(httpServerConfiguration).initialize();
    }

    public static HikariDataSource getDatabase() {
        return database;
    }

    public static HttpServer getHttpServer() {
        return httpServer;
    }

    public static Long getDefaultResultsOffset() {
        return defaultResultsOffset;
    }

    public static Long getDefaultAuthTimeout() {
        return defaultAuthTimeout;
    }

    public static Integer[] getDefaultUserPasswordBoundary() {
        return defaultUserPasswordBoundary;
    }
}
