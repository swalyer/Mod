package com.example.arcanomech.aspects;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.example.arcanomech.Arcanomech;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public final class AspectRegistry implements SimpleSynchronousResourceReloadListener {
    private static final AspectRegistry INSTANCE = new AspectRegistry();
    private static final String DIRECTORY = "aspects";

    private Map<Identifier, Aspect> aspects = Collections.emptyMap();

    private AspectRegistry() {
    }

    public static AspectRegistry getInstance() {
        return INSTANCE;
    }

    public Optional<Aspect> get(Identifier id) {
        return Optional.ofNullable(aspects.get(id));
    }

    public Collection<Aspect> getAspects() {
        return aspects.values();
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(Arcanomech.MOD_ID, "aspect_registry");
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<Identifier, Resource> resources = manager.findResources(DIRECTORY, path -> path.getPath().endsWith(".json"));
        Map<Identifier, Aspect> loaded = new LinkedHashMap<>();
        resources.forEach((resourceId, ignored) -> {
            Identifier aspectId = resolve(resourceId);
            Resource resource = manager.getResource(resourceId).orElse(null);
            if (resource == null) {
                return;
            }
            try (InputStream in = resource.getInputStream(); InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonElement element = JsonHelper.deserialize(reader);
                if (!element.isJsonObject()) {
                    Arcanomech.LOGGER.warn("Skipping aspect {} because it is not a JSON object", aspectId);
                    return;
                }
                JsonObject json = element.getAsJsonObject();
                String name = JsonHelper.getString(json, "name");
                String colorHex = JsonHelper.getString(json, "color");
                int color = (int) Long.parseLong(colorHex.substring(1), 16);
                loaded.put(aspectId, new Aspect(aspectId, color, "aspect." + aspectId.getNamespace() + "." + aspectId.getPath()));
            } catch (IOException | NumberFormatException exception) {
                Arcanomech.LOGGER.error("Failed to load aspect {}", aspectId, exception);
            }
        });
        aspects = loaded;
        Arcanomech.LOGGER.info("Loaded {} aspects", aspects.size());
    }

    private Identifier resolve(Identifier fileId) {
        String path = fileId.getPath();
        String relative = path.substring(DIRECTORY.length() + 1, path.length() - ".json".length());
        return new Identifier(fileId.getNamespace(), relative);
    }
}
