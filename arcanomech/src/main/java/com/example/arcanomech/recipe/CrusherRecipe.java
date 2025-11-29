package com.example.arcanomech.recipe;

import com.example.arcanomech.energy.Balance;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class CrusherRecipe implements Recipe<SimpleInventory> {
    private final Identifier id;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final int workTime;

    public CrusherRecipe(Identifier id, Ingredient ingredient, ItemStack result, int workTime) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.workTime = workTime <= 0 ? Balance.CRUSHER_WORK_TIME : workTime;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        return ingredient.test(inventory.getStack(0));
    }

    @Override
    public ItemStack craft(SimpleInventory inventory, DynamicRegistryManager manager) {
        return result.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return result.copy();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRUSHER_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CRUSHER_RECIPE_TYPE;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.ofSize(1, ingredient);
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getResult() {
        return result;
    }

    public int getWorkTime() {
        return workTime;
    }
}
