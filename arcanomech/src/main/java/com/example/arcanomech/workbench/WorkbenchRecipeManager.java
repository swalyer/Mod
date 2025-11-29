package com.example.arcanomech.workbench;

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
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public final class WorkbenchRecipeManager implements SimpleSynchronousResourceReloadListener {
    private static final WorkbenchRecipeManager INSTANCE = new WorkbenchRecipeManager();
    private static final String DIRECTORY = "recipes_workbench";

    private Map<Identifier, WorkbenchRecipe> recipes = Collections.emptyMap();

    private WorkbenchRecipeManager() {
    }

    public static WorkbenchRecipeManager getInstance() {
        return INSTANCE;
    }

    public Optional<WorkbenchRecipe> getFirstMatch(SimpleInventory inventory, World world) {
        if (inventory == null) {
            return Optional.empty();
        }
        for (WorkbenchRecipe recipe : recipes.values()) {
            if (recipe.matches(inventory, world)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    public Collection<WorkbenchRecipe> getRecipes() {
        return recipes.values();
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(Arcanomech.MOD_ID, "workbench_recipe_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<Identifier, Resource> resources = manager.findResources(DIRECTORY, path -> path.getPath().endsWith(".json"));
        Map<Identifier, WorkbenchRecipe> loaded = new LinkedHashMap<>();
        WorkbenchRecipeSerializer serializer = new WorkbenchRecipeSerializer();
        resources.forEach((resourceId, ignored) -> {
            Identifier recipeId = resolveRecipeId(resourceId);
            Resource resource = manager.getResource(resourceId).orElse(null);
            if (resource == null) {
                return;
            }
            try (InputStream in = resource.getInputStream(); InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonElement element = JsonHelper.deserialize(reader);
                if (!element.isJsonObject()) {
                    Arcanomech.LOGGER.warn("Skipping workbench recipe {} because it is not a JSON object", recipeId);
                    return;
                }
                JsonObject json = element.getAsJsonObject();
                WorkbenchRecipe recipe = serializer.read(recipeId, json);
                loaded.put(recipeId, recipe);
            } catch (IOException exception) {
                Arcanomech.LOGGER.error("Failed to load workbench recipe {}", recipeId, exception);
            }
        });
        recipes = loaded;
        Arcanomech.LOGGER.info("Loaded {} arcane workbench recipes", recipes.size());
    }

    private Identifier resolveRecipeId(Identifier fileId) {
        String path = fileId.getPath();
        String relative = path.substring(DIRECTORY.length() + 1, path.length() - ".json".length());
        return new Identifier(fileId.getNamespace(), relative);
    }
}
