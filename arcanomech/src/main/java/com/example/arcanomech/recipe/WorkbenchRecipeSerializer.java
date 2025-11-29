package com.example.arcanomech.recipe;

import com.example.arcanomech.energy.Balance;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

public class WorkbenchRecipeSerializer implements RecipeSerializer<WorkbenchRecipe> {
    private static final int SLOT_COUNT = 6;

    @Override
    public WorkbenchRecipe read(Identifier id, JsonObject json) {
        JsonArray ingredientArray = JsonHelper.getArray(json, "ingredients");
        if (ingredientArray.size() != SLOT_COUNT) {
            throw new IllegalArgumentException("Arcane workbench recipes require exactly six ingredients");
        }
        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(SLOT_COUNT, Ingredient.EMPTY);
        for (int index = 0; index < SLOT_COUNT; index++) {
            ingredients.set(index, Ingredient.fromJson(ingredientArray.get(index)));
        }
        ItemStack result = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
        int manaCost = JsonHelper.getInt(json, "mana_cost", Balance.WORKBENCH_DEFAULT_MANA_PER_TICK * Balance.WORKBENCH_DEFAULT_WORK_TIME);
        int workTime = JsonHelper.getInt(json, "work_time", Balance.WORKBENCH_DEFAULT_WORK_TIME);
        return new WorkbenchRecipe(id, ingredients, result, manaCost, workTime);
    }

    @Override
    public WorkbenchRecipe read(Identifier id, PacketByteBuf buf) {
        int manaCost = buf.readVarInt();
        int workTime = buf.readVarInt();
        ItemStack result = buf.readItemStack();
        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(SLOT_COUNT, Ingredient.EMPTY);
        for (int i = 0; i < SLOT_COUNT; i++) {
            ingredients.set(i, Ingredient.fromPacket(buf));
        }
        return new WorkbenchRecipe(id, ingredients, result, manaCost, workTime);
    }

    @Override
    public void write(PacketByteBuf buf, WorkbenchRecipe recipe) {
        buf.writeVarInt(recipe.getManaCost());
        buf.writeVarInt(recipe.getWorkTime());
        buf.writeItemStack(recipe.getOutput(null));
        for (int i = 0; i < SLOT_COUNT; i++) {
            recipe.getIngredients().get(i).write(buf);
        }
    }
}
