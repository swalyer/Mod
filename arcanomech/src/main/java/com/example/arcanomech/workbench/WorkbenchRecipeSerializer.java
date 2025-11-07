package com.example.arcanomech.workbench;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.example.arcanomech.energy.Balance;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class WorkbenchRecipeSerializer implements RecipeSerializer<WorkbenchRecipe> {
    @Override
    public WorkbenchRecipe read(Identifier id, JsonObject json) {
        int manaCost = JsonHelper.getInt(json, "mana_cost");
        int workTime = JsonHelper.getInt(json, "work_time", Balance.WORKBENCH_DEFAULT_WORK_TIME);
        JsonArray inputsArray = JsonHelper.getArray(json, "inputs");
        List<WorkbenchRecipe.CountedIngredient> ingredients = new ArrayList<>();
        for (JsonElement element : inputsArray) {
            JsonObject obj = element.getAsJsonObject();
            int count = JsonHelper.getInt(obj, "count", 1);
            Ingredient ingredient = Ingredient.fromJson(obj);
            ingredients.add(new WorkbenchRecipe.CountedIngredient(ingredient, count));
        }
        ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "output"));
        return new WorkbenchRecipe(id, output, ingredients, manaCost, workTime);
    }

    @Override
    public WorkbenchRecipe read(Identifier id, PacketByteBuf buf) {
        return WorkbenchRecipe.read(id, buf);
    }

    @Override
    public void write(PacketByteBuf buf, WorkbenchRecipe recipe) {
        recipe.write(buf);
    }
}
