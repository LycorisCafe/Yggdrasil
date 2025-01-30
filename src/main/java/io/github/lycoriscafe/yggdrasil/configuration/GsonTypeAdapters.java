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

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class GsonTypeAdapters {
    public static class Year implements JsonSerializer<java.time.Year>, JsonDeserializer<java.time.Year> {

        @Override
        public java.time.Year deserialize(JsonElement jsonElement,
                                          Type type,
                                          JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonElement == null ? null : java.time.Year.parse(jsonElement.getAsString(), Utils.getYearFormatter());
        }

        @Override
        public JsonElement serialize(java.time.Year year,
                                     Type type,
                                     JsonSerializationContext jsonSerializationContext) {
            return year == null ? null : new JsonPrimitive(year.format(Utils.getYearFormatter()));
        }
    }

    public static class Date implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonElement jsonElement,
                                     Type type,
                                     JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonElement == null ? null : LocalDate.parse(jsonElement.getAsString(), Utils.getDateFormatter());
        }

        @Override
        public JsonElement serialize(LocalDate localDate,
                                     Type type,
                                     JsonSerializationContext jsonSerializationContext) {
            return localDate == null ? null : new JsonPrimitive(localDate.format(Utils.getDateFormatter()));
        }
    }

    public static class Time implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
        @Override
        public LocalTime deserialize(JsonElement jsonElement,
                                     Type type,
                                     JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonElement == null ? null : LocalTime.parse(jsonElement.getAsString(), Utils.getTimeFormatter());
        }

        @Override
        public JsonElement serialize(LocalTime localTime,
                                     Type type,
                                     JsonSerializationContext jsonSerializationContext) {
            return localTime == null ? null : new JsonPrimitive(localTime.format(Utils.getTimeFormatter()));
        }
    }

    public static class DateTime implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonElement jsonElement,
                                         Type type,
                                         JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonElement == null ? null : LocalDateTime.parse(jsonElement.getAsString(), Utils.getDateTimeFormatter());
        }

        @Override
        public JsonElement serialize(LocalDateTime localDateTime,
                                     Type type,
                                     JsonSerializationContext jsonSerializationContext) {
            return localDateTime == null ? null : new JsonPrimitive(localDateTime.format(Utils.getDateTimeFormatter()));
        }
    }
}
