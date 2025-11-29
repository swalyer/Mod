package com.example.arcanomech.client.compat.emi;

import java.util.ArrayList;
import java.util.List;

import com.example.arcanomech.recipe.WorkbenchRecipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class WorkbenchEmiRecipe implements EmiRecipe {
    private static final int WIDTH = 132;
    private static final int HEIGHT = 58;

    private final EmiRecipeCategory category;
    private final WorkbenchRecipe recipe;
    private final Identifier id;
    private final List<EmiIngredient> inputs;

    public WorkbenchEmiRecipe(EmiRecipeCategory category, WorkbenchRecipe recipe) {
        this.category = category;
        this.recipe = recipe;
        this.id = recipe.getId();
        this.inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            inputs.add(EmiIngredient.of(ingredient));
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(recipe.getOutput(null)));
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int startX = 6;
        int startY = 6;
        for (int i = 0; i < inputs.size(); i++) {
            int col = i % 3;
            int row = i / 3;
            widgets.addSlot(inputs.get(i), startX + col * 18, startY + row * 18);
        }
        widgets.addSlot(getOutputs().get(0), 96, 10).large(true);
        widgets.addText(net.minecraft.text.Text.literal("Mana: " + recipe.getManaCost()), 6, 42, 0x404040, false);
        widgets.addText(net.minecraft.text.Text.literal("Time: " + recipe.getWorkTime() + "t"), 6, 52, 0x404040, false);
    }
}
