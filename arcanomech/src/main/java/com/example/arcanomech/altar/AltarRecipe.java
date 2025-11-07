package com.example.arcanomech.altar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.arcanomech.recipe.ModRecipes;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class AltarRecipe implements Recipe<Inventory> {
    private final Identifier id;
    private final Ingredient center;
    private final List<Ingredient> pedestalInputs;
    private final ItemStack output;
    private final int manaCost;
    private final int workTime;
    private final Map<Identifier, Integer> aspects;
    private final int baseStability;

    public AltarRecipe(Identifier id, Ingredient center, List<Ingredient> pedestalInputs, ItemStack output, int manaCost, int workTime, Map<Identifier, Integer> aspects, int baseStability) {
        this.id = id;
        this.center = center;
        this.pedestalInputs = List.copyOf(pedestalInputs);
        this.output = output;
        this.manaCost = manaCost;
        this.workTime = workTime;
        this.aspects = Map.copyOf(aspects);
        this.baseStability = baseStability;
    }

    public Ingredient getCenter() {
        return center;
    }

    public List<Ingredient> getPedestalInputs() {
        return pedestalInputs;
    }

    public Map<Identifier, Integer> getAspects() {
        return aspects;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getWorkTime() {
        return workTime;
    }

    public int getBaseStability() {
        return baseStability;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        if (!(inventory instanceof AltarRecipeInventory altarInventory)) {
            return false;
        }
        if (!center.test(altarInventory.getCenter())) {
            return false;
        }
        List<ItemStack> pedestals = new ArrayList<>(altarInventory.getPedestals());
        List<Ingredient> required = new ArrayList<>(pedestalInputs);
        for (Ingredient ingredient : pedestalInputs) {
            boolean matched = false;
            for (int i = 0; i < pedestals.size(); i++) {
                ItemStack stack = pedestals.get(i);
                if (ingredient.test(stack)) {
                    pedestals.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        return output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return output.copy();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ALTAR_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ALTAR_RECIPE_TYPE;
    }

    @Override
    public ItemStack createIcon() {
        return output.copy();
    }

    public ItemStack getResult() {
        return output.copy();
    }

    public static class AltarRecipeInventory implements Inventory {
        private final ItemStack center;
        private final List<ItemStack> pedestals;

        public AltarRecipeInventory(ItemStack center, List<ItemStack> pedestals) {
            this.center = center;
            this.pedestals = List.copyOf(pedestals);
        }

        public ItemStack getCenter() {
            return center;
        }

        public List<ItemStack> getPedestals() {
            return pedestals;
        }

        @Override
        public int size() {
            return 1 + pedestals.size();
        }

        @Override
        public boolean isEmpty() {
            if (!center.isEmpty()) {
                return false;
            }
            for (ItemStack stack : pedestals) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getStack(int slot) {
            if (slot == 0) {
                return center;
            }
            int index = slot - 1;
            return index >= 0 && index < pedestals.size() ? pedestals.get(index) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeStack(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
        }

        @Override
        public void markDirty() {
        }

        @Override
        public boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity player) {
            return true;
        }

        @Override
        public void clear() {
        }
    }
}
