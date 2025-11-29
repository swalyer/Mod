package com.example.arcanomech.ritual;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.arcanomech.Arcanomech;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;

public final class RitualManager implements SimpleSynchronousResourceReloadListener {
    private static final RitualManager INSTANCE = new RitualManager();
    private static final String DIRECTORY = "altar/rituals";

    private Map<Identifier, RitualDefinition> rituals = Collections.emptyMap();

    private RitualManager() {
    }

    public static RitualManager getInstance() {
        return INSTANCE;
    }

    public List<RitualDefinition> getRituals() {
        return List.copyOf(rituals.values());
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(Arcanomech.MOD_ID, "altar_rituals");
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<Identifier, RitualDefinition> loaded = new LinkedHashMap<>();
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
                    return;
                }
                JsonObject json = element.getAsJsonObject();
                Identifier center = new Identifier(JsonHelper.getString(json, "center"));
                Identifier activation = new Identifier(JsonHelper.getString(json, "activation_item"));
                int manaCost = JsonHelper.getInt(json, "mana_cost", 0);
                List<RitualDefinition.RitualPedestal> pedestals = readPedestals(JsonHelper.getArray(json, "pedestals"));
                List<Ingredient> inputs = readIngredients(JsonHelper.getArray(json, "inputs"));
                ItemStack result = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
                RitualDefinition definition = new RitualDefinition(id, center, pedestals, inputs, activation, result, manaCost);
                loaded.put(id, definition);
            } catch (IOException exception) {
                Arcanomech.LOGGER.error("[Arcanomech] Failed to load ritual {}", id, exception);
            }
        });
        rituals = loaded;
        Arcanomech.LOGGER.info("[Arcanomech] Loaded {} altar rituals", rituals.size());
    }

    private Identifier resolve(Identifier fileId) {
        String path = fileId.getPath();
        String relative = path.substring(DIRECTORY.length() + 1, path.length() - ".json".length());
        return new Identifier(fileId.getNamespace(), relative);
    }

    private List<RitualDefinition.RitualPedestal> readPedestals(JsonArray array) {
        List<RitualDefinition.RitualPedestal> pedestals = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject obj = element.getAsJsonObject();
            JsonArray offsetArray = JsonHelper.getArray(obj, "offset");
            BlockPos offset = new BlockPos(offsetArray.get(0).getAsInt(), offsetArray.get(1).getAsInt(), offsetArray.get(2).getAsInt());
            Identifier blockId = new Identifier(JsonHelper.getString(obj, "block"));
            pedestals.add(new RitualDefinition.RitualPedestal(offset, blockId));
        }
        return pedestals;
    }

    private List<Ingredient> readIngredients(JsonArray array) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (JsonElement element : array) {
            ingredients.add(Ingredient.fromJson(element));
        }
        return ingredients;
    }
}
