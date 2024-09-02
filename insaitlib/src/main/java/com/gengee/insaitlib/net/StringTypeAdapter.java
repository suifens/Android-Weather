package com.gengee.insaitlib.net;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class StringTypeAdapter implements JsonSerializer<String>, JsonDeserializer<String> {

    private static final String TAG = StringTypeAdapter.class.getSimpleName();

    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json.getAsJsonPrimitive().isString()) {
            return json.getAsString();
        } else if (json.getAsJsonPrimitive().isNumber()) {
            return String.valueOf(json.getAsJsonPrimitive().getAsNumber());
        } else if (json.getAsJsonPrimitive().isBoolean()) {
            return String.valueOf(json.getAsJsonPrimitive().getAsBoolean());
        } else if (json.getAsJsonPrimitive().isJsonNull() || json.getAsString().isEmpty()) {
            return "";
        } else {
            return "";
        }
    }

    @Override
    public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src);
    }
}
