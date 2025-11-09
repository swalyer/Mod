package com.example.arcanomech.aspects;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.example.arcanomech.Arcanomech;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public final class AspectSourceManager implements SimpleSynchronousResourceReloadListener {
    private static final AspectSourceManager INSTANCE = new AspectSourceManager();
    private static final String DIRECTORY = "aspect_sources";

    private Map<Item, Map<Identifier, Integer>> sources = Collections.emptyMap();

    private AspectSourceManager() {
    }

    public static AspectSourceManager getInstance() {
        return INSTANCE;
    }

    public Map<Identifier, Integer> getYields(Item item) {
        return sources.getOrDefault(item, Collections.emptyMap());
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(Arcanomech.MOD_ID, "aspect_sources");
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<Item, Map<Identifier, Integer>> loaded = new HashMap<>();
        Map<Identifier, Resource> resources = manager.findResources(DIRECTORY, path -> path.getPath().endsWith(".json"));
        resources.forEach((resourceId, resource) -> {
            try (java.io.InputStream in = resource.getInputStream(); java.io.InputStreamReader reader = new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)) {
                JsonElement element = JsonHelper.deserialize(reader);
                if (!element.isJsonObject()) {
                    return;
                }
                JsonObject json = element.getAsJsonObject();
                for (JsonElement entryElement : JsonHelper.getArray(json, "sources")) {
                    JsonObject entry = entryElement.getAsJsonObject();
                    Identifier itemId = new Identifier(JsonHelper.getString(entry, "item"));
                    Item item = Registries.ITEM.get(itemId);
                    if (item == null) {
                        Arcanomech.LOGGER.warn("Unknown aspect source item {}", itemId);
                        continue;
                    }
                    JsonObject yields = JsonHelper.getObject(entry, "yields");
                    Map<Identifier, Integer> map = loaded.computeIfAbsent(item, ignored -> new HashMap<>());
                    for (String key : yields.keySet()) {
                        Identifier aspectId = new Identifier(key);
                        int amount = yields.get(key).getAsInt();
                        map.put(aspectId, amount);
                    }
                }
            } catch (IOException exception) {
                Arcanomech.LOGGER.error("Failed to load aspect source {}", resourceId, exception);
            }
        });
        sources = loaded;
    }
}
