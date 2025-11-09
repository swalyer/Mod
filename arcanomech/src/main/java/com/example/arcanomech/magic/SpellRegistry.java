package com.example.arcanomech.magic;

import java.io.IOException;
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

public final class SpellRegistry implements SimpleSynchronousResourceReloadListener {
    private static final SpellRegistry INSTANCE = new SpellRegistry();
    private static final String DIRECTORY = "spells";

    private final Map<Identifier, Spell> spells = new LinkedHashMap<>();
    private Map<Identifier, SpellConfig> configs = Collections.emptyMap();

    private SpellRegistry() {
    }

    public static SpellRegistry getInstance() {
        return INSTANCE;
    }

    public void register(Spell spell) {
        spells.put(spell.getId(), spell);
    }

    public Optional<Spell> getSpell(SpellId id) {
        return Optional.ofNullable(spells.get(id.id()));
    }

    public Optional<SpellConfig> getConfig(SpellId id) {
        return Optional.ofNullable(configs.get(id.id()));
    }

    public Collection<Spell> getSpells() {
        return spells.values();
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(Arcanomech.MOD_ID, "spell_registry");
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<Identifier, Resource> resources = manager.findResources(DIRECTORY, path -> path.getPath().endsWith(".json"));
        Map<Identifier, SpellConfig> loaded = new LinkedHashMap<>();
        resources.forEach((resourceId, resource) -> {
            Identifier spellId = resolveSpellId(resourceId);
            try (java.io.InputStream in = resource.getInputStream(); java.io.InputStreamReader reader = new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)) {
                JsonElement element = JsonHelper.deserialize(reader);
                if (!element.isJsonObject()) {
                    Arcanomech.LOGGER.warn("Skipping spell config {} because it is not a JSON object", spellId);
                    return;
                }
                JsonObject json = element.getAsJsonObject();
                int cost = JsonHelper.getInt(json, "cost");
                int cooldown = JsonHelper.getInt(json, "cooldown");
                JsonObject extras = new JsonObject();
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if (!"cost".equals(entry.getKey()) && !"cooldown".equals(entry.getKey())) {
                        extras.add(entry.getKey(), entry.getValue());
                    }
                }
                loaded.put(spellId, new SpellConfig(cost, cooldown, extras));
            } catch (IOException exception) {
                Arcanomech.LOGGER.error("Failed to load spell config {}", spellId, exception);
            }
        });
        configs = loaded;
        Arcanomech.LOGGER.info("Loaded {} spell definitions", configs.size());
    }

    private Identifier resolveSpellId(Identifier fileId) {
        String path = fileId.getPath();
        String relative = path.substring(DIRECTORY.length() + 1, path.length() - ".json".length());
        return new Identifier(fileId.getNamespace(), relative);
    }
}
