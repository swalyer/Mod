package com.example.arcanomech.recipe;

import com.example.arcanomech.energy.Balance;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class WorkbenchRecipe implements Recipe<Inventory> {
    private final Identifier id;
    private final DefaultedList<Ingredient> ingredients;
    private final ItemStack result;
    private final int manaCost;
    private final int workTime;

    public WorkbenchRecipe(Identifier id, DefaultedList<Ingredient> ingredients, ItemStack result, int manaCost, int workTime) {
        this.id = id;
        this.ingredients = ingredients;
        this.result = result;
        this.manaCost = manaCost;
        this.workTime = workTime <= 0 ? Balance.WORKBENCH_DEFAULT_WORK_TIME : workTime;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        if (inventory == null || inventory.size() < 6) {
            return false;
        }
        for (int i = 0; i < 6; i++) {
            Ingredient ingredient = ingredients.get(i);
            ItemStack stack = inventory.getStack(i);
            if (ingredient.isEmpty()) {
                if (!stack.isEmpty()) {
                    return false;
                }
                continue;
            }
            if (stack.isEmpty() || !ingredient.test(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        return result.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 6;
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
        return ModRecipes.WORKBENCH_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.WORKBENCH_RECIPE_TYPE;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public ItemStack createIcon() {
        return result.copy();
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(Inventory inventory) {
        DefaultedList<ItemStack> remaining = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < Math.min(inventory.size(), ingredients.size()); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem().hasRecipeRemainder()) {
                remaining.set(i, new ItemStack(stack.getItem().getRecipeRemainder()));
            }
        }
        return remaining;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getWorkTime() {
        return workTime;
    }
}
