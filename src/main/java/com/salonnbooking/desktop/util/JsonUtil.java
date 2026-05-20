package com.salonnbooking.desktop.util;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (value, type, ctx) ->
                    value == null ? null : ctx.serialize(value.toString()))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, ctx) -> {
                if (json == null || json.isJsonNull()) {
                    return null;
                }
                String value = json.getAsString();
                return value == null || value.isBlank() ? null : LocalDate.parse(value);
            })
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (value, type, ctx) ->
                    value == null ? null : ctx.serialize(value.toString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, ctx) -> {
                if (json == null || json.isJsonNull()) {
                    return null;
                }
                String value = json.getAsString();
                return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
            })
            .create();

    private JsonUtil() {
    }

    public static Gson gson() {
        return GSON;
    }

    public static <T> T fromJson(String json, Class<T> cls) {
        return GSON.fromJson(json, cls);
    }

    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }
}
