package com.example.arcanomech.recipe;

import com.example.arcanomech.energy.Balance;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class CrusherRecipeSerializer implements RecipeSerializer<CrusherRecipe> {
    @Override
    public CrusherRecipe read(Identifier id, JsonObject json) {
        Ingredient ingredient = Ingredient.fromJson(JsonHelper.getObject(json, "ingredient"));
        JsonObject resultObject = JsonHelper.getObject(json, "result");
        ItemStack output = net.minecraft.recipe.ShapedRecipe.outputFromJson(resultObject);
        int workTime = JsonHelper.getInt(json, "work_time", Balance.CRUSHER_WORK_TIME);
        return new CrusherRecipe(id, ingredient, output, workTime);
    }

    @Override
    public CrusherRecipe read(Identifier id, PacketByteBuf buf) {
        Ingredient ingredient = Ingredient.fromPacket(buf);
        ItemStack output = buf.readItemStack();
        int workTime = buf.readVarInt();
        return new CrusherRecipe(id, ingredient, output, workTime);
    }

    @Override
    public void write(PacketByteBuf buf, CrusherRecipe recipe) {
        recipe.getIngredient().write(buf);
        buf.writeItemStack(recipe.getResult());
        buf.writeVarInt(recipe.getWorkTime());
    }
}
