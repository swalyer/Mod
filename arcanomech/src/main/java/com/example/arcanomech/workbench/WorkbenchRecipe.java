package com.example.arcanomech.workbench;

import java.util.ArrayList;
import java.util.List;

import com.example.arcanomech.recipe.ModRecipes;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class WorkbenchRecipe implements Recipe<Inventory> {
    private final Identifier id;
    private final ItemStack output;
    private final List<CountedIngredient> countedIngredients;
    private final DefaultedList<Ingredient> displayIngredients;
    private final int manaCost;
    private final int workTime;

    public WorkbenchRecipe(Identifier id, ItemStack output, List<CountedIngredient> countedIngredients, int manaCost, int workTime) {
        this.id = id;
        this.output = output;
        this.countedIngredients = List.copyOf(countedIngredients);
        this.manaCost = manaCost;
        this.workTime = workTime;
        DefaultedList<Ingredient> display = DefaultedList.ofSize(countTotalItems(), Ingredient.EMPTY);
        int index = 0;
        for (CountedIngredient counted : countedIngredients) {
            for (int i = 0; i < counted.count(); i++) {
                display.set(index++, counted.ingredient());
            }
        }
        this.displayIngredients = display;
    }

    public static WorkbenchRecipe read(Identifier id, PacketByteBuf buf) {
        int manaCost = buf.readVarInt();
        int workTime = buf.readVarInt();
        ItemStack output = buf.readItemStack();
        int ingredientCount = buf.readVarInt();
        List<CountedIngredient> counted = new ArrayList<>(ingredientCount);
        for (int i = 0; i < ingredientCount; i++) {
            Ingredient ingredient = Ingredient.fromPacket(buf);
            int count = buf.readVarInt();
            counted.add(new CountedIngredient(ingredient, count));
        }
        return new WorkbenchRecipe(id, output, counted, manaCost, workTime);
    }

    public void write(PacketByteBuf buf) {
        buf.writeVarInt(manaCost);
        buf.writeVarInt(workTime);
        buf.writeItemStack(output);
        buf.writeVarInt(countedIngredients.size());
        for (CountedIngredient counted : countedIngredients) {
            counted.ingredient().write(buf);
            buf.writeVarInt(counted.count());
        }
    }

    @Override
    public boolean matches(Inventory inventory, net.minecraft.world.World world) {
        if (inventory == null) {
            return false;
        }
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        int totalRequired = countTotalItems();
        if (inputs.size() < totalRequired) {
            int sum = inputs.stream().mapToInt(ItemStack::getCount).sum();
            if (sum < totalRequired) {
                return false;
            }
        }
        List<ItemStack> expandedInputs = new ArrayList<>(totalRequired);
        for (ItemStack stack : inputs) {
            for (int i = 0; i < stack.getCount(); i++) {
                expandedInputs.add(stack);
                if (expandedInputs.size() >= totalRequired) {
                    break;
                }
            }
        }
        if (expandedInputs.size() < totalRequired) {
            return false;
        }
        List<Ingredient> needed = new ArrayList<>(totalRequired);
        for (CountedIngredient counted : countedIngredients) {
            for (int i = 0; i < counted.count(); i++) {
                needed.add(counted.ingredient());
            }
        }
        return RecipeMatcherHelper.matches(expandedInputs, needed);
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
        return ModRecipes.WORKBENCH_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.WORKBENCH_RECIPE_TYPE;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return displayIngredients;
    }

    @Override
    public ItemStack createIcon() {
        return output.copy();
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getWorkTime() {
        return workTime;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(Inventory inventory) {
        DefaultedList<ItemStack> remaining = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem().hasRecipeRemainder()) {
                remaining.set(i, new ItemStack(stack.getItem().getRecipeRemainder()));
            }
        }
        return remaining;
    }

    private int countTotalItems() {
        int total = 0;
        for (CountedIngredient counted : countedIngredients) {
            total += counted.count();
        }
        return Math.max(1, total);
    }

    public record CountedIngredient(Ingredient ingredient, int count) {
        public CountedIngredient {
            if (count <= 0) {
                throw new IllegalArgumentException("Ingredient count must be positive");
            }
        }
    }

    private static final class RecipeMatcherHelper {
        private RecipeMatcherHelper() {
        }

        static boolean matches(List<ItemStack> inputs, List<Ingredient> ingredients) {
            List<Ingredient> remaining = new ArrayList<>(ingredients);
            outer:
            for (ItemStack stack : inputs) {
                for (int i = 0; i < remaining.size(); i++) {
                    Ingredient ingredient = remaining.get(i);
                    if (ingredient.test(stack)) {
                        remaining.remove(i);
                        continue outer;
                    }
                }
                return false;
            }
            return remaining.isEmpty();
        }
    }
}
