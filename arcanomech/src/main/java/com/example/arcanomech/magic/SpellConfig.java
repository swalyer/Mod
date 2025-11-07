package com.example.arcanomech.magic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class SpellConfig {
    private final int cost;
    private final int cooldown;
    private final JsonObject extras;

    public SpellConfig(int cost, int cooldown, JsonObject extras) {
        this.cost = Math.max(0, cost);
        this.cooldown = Math.max(0, cooldown);
        this.extras = extras == null ? new JsonObject() : extras;
    }

    public int cost() {
        return cost;
    }

    public int cooldown() {
        return cooldown;
    }

    public int getInt(String key, int fallback) {
        if (extras.has(key)) {
            JsonElement element = extras.get(key);
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                return element.getAsInt();
            }
        }
        return fallback;
    }

    public double getDouble(String key, double fallback) {
        if (extras.has(key)) {
            JsonElement element = extras.get(key);
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                return element.getAsDouble();
            }
        }
        return fallback;
    }

    public JsonObject extras() {
        return extras;
    }
}
