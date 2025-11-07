package com.example.arcanomech.altar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class AltarRecipeSerializer implements RecipeSerializer<AltarRecipe> {
    @Override
    public AltarRecipe read(Identifier id, JsonObject json) {
        int manaCost = JsonHelper.getInt(json, "mana_cost");
        int workTime = JsonHelper.getInt(json, "work_time");
        JsonObject aspectsJson = JsonHelper.getObject(json, "aspects", new JsonObject());
        Map<Identifier, Integer> aspects = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : aspectsJson.entrySet()) {
            aspects.put(new Identifier(entry.getKey()), entry.getValue().getAsInt());
        }
        List<Ingredient> pedestalInputs = new ArrayList<>();
        for (JsonElement element : JsonHelper.getArray(json, "pedestal_inputs")) {
            pedestalInputs.add(Ingredient.fromJson(element));
        }
        Ingredient center = Ingredient.fromJson(JsonHelper.getObject(json, "center"));
        ItemStack result = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
        int stability = JsonHelper.getInt(json, "base_stability", 5);
        return new AltarRecipe(id, center, pedestalInputs, result, manaCost, workTime, aspects, stability);
    }

    @Override
    public AltarRecipe read(Identifier id, PacketByteBuf buf) {
        int manaCost = buf.readVarInt();
        int workTime = buf.readVarInt();
        int aspectCount = buf.readVarInt();
        Map<Identifier, Integer> aspects = new HashMap<>();
        for (int i = 0; i < aspectCount; i++) {
            aspects.put(buf.readIdentifier(), buf.readVarInt());
        }
        int pedestalCount = buf.readVarInt();
        List<Ingredient> pedestals = new ArrayList<>(pedestalCount);
        for (int i = 0; i < pedestalCount; i++) {
            pedestals.add(Ingredient.fromPacket(buf));
        }
        Ingredient center = Ingredient.fromPacket(buf);
        ItemStack result = buf.readItemStack();
        int stability = buf.readVarInt();
        return new AltarRecipe(id, center, pedestals, result, manaCost, workTime, aspects, stability);
    }

    @Override
    public void write(PacketByteBuf buf, AltarRecipe recipe) {
        buf.writeVarInt(recipe.getManaCost());
        buf.writeVarInt(recipe.getWorkTime());
        buf.writeVarInt(recipe.getAspects().size());
        for (Map.Entry<Identifier, Integer> entry : recipe.getAspects().entrySet()) {
            buf.writeIdentifier(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
        buf.writeVarInt(recipe.getPedestalInputs().size());
        for (Ingredient ingredient : recipe.getPedestalInputs()) {
            ingredient.write(buf);
        }
        recipe.getCenter().write(buf);
        buf.writeItemStack(recipe.getResult());
        buf.writeVarInt(recipe.getBaseStability());
    }
}
