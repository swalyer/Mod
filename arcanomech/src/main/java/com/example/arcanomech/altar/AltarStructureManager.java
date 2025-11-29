package com.example.arcanomech.altar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.arcanomech.Arcanomech;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public final class AltarStructureManager implements SimpleSynchronousResourceReloadListener {
    private static final AltarStructureManager INSTANCE = new AltarStructureManager();
    private static final String DIRECTORY = "altar_structures";

    private Map<Identifier, AltarStructure> structures = Collections.emptyMap();

    private AltarStructureManager() {
    }

    public static AltarStructureManager getInstance() {
        return INSTANCE;
    }

    public Collection<AltarStructure> getStructures() {
        return structures.values();
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(Arcanomech.MOD_ID, "altar_structures");
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<Identifier, AltarStructure> loaded = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.findResources(DIRECTORY, path -> path.getPath().endsWith(".json"));
        resources.forEach((resourceId, ignored) -> {
            Identifier id = resolve(resourceId);
            Resource resource = manager.getResource(resourceId).orElse(null);
            if (resource == null) {
                return;
            }
            try (InputStream in = resource.getInputStream(); InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonElement element = JsonHelper.deserialize(reader);
                if (!element.isJsonObject()) {
                    Arcanomech.LOGGER.warn("Invalid altar structure {}", id);
                    return;
                }
                JsonObject json = element.getAsJsonObject();
                int radius = JsonHelper.getInt(json, "radius", 2);
                int min = JsonHelper.getInt(json, "min_pedestals", 6);
                int max = JsonHelper.getInt(json, "max_pedestals", 8);
                loaded.put(id, new AltarStructure(radius, min, max));
            } catch (IOException exception) {
                Arcanomech.LOGGER.error("Failed to load altar structure {}", id, exception);
            }
        });
        structures = loaded;
    }

    private Identifier resolve(Identifier fileId) {
        String path = fileId.getPath();
        String relative = path.substring(DIRECTORY.length() + 1, path.length() - ".json".length());
        return new Identifier(fileId.getNamespace(), relative);
    }
}
