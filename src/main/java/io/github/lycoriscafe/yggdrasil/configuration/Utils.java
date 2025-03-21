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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;

public final class Utils {
    private static final String YEAR_FORMAT = "yyyy";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final Gson GSON_INSTANCE = new GsonBuilder()
            .serializeNulls()
            .setDateFormat(DATE_TIME_FORMAT)
            .registerTypeAdapter(Year.class, new GsonTypeAdapters.Year())
            .registerTypeAdapter(LocalDate.class, new GsonTypeAdapters.Date())
            .registerTypeAdapter(LocalTime.class, new GsonTypeAdapters.Time())
            .registerTypeAdapter(LocalDateTime.class, new GsonTypeAdapters.DateTime())
            .create();

    public static Connection getDatabaseConnection() throws SQLException {
        return YggdrasilConfig.getDatabase().getConnection();
    }

    public static DateTimeFormatter getYearFormatter() {
        return DateTimeFormatter.ofPattern(YEAR_FORMAT);
    }

    public static DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    }

    public static DateTimeFormatter getDateFormatter() {
        return DateTimeFormatter.ofPattern(DATE_FORMAT);
    }

    public static DateTimeFormatter getTimeFormatter() {
        return DateTimeFormatter.ofPattern(TIME_FORMAT);
    }

    public static Gson getGson() {
        return GSON_INSTANCE;
    }
}
